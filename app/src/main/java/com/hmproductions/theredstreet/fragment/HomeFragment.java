package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.CompanyRecyclerAdapter;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.data.Company;
import com.hmproductions.theredstreet.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;

public class HomeFragment extends Fragment {

    private static final int COMPANY_NEWS_DURATION = 3000;

    @Inject
    DalalActionServiceGrpc.DalalActionServiceStub actionServiceStub;

    @BindView(R.id.companies_recyclerView)
    RecyclerView companiesRecyclerView;

    @BindView(R.id.news_recyclerView)
    RecyclerView newsRecyclerView;

    CompanyRecyclerAdapter companiesAdapter;
    LinearLayoutManager linearLayoutManager;

    List<Company> companyList = new ArrayList<>();
    List<NewsDetails> newsList = new ArrayList<>();

    Handler handler = new Handler();

    public HomeFragment() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);
        if (getActivity() != null)
            getActivity().setTitle("Home");

        setValues();

        companiesAdapter = new CompanyRecyclerAdapter(getContext(), companyList);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        companiesRecyclerView.setLayoutManager(linearLayoutManager);
        companiesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        companiesRecyclerView.setAdapter(companiesAdapter);

        SnapHelper companiesSnapHelper = new PagerSnapHelper();
        companiesSnapHelper.attachToRecyclerView(companiesRecyclerView);

        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setHasFixedSize(true);
        newsRecyclerView.setAdapter(new NewsRecyclerAdapter(getContext(), newsList));

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

    public void setValues() { //todo : get from service,checkout companyAdapter

        newsList.clear();
        companyList.clear();

        companyList.add(new Company("Github", String.valueOf(50), R.drawable.github2, R.drawable.down_arrow));
        companyList.add(new Company("Apple", String.valueOf(100), R.drawable.apple, R.drawable.up_arrow));
        companyList.add(new Company("Yahoo", String.valueOf(125), R.drawable.yahoo2, R.drawable.down_arrow));
        companyList.add(new Company("HDFC", String.valueOf(95), R.drawable.hdfc3, R.drawable.down_arrow));
        companyList.add(new Company("LG", String.valueOf(110), R.drawable.lg2, R.drawable.up_arrow));
        companyList.add(new Company("Sony", String.valueOf(50), R.drawable.sony, R.drawable.down_arrow));
        companyList.add(new Company("Infosys", String.valueOf(50), R.drawable.infosys, R.drawable.down_arrow));

        newsList = new ArrayList<>();
        newsList.clear();
        newsList.add(new NewsDetails("Github makes private repos free!", null));
        newsList.add(new NewsDetails("Apple revokes iphone 7 plus due to faulty cameras", null));
        newsList.add(new NewsDetails("Yahoo employees announce strike due to non payment of salary", null));
        newsList.add(new NewsDetails("Sony launches Xperia X conpact priced at 45,000", null));
        newsList.add(new NewsDetails("LG patents new refrigerant for its refrigerator products", null));
    }
}