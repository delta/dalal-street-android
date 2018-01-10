package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.TransactionRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.Transaction;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetTransactionsRequest;
import dalalstreet.api.actions.GetTransactionsResponse;

/* Uses GetTransactions() to get user's latest transactions */
public class TransactionsFragment extends Fragment {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceStub;

    RelativeLayout noTransactionsRelativeLayout;
    RecyclerView transactionRecyclerView;

    private List<Transaction> transactionList = new ArrayList<>();
    private TransactionRecyclerAdapter adapter;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        if (getActivity() != null) getActivity().setTitle("TransactionsFragment");

        adapter = new TransactionRecyclerAdapter(getContext(), null);

        noTransactionsRelativeLayout = rootView.findViewById(R.id.noTransactions_relativeLayout);
        transactionRecyclerView = rootView.findViewById(R.id.transactions_recyclerView);

        setValues();

        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionRecyclerView.setHasFixedSize(false);
        transactionRecyclerView.setAdapter(adapter);

        return rootView;
    }

    public void setValues() {

        transactionList.clear();

        GetTransactionsResponse response =
                actionServiceStub.getTransactions(GetTransactionsRequest.newBuilder().setCount(0).setLastTransactionId(0).build());

        for (int i = 0; i < response.getTransactionsCount(); ++i) {
            dalalstreet.api.models.Transaction currentTransaction = response.getTransactions(i);
            transactionList.add(new Transaction(
                    currentTransaction.getType().getNumber(),
                    currentTransaction.getStockId(),
                    currentTransaction.getStockQuantity(),
                    currentTransaction.getPrice(),
                    currentTransaction.getCreatedAt(),
                    currentTransaction.getTotal()
            ));
        }

        if (transactionList == null || transactionList.size() == 0) {
            transactionRecyclerView.setVisibility(View.GONE);
            noTransactionsRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            adapter.swapData(transactionList);
            transactionRecyclerView.setVisibility(View.VISIBLE);
            noTransactionsRelativeLayout.setVisibility(View.GONE);
        }
    }
}