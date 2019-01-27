package org.pragyan.dalal18.fragment


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.*
import kotlinx.android.synthetic.main.fragment_mortgage.*
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
import org.pragyan.dalal18.utils.StockUtils.*
import javax.inject.Inject

/*  Uses GetMortgageDetails() for setting stocksMortgaged (int data member)
 *  Uses MortgageStocks() to mortgage stocks
 *  Uses RetrieveStocksFromMortgage() to get back mortgaged stocks */

class MortgageFragment : Fragment(), RetrieveRecyclerAdapter.OnRetrieveButtonClickListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var lastStockId = 1
    private var companiesArray = StockUtils.getCompanyNamesArray()

    private lateinit var model: DalalViewModel

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private var loadingDialog: AlertDialog? = null

    private val mortgageDetailsList = mutableListOf<MortgageDetails>()
    private lateinit var retrieveAdapter: RetrieveRecyclerAdapter

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_STOCK_PRICES_ACTION, ignoreCase = true))
                getMortgageDetailsAsynchronously()

            if (intent.action != null && intent.action.equals(Constants.REFRESH_MORTGAGE_UPDATE_ACTION, ignoreCase = true) &&
                    intent.getIntExtra(STOCKS_ID_KEY, 0) > 0) {

                val quantity = intent.getLongExtra(STOCKS_QUANTITY_KEY, 0)
                val stockId = intent.getIntExtra(STOCKS_ID_KEY, 0)
                val price = intent.getLongExtra(STOCKS_PRICE_KEY, 0)

                val ownedString = " :  " + getQuantityOwnedFromStockId(model.ownedStockDetails, stockId).toString()
                stocksOwnedTextView.text = ownedString

//                val mortgagedStocks = " :  " + (Integer.parseInt(stocksMortgagedTextView.text.toString()) - quantity).toString()
//                stocksMortgagedTextView.text = mortgagedStocks

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
            val tempString = "Getting mortgage details..."
            (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
            loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_mortgage, container, false)

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

        with(mortgage_companies_spinner) {
            adapter = ArrayAdapter(context!!, R.layout.company_spinner_item, companiesArray)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val ownedString = " :  " + getQuantityOwnedFromStockId(model.ownedStockDetails, mortgage_companies_spinner.selectedItemPosition + 1).toString()
                    stocksOwnedTextView.text = ownedString
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }

        mortgage_button.setOnClickListener { onMortgageButtonClick() }

        getMortgageDetailsAsynchronously()
    }

    private fun onMortgageButtonClick() {

        with(stocks_editText) {
            if (text.toString().trim { it <= ' ' }.isEmpty()) {
                error = "Stocks quantity missing"
                requestFocus()
                return
            } else {
                error = null
                clearFocus()
            }
        }

        val stocksTransaction = (stocks_editText.text.toString().trim { it <= ' ' }).toLong()

        if (stocksTransaction <= getQuantityOwnedFromCompanyName(model.ownedStockDetails, getCompanyNameFromStockId(lastStockId))) {
            val mortgageStocksResponse = actionServiceBlockingStub.mortgageStocks(
                    MortgageStocksRequest.newBuilder().setStockId(mortgage_companies_spinner.selectedItemPosition + 1)
                            .setStockQuantity(stocksTransaction).build())

            if (mortgageStocksResponse.statusCode == MortgageStocksResponse.StatusCode.OK) {
                Toast.makeText(context, "Transaction successful", Toast.LENGTH_SHORT).show()
                stocks_editText.setText("")
            } else
                Toast.makeText(context, mortgageStocksResponse.statusMessage, Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(activity, "Insufficient Stocks", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMortgageDetailsAsynchronously() {
        loadingDialog?.show()
        mortgage_companies_spinner.isEnabled = false

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val response = actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build())

                uiThread {
                    loadingDialog?.dismiss()
                    mortgage_companies_spinner.isEnabled = true

                    if (response.statusCode == GetMortgageDetailsResponse.StatusCode.OK) {
                        lastStockId = StockUtils.getStockIdFromCompanyName(companiesArray[mortgage_companies_spinner.selectedItemPosition])

                        mortgageDetailsList.clear()
                        for (currentDetails in response.mortgageDetailsList) {
                            mortgageDetailsList.add(MortgageDetails(currentDetails.stockId, currentDetails.stocksInBank, currentDetails.mortgagePrice))
                        }

                        retrieveAdapter.swapData(mortgageDetailsList)

                        val ownedString = " :  " + getQuantityOwnedFromCompanyName(model.ownedStockDetails, getCompanyNameFromStockId(lastStockId)).toString()
                        stocksOwnedTextView.text = ownedString

                        val tempString = " :  " + Constants.RUPEE_SYMBOL + " " + StockUtils.getPriceFromStockId(model.globalStockDetails, lastStockId).toString()
                        currentPrice_textView.text = tempString

                    } else {
                        Toast.makeText(context, response.statusMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
        }
    }

    override fun onRetrieveButtonClick(position: Int, quantity: Long) {

        val retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                RetrieveMortgageStocksRequest.newBuilder().setStockId(mortgageDetailsList[position].stockId)
                        .setStockQuantity(quantity).setRetrievePrice(mortgageDetailsList[position].mortgagePrice).build())

        if (retrieveStocksResponse.statusCode == RetrieveMortgageStocksResponse.StatusCode.OK) {
            Toast.makeText(context, "Transaction successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, retrieveStocksResponse.statusMessage, Toast.LENGTH_SHORT).show()
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

    companion object {
        const val STOCKS_ID_KEY = "stocks-id-key"
        const val STOCKS_QUANTITY_KEY = "stocks-quantity-key"
        const val STOCKS_PRICE_KEY = "stocks-price-key"
    }
}