package org.pragyan.dalal18.fragment

import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.PlaceOrderRequest
import kotlinx.android.synthetic.main.fragment_trade.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.*
import org.pragyan.dalal18.utils.Constants.ORDER_FEE_RATE
import org.pragyan.dalal18.utils.Constants.PREF_TRADE
import java.text.DecimalFormat
import javax.inject.Inject

class TradeFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var model: DalalViewModel

    private var decimalFormat = DecimalFormat(Constants.PRICE_FORMAT)

    private var loadingDialog: AlertDialog? = null
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private val refreshOwnedStockDetails = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && (intent.action == Constants.REFRESH_OWNED_STOCKS_FOR_ALL || intent.action == Constants.REFRESH_STOCK_PRICES_FOR_ALL)) {
                val stocksOwned = model.getQuantityOwnedFromCompanyName(companySpinner.selectedItem.toString())
                var tempString = " :" + decimalFormat.format(stocksOwned).toString()
                stocksOwnedTextView.text = tempString

                tempString = " : " + Constants.RUPEE_SYMBOL + " " + decimalFormat.format(model.getPriceFromCompanyName(companySpinner.selectedItem.toString())).toString()
                currentStockPrice_textView.text = tempString

                setOrderPriceWindow()
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
        val rootView = inflater.inflate(R.layout.fragment_trade, container, false)

        // company model which has the company name data, and is commonly used for trade fragment and market depth fragment.
        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.trade)
        val companiesAdapter = ArrayAdapter(context!!, R.layout.order_spinner_item, model.getCompanyNamesArray())
        val orderSelectAdapter = ArrayAdapter(context!!, R.layout.order_spinner_item, resources.getStringArray(R.array.orderType))

        with(order_select_spinner) {
            adapter = orderSelectAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (parent?.getItemAtPosition(position).toString() == "Market Order") {
                        order_price_input.visibility = View.GONE
                        orderPriceWindowTextView.visibility = View.GONE
                    } else {
                        order_price_input.visibility = View.VISIBLE
                        orderPriceWindowTextView.visibility = View.VISIBLE
                        setOrderPriceWindow()
                    }
                    calculateOrderFee()
                }
            }
        }

        with(companySpinner) {
            adapter = companiesAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    var selectedCompany = companySpinner.selectedItem.toString()
                    //first checking the suffix and then removing it
                    //NEED TO CHECK IF IT WORKS FINE
                    if(selectedCompany.endsWith(getString(R.string.dividendSuffix))){
                        selectedCompany=selectedCompany.removeSuffix(getString(R.string.dividendSuffix))
                    }
                    else if (selectedCompany.endsWith(getString(R.string.bankruptSuffix))){
                        selectedCompany=selectedCompany.removeSuffix(getString(R.string.dividendSuffix))
                    }

                    model.updateFavouriteCompanyName(selectedCompany)

                    val stocksOwned = model.getQuantityOwnedFromCompanyName(selectedCompany)
                    var tempString = " : $stocksOwned"
                    stocksOwnedTextView.text = tempString

                    tempString = " : " + Constants.RUPEE_SYMBOL + " " + decimalFormat.format(model.getPriceFromCompanyName(companySpinner.selectedItem.toString())).toString()
                    currentStockPrice_textView.text = tempString

                    calculateOrderFee()
                    setOrderPriceWindow()
                }
            }
        }

        radioGroupStock.setOnCheckedChangeListener { _, id ->
            bidAskButton.text = if (id == R.id.bidRadioButton) "BID" else "ASK"
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Placing Order..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        noOfStocksEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                calculateOrderFee()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        orderPriceEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                calculateOrderFee()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        val orderTypeSpinner = view.findViewById<Spinner>(R.id.order_select_spinner)
        if (preferences.getBoolean(PREF_TRADE, true)) {
            preferences.edit().putBoolean(PREF_TRADE, false).apply()
            DalalTourUtils.genericViewTour(activity as AppCompatActivity, orderTypeSpinner, 1, getString(R.string.ordertype_tour))
        }

        bidAskButton.setOnClickListener { onBidAskButtonClick() }
        btnMarketDepth.setOnClickListener { onMarketDepthButtonPressed() }

    }

    private fun onMarketDepthButtonPressed() {
        findNavController().navigate(R.id.market_depth_dest)
    }

    private fun calculateOrderFee() {

        val price = if (order_price_input.visibility == View.GONE) {
            model.getPriceFromCompanyName(companySpinner.selectedItem.toString())
        } else {
            if (orderPriceEditText.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                (orderPriceEditText.text.toString()).toLong()
            } else {
                0
            }
        }

        val noOfStocks = if (noOfStocksEditText.text.toString().trim { it <= ' ' }.isNotEmpty()) {
            noOfStocksEditText.text.toString().toLong()
        } else {
            0
        }

        val orderFee = (ORDER_FEE_RATE * price.toDouble() * noOfStocks.toDouble()).toLong()

        val temp = " : " + Constants.RUPEE_SYMBOL + decimalFormat.format(orderFee).toString()
        order_fee_textview.text = temp
    }

    private fun setOrderPriceWindow() {
        val currentPrice = model.getPriceFromCompanyName(companySpinner.selectedItem.toString())
        val lowerLimit = currentPrice.toDouble() * (1 - Constants.ORDER_PRICE_WINDOW.toDouble() / 100)
        val higherLimit = currentPrice.toDouble() * (1 + Constants.ORDER_PRICE_WINDOW.toDouble() / 100)
        val tempOrderPriceText = "Price must be between " + Constants.RUPEE_SYMBOL + DecimalFormat(Constants.PRICE_FORMAT).format(lowerLimit.toLong()) + " - " +
                Constants.RUPEE_SYMBOL + DecimalFormat(Constants.PRICE_FORMAT).format(higherLimit.toLong())
        orderPriceWindowTextView.text = tempOrderPriceText
    }

    private fun onBidAskButtonClick() {
        if (noOfStocksEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            context?.toast("Enter the number of stocks")
        } else if ((noOfStocksEditText.text.toString()).toLong() == 0L) {
            context?.toast("Enter valid number of stocks")
        } else if (radioGroupStock.checkedRadioButtonId == -1) {
            context?.toast("Select order type")
        } else if (order_price_input.visibility == View.VISIBLE && orderPriceEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            context?.toast("Enter the order price")
        } else {
            tradeAsynchronously()
        }
    }

    private fun tradeAsynchronously() {

        loadingDialog?.show()

        val price = if (order_price_input.visibility == View.GONE) 0 else (orderPriceEditText.text.toString()).toLong()
        val orderRequest = PlaceOrderRequest
                .newBuilder()
                .setIsAsk(radioGroupStock.checkedRadioButtonId == R.id.askRadioButton)
                .setStockId(model.getStockIdFromCompanyName(companySpinner.selectedItem.toString()))
                .setOrderType(OrderTypeUtils.getOrderTypeFromName(order_select_spinner.selectedItem.toString()))
                .setPrice(price)
                .setStockQuantity((noOfStocksEditText.text.toString()).toLong())
                .build()

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val orderResponse = actionServiceBlockingStub.placeOrder(orderRequest)

                    uiThread {
                        if (orderResponse.statusCodeValue == 0) {
                            context?.toast("Order Placed")
                            noOfStocksEditText.setText("")
                            orderPriceEditText.setText("")
                            view?.hideKeyboard()
                        } else {
                            context?.toast(orderResponse.statusMessage)
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.trade_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.trade_dest) }
            }
            uiThread { loadingDialog?.dismiss() }
        }
    }

    override fun onResume() {
        super.onResume()

        // to manually select the element after trade fragment is opened by pressing back key from market depth frag.
        if (model.favoriteCompanyName != null) {
            companySpinner.setSelection(model.getIndexForFavoriteCompany())
        }

        val intentFilter = IntentFilter(Constants.REFRESH_OWNED_STOCKS_FOR_ALL)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_FOR_ALL)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshOwnedStockDetails, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshOwnedStockDetails)
    }
}
