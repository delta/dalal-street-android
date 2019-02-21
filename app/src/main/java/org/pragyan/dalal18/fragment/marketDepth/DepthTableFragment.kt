package org.pragyan.dalal18.fragment.marketDepth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.actions.GetCompanyProfileRequest
import dalalstreet.api.datastreams.*
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.fragment_depth_table.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.MarketDepthRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.MarketDepth
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.StockUtils
import org.pragyan.dalal18.utils.StockUtils.getStockIdFromCompanyName
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

class DepthTableFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    private var bidArrayList = mutableListOf<MarketDepth>()
    private var askArrayList = mutableListOf<MarketDepth>()
    private val df = DecimalFormat(Constants.PRICE_FORMAT)
    private var loadingDialog: AlertDialog? = null
    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    lateinit var bidDepthAdapter: MarketDepthRecyclerAdapter
    lateinit var askDepthAdapter:MarketDepthRecyclerAdapter

    private var subscriptionId: SubscriptionId? = null
    private var prevSubscriptionId:SubscriptionId? = null


    private val refreshMarketDepth = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (activity != null && isAdded) {
                if (askArrayList.size == 0) {
                    ask_depth_holder.visibility = View.VISIBLE
                } else if (askArrayList.size > 0) {
                    ask_depth_holder.visibility = View.GONE
                }

                if (bidArrayList.size == 0) {
                    bid_depth_holder.visibility = View.VISIBLE
                } else if (bidArrayList.size > 0) {
                    bid_depth_holder.visibility = View.GONE
                }
                ask_depth_layout.visibility = View.VISIBLE
                bid_depth_layout.visibility = View.VISIBLE
                market_order_text.visibility = View.VISIBLE
                depth_table_holder.visibility = View.INVISIBLE
                bidArrayList.sortWith(Comparator { (price1), (price2) -> price1.compareTo(price2) })
                bidArrayList.reverse()
                askArrayList.sortWith(Comparator { (price1), (price2) -> price1.compareTo(price2) })
                bidDepthAdapter.swapData(bidArrayList)
                askDepthAdapter.swapData(askArrayList)
                loadingDialog?.dismiss()
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
        val rootView = inflater.inflate(R.layout.fragment_depth_table, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_depth)
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        bidDepthAdapter = MarketDepthRecyclerAdapter(context, bidArrayList)
        askDepthAdapter = MarketDepthRecyclerAdapter(context, askArrayList)

        with(bid_depth_rv){
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = bidDepthAdapter
            isNestedScrollingEnabled = false
        }

        with(ask_depth_rv){
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = askDepthAdapter
            isNestedScrollingEnabled = false
        }

        val arrayAdapter = ArrayAdapter(activity!!, R.layout.company_spinner_item, StockUtils.getCompanyNamesArray())
        with(companySpinner){
            setAdapter(arrayAdapter)
            setOnItemClickListener { _, _, _, _ ->
                val currentCompany = companySpinner.text.toString()
                bidArrayList.clear()
                askArrayList.clear()
                getValues(currentCompany)
                unsubscribe(prevSubscriptionId)
                current_stock_price_layout.visibility = View.INVISIBLE
                ask_depth_layout.visibility = View.INVISIBLE
                bid_depth_layout.visibility = View.INVISIBLE
                if (activity != null && isAdded) {
                    loadingDialog?.show()
                    getCompanyProfileAsynchronously(currentCompany)
                }
            }
        }
    }

    private fun getValues(currentCompany: String) {

        prevSubscriptionId = subscriptionId
        doAsync {
            streamServiceStub.subscribe(
                    SubscribeRequest
                            .newBuilder()
                            .setDataStreamType(DataStreamType.MARKET_DEPTH)
                            .setDataStreamId(getStockIdFromCompanyName(currentCompany).toString())
                            .build(),
                    object : StreamObserver<SubscribeResponse> {
                        override fun onNext(value: SubscribeResponse) {
                            if (value.statusCode.number == 0) {
                                subscriptionId = value.subscriptionId
                                getMarketDepth(value.subscriptionId)
                            } else {
                                uiThread { context?.toast("Server internal error") }
                            }
                            onCompleted()
                        }

                        override fun onError(t: Throwable) {

                        }

                        override fun onCompleted() {

                        }
                    })
        }
    }

    private fun getMarketDepth(subscriptionId: SubscriptionId) {
        streamServiceStub.getMarketDepthUpdates(subscriptionId,
                object : StreamObserver<MarketDepthUpdate> {
                    override fun onNext(value: MarketDepthUpdate) {

                        for (map in value.bidDepthMap.entries) {
                            if (map.key >= 0) {
                                var price = map.key
                                val volume = map.value
                                if (price == 0L) {
                                    price = Long.MAX_VALUE
                                }
                                val temp = MarketDepth(price, volume)
                                bidArrayList.add(temp)
                            }
                        }

                        for ((key, value1) in value.askDepthMap) {
                            if (key >= 0) {
                                val temp = MarketDepth(key, value1)
                                askArrayList.add(temp)
                            }
                        }

                        for (map in value.bidDepthDiffMap.entries) {
                            var price = map.key
                            val volume = map.value
                            if (price == 0L) {
                                price = Long.MAX_VALUE
                            }
                            if (!containsBid(price, volume) && price > 0) {
                                bidArrayList.add(MarketDepth(price, volume))
                            }
                        }

                        for ((price, volume) in value.askDepthDiffMap) {
                            if (!containsAsk(price, volume) && price > 0) {
                                askArrayList.add(MarketDepth(price, volume))
                            }
                        }
                        val marketDepthIntent = Intent(Constants.REFRESH_MARKET_DEPTH)

                        if (context != null)
                            LocalBroadcastManager.getInstance(context!!).sendBroadcast(marketDepthIntent)
                    }

                    override fun onError(t: Throwable) {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    private fun unsubscribe(prevSubscriptionId: SubscriptionId?) {

        doAsync {
            if (prevSubscriptionId != null) {
                streamServiceStub.unsubscribe(
                        UnsubscribeRequest.newBuilder()
                                .setSubscriptionId(prevSubscriptionId)
                                .build(),
                        object : StreamObserver<UnsubscribeResponse> {
                            override fun onNext(value: UnsubscribeResponse) {
                                if (value.statusCode.number != 0)
                                    context?.toast("Server internal error")
                                onCompleted()
                            }

                            override fun onError(t: Throwable) {

                            }

                            override fun onCompleted() {

                            }
                        })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribe(subscriptionId)
    }

    private fun getCompanyProfileAsynchronously(currentCompany: String) {

        doAsync {

            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)){
                val companyProfileResponse = actionServiceBlockingStub.getCompanyProfile(
                        GetCompanyProfileRequest.newBuilder().setStockId(getStockIdFromCompanyName(currentCompany)).build())
                uiThread {
                    if (activity != null && isAdded) {
                        val currentStock = companyProfileResponse.stockDetails

                        val currentPrice = currentStock.currentPrice
                        val prevDayClose = currentStock.previousDayClose

                        current_stock_price_layout.visibility = View.VISIBLE
                        val currentStockPrice = "Current Stock Price : " + Constants.RUPEE_SYMBOL + df.format(currentPrice).toString()
                        current_stock_price_textView.text = currentStockPrice
                        val prevDayClosePrice = Constants.RUPEE_SYMBOL + df.format(Math.abs(currentPrice - prevDayClose)).toString()
                        prev_day_close_stock_price.text = prevDayClosePrice
                        if (currentPrice >= prevDayClose) {
                            arrow_image_view.setImageResource(R.drawable.arrow_up_green)
                        } else {
                            arrow_image_view.setImageResource(R.drawable.arrow_down_red)
                        }
                    }
                }
            } else{
                uiThread { networkDownHandler.onNetworkDownError() }
            }

        }
    }


    private fun containsBid(price: Long, newVolume: Long): Boolean {
        for (i in bidArrayList.indices) {
            if (bidArrayList[i].price == price) {
                val tempVol = bidArrayList[i].volume + newVolume
                bidArrayList[i] = MarketDepth(price, tempVol)
                return true
            }
        }
        return false
    }

    private fun containsAsk(price: Long, newVolume: Long): Boolean {
        for (i in askArrayList.indices) {
            if (askArrayList[i].price == price) {
                val tempVol = askArrayList[i].volume + newVolume
                askArrayList[i] = MarketDepth(price, tempVol)
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.REFRESH_MARKET_DEPTH)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(refreshMarketDepth, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(refreshMarketDepth)
    }
}
