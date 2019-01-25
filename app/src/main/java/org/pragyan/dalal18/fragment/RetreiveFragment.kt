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
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetMortgageDetailsRequest
import dalalstreet.api.actions.GetMortgageDetailsResponse
import dalalstreet.api.actions.RetrieveMortgageStocksRequest
import dalalstreet.api.actions.RetrieveMortgageStocksResponse
import kotlinx.android.synthetic.main.fragment_mortgage.*
import kotlinx.android.synthetic.main.fragment_retrieve.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.RetrieveRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.MortgageDetails
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import javax.inject.Inject

class RetreiveFragment:Fragment(),RetrieveRecyclerAdapter.OnRetrieveButtonClickListener {
    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private var loadingDialog: AlertDialog? = null

    private val mortgageDetailsList = mutableListOf<MortgageDetails>()
    private lateinit var retrieveAdapter: RetrieveRecyclerAdapter

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_MORTGAGE_UPDATE_ACTION, ignoreCase = true)){
                val quantity = intent.getLongExtra(MortgageFragment.STOCKS_QUANTITY_KEY, 0)
                val stockId = intent.getIntExtra(MortgageFragment.STOCKS_ID_KEY, 0)
                val price = intent.getLongExtra(MortgageFragment.STOCKS_PRICE_KEY, 0)

                /*val ownedString = " :  " + StockUtils.getQuantityOwnedFromStockId(model.ownedStockDetails, stockId).toString()
                stocksOwnedTextView.text = ownedString*/


                if (quantity < 0) /* Mortgage Action */ {

                    mortgageDetailsList.forEachIndexed { index, mortgageDetails ->
                        if (mortgageDetails.stockId == stockId && mortgageDetails.mortgagePrice == price) {
                            mortgageDetails.stockQuantity -= quantity
                            retrieveAdapter.changeSingleItem(mortgageDetailsList, index)
                            return
                        }
                    }

                    mortgageDetailsList.add(MortgageDetails(stockId, -quantity, price))
                    retrieveAdapter.addSingleItem(mortgageDetailsList, mortgageDetailsList.size - 1)

                } else /* Retrieve Action */ {
                    mortgageDetailsList.forEachIndexed { index, mortgageDetails ->
                        if (mortgageDetails.stockId == stockId && mortgageDetails.mortgagePrice == price) {
                            if (mortgageDetails.stockQuantity == quantity) {
                                mortgageDetailsList.remove(mortgageDetails)
                                retrieveAdapter.removeSingleItem(mortgageDetailsList, index)
                                return
                            } else {
                                mortgageDetails.stockQuantity -= quantity
                                retrieveAdapter.changeSingleItem(mortgageDetailsList, index)
                                return
                            }
                        }
                    }
                }
            }

        }
    }


    override fun onRetrieveButtonClick(position: Int, quantity: Long) {
        //To change body of created functions use File | Settings | File Templates.
        val retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                RetrieveMortgageStocksRequest.newBuilder().setStockId(mortgageDetailsList[position].stockId)
                        .setStockQuantity(quantity).setRetrievePrice(mortgageDetailsList[position].mortgagePrice).build())

        if (retrieveStocksResponse.statusCode == RetrieveMortgageStocksResponse.StatusCode.OK) {
            Toast.makeText(context, "Transaction successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, retrieveStocksResponse.statusMessage, Toast.LENGTH_SHORT).show()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (context != null) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            val tempString = "Getting Retrieve details..."
            (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
            loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_retrieve, container, false)

        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) } ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        retrieveAdapter = RetrieveRecyclerAdapter(context, null, this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage)


        retrieveRecyclerView.layoutManager = LinearLayoutManager(context)
        retrieveRecyclerView.adapter = retrieveAdapter
        retrieveRecyclerView.setHasFixedSize(false)

        getMortgageDetailsAsynchronously()
    }

    private fun getMortgageDetailsAsynchronously() {
        loadingDialog?.show()

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val response = actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build())

                uiThread {
                    loadingDialog?.dismiss()

                    if (response.statusCode == GetMortgageDetailsResponse.StatusCode.OK) {

                        mortgageDetailsList.clear()
                        for (currentDetails in response.mortgageDetailsList) {
                            mortgageDetailsList.add(MortgageDetails(currentDetails.stockId, currentDetails.stocksInBank, currentDetails.mortgagePrice))
                        }

                        retrieveAdapter.swapData(mortgageDetailsList)


                    } else {
                        Toast.makeText(context, response.statusMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
        }

    }
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.REFRESH_STOCK_PRICES_ACTION)
        intentFilter.addAction(Constants.REFRESH_MORTGAGE_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }
}