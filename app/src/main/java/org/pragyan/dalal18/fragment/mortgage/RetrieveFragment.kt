package org.pragyan.dalal18.fragment.mortgage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetMortgageDetailsRequest
import dalalstreet.api.actions.GetMortgageDetailsResponse
import dalalstreet.api.actions.RetrieveMortgageStocksRequest
import dalalstreet.api.actions.RetrieveMortgageStocksResponse
import kotlinx.android.synthetic.main.fragment_retrieve.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.RetrieveRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.MortgageDetails
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import javax.inject.Inject

class RetrieveFragment : Fragment(), RetrieveRecyclerAdapter.OnRetrieveButtonClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private lateinit var model: DalalViewModel

    private val mortgageDetailsList = mutableListOf<MortgageDetails>()
    private lateinit var retrieveAdapter: RetrieveRecyclerAdapter

    private val refreshMortgageListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_STOCKS_FOR_MORTGAGE, ignoreCase = true)) {

                val quantity = intent.getLongExtra(MortgageFragment.STOCKS_QUANTITY_KEY, 0)
                val stockId = intent.getIntExtra(MortgageFragment.STOCKS_ID_KEY, 0)
                val price = intent.getLongExtra(MortgageFragment.STOCKS_PRICE_KEY, 0)

                if (quantity < 0) /* Mortgage Action */ {

                    mortgageDetailsList.forEachIndexed { index, mortgageDetails ->
                        if (mortgageDetails.stockId == stockId && mortgageDetails.mortgagePrice == price) {
                            mortgageDetails.stockQuantity -= quantity
                            retrieveAdapter.changeSingleItem(mortgageDetailsList, index)
                            return
                        }
                    }

                    mortgageDetailsList.add(MortgageDetails(stockId, model.getCompanyNameFromStockId(stockId), -quantity, price))
                    retrieveAdapter.addSingleItem(mortgageDetailsList, mortgageDetailsList.size - 1)
                    if (mortgageDetailsList.size > 0) flipVisibilities(false)

                } else /* Retrieve Action */ {
                    var modifyIndex = -1
                    var modifyMortgageDetails: MortgageDetails? = null

                    mortgageDetailsList.forEachIndexed { index, mortgageDetails ->
                        if (mortgageDetails.stockId == stockId && mortgageDetails.mortgagePrice == price) {
                            if (mortgageDetails.stockQuantity == quantity) {
                                modifyMortgageDetails = mortgageDetails
                                modifyIndex = index
                                return@forEachIndexed
                            } else {
                                mortgageDetails.stockQuantity -= quantity
                                retrieveAdapter.changeSingleItem(mortgageDetailsList, index)
                                return@forEachIndexed
                            }
                        }
                    }

                    if (modifyIndex != -1) {
                        mortgageDetailsList.remove(modifyMortgageDetails)
                        retrieveAdapter.removeSingleItem(mortgageDetailsList, modifyIndex)
                    }
                }

                if (mortgageDetailsList.size == 0) flipVisibilities(true)
            }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_retrieve, container, false)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        retrieveAdapter = RetrieveRecyclerAdapter(context, null, this)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage)

        retrieveRecyclerView.layoutManager = LinearLayoutManager(context)
        retrieveRecyclerView.adapter = retrieveAdapter
        retrieveRecyclerView.setHasFixedSize(false)

        retrieveSwipeRefreshLayout.setOnRefreshListener(this)
        val tempString = "(Retrieve Rate: ${Constants.MORTGAGE_RETRIEVE_RATE}%)"
        retrieveRateTextView.text = tempString

        getMortgageDetailsAsynchronously()
    }

    private fun getMortgageDetailsAsynchronously() {
        retrieveSwipeRefreshLayout.isRefreshing = true
        messageStocksMortgagedTextview.visibility = View.VISIBLE
        retrieveRecyclerViewParentLayout.visibility = View.GONE
        messageStocksMortgagedTextview.text = getString(R.string.swipe_to_refresh)
        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val response = actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build())

                    uiThread {

                        if (response.statusCode == GetMortgageDetailsResponse.StatusCode.OK) {

                            mortgageDetailsList.clear()
                            for (currentDetails in response.mortgageDetailsList) {
                                mortgageDetailsList.add(MortgageDetails(currentDetails.stockId, model.getCompanyNameFromStockId(currentDetails.stockId),
                                        currentDetails.stocksInBank, currentDetails.mortgagePrice))
                            }

                            if (mortgageDetailsList.size == 0) {
                                flipVisibilities(true)
                            } else {
                                retrieveAdapter.swapData(mortgageDetailsList)
                                flipVisibilities(false)
                            }
                        } else {
                            context?.toast(response.statusMessage)
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.main_mortgage_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.main_mortgage_dest) }
            }
            uiThread { retrieveSwipeRefreshLayout.isRefreshing = false }
        }
    }

    override fun onRetrieveButtonClick(position: Int, retrieveQuantity: String, stocksQuantity: String) {
        when {
            retrieveQuantity.isEmpty() || retrieveQuantity == "" -> context?.toast("Enter stocks to retrieve")
            retrieveQuantity.toInt() == 0 -> context?.toast("Enter valid number of stocks")
            retrieveQuantity.toLong() > stocksQuantity.toLong() -> context?.toast("Insufficient stocks to retrieve")
            else -> {
                doAsync {
                    if (ConnectionUtils.getConnectionInfo(context)) {
                        if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                            val retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                                    RetrieveMortgageStocksRequest.newBuilder().setStockId(mortgageDetailsList[position].stockId)
                                            .setStockQuantity(retrieveQuantity.toLong()).setRetrievePrice(mortgageDetailsList[position].mortgagePrice).build())
                            uiThread {
                                if (retrieveStocksResponse.statusCode == RetrieveMortgageStocksResponse.StatusCode.OK)
                                    context?.toast("Transaction successful")
                                else
                                    context?.toast(retrieveStocksResponse.statusMessage)

                                view?.hideKeyboard()
                            }
                        } else {
                            uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.main_mortgage_dest) }
                        }
                    } else {
                        uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.main_mortgage_dest) }
                    }
                }
            }
        }
    }

    private fun flipVisibilities(noStocksMortgaged: Boolean) {
        messageStocksMortgagedTextview.text = getString(R.string.no_stocks_mortgaged)
        messageStocksMortgagedTextview.visibility = if (noStocksMortgaged) View.VISIBLE else View.GONE
        retrieveRecyclerViewParentLayout.visibility = if (noStocksMortgaged) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.REFRESH_STOCKS_FOR_MORTGAGE)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshMortgageListReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshMortgageListReceiver)
    }

    override fun onRefresh() {
        getMortgageDetailsAsynchronously()
    }

}
