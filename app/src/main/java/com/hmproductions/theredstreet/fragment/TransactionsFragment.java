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
import com.hmproductions.theredstreet.adapter.TransactionRecyclerAdapter;
import com.hmproductions.theredstreet.data.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    RecyclerView transactionView;

    private List<Transaction> transactionList = new ArrayList<>();

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_transactions, container, false);

        if (getActivity() != null) getActivity().setTitle("TransactionsFragment");

        transactionView = rootView.findViewById(R.id.transactions_recyclerView);

        setValues();

        TransactionRecyclerAdapter adapter = new TransactionRecyclerAdapter(getActivity(), transactionList);

        transactionView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionView.setHasFixedSize(false);
        transactionView.setAdapter(adapter);

        return rootView;
    }

    public void setValues(){

        //TODO : Get from service
        transactionList.clear();

        transactionList.add(new Transaction("MortgageFragment","Github",50,43,"10:00",-100));
        transactionList.add(new Transaction("Exchange","Yahoo",50,43,"11:00",+50));
        transactionList.add(new Transaction("MortgageFragment","EA",70,43,"12:00",-25));
        transactionList.add(new Transaction("Market","Sony",40,43,"12:01",+58));
        transactionList.add(new Transaction("Exchange","LG",10,43,"12:02",+75));
    }
}