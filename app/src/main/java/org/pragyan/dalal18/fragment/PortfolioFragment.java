package org.pragyan.dalal18.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.PortfolioRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.GlobalStockDetails;
import org.pragyan.dalal18.data.Portfolio;
import org.pragyan.dalal18.data.StockDetails;
import org.pragyan.dalal18.ui.MainActivity;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.StockUtils;

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

            if(currentStockDetails.getQuantity() != 0){
                portfolioList.add(new Portfolio(
                        StockUtils.getShortNameForStockId(MainActivity.globalStockDetails, currentStockDetails.getStockId()),
                        StockUtils.getCompanyNameFromStockId(currentStockDetails.getStockId()),
                        currentStockDetails.getQuantity(),
                        currentPrice,
                        StockUtils.getPreviousDayCloseFromStockId(MainActivity.globalStockDetails, currentStockDetails.getStockId())
                ));
            }

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