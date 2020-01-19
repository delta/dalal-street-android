package org.pragyan.dalal18.fragment


import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.pragyan.dalal18.data.DalalViewModel
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

    private lateinit var model: DalalViewModel

    private var ordersRecyclerAdapter: OrdersRecyclerAdapter? = null
    private var orderSubscriptionId: SubscriptionId? = null

    private var networkDownHandler: ConnectionUtils.OnNetworkDownHandler? = null
    private lateinit var loadingOrdersDialog: AlertDialog

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

        model = activity?.run { ViewModelProviders.of(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

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

        applySwipeFeature()

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
                val empty = ordersRecyclerAdapter?.updateOrder(orderUpdate, model.getCompanyNameFromStockId(orderUpdate.stockId))
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
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val openOrdersResponse = actionServiceBlockingStub.getMyOpenOrders(GetMyOpenOrdersRequest.newBuilder().build())

                    uiThread {

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
                                            model.getCompanyNameFromStockId(currentAskOrder.stockId),
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
                                            model.getCompanyNameFromStockId(currentBidOrder.stockId),
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
                    uiThread { networkDownHandler?.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.open_orders_dest) }
                }
            } else {
                uiThread { networkDownHandler?.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.open_orders_dest) }
            }
            uiThread { loadingOrdersDialog.dismiss() }
        }
    }

    override fun onCancelOrderClick(orderId: Int, bid: Boolean) {

        if (context != null) {
            val builder = AlertDialog.Builder(context!!, R.style.AlertDialogTheme)
                    .setTitle("Cancel Confirm")
                    .setCancelable(true)
                    .setMessage("Do you want to cancel this order ?")
                    .setPositiveButton("Yes") { _, _ -> cancelOrder(orderId, bid) }
                    .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            builder.show()
        }
    }

    private fun cancelOrder(orderId: Int, bid: Boolean) {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                    val response = actionServiceBlockingStub.cancelOrder(
                            CancelOrderRequest.newBuilder().setOrderId(orderId).setIsAsk(!bid).build())

                    uiThread {
                        if (response.statusCode == CancelOrderResponse.StatusCode.OK) {
                            context?.toast("Order cancelled")
                            val empty = ordersRecyclerAdapter?.cancelOrder(orderId)
                            flipVisibilities(empty)
                        } else {
                            context?.toast(response.statusMessage)
                        }
                    }

                } else {
                    uiThread { networkDownHandler?.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.open_orders_dest) }
                }
            } else {
                uiThread { networkDownHandler?.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.open_orders_dest) }
            }
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

    fun applySwipeFeature(){


        val itemTouchHelperCallback =object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT  or ItemTouchHelper.RIGHT){

            val mBackground : ColorDrawable ?=null
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                val itemView =viewHolder.itemView
                val itemHeight =itemView.height
                val deleteDrawable =resources.getDrawable(R.drawable.delete_order)
                val intrinsicWidth = deleteDrawable.intrinsicWidth
                val intrinsicHeight =deleteDrawable.intrinsicHeight
                var  mClearPaint  =Paint()
                mClearPaint.setXfermode( PorterDuffXfermode(PorterDuff.Mode.CLEAR))
                // mClearPaint.color=  resources.getColor(R.color.neon_orange)

                val isCancelled= dX==0f && ! isCurrentlyActive
                if(isCancelled){
                    c.drawRect(itemView.right.toFloat()+dX, itemView.top.toFloat() ,itemView.right.toFloat(),itemView.bottom.toFloat(),mClearPaint)
                    return
                }
                mBackground?.color = resources.getColor(R.color.neon_green)
                mBackground?.setBounds(itemView.right + dX.toInt() ,itemView.top, itemView.right,itemView.bottom)
                mBackground?.draw(c)

                val deleteIconTop =itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = ((itemHeight - intrinsicHeight) / 2)
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight =  itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight
                deleteDrawable.level=0
                deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteDrawable.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.7f
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                val orderid = ordersRecyclerAdapter!!.getOrderIdfromposition(position)
                val isbid=ordersRecyclerAdapter!!.gettypefromposition(position)
                ordersRecyclerAdapter!!.swipedata(orderid,isbid)
                ordersRecyclerAdapter!!.notifyItemRemoved(position)

            }

        }
        val itemTouchHelper =ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(orders_recyclerView)
    }

}