package org.pragyan.dalal18.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import org.pragyan.dalal18.data.Subscription;
import org.pragyan.dalal18.data.Subscription.SubscriptionType;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;

public class SubscriptionLoader extends AsyncTaskLoader<List<Subscription>> {


    private final DalalStreamServiceGrpc.DalalStreamServiceBlockingStub stub;

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    public SubscriptionLoader(@NonNull Context context, DalalStreamServiceGrpc.DalalStreamServiceBlockingStub stub) {
        super(context);
        this.stub = stub;
    }

    @Nullable
    @Override
    public List<Subscription> loadInBackground() {
        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

            List<Subscription> subscriptionList = new ArrayList<>();

            SubscribeResponse response = stub.subscribe(
                    SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_EXCHANGE).setDataStreamId("").build());
            subscriptionList.add(new Subscription(SubscriptionType.STOCK_EXCHANGE, response.getSubscriptionId()));

            response = stub.subscribe(
                    SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_PRICES).setDataStreamId("").build());
            subscriptionList.add(new Subscription(SubscriptionType.STOCK_PRICES, response.getSubscriptionId()));

            response = stub.subscribe(
                    SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MARKET_EVENTS).setDataStreamId("").build());
            subscriptionList.add(new Subscription(SubscriptionType.MARKET_EVENTS, response.getSubscriptionId()));

            response = stub.subscribe(
                    SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.TRANSACTIONS).setDataStreamId("").build());
            subscriptionList.add(new Subscription(SubscriptionType.TRANSACTIONS, response.getSubscriptionId()));

            return subscriptionList;
        } else {
            return null;
        }
    }
}
