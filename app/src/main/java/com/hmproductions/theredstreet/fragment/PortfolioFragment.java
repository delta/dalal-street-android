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
import android.widget.RelativeLayout;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.PortfolioRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.Portfolio;
import com.hmproductions.theredstreet.data.StockDetails;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PortfolioFragment extends Fragment {

    @Inject
    PortfolioRecyclerAdapter portfolioRecyclerAdapter;

    @BindView(R.id.emptyPortfolio_relativeLayout)
    RelativeLayout emptyPortfolioRelativeLayout;

    @BindView(R.id.portfolio_recyclerView)
    RecyclerView portfolioRecyclerView;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    private BroadcastReceiver refreshPortfolioDetails = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ( intent.getAction() != null &&
                    (intent.getAction().equals(Constants.REFRESH_STOCK_PRICES_ACTION) || intent.getAction().equals(Constants.REFRESH_OWNED_STOCKS_ACTION))) {
                updatePortfolioTable();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_portfolio, container, false);
        if (getActivity() != null) getActivity().setTitle("Portfolio");

        ButterKnife.bind(this, rootView);
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        portfolioRecyclerView.setHasFixedSize(false);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        portfolioRecyclerView.setAdapter(portfolioRecyclerAdapter);

        updatePortfolioTable();

        return rootView;
    }

    private void updatePortfolioTable() {

        List<Portfolio> portfolioList = new ArrayList<>();

        for (StockDetails currentStockDetails : MainActivity.ownedStockDetails) {

            int currentPrice = -1;

            for (GlobalStockDetails globalStockDetails : MainActivity.globalStockDetails) {
                if (currentStockDetails.getStockId() == globalStockDetails.getStockId()) {
                    currentPrice = globalStockDetails.getPrice();
                    break;
                }
            }

            portfolioList.add(new Portfolio(
                    StockUtils.getShortNameForStockId(MainActivity.globalStockDetails, currentStockDetails.getStockId()),
                    StockUtils.getCompanyNameFromStockId(currentStockDetails.getStockId()),
                    currentStockDetails.getQuantity(),
                    currentPrice,
                    StockUtils.getPreviousDayCloseFromStockId(MainActivity.globalStockDetails, currentStockDetails.getStockId())
            ));
        }

        if (portfolioList.size() > 0) {
            portfolioRecyclerAdapter.swapData(portfolioList);
            portfolioRecyclerView.setVisibility(View.VISIBLE);
            emptyPortfolioRelativeLayout.setVisibility(View.GONE);
        } else {
            portfolioRecyclerView.setVisibility(View.GONE);
            emptyPortfolioRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            IntentFilter intentFilter = new IntentFilter(Constants.REFRESH_OWNED_STOCKS_ACTION);
            intentFilter.addAction(Constants.REFRESH_STOCK_PRICES_ACTION);
            intentFilter.addAction(Constants.REFRESH_STOCKS_EXCHANGE_ACTION);
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(refreshPortfolioDetails, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(refreshPortfolioDetails);
    }
}