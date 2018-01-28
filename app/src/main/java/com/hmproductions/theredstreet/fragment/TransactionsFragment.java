package com.hmproductions.theredstreet.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetTransactionsResponse;

import static com.hmproductions.theredstreet.utils.Constants.TRANSACTION_LOADER_ID;

/* Uses GetTransactions() to get user's latest transactions */
public class TransactionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetTransactionsResponse> {

    private static final String LAST_TRANSACTION_ID = "last_transaction_id";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceStub;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.noTransactions_relativeLayout)
    RelativeLayout noTransactionsRelativeLayout;

    @BindView(R.id.transactions_recyclerView)
    RecyclerView transactionRecyclerView;

    private List<Transaction> transactionList = new ArrayList<>();
    private TransactionRecyclerAdapter adapter;
    private AlertDialog loadingDialog;
    boolean paginate = true;

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
        if (getActivity() != null) getActivity().setTitle("Transactions");
        ButterKnife.bind(this,rootView);

        adapter = new TransactionRecyclerAdapter(getContext(), null);

        getActivity().getSupportLoaderManager().restartLoader(TRANSACTION_LOADER_ID, null, this);
        loadingDialog.show();

        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionRecyclerView.setHasFixedSize(false);
        transactionRecyclerView.addOnScrollListener(new CustomScrollListener());
        transactionRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        preferences
                .edit()
                .putInt(LAST_TRANSACTION_ID,0)
                .apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferences
                .edit()
                .putInt(LAST_TRANSACTION_ID,0)
                .apply();

    }

    @Override
    public Loader<GetTransactionsResponse> onCreateLoader(int id, Bundle args) {

        if (getContext() != null) {

            int lastId = preferences.getInt(LAST_TRANSACTION_ID,0);
            return new TransactionLoader(getContext(), actionServiceStub,lastId);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<GetTransactionsResponse> loader, GetTransactionsResponse data) {

        loadingDialog.dismiss();
        paginate = data.getTransactionsCount() == 10;

        for (int i = 0; i < data.getTransactionsCount(); ++i) {
            dalalstreet.api.models.Transaction currentTransaction = data.getTransactions(i);
            transactionList.add(new Transaction(
                    currentTransaction.getType().name(),
                    currentTransaction.getStockId(),
                    currentTransaction.getStockQuantity(),
                    currentTransaction.getPrice(),
                    currentTransaction.getCreatedAt(),
                    currentTransaction.getTotal()
            ));
            preferences.edit()
                    .putInt(LAST_TRANSACTION_ID,currentTransaction.getId())
                    .apply();
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

    public class CustomScrollListener extends RecyclerView.OnScrollListener {

        CustomScrollListener() {

        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
            int totalItemCount = recyclerView.getLayoutManager().getItemCount();
            int pastVisibleItems =  ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (pastVisibleItems + visibleItemCount >= totalItemCount) {

                if(paginate){
                    if (getActivity() != null) {
                        getActivity().getSupportLoaderManager().restartLoader(TRANSACTION_LOADER_ID, null, TransactionsFragment.this);
                        paginate = false;
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.edit().putInt(LAST_TRANSACTION_ID, 0).apply();
    }
}