package org.pragyan.dalal18.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.OrdersRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.Order;
import org.pragyan.dalal18.loaders.OpenOrdersLoader;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.actions.CancelOrderRequest;
import dalalstreet.api.actions.CancelOrderResponse;
import dalalstreet.api.actions.GetMyOpenOrdersResponse;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.MyOrderUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.datastreams.UnsubscribeRequest;
import dalalstreet.api.datastreams.UnsubscribeResponse;
import dalalstreet.api.models.Ask;
import dalalstreet.api.models.Bid;
import io.grpc.stub.StreamObserver;

public class OrdersFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<GetMyOpenOrdersResponse>,
        OrdersRecyclerAdapter.OnOrderClickListener,
        SwipeRefreshLayout.OnRefreshListener{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    RecyclerView orderRecyclerView;
    RelativeLayout emptyOrderRelativeLayout;
    SwipeRefreshLayout recyclerContainerSwipeRefreshLayout;

    private OrdersRecyclerAdapter ordersRecyclerAdapter;
    private SubscriptionId orderSubscriptionId = null;

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    AlertDialog loadingOrdersDialog;

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_orders, container, false);

        if (getActivity() != null) getActivity().setTitle("Open Orders");
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        orderRecyclerView = rootView.findViewById(R.id.orders_recyclerView);
        emptyOrderRelativeLayout = rootView.findViewById(R.id.emptyOrders_relativeLayout);
        recyclerContainerSwipeRefreshLayout = rootView.findViewById(R.id.ordersRecycler_swipeRefreshLayout);

        ordersRecyclerAdapter = new OrdersRecyclerAdapter(getContext(), null, this);
        recyclerContainerSwipeRefreshLayout.setOnRefreshListener(this);

        orderRecyclerView.setHasFixedSize(false);
        orderRecyclerView.setAdapter(ordersRecyclerAdapter);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_your_orders);
            loadingOrdersDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
        }

        getActivity().getSupportLoaderManager().restartLoader(Constants.ORDERS_LOADER_ID, null, this);

        getMyOrdersSubscriptionId();

        return rootView;
    }

    private void getMyOrdersSubscriptionId() {
        new Thread(() -> streamServiceStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MY_ORDERS).setDataStreamId("").build(),
                new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        subscribeToMyOrdersStream(value.getSubscriptionId());
                        orderSubscriptionId = value.getSubscriptionId();
                        onCompleted();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                })).start();
    }

    private void subscribeToMyOrdersStream(SubscriptionId subscriptionId) {
        streamServiceStub.getMyOrderUpdates(subscriptionId, new StreamObserver<MyOrderUpdate>() {
            @Override
            public void onNext(MyOrderUpdate orderUpdate) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> getActivity().getSupportLoaderManager().restartLoader(Constants.ORDERS_LOADER_ID, null, OrdersFragment.this));
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public Loader<GetMyOpenOrdersResponse> onCreateLoader(int id, Bundle args) {
        loadingOrdersDialog.show();

        if (getContext() != null)
            return new OpenOrdersLoader(getContext(), actionServiceBlockingStub);
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetMyOpenOrdersResponse> loader, GetMyOpenOrdersResponse response) {

        if (response == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        recyclerContainerSwipeRefreshLayout.setRefreshing(false);

        ArrayList<Order> ordersList = new ArrayList<>();

        List<Ask> askList = response.getOpenAskOrdersList();
        List<Bid> bidList = response.getOpenBidOrdersList();

        if (askList.size()>0) {
            for (Ask currentAskOrder : askList) {
                ordersList.add(new Order(
                        currentAskOrder.getId(),
                        false,
                        false,
                        currentAskOrder.getPrice(),
                        currentAskOrder.getStockId(),
                        currentAskOrder.getOrderType().getNumber(),
                        currentAskOrder.getStockQuantity(),
                        currentAskOrder.getStockQuantityFulfilled()
                        ));
            }
        }

        if (bidList.size()>0) {
            for (Bid currentBidOrder : bidList) {
                ordersList.add(new Order(
                        currentBidOrder.getId(),
                        true,
                        false,
                        currentBidOrder.getPrice(),
                        currentBidOrder.getStockId(),
                        currentBidOrder.getOrderType().getNumber(),
                        currentBidOrder.getStockQuantity(),
                        currentBidOrder.getStockQuantityFulfilled()
                ));
            }
        }

        loadingOrdersDialog.dismiss();

        if (ordersList.size() > 0) {
            ordersRecyclerAdapter.swapData(ordersList);
            recyclerContainerSwipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyOrderRelativeLayout.setVisibility(View.GONE);
        } else {
            recyclerContainerSwipeRefreshLayout.setVisibility(View.GONE);
            emptyOrderRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<GetMyOpenOrdersResponse> loader) {
        // Do Nothing
    }

    @Override
    public void onOrderClick(int orderId, boolean bid) {

        if (getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle("Cancel Confirm")
                    .setCancelable(true)
                    .setMessage("Do you want to cancel this order ?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> new Handler().post(() -> {
                        CancelOrderResponse response = actionServiceBlockingStub.cancelOrder(
                                CancelOrderRequest.newBuilder().setOrderId(orderId).setIsAsk(!bid).build());

                        if (response.getStatusCodeValue() == 0) {
                            Toast.makeText(getContext(), "Order cancelled", Toast.LENGTH_SHORT).show();
                            if (getActivity() != null)
                                getActivity().getSupportLoaderManager().restartLoader(Constants.ORDERS_LOADER_ID, null, OrdersFragment.this);
                        } else {
                            Toast.makeText(getContext(), response.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new Handler().post(() -> {
            if (orderSubscriptionId != null) {
                streamServiceStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(orderSubscriptionId).build(),
                        new StreamObserver<UnsubscribeResponse>() {
                            @Override
                            public void onNext(UnsubscribeResponse value) {
                                onCompleted();
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        });
            }
        });
    }

    @Override
    public void onRefresh() {
        if (getActivity() != null)
            getActivity().getSupportLoaderManager().restartLoader(Constants.ORDERS_LOADER_ID, null, this);
    }
}