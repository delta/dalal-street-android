package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.TransactionRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.Transaction;
import com.hmproductions.theredstreet.loaders.TransactionLoader;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetTransactionsResponse;

import static com.hmproductions.theredstreet.utils.Constants.TRANSACTION_LOADER_ID;

/* Uses GetTransactions() to get user's latest transactions */
public class TransactionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetTransactionsResponse> {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceStub;

    RelativeLayout noTransactionsRelativeLayout;
    RecyclerView transactionRecyclerView;

    private List<Transaction> transactionList = new ArrayList<>();
    private TransactionRecyclerAdapter adapter;
    private AlertDialog loadingDialog;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null){
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.loading_transaction);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }
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

        getActivity().getSupportLoaderManager().restartLoader(TRANSACTION_LOADER_ID, null, this);
    }

    @Override
    public Loader<GetTransactionsResponse> onCreateLoader(int id, Bundle args) {

        loadingDialog.show();
        return new TransactionLoader(getContext(),actionServiceStub);
    }

    @Override
    public void onLoadFinished(Loader<GetTransactionsResponse> loader, GetTransactionsResponse data) {

        loadingDialog.dismiss();
        transactionList.clear();
        for (int i = 0; i < data.getTransactionsCount(); ++i) {
            dalalstreet.api.models.Transaction currentTransaction = data.getTransactions(i);
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

    @Override
    public void onLoaderReset(Loader<GetTransactionsResponse> loader) {

    }
}