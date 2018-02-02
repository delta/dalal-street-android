package com.hmproductions.theredstreet.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMyOpenOrdersRequest;
import dalalstreet.api.actions.GetMyOpenOrdersResponse;

public class OpenOrdersLoader extends AsyncTaskLoader<GetMyOpenOrdersResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub stub;

    public OpenOrdersLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub) {
        super(context);
        this.stub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetMyOpenOrdersResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            return stub.getMyOpenOrders(GetMyOpenOrdersRequest.newBuilder().build());
        else
            return null;
    }
}
