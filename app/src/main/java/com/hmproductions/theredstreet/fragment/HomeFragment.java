package com.hmproductions.theredstreet.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.CompanyRecyclerAdapter;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.Company;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.NewsDetails;
import com.hmproductions.theredstreet.loaders.NewsLoader;
import com.hmproductions.theredstreet.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsDetails>>{

    private static final int COMPANY_NEWS_DURATION = 2000;

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    CompanyRecyclerAdapter companyRecyclerAdapter;

    @BindView(R.id.companies_recyclerView)
    RecyclerView companiesRecyclerView;

    @BindView(R.id.breakingNewsTexts)
    TextView breakingNewsTexts;

    @BindView(R.id.breakingNewsText)
    TextView breakingNewsText;

    LinearLayoutManager linearLayoutManager;

    List<Company> companyList = new ArrayList<>();
    String breakingNews;

    Handler handler = new Handler();

    private BroadcastReceiver refreshNewsListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_NEWS_ACTION)) {
                getActivity().getSupportLoaderManager().restartLoader(Constants.NEWS_LOADER_ID, null, HomeFragment.this);
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        if (getActivity() != null)
            getActivity().setTitle("Home");

        breakingNewsText.setVisibility(View.INVISIBLE);
        breakingNewsTexts.setSelected(true);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        companiesRecyclerView.setLayoutManager(linearLayoutManager);
        companiesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        companiesRecyclerView.setAdapter(companyRecyclerAdapter);

        SnapHelper companiesSnapHelper = new PagerSnapHelper();
        companiesSnapHelper.attachToRecyclerView(companiesRecyclerView);

        setValues();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int position = linearLayoutManager.findFirstVisibleItemPosition();
                if (position == companyList.size()-1) {
                    companiesRecyclerView.smoothScrollToPosition(0);
                } else {
                    companiesRecyclerView.smoothScrollToPosition(position+1);
                }
                handler.postDelayed(this, COMPANY_NEWS_DURATION);
            }
        };
        handler.postDelayed(runnable, COMPANY_NEWS_DURATION);

        return rootView;
    }

    public void setValues() {

        if (getActivity() != null)
            getActivity().getSupportLoaderManager().restartLoader(Constants.NEWS_LOADER_ID, null, this);

        companyList.clear();

        for (GlobalStockDetails currentStockDetails : MainActivity.globalStockDetails) {
            companyList.add(new Company(
                    currentStockDetails.getFullName(),
                    null,
                    currentStockDetails.getPreviousDayClose(),
                    currentStockDetails.getUp()==0));
        }

        if (companyList != null && companyList.size()!=0) {
            companyRecyclerAdapter.swapData(companyList);
        }

    }

    @Override
    public Loader<List<NewsDetails>> onCreateLoader(int id, Bundle args) {
        return new NewsLoader(getContext(), actionServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(Loader<List<NewsDetails>> loader, List<NewsDetails> data) {

        StringBuilder builder = new StringBuilder();
        builder.append("     ");
        for(int i=0 ; i<data.size() ; i++){

            builder.append(data.get(i).getHeadlines().trim());
            builder.append(".     ");

        }
        breakingNews = builder.toString();
        breakingNewsTexts.setText(builder);
        breakingNewsText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<NewsDetails>> loader) {
        //Do Nothing
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