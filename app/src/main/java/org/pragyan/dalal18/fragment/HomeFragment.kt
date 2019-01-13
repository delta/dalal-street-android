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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetMarketEventsRequest
import dalalstreet.api.actions.GetMarketEventsResponse
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.CompanyTickerRecyclerAdapter
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.CompanyTickerDetails
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.NewsDetails
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import javax.inject.Inject

class HomeFragment : Fragment(), NewsRecyclerAdapter.NewsItemClickListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var companyTickerRecyclerAdapter: CompanyTickerRecyclerAdapter

    private var linearLayoutManager: LinearLayoutManager? = null
    private var newsRecyclerAdapter: NewsRecyclerAdapter? = null

    private var companyTickerDetailsList = mutableListOf<CompanyTickerDetails>()
    private var newsList = mutableListOf<NewsDetails>()

    private lateinit var tickerRunnable: Runnable
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private lateinit var model: DalalViewModel
    private var handler = Handler()

    private val refreshNewsListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Constants.REFRESH_NEWS_ACTION, ignoreCase = true)) {

                getLatestNewsAsynchronously()

            } else if (intent.action.equals(Constants.REFRESH_PRICE_TICKER_ACTION, ignoreCase = true)) {

                val builder = StringBuilder("")
                if (model.globalStockDetails.isNotEmpty()) {
                    for ((_, shortName, _, price, _, _, _, up) in model.globalStockDetails) {
                        builder.append(shortName).append(" : ").append(price)
                        builder.append(if (up == 1) "\u2191" else "\u2193").append("     ")
                    }
                }
                breakingNewsTextView.text = builder.toString()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) } ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        breakingNewsTextView.isSelected = true
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.dalal)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        companiesRecyclerView.layoutManager = linearLayoutManager
        companiesRecyclerView.itemAnimator = DefaultItemAnimator()
        companiesRecyclerView.adapter = companyTickerRecyclerAdapter

        val companiesSnapHelper = PagerSnapHelper()
        companiesSnapHelper.attachToRecyclerView(companiesRecyclerView)

        newsRecyclerAdapter = NewsRecyclerAdapter(context, null, this)

        newsRecyclerView.layoutManager = LinearLayoutManager(context)
        newsRecyclerView.setHasFixedSize(true)
        newsRecyclerView.adapter = newsRecyclerAdapter

        setTickerAndNewsValues()

        tickerRunnable = object : Runnable {
            override fun run() {
                val position = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
                companiesRecyclerView.smoothScrollToPosition(position + 1)
                handler.postDelayed(this, COMPANY_TICKER_DURATION.toLong())
            }
        }
        handler.postDelayed(tickerRunnable, COMPANY_TICKER_DURATION.toLong())
    }

    private fun getLatestNewsAsynchronously() {
        loadingNewsRelativeLayout.visibility = View.VISIBLE
        newsRecyclerView.visibility = View.GONE

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val marketEventsResponse = actionServiceBlockingStub.getMarketEvents(GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build())

                if (marketEventsResponse.statusCode == GetMarketEventsResponse.StatusCode.OK) {

                    newsList.clear()

                    for (currentMarketEvent in marketEventsResponse.marketEventsList) {
                        newsList.add(NewsDetails(currentMarketEvent.createdAt, currentMarketEvent.headline, currentMarketEvent.text, currentMarketEvent.imagePath))
                    }

                    uiThread {
                        if (newsList.isNotEmpty()) {
                            newsRecyclerAdapter?.swapData(newsList)
                        }
                        loadingNewsRelativeLayout.visibility = View.GONE
                        newsRecyclerView.visibility = View.VISIBLE
                    }
                } else {
                    uiThread { Toast.makeText(context, marketEventsResponse.statusMessage, Toast.LENGTH_SHORT).show() }
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

    private fun setTickerAndNewsValues() {

        getLatestNewsAsynchronously()

        companyTickerDetailsList.clear()

        for ((fullName, _, _, price, _, _, _, up, imagePath) in model.globalStockDetails) {
            companyTickerDetailsList.add(CompanyTickerDetails(fullName!!, imagePath, price, up == 1))
        }

        if (companyTickerDetailsList.size != 0) {
            companyTickerRecyclerAdapter.swapData(companyTickerDetailsList)
        }


        val builder = StringBuilder("")
        for ((_, shortName, _, price, _, _, _, up) in model.globalStockDetails) {

            builder.append(shortName).append(" : ").append(price)
            if (activity != null) {
                builder.append(if (up == 1) "\u2191" else "\u2193")
            }
            builder.append("     ")
        }
        breakingNewsTextView.text = builder.toString()
    }

    override fun onNewsClicked(view: View, position: Int) {
        val bundle = Bundle()
        bundle.putString("created-at",newsList[position].createdAt)
        bundle.putString("content",newsList[position].content)
        bundle.putString("title",newsList[position].headlines)
        bundle.putString("image-path",newsList[position].imagePath)
        view.findNavController().navigate(R.id.nav_news_details,bundle)
    }

    override fun onResume() {
        super.onResume()
        if (context != null) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Constants.REFRESH_NEWS_ACTION)
            intentFilter.addAction(Constants.REFRESH_PRICE_TICKER_ACTION)
            LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshNewsListReceiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshNewsListReceiver)
        handler.removeCallbacks(tickerRunnable)
    }

    companion object {
        private const val COMPANY_TICKER_DURATION = 2500
    }
}