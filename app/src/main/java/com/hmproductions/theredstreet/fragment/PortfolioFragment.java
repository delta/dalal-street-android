package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.PortfolioRecyclerAdapter;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.PortfolioDetails;
import com.hmproductions.theredstreet.ui.MainActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PortfolioFragment extends Fragment {

    @BindView(R.id.portfolio_recyclerView)
    RecyclerView portfolioRecyclerView;

    private ArrayList<PortfolioDetails> portfolioList = new ArrayList<>();
    private PortfolioRecyclerAdapter adapter;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_portfolio, container, false);

        if (getActivity() != null)  getActivity().setTitle("Portfolio");
        ButterKnife.bind(this, rootView);

        adapter = new PortfolioRecyclerAdapter(getContext(), null);

        updateValues();

        portfolioRecyclerView.setAdapter(adapter);
        portfolioRecyclerView.setHasFixedSize(false);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    public void updateValues(){

        portfolioList.clear();

        for (GlobalStockDetails currentStockDetail : MainActivity.globalStockDetails) {
            portfolioList.add(new PortfolioDetails(
                    currentStockDetail.getFullName(), currentStockDetail.getShortName(),
                    currentStockDetail.getQuantityInExchange(), currentStockDetail.getPrice())
            );
        }

        adapter.swapData(portfolioList);
    }
}