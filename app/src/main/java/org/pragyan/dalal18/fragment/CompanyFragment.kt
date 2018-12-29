package org.pragyan.dalal18.fragment


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_companies.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.CompanyRecyclerAdapter
import org.pragyan.dalal18.data.CompanyDetails
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.Constants
import java.util.*

class CompanyFragment : Fragment() {

    private val portfolioList = ArrayList<CompanyDetails>()
    private lateinit var adapter: CompanyRecyclerAdapter

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Constants.REFRESH_STOCK_PRICES_ACTION, ignoreCase = true) ||
                    intent.action.equals(Constants.REFRESH_STOCKS_EXCHANGE_ACTION, ignoreCase = true)) {
                updateValues()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_companies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CompanyRecyclerAdapter(context, null)

        updateValues()
        portfolio_recyclerView.setHasFixedSize(false)
        portfolio_recyclerView.adapter = adapter
        portfolio_recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun updateValues() {

        portfolioList.clear()

        for ((fullName, shortName, _, price, _, _, previousDayClose) in MainActivity.globalStockDetails) {
            portfolioList.add(CompanyDetails(fullName, shortName, price, previousDayClose))
        }

        portfolioList.sortWith(compareBy { it.value })
        adapter.swapData(portfolioList)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.REFRESH_STOCKS_EXCHANGE_ACTION)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }
}