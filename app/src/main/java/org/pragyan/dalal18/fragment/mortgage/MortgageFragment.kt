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
import dalalstreet.api.actions.MortgageStocksRequest
import dalalstreet.api.actions.MortgageStocksResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentMortgageBinding
import org.pragyan.dalal18.ui.MainActivity.Companion.GAME_STATE_UPDATE_ACTION
import org.pragyan.dalal18.utils.*
import java.text.DecimalFormat
import javax.inject.Inject

class MortgageFragment : Fragment() {

    private var binding by viewLifecycle<FragmentMortgageBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var lastStockId = 1
    private val decimalFormat = DecimalFormat(Constants.PRICE_FORMAT)
    private lateinit var model: DalalViewModel

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private var loadingDialog: AlertDialog? = null

    private val refreshStockPricesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action.equals(Constants.REFRESH_STOCK_PRICES_FOR_ALL, ignoreCase = true) ||
                    intent.action.equals(Constants.REFRESH_STOCKS_FOR_MORTGAGE, ignoreCase = true) ||
                    intent.action.equals(GAME_STATE_UPDATE_ACTION, ignoreCase = true)) {
                setupMortgageDetails(lastStockId)
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
        binding = FragmentMortgageBinding.inflate(inflater, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        lastStockId = model.favoriteCompanyStockId ?: 1
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Getting mortgage details..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        setupMortgageDetails(lastStockId)

        binding.apply {
            with(mortgageCompaniesSpinner) {
                val companiesArray = model.getCompanyNamesArray()
                adapter = ArrayAdapter(context!!, R.layout.company_spinner_item, companiesArray)

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                        lastStockId = model.getStockIdFromCompanyName(companiesArray[position])

                        model.updateFavouriteCompanyStockId(lastStockId)

                        setupMortgageDetails(lastStockId)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
            }

            mortgageButton.setOnClickListener { onMortgageButtonClick() }
            stockIncrementFiveButton.setOnClickListener { addToMortgageInput(5) }
            stockIncrementOneButton.setOnClickListener { addToMortgageInput(1) }

            val tempText = " :  ${Constants.MORTGAGE_DEPOSIT_RATE}%"
            depositRateTextView.text = tempText
        }
    }

    private fun addToMortgageInput(increment: Int) {
        binding.apply {
            if (mortgageStocksEditText.text.isBlank())
                mortgageStocksEditText.setText("0")
            var noOfStocks = mortgageStocksEditText.text.toString().toInt()
            noOfStocks = (noOfStocks + increment).coerceAtMost(Constants.ASK_LIMIT)
            mortgageStocksEditText.setText(noOfStocks.toString())
        }
    }

    private fun onMortgageButtonClick() {
        with(binding.mortgageStocksEditText) {
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

        val stocksTransaction = (binding.mortgageStocksEditText.text.toString().trim { it <= ' ' }).toLong()

        if (stocksTransaction <= model.getQuantityOwnedFromStockId(lastStockId))
            mortgageStocksAsynchronously(stocksTransaction)
        else context?.toast("Insufficient Stocks")
    }

    private fun setupMortgageDetails(stockId: Int) {
        binding.apply {
            val ownedString = " :  " + decimalFormat.format(model.getQuantityOwnedFromStockId(stockId))
            stocksOwnedTextView.text = ownedString

            val tempString = " :  " + Constants.RUPEE_SYMBOL + " " + decimalFormat.format(model.getPriceFromStockId(stockId))
            currentPriceTextView.text = tempString

            val mortgagedString = " :  " + decimalFormat.format(model.getMortgagedStocksFromStockId(stockId))
            stocksMortgagedTextView.text = mortgagedString

            when {
                model.getIsBankruptFromStockId(lastStockId) -> companyStatusIndicatorImageView.setStatusIndicator(context, View.VISIBLE, getString(R.string.this_company_is_bankrupt), R.drawable.bankrupt_icon)
                model.getGivesDividendFromStockId(lastStockId) -> companyStatusIndicatorImageView.setStatusIndicator(context, View.VISIBLE, getString(R.string.this_company_gives_dividend), R.drawable.dividend_icon)
                else -> companyStatusIndicatorImageView.setStatusIndicator(context, View.GONE, "", R.drawable.clear_icon)
            }
        }
    }

    private fun mortgageStocksAsynchronously(stocksTransaction: Long) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Mortgaging Stocks..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()
        loadingDialog?.show()
        GlobalScope.async (Dispatchers.Default){

            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val mortgageStocksResponse = actionServiceBlockingStub.mortgageStocks(
                            MortgageStocksRequest.newBuilder().setStockId(lastStockId)
                                    .setStockQuantity(stocksTransaction).build())

                    withContext(Dispatchers.Main) {
                        if (mortgageStocksResponse.statusCode == MortgageStocksResponse.StatusCode.OK) {
                            context?.toast("Transaction successful")
                            binding.mortgageStocksEditText.setText("0")
                            view?.hideKeyboard()
                        } else {
                            context?.toast(mortgageStocksResponse.statusMessage)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.main_mortgage_dest) }
                }
            } else {
                withContext(Dispatchers.Main) { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.main_mortgage_dest) }
            }
            withContext(Dispatchers.Main) { loadingDialog?.dismiss() }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.mortgageCompaniesSpinner.setSelection(model.getIndexForFavoriteCompany())

        val intentFilter = IntentFilter(Constants.REFRESH_STOCK_PRICES_FOR_ALL)
        intentFilter.addAction(Constants.REFRESH_STOCKS_FOR_MORTGAGE)
        intentFilter.addAction(GAME_STATE_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshStockPricesReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshStockPricesReceiver)
    }
}
