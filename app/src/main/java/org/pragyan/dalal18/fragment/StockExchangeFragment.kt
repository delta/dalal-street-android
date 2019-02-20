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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.BuyStocksFromExchangeRequest
import dalalstreet.api.actions.BuyStocksFromExchangeResponse
import dalalstreet.api.actions.GetCompanyProfileRequest
import dalalstreet.api.models.Stock
import kotlinx.android.synthetic.main.fragment_stock_exchange.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import org.pragyan.dalal18.utils.hideKeyboard
import java.text.DecimalFormat
import javax.inject.Inject

class StockExchangeFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var currentStock: Stock
    private var lastSelectedStockId: Int = 0
    lateinit var companiesArray: Array<String>
    private var loadingDialog: AlertDialog? = null
    private var decimalFormat = DecimalFormat(Constants.PRICE_FORMAT)
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler


    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (activity != null && intent.action != null && intent.action!!.equals(Constants.REFRESH_STOCK_PRICES_ACTION, ignoreCase = true)) {
                getCompanyProfileAsynchronously(lastSelectedStockId)
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
        val rootView = inflater.inflate(R.layout.fragment_stock_exchange, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.stock_exchange)
        val tempString = "Getting stocks details..."
        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        companiesArray = StockUtils.getCompanyNamesArray()
        val arrayAdapter = ArrayAdapter<String>(activity!!, R.layout.company_spinner_item, companiesArray)
        with(companySpinner) {
            adapter = arrayAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val stockId = StockUtils.getStockIdFromCompanyName(companiesArray[position])
                    lastSelectedStockId = stockId
                    getCompanyProfileAsynchronously(lastSelectedStockId)
                }
            }
        }

        buyExchangeButton.setOnClickListener { buyStocksFromExchange() }
    }

    private fun buyStocksFromExchange() {

        if (noOfStocksEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            context?.toast("Enter the number of Stocks")
        } else {

            doAsync {
                if (!ConnectionUtils.getConnectionInfo(context!!)) {
                    uiThread {
                        networkDownHandler.onNetworkDownError()
                    }
                } else {
                    if ((noOfStocksEditText.text.toString().trim { it <= ' ' }).toLong() <= currentStock.stocksInExchange) {

                        val response = actionServiceBlockingStub.buyStocksFromExchange(
                                BuyStocksFromExchangeRequest.newBuilder().setStockId(lastSelectedStockId)
                                        .setStockQuantity(noOfStocksEditText.text.toString().toLong()).build()
                        )

                        uiThread {
                            if (response.statusCode == BuyStocksFromExchangeResponse.StatusCode.OK) {
                                context?.toast("Stocks bought")
                                noOfStocksEditText.setText("")
                                getCompanyProfileAsynchronously(lastSelectedStockId)
                                view?.hideKeyboard()
                            }
                            else
                                context?.toast(response.statusMessage)
                        }
                    } else {
                        uiThread { context?.toast("Insufficient stocks in exchange") }
                    }
                }
            }
        }
    }

    private fun getCompanyProfileAsynchronously(stockId: Int) {

        loadingDialog?.show()

        dailyHigh_textView.text = ""
        dailyLow_textView.text = ""
        currentStockPrice_textView.text = ""
        stocksInMarket_textView.text = ""
        stocksInExchange_textView.text = ""

        doAsync {

            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val companyProfileResponse = actionServiceBlockingStub.getCompanyProfile(
                        GetCompanyProfileRequest.newBuilder().setStockId(stockId).build())

                uiThread {
                    loadingDialog?.dismiss()

                    currentStock = companyProfileResponse.stockDetails

                    var temporaryTextViewString: String = ": ₹" + decimalFormat.format(currentStock.currentPrice).toString()

                    currentStockPrice_textView.text = temporaryTextViewString

                    temporaryTextViewString = ": ₹" + decimalFormat.format(currentStock.dayHigh).toString()
                    dailyHigh_textView.text = temporaryTextViewString

                    temporaryTextViewString = ": ₹" + decimalFormat.format(currentStock.dayLow).toString()
                    dailyLow_textView.text = temporaryTextViewString

                    temporaryTextViewString = ": " + decimalFormat.format(currentStock.stocksInMarket).toString()
                    stocksInMarket_textView.text = temporaryTextViewString

                    temporaryTextViewString = ": " + decimalFormat.format(currentStock.stocksInExchange).toString()
                    stocksInExchange_textView.text = temporaryTextViewString
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver, IntentFilter(Constants.REFRESH_STOCK_PRICES_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }
}
