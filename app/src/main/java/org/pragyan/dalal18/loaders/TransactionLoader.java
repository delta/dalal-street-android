package org.pragyan.dalal18.loaders;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetTransactionsRequest;
import dalalstreet.api.actions.GetTransactionsResponse;

public class TransactionLoader extends AsyncTaskLoader<GetTransactionsResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;
    private int lastId = 0;

    public TransactionLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub,int lastId) {
        super(context);
        this.actionServiceBlockingStub = actionServiceBlockingStub;
        this.lastId = lastId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetTransactionsResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
            return actionServiceBlockingStub.getTransactions(GetTransactionsRequest
                    .newBuilder()
                    .setCount(0)
                    .setLastTransactionId(lastId)
                    .build());
        } else {
            return null;
        }
    }
}
