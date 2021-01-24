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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetMarketEventsRequest
import dalalstreet.api.actions.GetMarketEventsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.NewsDetails
import org.pragyan.dalal18.databinding.FragmentNewsBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.toast
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

class NewsFragment : Fragment(), NewsRecyclerAdapter.NewsItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var binding by viewLifecycle<FragmentNewsBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var newsRecyclerAdapter: NewsRecyclerAdapter? = null

    private var loadingNewsDialog: AlertDialog? = null

    private var newsDetailsList = mutableListOf<NewsDetails>()

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private val refreshNewsListListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_MARKET_EVENTS_FOR_HOME_AND_NEWS, ignoreCase = true))
                getNewsAsynchronously()
        }
    }

    private fun getNewsAsynchronously() = lifecycleScope.launch {

        loadingNewsDialog?.show()

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context!!) }) {

            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {

                val marketEventsResponse = withContext(Dispatchers.IO) {
                    actionServiceBlockingStub.getMarketEvents(GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build())
                }

                binding.newsSwipeRefreshLayout.isRefreshing = false

                if (marketEventsResponse.statusCode == GetMarketEventsResponse.StatusCode.OK) {

                    newsDetailsList.clear()

                    for (currentMarketEvent in marketEventsResponse.marketEventsList) {
                        newsDetailsList.add(NewsDetails(currentMarketEvent.createdAt, currentMarketEvent.headline, currentMarketEvent.text, currentMarketEvent.imagePath))
                    }

                    binding.apply {
                        if (newsDetailsList.size != 0) {
                            newsRecyclerAdapter?.swapData(newsDetailsList)
                            noNewsTextView.visibility = View.GONE
                            newsRecyclerView.visibility = View.VISIBLE
                        } else {
                            noNewsTextView.visibility = View.VISIBLE
                            newsRecyclerView.visibility = View.GONE
                        }
                    }

                } else {
                    context?.toast("Server internal error")
                }
            } else {
                networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.news_dest)
            }
        } else {
            networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.news_dest)
        }

        loadingNewsDialog?.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        postponeEnterTransition()
        binding.newsRecyclerView.viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                } 
        newsRecyclerAdapter = NewsRecyclerAdapter(context, null, this)

        binding.apply {
            with(newsRecyclerView) {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(false)
                adapter = newsRecyclerAdapter
            }

            newsSwipeRefreshLayout.setOnRefreshListener(this@NewsFragment)
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_latest_news)
        loadingNewsDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        getNewsAsynchronously()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshNewsListListener, IntentFilter(Constants.REFRESH_MARKET_EVENTS_FOR_HOME_AND_NEWS))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshNewsListListener)
    }

    override fun onRefresh() {
        getNewsAsynchronously()
    }

    override fun onNewsClicked(layout: View, position: Int, headlinesTextView: View, contentTextView: View, createdAtTextView: View) {
        val headTransition = "head$position"
        val contentTransition = "content$position"
        val createdAtTransition = "created$position"

        val bundle = Bundle()
        bundle.putString(Constants.NEWS_CREATED_AT_KEY, newsDetailsList[position].createdAt)
        bundle.putString(Constants.NEWS_CONTENT_KEY, newsDetailsList[position].content)
        bundle.putString(Constants.NEWS_HEAD_KEY, newsDetailsList[position].headlines)
        bundle.putString(Constants.NEWS_IMAGE_PATH_KEY, newsDetailsList[position].imagePath)
        bundle.putString(Constants.HEAD_TRANSITION_KEY, headTransition)
        bundle.putString(Constants.CONTENT_TRANSITION_KEY, contentTransition)
        bundle.putString(Constants.CREATED_AT_TRANSITION_KEY, createdAtTransition)

        val extras = FragmentNavigator.Extras.Builder()
                .addSharedElement(headlinesTextView, ViewCompat.getTransitionName(headlinesTextView)!!)
                .addSharedElement(contentTextView, ViewCompat.getTransitionName(contentTextView)!!)
                .addSharedElement(createdAtTextView, ViewCompat.getTransitionName(createdAtTextView)!!)
                .build()
        layout.findNavController().navigate(R.id.action_news_list_to_details, bundle, null, extras)
    }
}
