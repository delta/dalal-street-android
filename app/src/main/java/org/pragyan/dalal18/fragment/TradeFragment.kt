package org.pragyan.dalal18.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.PlaceOrderRequest
import kotlinx.android.synthetic.main.fragment_trade.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import org.pragyan.dalal18.utils.StockUtils.*
import javax.inject.Inject

class TradeFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private var loadingDialog: AlertDialog? = null
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private val refreshOwnedStockDetails = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && (intent.action == Constants.REFRESH_OWNED_STOCKS_ACTION || intent.action == Constants.REFRESH_STOCK_PRICES_ACTION)) {
                val stocksOwned = StockUtils.getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, companySpinner.selectedItem.toString())
                var tempString = " :  " + stocksOwned.toString()
                stocksOwned_textView.text = tempString

                tempString = " : " + Constants.RUPEE_SYMBOL + " " + StockUtils.getPriceFromStockId(MainActivity.globalStockDetails, StockUtils.getStockIdFromCompanyName(companySpinner.selectedItem.toString())).toString()
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
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val companiesAdapter = ArrayAdapter(context!!, R.layout.order_spinner_item, StockUtils.getCompanyNamesArray())
        val orderSelectAdapter = ArrayAdapter(context!!, R.layout.order_spinner_item, resources.getStringArray(R.array.orderType))

        with(order_select_spinner){
            adapter = orderSelectAdapter
            setSelection(1)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (parent?.getItemAtPosition(position).toString() == "Market Order") {
                        orderPriceEditText.visibility = View.GONE
                    } else {
                        orderPriceEditText.visibility = View.VISIBLE
                    }
                }
            }
        }

        with(companySpinner){
            adapter = companiesAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val stocksOwned = StockUtils.getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, companySpinner.selectedItem.toString())
                    var tempString = " :  " + stocksOwned.toString()
                    stocksOwned_textView.text = tempString

                    tempString = " : " + Constants.RUPEE_SYMBOL + " " + StockUtils.getPriceFromStockId(MainActivity.globalStockDetails, StockUtils.getStockIdFromCompanyName(companySpinner.selectedItem.toString())).toString()
                    currentStockPrice_textView.text = tempString
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

        bidAskButton.setOnClickListener { onBidAskButtonClick() }
    }

    private fun onBidAskButtonClick() {
        if (noOfStocksEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(activity, "Enter the number of stocks", Toast.LENGTH_SHORT).show()
        } else if (Integer.parseInt(noOfStocksEditText.text.toString()) == 0) {
            Toast.makeText(activity, "Enter valid number of stocks", Toast.LENGTH_SHORT).show()
        } else if (radioGroupStock.checkedRadioButtonId == -1) {
            Toast.makeText(activity, "Select order type", Toast.LENGTH_SHORT).show()
        } else if (orderPriceEditText.visibility == View.VISIBLE && orderPriceEditText.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(activity, "Enter the order price", Toast.LENGTH_SHORT).show()
        } else if (radioGroupStock.checkedRadioButtonId == R.id.askRadioButton) {
            val validQuantity = getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, companySpinner.selectedItem.toString())
            val askingQuantity = Integer.parseInt(noOfStocksEditText.text.toString())
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

        val price = if (orderPriceEditText.visibility == View.GONE) 0 else Integer.parseInt(orderPriceEditText.text.toString())
        val orderRequest = PlaceOrderRequest
                .newBuilder()
                .setIsAsk(radioGroupStock.checkedRadioButtonId == R.id.askRadioButton)
                .setStockId(getStockIdFromCompanyName(companySpinner.selectedItem.toString()))
                .setOrderType(getOrderTypeFromName(order_select_spinner.selectedItem.toString()))
                .setPrice(price)
                .setStockQuantity(Integer.parseInt(noOfStocksEditText.text.toString()))
                .build()

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)){
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
