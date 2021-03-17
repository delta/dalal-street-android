package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetMarketEventsRequest
import dalalstreet.api.actions.GetMarketEventsResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.CompanyTickerRecyclerAdapter
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.CompanyTickerDetails
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.NewsDetails
import org.pragyan.dalal18.databinding.FragmentHomeBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import java.text.DecimalFormat
import javax.inject.Inject

class HomeFragment : Fragment(), NewsRecyclerAdapter.NewsItemClickListener, SwipeRefreshLayout.OnRefreshListener,
        CompanyTickerRecyclerAdapter.OnCompanyTickerClickListener {

    private var binding by viewLifecycle<FragmentHomeBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var linearLayoutManager: LinearLayoutManager? = null

    private lateinit var newsRecyclerAdapter: NewsRecyclerAdapter
    private lateinit var companyTickerRecyclerAdapter: CompanyTickerRecyclerAdapter

    private var companyTickerDetailsList = mutableListOf<CompanyTickerDetails>()
    private var newsList = mutableListOf<NewsDetails>()

    private lateinit var tickerRunnable: Runnable
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private lateinit var model: DalalViewModel
    private var handler = Handler()

    private val refreshNewsListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Constants.REFRESH_MARKET_EVENTS_FOR_HOME_AND_NEWS, ignoreCase = true)) {

                getLatestNewsAsynchronously()

            } else if (intent.action.equals(Constants.REFRESH_PRICE_TICKER_FOR_HOME, ignoreCase = true)) {

                val builder = StringBuilder("")
                if (model.globalStockDetails.isNotEmpty()) {
                    for ((_, currentStock) in model.globalStockDetails) {
                        builder.append(currentStock.shortName).append(" : ").append(currentStock.price)
                        builder.append(if (currentStock.up == 1) "\u2191" else "\u2193").append("     ")
                    }
                }
                binding.breakingNewsTextView.text = builder.toString()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.breakingNewsTextView.isSelected = true
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        companyTickerRecyclerAdapter = CompanyTickerRecyclerAdapter(context, null, this)
        binding.apply {
            companiesRecyclerView.layoutManager = linearLayoutManager
            companiesRecyclerView.itemAnimator = DefaultItemAnimator()
            companiesRecyclerView.adapter = companyTickerRecyclerAdapter
        }

        val companiesSnapHelper = PagerSnapHelper()
        companiesSnapHelper.attachToRecyclerView(binding.companiesRecyclerView)

        newsRecyclerAdapter = NewsRecyclerAdapter(context, null, this)

        binding.apply {
            newsRecyclerView.layoutManager = LinearLayoutManager(context)
            newsRecyclerView.setHasFixedSize(true)
            newsRecyclerView.adapter = newsRecyclerAdapter

            setTickerAndNewsValues()
            newsSwipeRefreshLayout.setOnRefreshListener(this@HomeFragment)
        }

        tickerRunnable = object : Runnable {
            override fun run() {
                val position = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
                binding.companiesRecyclerView.smoothScrollToPosition(position + 1)
                handler.postDelayed(this, COMPANY_TICKER_DURATION.toLong())
            }
        }
        handler.postDelayed(tickerRunnable, COMPANY_TICKER_DURATION.toLong())
    }

    private fun getLatestNewsAsynchronously() {
        showNewsAvailable(false)
        binding.loadingNewsHomeFragmentProgressBar.visibility = View.VISIBLE
        binding.loadingNewsTextView.text = getString(R.string.getting_latest_news)

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                    val marketEventsResponse = actionServiceBlockingStub.getMarketEvents(GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build())

                    uiThread {
                        binding.newsSwipeRefreshLayout.isRefreshing = false
                        if (marketEventsResponse.statusCode == GetMarketEventsResponse.StatusCode.OK) {

                            newsList.clear()

                            for (currentMarketEvent in marketEventsResponse.marketEventsList) {
                                newsList.add(NewsDetails(currentMarketEvent.createdAt, currentMarketEvent.headline, currentMarketEvent.text, currentMarketEvent.imagePath))
                            }

                            if (newsList.isNotEmpty()) {
                                newsRecyclerAdapter.swapData(newsList)
                                showNewsAvailable(true)
                            } else {
                                showNewsAvailable(false)
                            }

                        } else {
                            context?.toast(marketEventsResponse.statusMessage)
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.home_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.home_dest) }
            }
        }
    }

    private fun showNewsAvailable(show: Boolean) {
        binding.apply {
            loadingNewsTextView.text = if (show) getString(R.string.getting_latest_news) else getString(R.string.news_not_available)
            loadingNewsHomeFragmentProgressBar.visibility = if (show) View.VISIBLE else View.GONE
            loadingNewsRelativeLayout.visibility = if (show) View.GONE else View.VISIBLE
            newsRecyclerView.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }

    }

    private fun setTickerAndNewsValues() {

        getLatestNewsAsynchronously()

        companyTickerDetailsList.clear()

        for ((_, stock) in model.globalStockDetails) {
            companyTickerDetailsList.add(CompanyTickerDetails(stock.stockId, stock.fullName, stock.imagePath, stock.price, stock.up == 1, stock.isBankrupt, stock.givesDividend))
        }

        companyTickerRecyclerAdapter.updateData(companyTickerDetailsList)

        val builder = StringBuilder("")
        for ((_, currentStock) in model.globalStockDetails) {

            builder.append(currentStock.shortName).append(" : ").append(DecimalFormat(Constants.PRICE_FORMAT).format(currentStock.price))
            if (activity != null) {
                builder.append(if (currentStock.up == 1) "\u2191" else "\u2193")
            }
            builder.append("     ")
        }
        binding.breakingNewsTextView.text = builder.toString()
    }

    override fun onNewsClicked(layout: View, position: Int, headlinesTextView: View, contentTextView: View, createdAtTextView: View) {
        val headTransition = "head$position"
        val contentTransition = "content$position"
        val createdAtTransition = "created$position"

        val bundle = Bundle()
        bundle.putString(Constants.NEWS_CREATED_AT_KEY, newsList[position].createdAt)
        bundle.putString(Constants.NEWS_CONTENT_KEY, newsList[position].content)
        bundle.putString(Constants.NEWS_HEAD_KEY, newsList[position].headlines)
        bundle.putString(Constants.NEWS_IMAGE_PATH_KEY, newsList[position].imagePath)
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

    override fun onCompanyTickerClick(view: View, position: Int) {
        val bundle = Bundle()
        bundle.putInt(CompanyDescriptionFragment.COMPANY_STOCK_ID_KEY,
                companyTickerDetailsList[position % companyTickerDetailsList.size].stockId)
        findNavController().navigate(R.id.action_company_ticker_to_details, bundle)
    }

    override fun onRefresh() = getLatestNewsAsynchronously()

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.REFRESH_MARKET_EVENTS_FOR_HOME_AND_NEWS)
        intentFilter.addAction(Constants.REFRESH_PRICE_TICKER_FOR_HOME)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshNewsListReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshNewsListReceiver)
        handler.removeCallbacks(tickerRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshNewsListReceiver)
    }

    companion object {
        private const val COMPANY_TICKER_DURATION = 2500
    }
}
