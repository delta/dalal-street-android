package org.pragyan.dalal18.fragment;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.ui.NewsDetailsActivity;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.NewsDetails;
import org.pragyan.dalal18.loaders.NewsLoader;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;

import static org.pragyan.dalal18.utils.Constants.NEWS_LOADER_ID;

/* Gets latest news from GetMarketEvents action*/
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsDetails>>{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    NewsRecyclerAdapter newsRecyclerAdapter;

    RecyclerView newsRecyclerView;
    TextView noNewsTextView;
    AlertDialog loadingNewsDialog;
    List<NewsDetails> newsDetailsList = new ArrayList<>();

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

        newsRecyclerAdapter.setOnClickListener(new NewsRecyclerAdapter.OnClickListener() {
            @Override
            public void itemClicked(View view, int position) {

                Intent intent = new Intent(getContext(), NewsDetailsActivity.class);
                intent.putExtra("newsdetails", newsDetailsList.get(position));
                startActivity(intent);
            }
        });

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
            newsDetailsList = data;
            Log.e("SAN",newsDetailsList.size() + "");
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