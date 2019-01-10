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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.*
import kotlinx.android.synthetic.main.fragment_mortgage.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import org.pragyan.dalal18.utils.StockUtils.getCompanyNameFromStockId
import org.pragyan.dalal18.utils.StockUtils.getQuantityOwnedFromCompanyName
import javax.inject.Inject

/*  Uses GetMortgageDetails() for setting stocksMortgaged (int data member)
 *  Uses MortgageStocks() to mortgage stocks
 *  Uses RetrieveStocksFromMortgage() to get back mortgaged stocks */

class MortgageFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var stocksOwned = 0
    private var stocksMortgaged = 0
    private var stocksTransaction = 0
    private var lastStockId = 1
    private var companiesArray = StockUtils.getCompanyNamesArray()

    private lateinit var model: DalalViewModel

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private var loadingDialog: AlertDialog? = null

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action.equals(Constants.REFRESH_STOCK_PRICES_ACTION, ignoreCase = true))
                getMortgageDetailsAsynchronously()
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

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(mortgage_companies_spinner) {
            adapter = ArrayAdapter(context!!, R.layout.company_spinner_item, companiesArray)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    getMortgageDetailsAsynchronously()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }

        mortgageRadioGroup.setOnCheckedChangeListener { _, id ->
            val currentPrice = StockUtils.getPriceFromStockId(model.globalStockDetails, lastStockId)

            if (id == R.id.mortgage_radioButton) {
                mortgage_button.setText(R.string.mortgage_uppercase)
                mortgageRateText_textView.setText(R.string.mortgage_deposit_rate)

                var temp = " :  " + Constants.MORTGAGE_DEPOSIT_RATE.toString() + "%"
                mortgageRate_textView.text = temp

                depositPriceText_textView.setText(R.string.deposit_rate_per_stock)

                temp = " :  " + Constants.RUPEE_SYMBOL + " " + (Constants.MORTGAGE_DEPOSIT_RATE * currentPrice / 100).toString()
                depositPrice_textView.text = temp

            } else {
                mortgage_button.setText(R.string.retrieve_uppercase)
                mortgageRateText_textView.setText(R.string.mortgage_retrival_rate)

                depositPriceText_textView.setText(R.string.retrieval_rate_per_stock)

                var temp = " :  " + Constants.MORTGAGE_RETRIEVE_RATE.toString() + "%"
                mortgageRate_textView.text = temp

                temp = " :  " + Constants.RUPEE_SYMBOL + " " + (Constants.MORTGAGE_RETRIEVE_RATE * currentPrice / 100).toString()
                depositPrice_textView.text = temp
            }
        }

        val ownedString = "N/A"
        stocksOwned_textView.text = ownedString

        val mortgageString = "N/A"
        stocksMortgaged_textView.text = mortgageString

        mortgage_button.setOnClickListener { onMortgageButtonClick() }
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

        stocksTransaction = Integer.parseInt(stocks_editText.text.toString().trim { it <= ' ' })

        when (mortgageRadioGroup.checkedRadioButtonId) {

            R.id.mortgage_radioButton -> {
                if (stocksTransaction <= stocksOwned) {
                    val mortgageStocksResponse = actionServiceBlockingStub.mortgageStocks(
                            MortgageStocksRequest.newBuilder().setStockId(mortgage_companies_spinner.selectedItemPosition + 1)
                                    .setStockQuantity(Integer.parseInt(stocks_editText.text.toString())).build()
                    )

                    if (mortgageStocksResponse.statusCode == MortgageStocksResponse.StatusCode.OK) {
                        Toast.makeText(context, "Transaction successful", Toast.LENGTH_SHORT).show()

                        // Not using ViewModel data because stream update might be slow; anyway it is in sync with logic
                        stocksOwned -= stocksTransaction
                        stocksMortgaged += stocksTransaction

                        val ownedString = " :  " + stocksOwned.toString()
                        stocksOwned_textView.text = ownedString

                        val mortgageString = " :  " + stocksMortgaged.toString()
                        stocksMortgaged_textView.text = mortgageString

                        stocks_editText.setText("")

                    } else
                        Toast.makeText(context, mortgageStocksResponse.statusMessage, Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(activity, "Insufficient Stocks", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.retrieve_radioButton -> {
                if (stocksTransaction <= stocksMortgaged && stocksMortgaged >= 0) {
                    val retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                            RetrieveMortgageStocksRequest.newBuilder().setStockId(mortgage_companies_spinner.selectedItemPosition + 1)
                                    .setStockQuantity(Integer.parseInt(stocks_editText.text.toString())).build()
                    )

                    if (retrieveStocksResponse.statusCode == RetrieveMortgageStocksResponse.StatusCode.OK) {
                        Toast.makeText(context, "Transaction successful", Toast.LENGTH_SHORT).show()

                        stocksOwned += stocksTransaction
                        stocksMortgaged -= stocksTransaction

                        val ownedString = " :  " + stocksOwned.toString()
                        stocksOwned_textView.text = ownedString

                        val mortgageString = " :  " + stocksMortgaged.toString()
                        stocksMortgaged_textView.text = mortgageString

                        stocks_editText.setText("")

                    } else {
                        Toast.makeText(context, retrieveStocksResponse.statusMessage, Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(activity, "You don't have sufficient stocks mortgaged", Toast.LENGTH_SHORT).show()
                }
            }

            else -> Toast.makeText(context, "Select mortgage or retrieve", Toast.LENGTH_SHORT).show()
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

                        stocksMortgaged = if (response.mortgageMapMap.containsKey(lastStockId)) response.mortgageMapMap[lastStockId]!! else 0

                        val mortgageString = " :  " + stocksMortgaged.toString()
                        stocksMortgaged_textView.text = mortgageString

                        stocksOwned = getQuantityOwnedFromCompanyName(model.ownedStockDetails, getCompanyNameFromStockId(lastStockId))

                        val ownedString = " :  " + stocksOwned.toString()
                        stocksOwned_textView.text = ownedString

                        val currentPrice = StockUtils.getPriceFromStockId(model.globalStockDetails, lastStockId)

                        val tempString = " :  " + Constants.RUPEE_SYMBOL + " " + currentPrice.toString()
                        currentPrice_textView.text = tempString

                        if (mortgageRadioGroup.checkedRadioButtonId == R.id.mortgage_radioButton) {
                            mortgage_button.setText(R.string.mortgage_uppercase)
                            mortgageRateText_textView.setText(R.string.mortgage_deposit_rate)

                            var temp = " :  " + Constants.MORTGAGE_DEPOSIT_RATE.toString() + "%"
                            mortgageRate_textView.text = temp

                            depositPriceText_textView.setText(R.string.deposit_rate_per_stock)

                            temp = " :  " + Constants.RUPEE_SYMBOL + " " + (Constants.MORTGAGE_DEPOSIT_RATE * currentPrice / 100).toString()
                            depositPrice_textView.text = temp

                        } else if (mortgageRadioGroup.checkedRadioButtonId == R.id.retrieve_radioButton) {
                            mortgage_button.setText(R.string.retrieve_uppercase)
                            mortgageRateText_textView.setText(R.string.mortgage_retrival_rate)

                            depositPriceText_textView.setText(R.string.retrieval_rate_per_stock)

                            var temp = " :  " + Constants.MORTGAGE_RETRIEVE_RATE.toString() + "%"
                            mortgageRate_textView.text = temp

                            temp = " :  " + Constants.RUPEE_SYMBOL + " " + (Constants.MORTGAGE_RETRIEVE_RATE * currentPrice / 100).toString()
                            depositPrice_textView.text = temp
                        }
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
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver,
                IntentFilter(Constants.REFRESH_STOCK_PRICES_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }
}