package org.pragyan.dalal18.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.pragyan.dalal18.ui.NewsDetailsActivity;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMarketEventsRequest;
import dalalstreet.api.actions.GetMarketEventsResponse;
import dalalstreet.api.models.MarketEvent;

/* Gets latest news from GetMarketEvents action*/
public class NewsFragment extends Fragment implements
        NewsRecyclerAdapter.NewsItemClickListener{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    private RecyclerView newsRecyclerView;
    private NewsRecyclerAdapter newsRecyclerAdapter;

    private TextView noNewsTextView;
    private AlertDialog loadingNewsDialog;
    private List<NewsDetails> newsDetailsList = new ArrayList<>();
    private NewsAsyncTask newsAsyncTast = new NewsAsyncTask();

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    private BroadcastReceiver refreshNewsListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_NEWS_ACTION)) {
                newsAsyncTast.execute();
            }
        }
    };

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_news, container, false);

        if (getActivity() != null) getActivity().setTitle("News");
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        newsRecyclerView = rootView.findViewById(R.id.newsRecyclerView);
        noNewsTextView = rootView.findViewById(R.id.noNews_textView);

        newsRecyclerAdapter = new NewsRecyclerAdapter(getContext(), null, this);

        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setHasFixedSize(false);
        newsRecyclerView.setAdapter(newsRecyclerAdapter);

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_latest_news);
            loadingNewsDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
        }

        newsAsyncTast.execute();
        return rootView;
    }


    public class NewsAsyncTask extends AsyncTask<Void,Void,List<NewsDetails>>{

        @Override
        protected List<NewsDetails> doInBackground(Void... voids) {

            loadingNewsDialog.show();

            if (ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                List<NewsDetails> newsList = new ArrayList<>();

                GetMarketEventsResponse marketEventsResponse = actionServiceBlockingStub.getMarketEvents(
                        GetMarketEventsRequest.newBuilder().setCount(0).setLastEventId(0).build());

                if (marketEventsResponse.getStatusCode().getNumber() == 0) {

                    newsList.clear();

                    for (MarketEvent currentMarketEvent : marketEventsResponse.getMarketEventsList()) {
                        newsList.add(new NewsDetails(currentMarketEvent.getCreatedAt(), currentMarketEvent.getHeadline(),
                                currentMarketEvent.getText(), currentMarketEvent.getImagePath()));
                    }

                    return newsList;

                } else {

                    publishProgress();
                    return null;

                }
            } else {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<NewsDetails> newsDetails) {
            super.onPostExecute(newsDetails);

            loadingNewsDialog.dismiss();

            if (newsDetails == null) {
                networkDownHandler.onNetworkDownError();
                return;
            }

            if (newsDetails.size()!=0) {
                newsDetailsList = newsDetails;
                newsRecyclerAdapter.swapData(newsDetailsList);
                noNewsTextView.setVisibility(View.GONE);
                newsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                noNewsTextView.setVisibility(View.VISIBLE);
                newsRecyclerView.setVisibility(View.GONE);
            }
        }
    }




    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(refreshNewsListReceiver, new IntentFilter(Constants.REFRESH_NEWS_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(refreshNewsListReceiver);
    }

    @Override
    public void onNewsClicked(View view, int position) {
        Intent intent = new Intent(getContext(), NewsDetailsActivity.class);
        intent.putExtra(NewsDetailsActivity.NEWS_DETAILS_KEY, newsDetailsList.get(position));
        startActivity(intent);
    }
}