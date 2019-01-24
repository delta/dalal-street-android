package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.ViewModelProviders
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_portfolio.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.PortfolioRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.Portfolio
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import java.util.ArrayList
import javax.inject.Inject


class PortfolioFragment : Fragment() {

    @Inject
    lateinit var portfolioRecyclerAdapter: PortfolioRecyclerAdapter

    private lateinit var model: DalalViewModel

    private val refreshPortfolioDetails = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && (intent.action == Constants.REFRESH_STOCK_PRICES_ACTION || intent.action == Constants.REFRESH_OWNED_STOCKS_ACTION)) {
                updatePortfolioTable()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_portfolio, container, false)
        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) } ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.portfolio)
        with(portfolioRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = portfolioRecyclerAdapter
            setHasFixedSize(false)
        }
        updatePortfolioTable()
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter(Constants.REFRESH_OWNED_STOCKS_ACTION)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION)
        intentFilter.addAction(Constants.REFRESH_STOCKS_EXCHANGE_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshPortfolioDetails, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshPortfolioDetails)
    }

    private fun updatePortfolioTable() {

        val portfolioList = ArrayList<Portfolio>()
        for ((stockId, quantity) in model.ownedStockDetails) {

            var currentPrice = -1L

            for (globalStockDetails in model.globalStockDetails) {
                if (stockId == globalStockDetails.stockId) {
                    currentPrice = globalStockDetails.price
                    break
                }
            }

            if (quantity != 0L) {
                portfolioList.add(Portfolio(
                        StockUtils.getShortNameForStockId(stockId)!!,
                        StockUtils.getCompanyNameFromStockId(stockId),
                        quantity,
                        currentPrice,
                        StockUtils.getPreviousDayCloseFromStockId(model.globalStockDetails, stockId)
                ))
            }
        }

        if (portfolioList.size > 0) {
            portfolioRecyclerAdapter.swapData(portfolioList)
            portfolioRecyclerView.visibility = View.VISIBLE
            emptyPortfolioRelativeLayout.visibility = View.GONE
        } else {
            portfolioRecyclerView.visibility = View.GONE
            emptyPortfolioRelativeLayout.visibility = View.VISIBLE
        }
    }
}