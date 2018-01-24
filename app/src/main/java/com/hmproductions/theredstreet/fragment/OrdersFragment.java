package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.OrdersRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.Order;
import com.hmproductions.theredstreet.loaders.OpenOrdersLoader;
import com.hmproductions.theredstreet.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.CancelOrderRequest;
import dalalstreet.api.actions.CancelOrderResponse;
import dalalstreet.api.actions.GetMyOpenOrdersResponse;
import dalalstreet.api.models.Ask;
import dalalstreet.api.models.Bid;

public class OrdersFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<GetMyOpenOrdersResponse>, OrdersRecyclerAdapter.OnOrderClickListener{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    RecyclerView orderRecyclerView;
    RelativeLayout emptyOrderRelativeLayout, recyclerContainerRelativeLayout;

    OrdersRecyclerAdapter ordersRecyclerAdapter;
    AlertDialog loadingOrdersDialog;

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_orders, container, false);

        if (getActivity() != null) getActivity().setTitle("My Order");
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        orderRecyclerView = rootView.findViewById(R.id.orders_recyclerView);
        emptyOrderRelativeLayout = rootView.findViewById(R.id.emptyOrders_relativeLayout);
        recyclerContainerRelativeLayout = rootView.findViewById(R.id.recyclerViewsContainer_relativeLayout);

        ordersRecyclerAdapter = new OrdersRecyclerAdapter(getContext(), null, this);

        orderRecyclerView.setHasFixedSize(false);
        orderRecyclerView.setAdapter(ordersRecyclerAdapter);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_your_orders);
            loadingOrdersDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
        }

        getActivity().getSupportLoaderManager().restartLoader(Constants.ORDERS_LOADER_ID, null, this);

        return rootView;
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
            recyclerContainerRelativeLayout.setVisibility(View.VISIBLE);
            emptyOrderRelativeLayout.setVisibility(View.GONE);
        } else {
            recyclerContainerRelativeLayout.setVisibility(View.GONE);
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
                    .setPositiveButton("Cancel", (dialogInterface, i) -> {
                        CancelOrderResponse response = actionServiceBlockingStub.cancelOrder(
                                CancelOrderRequest.newBuilder().setOrderId(orderId).setIsAsk(!bid).build());

                        switch (response.getStatusCodeValue()) {
                            case 0:
                                Toast.makeText(getContext(), "Order cancelled", Toast.LENGTH_SHORT).show();
                                if (getActivity() != null)
                                    getActivity().getSupportLoaderManager().restartLoader(Constants.ORDERS_LOADER_ID, null, OrdersFragment.this);
                                break;

                            case 1:
                            case 3:
                                Toast.makeText(getContext(), "Inconsistent data server error", Toast.LENGTH_SHORT).show();

                            case 2:
                                Toast.makeText(getContext(), "Market is closed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Back", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        }
    }
}