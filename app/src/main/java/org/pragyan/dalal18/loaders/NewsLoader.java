package org.pragyan.dalal18.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.widget.Toast;

import org.pragyan.dalal18.data.NewsDetails;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMarketEventsRequest;
import dalalstreet.api.actions.GetMarketEventsResponse;
import dalalstreet.api.models.MarketEvent;

public class NewsLoader extends AsyncTaskLoader<List<NewsDetails>> {

    private DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    public NewsLoader(Context context, DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub) {
        super(context);
        this.actionServiceBlockingStub = actionServiceBlockingStub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<NewsDetails> loadInBackground() {

        if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

            List<NewsDetails> newsList = new ArrayList<>();

            GetMarketEventsResponse marketEventsResponse = actionServiceBlockingStub.getMarketEvents(
                    GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build());

            if (marketEventsResponse.getStatusCode().getNumber() == 0) {

                newsList.clear();

                for (MarketEvent currentMarketEvent : marketEventsResponse.getMarketEventsList()) {
                    newsList.add(new NewsDetails(currentMarketEvent.getCreatedAt(), currentMarketEvent.getHeadline(), currentMarketEvent.getText()));
                }

                return newsList;

            } else {
                Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
                return null;
            }
        } else {
            return null;
        }
    }
}
