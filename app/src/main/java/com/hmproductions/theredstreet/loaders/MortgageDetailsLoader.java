package com.hmproductions.theredstreet.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMortgageDetailsRequest;
import dalalstreet.api.actions.GetMortgageDetailsResponse;

public class MortgageDetailsLoader extends AsyncTaskLoader<GetMortgageDetailsResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    public MortgageDetailsLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub) {
        super(context);
        actionServiceBlockingStub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetMortgageDetailsResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            return actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build());
        else
            return null;
    }
}
