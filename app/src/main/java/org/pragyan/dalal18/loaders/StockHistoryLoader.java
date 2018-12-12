package org.pragyan.dalal18.loaders;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetStockHistoryRequest;
import dalalstreet.api.actions.GetStockHistoryResponse;
import dalalstreet.api.actions.StockHistoryResolution;

public class StockHistoryLoader extends AsyncTaskLoader<GetStockHistoryResponse> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;
    private int stockId;

    public StockHistoryLoader(@NonNull Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub stub, int stockId) {
        super(context);
        actionServiceBlockingStub = stub;
        this.stockId = stockId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public GetStockHistoryResponse loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            return actionServiceBlockingStub.getStockHistory(GetStockHistoryRequest
                    .newBuilder()
                    .setStockId(stockId)
                    .setResolution(StockHistoryResolution.SixtyMinutes)
                    .build()
            );
        else
            return null;
    }
}
