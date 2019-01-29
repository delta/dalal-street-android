package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog

import androidx.lifecycle.ViewModelProviders
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.PlaceOrderRequest
import kotlinx.android.synthetic.main.fragment_trade.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import org.pragyan.dalal18.utils.StockUtils.*
import javax.inject.Inject

class TradeFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel

    private var loadingDialog: AlertDialog? = null
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private val refreshOwnedStockDetails = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && (intent.action == Constants.REFRESH_OWNED_STOCKS_ACTION || intent.action == Constants.REFRESH_STOCK_PRICES_ACTION)) {
                val stocksOwned = StockUtils.getQuantityOwnedFromCompanyName(model.ownedStockDetails, companySpinner.selectedItem.toString())
                var tempString = " :  " + stocksOwned.toString()
                stocksOwnedTextView.text = tempString

                tempString = " : " + Constants.RUPEE_SYMBOL + " " + StockUtils.getPriceFromStockId(model.globalStockDetails, StockUtils.getStockIdFromCompanyName(companySpinner.selectedItem.toString())).toString()
                currentStockPrice_textView.text = tempString
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
        val rootView = inflater.inflate(R.layout.fragment_trade, container, false)
        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) } ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.trade)
        val companiesAdapter = ArrayAdapter(context!!, R.layout.order_spinner_item, StockUtils.getCompanyNamesArray())
        val orderSelectAdapter = ArrayAdapter(context!!, R.layout.order_spinner_item, resources.getStringArray(R.array.orderType))

        with(order_select_spinner) {
            adapter = orderSelectAdapter
            setSelection(1)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (parent?.getItemAtPosition(position).toString() == "Market Order") {
                        orderPriceEditText.visibility = View.GONE
                    } else {
                        orderPriceEditText.visibility = View.VISIBLE
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
                    val stocksOwned = StockUtils.getQuantityOwnedFromCompanyName(model.ownedStockDetails, companySpinner.selectedItem.toString())
                    var tempString = " :  " + stocksOwned.toString()
                    stocksOwnedTextView.text = tempString

                    tempString = " : " + Constants.RUPEE_SYMBOL + " " + StockUtils.getPriceFromStockId(model.globalStockDetails, StockUtils.getStockIdFromCompanyName(companySpinner.selectedItem.toString())).toString()
                    currentStockPrice_textView.text = tempString

                    calculateOrderFee()
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

        bidAskButton.setOnClickListener { onBidAskButtonClick() }
    }

    private fun calculateOrderFee() {

        val price = if (orderPriceEditText.visibility == View.GONE) {
            StockUtils.getPriceFromStockId(model.globalStockDetails, StockUtils.getStockIdFromCompanyName(companySpinner.selectedItem.toString()))
        } else {
            if (!orderPriceEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
                (orderPriceEditText.text.toString()).toLong()
            } else {
                0
            }
        }

        val noOfStocks = if (!noOfStocksEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            noOfStocksEditText.text.toString().toLong()
        } else {
            0
        }

        val orderFee = (.03 * price * noOfStocks).toLong()

        val temp = " : " + Constants.RUPEE_SYMBOL + orderFee.toString()
        order_fee_textview.text = temp
    }

    private fun onBidAskButtonClick() {
        if (noOfStocksEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(activity, "Enter the number of stocks", Toast.LENGTH_SHORT).show()
        } else if ((noOfStocksEditText.text.toString()).toLong() == 0L) {
            Toast.makeText(activity, "Enter valid number of stocks", Toast.LENGTH_SHORT).show()
        } else if (radioGroupStock.checkedRadioButtonId == -1) {
            Toast.makeText(activity, "Select order type", Toast.LENGTH_SHORT).show()
        } else if (orderPriceEditText.visibility == View.VISIBLE && orderPriceEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(activity, "Enter the order price", Toast.LENGTH_SHORT).show()
        } else if (radioGroupStock.checkedRadioButtonId == R.id.askRadioButton) {
            val validQuantity = getQuantityOwnedFromCompanyName(model.ownedStockDetails, companySpinner.selectedItem.toString())
            val askingQuantity = (noOfStocksEditText.text.toString()).toLong()
            if (askingQuantity > validQuantity) {
                Toast.makeText(context, "You don't have sufficient stocks", Toast.LENGTH_SHORT).show()
            } else {
                tradeAsynchronously()
            }
        } else {
            tradeAsynchronously()
        }
    }

    private fun tradeAsynchronously() {

        loadingDialog?.show()

        val price = if (orderPriceEditText.visibility == View.GONE) 0 else (orderPriceEditText.text.toString()).toLong()
        val orderRequest = PlaceOrderRequest
                .newBuilder()
                .setIsAsk(radioGroupStock.checkedRadioButtonId == R.id.askRadioButton)
                .setStockId(getStockIdFromCompanyName(companySpinner.selectedItem.toString()))
                .setOrderType(getOrderTypeFromName(order_select_spinner.selectedItem.toString()))
                .setPrice(price)
                .setStockQuantity((noOfStocksEditText.text.toString()).toLong())
                .build()

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val orderResponse = actionServiceBlockingStub.placeOrder(orderRequest)

                uiThread {
                    loadingDialog?.dismiss()
                    if (orderResponse.statusCodeValue == 0) {
                        Toast.makeText(context, "Order Placed", Toast.LENGTH_SHORT).show()
                        noOfStocksEditText.setText("")
                        orderPriceEditText.setText("")
                    } else {
                        Toast.makeText(context, orderResponse.statusMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constants.REFRESH_OWNED_STOCKS_ACTION)
        intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshOwnedStockDetails, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshOwnedStockDetails)
    }
}
