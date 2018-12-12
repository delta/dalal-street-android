package org.pragyan.dalal18.loaders;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

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
