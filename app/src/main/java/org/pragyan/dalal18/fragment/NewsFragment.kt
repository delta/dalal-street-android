package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import javax.inject.Inject


class NewsFragment : Fragment(), NewsRecyclerAdapter.NewsItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var newsRecyclerAdapter: NewsRecyclerAdapter? = null

    private var loadingNewsDialog: AlertDialog? = null

    private var newsDetailsList = mutableListOf<NewsDetails>()

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler


    companion object {
        @JvmStatic
        var NEWS_DETAILS_KEY = "news-detail-key"
    }

    private val refreshNewsListListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_NEWS_ACTION, ignoreCase = true))
                getNewsAsynchronously()
        }
    }

    private fun getNewsAsynchronously() {

        loadingNewsDialog?.show()

        doAsync {

            if (ConnectionUtils.getConnectionInfo(context!!) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val marketEventsResponse = actionServiceBlockingStub.getMarketEvents(
                        GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build())

                uiThread {
                    loadingNewsDialog?.dismiss()
                    newsSwipeRefreshLayout.isRefreshing = false

                    if (marketEventsResponse.statusCode.number == 0) {

                        newsDetailsList.clear()

                        for (currentMarketEvent in marketEventsResponse.marketEventsList) {
                            newsDetailsList.add(NewsDetails(currentMarketEvent.createdAt, currentMarketEvent.headline,
                                    currentMarketEvent.text, currentMarketEvent.imagePath))
                        }

                        if (newsDetailsList.size != 0) {
                            newsRecyclerAdapter?.swapData(newsDetailsList)
                            noNewsTextView.visibility = View.GONE
                            newsRecyclerView.visibility = View.VISIBLE
                        } else {
                            noNewsTextView.visibility = View.VISIBLE
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_news, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.news)
        newsRecyclerAdapter = NewsRecyclerAdapter(context, null, this)

        with(newsRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = newsRecyclerAdapter
        }

        newsSwipeRefreshLayout.setOnRefreshListener(this)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_latest_news)
        loadingNewsDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        getNewsAsynchronously()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshNewsListListener, IntentFilter(Constants.REFRESH_NEWS_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshNewsListListener)
    }

    override fun onRefresh() = getNewsAsynchronously()

    override fun onNewsClicked(view: View?, position: Int) {
        val bundle = Bundle()
        bundle.putString("created-at",newsDetailsList[position].createdAt)
        bundle.putString("content",newsDetailsList[position].content)
        bundle.putString("title",newsDetailsList[position].headlines)
        bundle.putString("image-path",newsDetailsList[position].imagePath)
        view?.findNavController()?.navigate(R.id.action_news_to_news_details,bundle)

    }
}