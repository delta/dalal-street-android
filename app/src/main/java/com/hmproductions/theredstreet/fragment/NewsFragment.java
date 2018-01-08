package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMarketEventsRequest;
import dalalstreet.api.actions.GetMarketEventsResponse;
import dalalstreet.api.models.MarketEvent;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/* Gets latest news from GetMarketEvents action*/
public class NewsFragment extends Fragment {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    NewsRecyclerAdapter newsRecyclerAdapter;

    @Inject
    Metadata metadata;

    RecyclerView newsRecyclerView;
    List<NewsDetails> newsList = new ArrayList<>();

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_news, container, false);

        if (getActivity() != null) getActivity().setTitle("NewsFragment");
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        newsRecyclerView = rootView.findViewById(R.id.news_recyclerView);

        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setHasFixedSize(false);
        newsRecyclerView.setAdapter(newsRecyclerAdapter);

        updateNews();

        return rootView;
    }

    public void updateNews(){

        newsList.clear();
        MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata);

        GetMarketEventsResponse marketEventsResponse = actionServiceBlockingStub.getMarketEvents(
                GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build());

        if (marketEventsResponse.getStatusCode().getNumber() == 0) {

            for (MarketEvent currentMarketEvent : marketEventsResponse.getMarketEventsList()) {
                newsList.add(new NewsDetails(currentMarketEvent.getCreatedAt(), currentMarketEvent.getHeadline(), currentMarketEvent.getText()));
            }

            newsRecyclerAdapter.swapData(newsList);

        } else {
            Toast.makeText(getContext(), "Server error", Toast.LENGTH_SHORT).show();
        }
    }
}