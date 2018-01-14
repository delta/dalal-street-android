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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompanyFragment extends Fragment {

    @BindView(R.id.portfolio_recyclerView)
    RecyclerView portfolioRecyclerView;

    private ArrayList<PortfolioDetails> portfolioList = new ArrayList<>();
    private PortfolioRecyclerAdapter adapter;

    public CompanyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_portfolio, container, false);

        if (getActivity() != null)  getActivity().setTitle("Company");
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

        sortList(portfolioList);
        adapter.swapData(portfolioList);
    }

    private void sortList(ArrayList<PortfolioDetails> list) {
        Collections.sort(list, new Comparator<PortfolioDetails>() {
            public int compare(PortfolioDetails ideaVal1, PortfolioDetails ideaVal2) {

                Integer idea1 = new Integer(ideaVal1.getValue());
                Integer idea2 = new Integer(ideaVal2.getValue());
                return idea1.compareTo(idea2);
            }
        });
    }
}