package com.hmproductions.theredstreet.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.PlaceOrderRequest;
import dalalstreet.api.actions.PlaceOrderResponse;

public class TradeLoader extends AsyncTaskLoader<PlaceOrderResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub stub;
    private PlaceOrderRequest orderRequest;

    public TradeLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub, PlaceOrderRequest request) {
        super(context);
        this.stub = stub;
        this.orderRequest = request;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public PlaceOrderResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            return stub.placeOrder(orderRequest);
        else
            return null;
    }
}
