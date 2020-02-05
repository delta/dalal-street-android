package org.pragyan.dalal18.fragment.mortgage


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
import dalalstreet.api.actions.GetMortgageDetailsRequest
import dalalstreet.api.actions.GetMortgageDetailsResponse
import dalalstreet.api.actions.MortgageStocksRequest
import dalalstreet.api.actions.MortgageStocksResponse
import kotlinx.android.synthetic.main.fragment_mortgage.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.MortgageDetails
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.hideKeyboard
import java.text.DecimalFormat
import javax.inject.Inject

/*  Uses GetMortgageDetails() for setting stocksMortgaged (int data member)
 *  Uses MortgageStocks() to mortgage stocks
 *  Uses RetrieveStocksFromMortgage() to get back mortgaged stocks */

class MortgageFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var lastStockId = 1
    private val decimalFormat = DecimalFormat(Constants.PRICE_FORMAT)
    private lateinit var model: DalalViewModel

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private var loadingDialog: AlertDialog? = null

    private val mortgageDetailsList = mutableListOf<MortgageDetails>()

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action.equals(Constants.REFRESH_STOCK_PRICES_FOR_ALL, ignoreCase = true)) {
                val currentPriceText = " :  " + Constants.RUPEE_SYMBOL +
                        decimalFormat.format(model.getPriceFromStockId(lastStockId)).toString()
                currentPriceTextView.text = currentPriceText

            } else if (intent.action.equals(Constants.REFRESH_STOCKS_FOR_MORTGAGE, ignoreCase = true)) {

                val quantity = intent.getLongExtra(STOCKS_QUANTITY_KEY, 0)
                val stockId = intent.getIntExtra(STOCKS_ID_KEY, 0)
                val price = intent.getLongExtra(STOCKS_PRICE_KEY, 0)

                val ownedString = " :  " + decimalFormat.format(model.getQuantityOwnedFromStockId(stockId)).toString()
                stocksOwnedTextView.text = ownedString

                if (quantity < 0) /* Mortgage Action */ {
                    var newStockMortgaged = true

                    for (mortgageDetails in mortgageDetailsList) {
                        if (mortgageDetails.stockId == stockId && mortgageDetails.mortgagePrice == price) {
                            mortgageDetails.stockQuantity -= quantity
                            newStockMortgaged = false
                            break
                        }
                    }

                    if (newStockMortgaged) mortgageDetailsList.add(MortgageDetails(stockId, model.getCompanyNameFromStockId(stockId), -quantity, price))

                } else /* Retrieve Action */ {
                    for (mortgageDetails in mortgageDetailsList) {
                        if (mortgageDetails.stockId == stockId && mortgageDetails.mortgagePrice == price) {
                            if (mortgageDetails.stockQuantity == quantity) {
                                mortgageDetailsList.remove(mortgageDetails)
                                break
                            } else {
                                mortgageDetails.stockQuantity -= quantity
                                break
                            }
                        }
                    }
                }
                val mortgagedString = " :  " + decimalFormat.format(getStocksMortgagedFromStockId(lastStockId))
                stocksMortgagedTextView.text = mortgagedString
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

        val rootView = inflater.inflate(R.layout.fragment_mortgage, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Getting mortgage details..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        getMortgageDetailsAsynchronously()

        with(mortgage_companies_spinner) {
            val companiesArray = model.getCompanyNamesArray()
            adapter = ArrayAdapter(context!!, R.layout.company_spinner_item, companiesArray)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    lastStockId = model.getStockIdFromCompanyName(companiesArray[position])
                    val ownedString = " :  " + decimalFormat.format(model.getQuantityOwnedFromStockId(lastStockId)).toString()
                    stocksOwnedTextView.text = ownedString

                    if (mortgageDetailsList.size > 0) {
                        val mortgageString = " :  " + decimalFormat.format(getStocksMortgagedFromStockId(lastStockId))
                        stocksMortgagedTextView.text = mortgageString
                    }

                    val currentPriceText = " :  " + Constants.RUPEE_SYMBOL + decimalFormat.format(model.getPriceFromStockId(lastStockId)).toString()
                    currentPriceTextView.text = currentPriceText

                    model.updateFavouriteCompanyName(companiesArray[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }

        mortgage_button.setOnClickListener { onMortgageButtonClick() }

        val tempText = " :  ${Constants.MORTGAGE_DEPOSIT_RATE}%"
        depositRateTextView.text = tempText
    }

    private fun onMortgageButtonClick() {

        with(stocks_editText) {
            when {
                text.toString().trim { it <= ' ' }.isEmpty() -> {
                    error = "Stocks quantity missing"
                    requestFocus()
                    return
                }
                text.toString().toLong() == 0L -> {
                    error = "Enter valid number of stocks"
                    requestFocus()
                    return
                }
                else -> {
                    error = null
                    clearFocus()
                }
            }
        }

        val stocksTransaction = (stocks_editText.text.toString().trim { it <= ' ' }).toLong()

        if (stocksTransaction <= model.getQuantityOwnedFromCompanyName(model.getCompanyNameFromStockId(lastStockId))) {
            mortgageStocksAsynchronously(stocksTransaction)
        } else {
            context?.toast("Insufficient Stocks")
        }
    }

    private fun getMortgageDetailsAsynchronously() {
        loadingDialog?.show()
        mortgage_companies_spinner.isEnabled = false

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                    val response = actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build())

                    uiThread {
                        mortgage_companies_spinner.isEnabled = true

                        if (response.statusCode == GetMortgageDetailsResponse.StatusCode.OK) {

                            mortgageDetailsList.clear()
                            for (currentDetails in response.mortgageDetailsList) {
                                mortgageDetailsList.add(MortgageDetails(currentDetails.stockId, model.getCompanyNameFromStockId(currentDetails.stockId),
                                        currentDetails.stocksInBank, currentDetails.mortgagePrice))
                            }

                            val ownedString = " :  " + decimalFormat.format(model.getQuantityOwnedFromCompanyName(model.getCompanyNameFromStockId(lastStockId))).toString()
                            stocksOwnedTextView.text = ownedString

                            val tempString = " :  " + Constants.RUPEE_SYMBOL + " " + decimalFormat.format(model.getPriceFromStockId(lastStockId)).toString()
                            currentPriceTextView.text = tempString

                            val mortgagedString = " :  " + decimalFormat.format(getStocksMortgagedFromStockId(lastStockId))
                            stocksMortgagedTextView.text = mortgagedString

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
            uiThread { loadingDialog?.dismiss() }
        }
    }

    private fun mortgageStocksAsynchronously(stocksTransaction: Long) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Mortgaging Stocks..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()
        loadingDialog?.show()
        doAsync {

            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val mortgageStocksResponse = actionServiceBlockingStub.mortgageStocks(
                            MortgageStocksRequest.newBuilder().setStockId(lastStockId)
                                    .setStockQuantity(stocksTransaction).build())

                    uiThread {
                        if (mortgageStocksResponse.statusCode == MortgageStocksResponse.StatusCode.OK) {
                            context?.toast("Transaction successful")
                            stocks_editText.setText("")
                            view?.hideKeyboard()
                        } else {
                            context?.toast(mortgageStocksResponse.statusMessage)
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.main_mortgage_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.main_mortgage_dest) }
            }
            uiThread { loadingDialog?.dismiss() }
        }
    }

    private fun getStocksMortgagedFromStockId(stockId: Int): Long {

        var totalCount = 0L

        for (mortgageDetail in mortgageDetailsList) {
            if (mortgageDetail.stockId == stockId)
                totalCount += mortgageDetail.stockQuantity
        }

        return totalCount
    }

    override fun onResume() {
        super.onResume()

        if(model.companyName!=null) {
            mortgage_companies_spinner.setSelection(model.getIndexForFavoriteCompany())
        }

        val intentFilter = IntentFilter(Constants.REFRESH_STOCK_PRICES_FOR_ALL)
        intentFilter.addAction(Constants.REFRESH_STOCKS_FOR_MORTGAGE)
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