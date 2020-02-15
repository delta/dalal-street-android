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
import androidx.lifecycle.ViewModelProvider
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
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import java.text.DecimalFormat
import javax.inject.Inject

class StockExchangeFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel

    private var currentStock: Stock? = null
    private var lastSelectedStockId: Int = 0
    private lateinit var companiesArray: MutableList<String>
    private var loadingDialog: AlertDialog? = null
    private var decimalFormat = DecimalFormat(Constants.PRICE_FORMAT)
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (activity != null && intent.action != null && intent.action!!.equals(Constants.REFRESH_STOCK_PRICES_FOR_ALL, ignoreCase = true)) {
                getCompanyProfileAsynchronously(lastSelectedStockId)
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
        val rootView = inflater.inflate(R.layout.fragment_stock_exchange, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

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

        companiesArray = model.getSpinnerArray()
        val arrayAdapter = ArrayAdapter<String>(activity!!, R.layout.company_spinner_item, companiesArray)
        with(companySpinner) {
            adapter = arrayAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    lastSelectedStockId = model.getStockIdFromSpinnerCompanyName(
                            companySpinner.selectedItem.toString(),
                            getString(R.string.bankruptSuffix),
                            getString(R.string.dividendSuffix)
                    )
                    model.updateFavouriteCompanyStockId(lastSelectedStockId)

                    changeStockExchangeOptions(!model.getIsBankruptFromStockId(lastSelectedStockId))

                    getCompanyProfileAsynchronously(lastSelectedStockId)
                }
            }
        }

        buyExchangeButton.setOnClickListener { buyStocksFromExchange() }
        stockIncrementFiveButton.setOnClickListener { addToStockExchangeInput(5) }
        stockIncrementOneButton.setOnClickListener { addToStockExchangeInput(1) }
    }

    private fun addToStockExchangeInput(increment: Int) {
        if (noOfStocksEditText.text.isEmpty()) {
            noOfStocksEditText.setText("0")
        }
        var noOfStocks = noOfStocksEditText.text.toString().toInt()
        noOfStocks = (noOfStocks + increment).coerceAtMost(Constants.ASK_LIMIT)
        noOfStocksEditText.setText(noOfStocks.toString())
    }

    private fun buyStocksFromExchange() {

        if (noOfStocksEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            context?.toast("Enter the number of Stocks")
        } else if (currentStock != null && dailyHigh_textView.text.isNotEmpty()) {
            if ((noOfStocksEditText.text.toString().trim { it <= ' ' }).toLong() <= currentStock!!.stocksInExchange) {
                doAsync {
                    if (ConnectionUtils.getConnectionInfo(context!!)) {
                        if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                            val response = actionServiceBlockingStub.buyStocksFromExchange(
                                    BuyStocksFromExchangeRequest.newBuilder().setStockId(lastSelectedStockId)
                                            .setStockQuantity(noOfStocksEditText.text.toString().toLong()).build()
                            )

                            uiThread {
                                if (response.statusCode == BuyStocksFromExchangeResponse.StatusCode.OK) {
                                    context?.toast("Stocks bought")
                                    noOfStocksEditText.setText("0")
                                    getCompanyProfileAsynchronously(lastSelectedStockId)
                                    view?.hideKeyboard()
                                } else
                                    context?.toast(response.statusMessage)
                            }
                        } else {
                            uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.exchange_dest) }
                        }
                    } else {
                        uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.exchange_dest) }
                    }
                }
            } else {
                context?.toast("Insufficient stocks in exchange")
            }
        } else {
            context?.toast("Select a company")
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
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val companyProfileResponse = actionServiceBlockingStub.getCompanyProfile(
                            GetCompanyProfileRequest.newBuilder().setStockId(stockId).build())

                    uiThread {

                        currentStock = companyProfileResponse.stockDetails

                        var temporaryTextViewString: String = ": ₹" + decimalFormat.format(currentStock?.currentPrice).toString()
                        currentStockPrice_textView.text = temporaryTextViewString

                        temporaryTextViewString = ": ₹" + decimalFormat.format(currentStock?.dayHigh).toString()
                        dailyHigh_textView.text = temporaryTextViewString

                        temporaryTextViewString = ": ₹" + decimalFormat.format(currentStock?.dayLow).toString()
                        dailyLow_textView.text = temporaryTextViewString

                        temporaryTextViewString = ": " + decimalFormat.format(currentStock?.stocksInMarket).toString()
                        stocksInMarket_textView.text = temporaryTextViewString

                        temporaryTextViewString = ": " + decimalFormat.format(currentStock?.stocksInExchange).toString()
                        stocksInExchange_textView.text = temporaryTextViewString
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.exchange_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.exchange_dest) }
            }
            uiThread { loadingDialog?.dismiss() }
        }
    }

    override fun onResume() {
        super.onResume()

        companySpinner.setSelection(model.getIndexForFavoriteCompany())

        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver, IntentFilter(Constants.REFRESH_STOCK_PRICES_FOR_ALL))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }

    fun changeStockExchangeOptions(openOptions: Boolean) {
        stocks_exchange_input.isEnabled = openOptions
        stockIncrementOneButton.isEnabled = openOptions
        stockIncrementFiveButton.isEnabled = openOptions
        buyExchangeButton.isEnabled = openOptions
    }
}
