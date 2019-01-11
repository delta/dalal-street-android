package org.pragyan.dalal18.fragment


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.actions.CancelOrderRequest
import dalalstreet.api.actions.GetMyOpenOrdersRequest
import dalalstreet.api.actions.GetMyOpenOrdersResponse
import dalalstreet.api.datastreams.*
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.fragment_my_orders.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.OrdersRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.Order
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import java.util.*
import javax.inject.Inject

class OrdersFragment : Fragment(), OrdersRecyclerAdapter.OnOrderClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    private var ordersRecyclerAdapter: OrdersRecyclerAdapter? = null
    private var orderSubscriptionId: SubscriptionId? = null

    private var networkDownHandler: ConnectionUtils.OnNetworkDownHandler? = null
    private lateinit var loadingOrdersDialog: AlertDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement network down handler.")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_my_orders, container, false)

        if (activity != null) activity!!.title = "Open Orders"
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.orders_frag_label)
        ordersRecyclerAdapter = OrdersRecyclerAdapter(context, null, this)
        ordersRecycler_swipeRefreshLayout.setOnRefreshListener(this)

        orders_recyclerView.setHasFixedSize(false)
        orders_recyclerView.adapter = ordersRecyclerAdapter
        orders_recyclerView.layoutManager = LinearLayoutManager(context)

        if (context != null) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_your_orders)
            loadingOrdersDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()
        }

        getOpenOrdersAsynchronously()

        getMyOrdersSubscriptionId()
    }

    private fun getMyOrdersSubscriptionId() {
        Thread {
            streamServiceStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MY_ORDERS).setDataStreamId("").build(),
                    object : StreamObserver<SubscribeResponse> {
                        override fun onNext(value: SubscribeResponse) {
                            subscribeToMyOrdersStream(value.subscriptionId)
                            orderSubscriptionId = value.subscriptionId
                            onCompleted()
                        }

                        override fun onError(t: Throwable) {

                        }

                        override fun onCompleted() {

                        }
                    })
        }.start()
    }

    private fun subscribeToMyOrdersStream(subscriptionId: SubscriptionId) {
        streamServiceStub.getMyOrderUpdates(subscriptionId, object : StreamObserver<MyOrderUpdate> {
            override fun onNext(orderUpdate: MyOrderUpdate) {
                if (activity != null) {
                    val id = orderUpdate.id
                    ordersRecyclerAdapter?.swapSingleItem(id, orderUpdate)
                }
            }

            override fun onError(t: Throwable) {

            }

            override fun onCompleted() {

            }
        })
    }

    private fun getOpenOrdersAsynchronously() {
        loadingOrdersDialog.show()
        doAsync {
            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                val openOrdersResponse = actionServiceBlockingStub.getMyOpenOrders(GetMyOpenOrdersRequest.newBuilder().build())
                if (openOrdersResponse?.statusCode == GetMyOpenOrdersResponse.StatusCode.OK) {
                    ordersRecycler_swipeRefreshLayout.isRefreshing = false

                    val ordersList = ArrayList<Order>()

                    val askList = openOrdersResponse.openAskOrdersList
                    val bidList = openOrdersResponse.openBidOrdersList

                    if (askList.size > 0) {
                        for (currentAskOrder in askList) {
                            ordersList.add(Order(
                                    currentAskOrder.id,
                                    false,
                                    false,
                                    currentAskOrder.price,
                                    currentAskOrder.stockId,
                                    currentAskOrder.orderType.number,
                                    currentAskOrder.stockQuantity,
                                    currentAskOrder.stockQuantityFulfilled
                            ))
                        }
                    }

                    if (bidList.size > 0) {
                        for (currentBidOrder in bidList) {
                            ordersList.add(Order(
                                    currentBidOrder.id,
                                    true,
                                    false,
                                    currentBidOrder.price,
                                    currentBidOrder.stockId,
                                    currentBidOrder.orderType.number,
                                    currentBidOrder.stockQuantity,
                                    currentBidOrder.stockQuantityFulfilled
                            ))
                        }
                    }
                    uiThread {
                        loadingOrdersDialog.dismiss()

                        if (ordersList.size > 0) {
                            ordersRecyclerAdapter!!.swapData(ordersList)
                            ordersRecycler_swipeRefreshLayout.visibility = View.VISIBLE
                            emptyOrders_relativeLayout.visibility = View.GONE
                        } else {
                            ordersRecycler_swipeRefreshLayout.visibility = View.GONE
                            emptyOrders_relativeLayout.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(activity, openOrdersResponse?.statusMessage, Toast.LENGTH_LONG).show()
                }
            } else {
                networkDownHandler?.onNetworkDownError()
            }
        }
    }

    override fun onOrderClick(orderId: Int, bid: Boolean) {

        if (context != null) {
            val builder = AlertDialog.Builder(context!!)
                    .setTitle("Cancel Confirm")
                    .setCancelable(true)
                    .setMessage("Do you want to cancel this order ?")
                    .setPositiveButton("Yes") { _, _ ->
                        Handler().post {
                            val response = actionServiceBlockingStub.cancelOrder(
                                    CancelOrderRequest.newBuilder().setOrderId(orderId).setIsAsk(!bid).build())

                            if (response.statusCodeValue == 0) {
                                Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                                if (activity != null)
                                    getOpenOrdersAsynchronously()
                            } else {
                                Toast.makeText(context, response.statusMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            builder.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingOrdersDialog.dismiss()
        Handler().post {
            if (orderSubscriptionId != null) {
                streamServiceStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(orderSubscriptionId).build(),
                        object : StreamObserver<UnsubscribeResponse> {
                            override fun onNext(value: UnsubscribeResponse) {
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

    override fun onRefresh() {
        getOpenOrdersAsynchronously()
    }
}