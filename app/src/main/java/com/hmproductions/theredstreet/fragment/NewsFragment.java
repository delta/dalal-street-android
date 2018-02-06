package com.hmproductions.theredstreet.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.NewsDetails;
import com.hmproductions.theredstreet.loaders.NewsLoader;

import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;

import static com.hmproductions.theredstreet.utils.Constants.NEWS_LOADER_ID;

/* Gets latest news from GetMarketEvents action*/
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsDetails>>{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    NewsRecyclerAdapter newsRecyclerAdapter;

    RecyclerView newsRecyclerView;
    TextView noNewsTextView;
    AlertDialog loadingNewsDialog;

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    private BroadcastReceiver refreshNewsListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_NEWS_ACTION)) {
                getActivity().getSupportLoaderManager().restartLoader(Constants.NEWS_LOADER_ID, null, NewsFragment.this);
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

        newsRecyclerView = rootView.findViewById(R.id.news_recyclerView);
        noNewsTextView = rootView.findViewById(R.id.noNews_textView);

        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setHasFixedSize(false);
        newsRecyclerView.setAdapter(newsRecyclerAdapter);

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_fresh_news);
            loadingNewsDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
        }

        getActivity().getSupportLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<List<NewsDetails>> onCreateLoader(int id, Bundle args) {
        loadingNewsDialog.show();
        return new NewsLoader(getContext(), actionServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(Loader<List<NewsDetails>> loader, List<NewsDetails> data) {

        loadingNewsDialog.dismiss();

        if (data == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        if (data.size()!=0) {
            newsRecyclerAdapter.swapData(data);
            noNewsTextView.setVisibility(View.GONE);
            newsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            noNewsTextView.setVisibility(View.VISIBLE);
            newsRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsDetails>> loader) {
        // Do nothing
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
}