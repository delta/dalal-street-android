package com.hmproductions.theredstreet.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.CompanyRecyclerAdapter;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.CompanyDetails;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompanyFragment extends Fragment {

    @BindView(R.id.portfolio_recyclerView)
    RecyclerView portfolioRecyclerView;

    private ArrayList<CompanyDetails> portfolioList = new ArrayList<>();
    private CompanyRecyclerAdapter adapter;

    public CompanyFragment() {
        // Required empty public constructor
    }

    private BroadcastReceiver refreshStockPricesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null &&
                    (intent.getAction().equalsIgnoreCase(Constants.REFRESH_STOCK_PRICES_ACTION) || intent.getAction().equalsIgnoreCase(Constants.REFRESH_STOCKS_EXCHANGE_ACTION))) {
                updateValues();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_companies, container, false);

        if (getActivity() != null)  getActivity().setTitle("Company Details");
        ButterKnife.bind(this, rootView);

        adapter = new CompanyRecyclerAdapter(getContext(), null);

        updateValues();

        portfolioRecyclerView.setAdapter(adapter);
        portfolioRecyclerView.setHasFixedSize(false);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    public void updateValues(){

        portfolioList.clear();

        for (GlobalStockDetails currentStockDetail : MainActivity.globalStockDetails) {
            portfolioList.add(new CompanyDetails(
                    currentStockDetail.getFullName(),
                    currentStockDetail.getShortName(),
                    currentStockDetail.getPrice(),
                    -1,
                    currentStockDetail.getPreviousDayClose())
            );
        }

        sortList(portfolioList);
        adapter.swapData(portfolioList);
    }

    private void sortList(ArrayList<CompanyDetails> list) {
        Collections.sort(list, (v1, v2) -> v1.getValue()>v2.getValue()?v1.getValue():v2.getValue());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            IntentFilter intentFilter = new IntentFilter(Constants.REFRESH_STOCKS_EXCHANGE_ACTION);
            intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION);

            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    refreshStockPricesReceiver, intentFilter
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshStockPricesReceiver);
        }
    }
}