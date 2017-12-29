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
import com.hmproductions.theredstreet.data.PortfolioDetails;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PortfolioFragment extends Fragment {

    @BindView(R.id.portfolio_recyclerView)
    RecyclerView portfolioRecyclerView;

    private ArrayList<PortfolioDetails> portfolioValues = new ArrayList<>();

    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_portfolio, container, false);

        if (getActivity() != null)  getActivity().setTitle("PortfolioFragment");
        ButterKnife.bind(this, rootView);

        updateValues();

        portfolioRecyclerView.setAdapter(new PortfolioRecyclerAdapter(getContext(), portfolioValues));
        portfolioRecyclerView.setHasFixedSize(false);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    public void updateValues(){

        //TODO : get from service

        portfolioValues.clear();

        portfolioValues.add(new PortfolioDetails("Github",30,20));
        portfolioValues.add(new PortfolioDetails("Apple",20,80));
        portfolioValues.add(new PortfolioDetails("Yahoo",45,100));
        portfolioValues.add(new PortfolioDetails("HDFC",30,20));
        portfolioValues.add(new PortfolioDetails("LG",15,60));
        portfolioValues.add(new PortfolioDetails("Sony",25,75));
        portfolioValues.add(new PortfolioDetails("Infosys",50,35));
    }
}