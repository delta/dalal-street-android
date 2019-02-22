package org.pragyan.dalal18.fragment


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.actions.CancelOrderRequest
import dalalstreet.api.actions.CancelOrderResponse
import dalalstreet.api.actions.GetMyOpenOrdersRequest
import dalalstreet.api.actions.GetMyOpenOrdersResponse
import dalalstreet.api.datastreams.*
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.fragment_my_orders.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.OrdersRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.Order
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import javax.inject.Inject

class OrdersFragment : Fragment(), OrdersRecyclerAdapter.OnOrderClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    @Inject
    lateinit var streamServiceBlockingStub: DalalStreamServiceGrpc.DalalStreamServiceBlockingStub

    private var ordersRecyclerAdapter: OrdersRecyclerAdapter? = null
    private var orderSubscriptionId: SubscriptionId? = null

    private var networkDownHandler: ConnectionUtils.OnNetworkDownHandler? = null
    private lateinit var loadingOrdersDialog: AlertDialog

    private var openOrdersList = mutableListOf<Order>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_my_orders, container, false)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.orders_frag_label)
        ordersRecyclerAdapter = OrdersRecyclerAdapter(context, null, this)
        ordersRecycler_swipeRefreshLayout.setOnRefreshListener(this)

        with(orders_recyclerView) {
            setHasFixedSize(false)
            adapter = ordersRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.getting_your_orders)
        loadingOrdersDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        getOpenOrdersAsynchronously()

        tradeFloatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.trade_dest)
        }
    }

    private fun getMyOrdersSubscriptionId() {
        doAsync {
            val response = streamServiceBlockingStub.subscribe(
                    SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MY_ORDERS).setDataStreamId("").build())
            orderSubscriptionId = response.subscriptionId
            uiThread { subscribeToMyOrdersStream(response.subscriptionId) }
        }
    }

    private fun subscribeToMyOrdersStream(subscriptionId: SubscriptionId) {
        streamServiceStub.getMyOrderUpdates(subscriptionId, object : StreamObserver<MyOrderUpdate> {
            override fun onNext(orderUpdate: MyOrderUpdate) {

                // TODO : Check if LocalBroadcast implementation is required because flipVisibilities() changes UI
                val empty = ordersRecyclerAdapter?.updateOrder(orderUpdate)
                flipVisibilities(empty)

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

                uiThread {
                    loadingOrdersDialog.dismiss()

                    if (openOrdersResponse?.statusCode == GetMyOpenOrdersResponse.StatusCode.OK) {
                        ordersRecycler_swipeRefreshLayout.isRefreshing = false

                        val openOrdersList = mutableListOf<Order>()


                        val askList = openOrdersResponse.openAskOrdersList
                        val bidList = openOrdersResponse.openBidOrdersList

                        if (askList.size > 0) {
                            for (currentAskOrder in askList) {
                                openOrdersList.add(Order(
                                        currentAskOrder.id,
                                        false,
                                        false,
                                        currentAskOrder.price,
                                        currentAskOrder.stockId,
                                        currentAskOrder.orderType,
                                        currentAskOrder.stockQuantity,
                                        currentAskOrder.stockQuantityFulfilled
                                ))
                            }
                        }

                        if (bidList.size > 0) {
                            for (currentBidOrder in bidList) {
                                openOrdersList.add(Order(
                                        currentBidOrder.id,
                                        true,
                                        false,
                                        currentBidOrder.price,
                                        currentBidOrder.stockId,
                                        currentBidOrder.orderType,
                                        currentBidOrder.stockQuantity,
                                        currentBidOrder.stockQuantityFulfilled
                                ))
                            }
                        }


                        val empty = ordersRecyclerAdapter?.swapData(openOrdersList)
                        flipVisibilities(empty)

                    } else {
                        context?.longToast(openOrdersResponse.statusMessage)
                    }
                }
            } else {
                uiThread { networkDownHandler?.onNetworkDownError() }
            }
        }
    }

    override fun onCancelOrderClick(orderId: Int, bid: Boolean) {

        if (context != null) {
            val builder = AlertDialog.Builder(context!!, R.style.AlertDialogTheme)
                    .setTitle("Cancel Confirm")
                    .setCancelable(true)
                    .setMessage("Do you want to cancel this order ?")
                    .setPositiveButton("Yes") { _, _ ->
                        Handler().post {
                            val response = actionServiceBlockingStub.cancelOrder(
                                    CancelOrderRequest.newBuilder().setOrderId(orderId).setIsAsk(!bid).build())

                            if (response.statusCode == CancelOrderResponse.StatusCode.OK) {
                                context?.toast("Order cancelled")

                                val empty = ordersRecyclerAdapter?.cancelOrder(orderId)
                                flipVisibilities(empty)

                            } else {
                                context?.toast(response.statusMessage)
                            }
                        }
                    }
                    .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            builder.show()
        }
    }

    private fun flipVisibilities(empty: Boolean?) {
        if (empty != null && !empty) {
            ordersRecycler_swipeRefreshLayout.visibility = View.VISIBLE
            emptyOrders_relativeLayout.visibility = View.GONE
        } else {
            ordersRecycler_swipeRefreshLayout.visibility = View.GONE
            emptyOrders_relativeLayout.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        getMyOrdersSubscriptionId()
    }

    override fun onPause() {
        super.onPause()
        loadingOrdersDialog.dismiss()
        doAsync {
            if (orderSubscriptionId != null) {
                streamServiceBlockingStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(orderSubscriptionId).build())
            }
        }
    }

    override fun onRefresh() = getOpenOrdersAsynchronously()
}