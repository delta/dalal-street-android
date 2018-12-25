package org.pragyan.dalal18.fragment


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.CompanyRecyclerAdapter
import org.pragyan.dalal18.data.CompanyDetails
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.Constants

import java.util.ArrayList
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.fragment_companies.*

class CompanyFragment : Fragment() {

    private val portfolioList = ArrayList<CompanyDetails>()
    private var adapter: CompanyRecyclerAdapter? = null


    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (activity != null && intent.action != null &&
                    (intent.action!!.equals(Constants.REFRESH_STOCK_PRICES_ACTION, ignoreCase = true) || intent.action!!.equals(Constants.REFRESH_STOCKS_EXCHANGE_ACTION, ignoreCase = true))) {
                updateValues()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_companies, container, false)

        if (activity != null) activity!!.title = "Company Details"
        ButterKnife.bind(this, rootView)

        adapter = CompanyRecyclerAdapter(context, null)

        updateValues()
        portfolio_recyclerView.setHasFixedSize(false)
        portfolio_recyclerView.adapter = adapter
        portfolio_recyclerView.layoutManager = LinearLayoutManager(context)

        return rootView
    }

    fun updateValues() {

        portfolioList.clear()

        for ((fullName, shortName, _, price, _, _, previousDayClose) in MainActivity.globalStockDetails) {
            portfolioList.add(CompanyDetails(
                    fullName,
                    shortName,
                    price,
                    previousDayClose)
            )
        }

        sortList(portfolioList)
        adapter!!.swapData(portfolioList)
    }

    private fun sortList(list: ArrayList<CompanyDetails>) {
        list.sortedWith(compareBy({ it.value }))
    }

    override fun onResume() {
        super.onResume()
        if (context != null) {
            val intentFilter = IntentFilter(Constants.REFRESH_STOCKS_EXCHANGE_ACTION)
            intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION)

            LocalBroadcastManager.getInstance(context!!).registerReceiver(
                    refreshStockPricesReceiver, intentFilter
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (context != null) {
            LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
        }
    }
}// Required empty public constructor