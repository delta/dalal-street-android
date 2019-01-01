package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetMarketEventsRequest
import kotlinx.android.synthetic.main.fragment_news.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.NewsDetails
import org.pragyan.dalal18.ui.NewsDetailsActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import java.util.ArrayList
import javax.inject.Inject


class NewsFragment : Fragment(),NewsRecyclerAdapter.NewsItemClickListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var newsRecyclerAdapter: NewsRecyclerAdapter? = null

    private var loadingNewsDialog: AlertDialog? = null
    private var newsDetailsList: List<NewsDetails> = ArrayList()

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler



    private val refreshNewsListListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_NEWS_ACTION, ignoreCase = true))
                getNewsAsync();
        }
    }

    private fun getNewsAsync() {

        loadingNewsDialog?.show()


        doAsync {

            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val newsList = ArrayList<NewsDetails>()

                val marketEventsResponse = actionServiceBlockingStub.getMarketEvents(
                        GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build())

                uiThread {
                    if (marketEventsResponse.statusCode.number == 0) {

                        newsList.clear()

                        for (currentMarketEvent in marketEventsResponse.marketEventsList) {
                            newsList.add(NewsDetails(currentMarketEvent.createdAt, currentMarketEvent.headline,
                                    currentMarketEvent.text, currentMarketEvent.imagePath))
                        }



                        loadingNewsDialog?.dismiss()

                        if (newsList.size != 0) {
                            newsDetailsList =  newsList
                            newsRecyclerAdapter?.swapData(newsDetailsList)
                            noNews_textView.visibility = View.GONE
                            newsRecyclerView.visibility = View.VISIBLE
                        } else {
                            noNews_textView.visibility = View.VISIBLE
                            newsRecyclerView.visibility = View.GONE
                        }

                    } else {

                        Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show()
                    }

                }

            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement network down handler.")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_news, container, false)

        if (activity != null) activity!!.title = "News"

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)



        if (context != null) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_latest_news)
            loadingNewsDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()
        }

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsRecyclerAdapter = NewsRecyclerAdapter(context, null, this)

        newsRecyclerView.setLayoutManager(LinearLayoutManager(context))
        newsRecyclerView.setHasFixedSize(false)
        newsRecyclerView.setAdapter(newsRecyclerAdapter)

        getNewsAsync()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!)
                .registerReceiver(refreshNewsListListener, IntentFilter(Constants.REFRESH_NEWS_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!)
                .unregisterReceiver(refreshNewsListListener)
    }

    override fun onNewsClicked(view: View?, position: Int) {
        val intent = Intent(context, NewsDetailsActivity::class.java)
        intent.putExtra(NewsDetailsActivity.NEWS_DETAILS_KEY, newsDetailsList[position])
        startActivity(intent)
    }

}
