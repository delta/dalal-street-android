package org.pragyan.dalal18.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.CompanyTickerRecyclerAdapter;
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.CompanyTickerDetails;
import org.pragyan.dalal18.data.GlobalStockDetails;
import org.pragyan.dalal18.data.NewsDetails;
import org.pragyan.dalal18.loaders.NewsLoader;
import org.pragyan.dalal18.ui.MainActivity;
import org.pragyan.dalal18.ui.NewsDetailsActivity;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsDetails>> {

    private static final int COMPANY_TICKER_DURATION = 2500;

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    NewsRecyclerAdapter newsRecyclerAdapter;

    @Inject
    CompanyTickerRecyclerAdapter companyTickerRecyclerAdapter;

    @BindView(R.id.companies_recyclerView)
    RecyclerView companiesRecyclerView;

    @BindView(R.id.news_recyclerView)
    RecyclerView newsRecyclerView;

    @BindView(R.id.loadingNews_relativeLayout)
    RelativeLayout loadingRelativeLayout;

    @BindView(R.id.breakingNewsText)
    TextView breakingNewsTextView;

    LinearLayoutManager linearLayoutManager;

    List<CompanyTickerDetails> companyTickerDetailsList = new ArrayList<>();
    List<NewsDetails> newsList = new ArrayList<>();
    ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    Handler handler = new Handler();

    private BroadcastReceiver refreshNewsListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_NEWS_ACTION)) {

                getActivity().getSupportLoaderManager().restartLoader(Constants.NEWS_LOADER_ID, null, HomeFragment.this);

            } else if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_PRICE_TICKER_ACTION)) {

                StringBuilder builder = new StringBuilder("");
                if (MainActivity.globalStockDetails.size() > 0) {
                    for (GlobalStockDetails currentStockDetails : MainActivity.globalStockDetails) {
                        builder.append(currentStockDetails.getShortName()).append(" : ").append(currentStockDetails.getPrice());
                        builder.append(currentStockDetails.getUp() == 1 ? "\u2191" : "\u2193").append("     ");
                    }
                }
                breakingNewsTextView.setText(builder.toString());
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        if (getActivity() != null)
            getActivity().setTitle("Home");

        breakingNewsTextView.setSelected(true);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        companiesRecyclerView.setLayoutManager(linearLayoutManager);
        companiesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        companiesRecyclerView.setAdapter(companyTickerRecyclerAdapter);

        SnapHelper companiesSnapHelper = new PagerSnapHelper();
        companiesSnapHelper.attachToRecyclerView(companiesRecyclerView);

        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setHasFixedSize(true);
        newsRecyclerView.setAdapter(newsRecyclerAdapter);

        newsRecyclerAdapter.setOnClickListener(new NewsRecyclerAdapter.OnClickListener() {
            @Override
            public void itemClicked(View view, int position) {

                Intent intent = new Intent(getContext(), NewsDetailsActivity.class);
                intent.putExtra("newsdetails", newsList.get(position));
                startActivity(intent);
            }
        });

        setValues();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int position = linearLayoutManager.findFirstVisibleItemPosition();
                companiesRecyclerView.smoothScrollToPosition(position + 1);
                handler.postDelayed(this, COMPANY_TICKER_DURATION);
            }
        };
        handler.postDelayed(runnable, COMPANY_TICKER_DURATION);

        return rootView;
    }

    public void setValues() {

        if (getActivity() != null)
            getActivity().getSupportLoaderManager().restartLoader(Constants.NEWS_LOADER_ID, null, this);

        companyTickerDetailsList.clear();

        for (GlobalStockDetails currentStockDetails : MainActivity.globalStockDetails) {
            companyTickerDetailsList.add(new CompanyTickerDetails(
                    currentStockDetails.getFullName(),
                    null,
                    currentStockDetails.getPrice(),
                    currentStockDetails.getUp()==1));
        }

        if (companyTickerDetailsList != null && companyTickerDetailsList.size() != 0) {
            companyTickerRecyclerAdapter.swapData(companyTickerDetailsList);
        }


        StringBuilder builder = new StringBuilder("");
        for (GlobalStockDetails currentStockDetails : MainActivity.globalStockDetails) {

            builder.append(currentStockDetails.getShortName()).append(" : ").append(currentStockDetails.getPrice());
            if (getActivity() != null && android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                builder.append(currentStockDetails.getUp()==1?"\u2191":"\u2193");
            }
            builder.append("     ");
        }
        breakingNewsTextView.setText(builder.toString());

    }

    @Override
    public Loader<List<NewsDetails>> onCreateLoader(int id, Bundle args) {
        loadingRelativeLayout.setVisibility(View.VISIBLE);
        newsRecyclerView.setVisibility(View.GONE);
        return new NewsLoader(getContext(), actionServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(Loader<List<NewsDetails>> loader, List<NewsDetails> data) {

        if (data == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }



        if (data.size() != 0) {
            newsList = data;
            newsRecyclerAdapter.swapData(data);


        }


        loadingRelativeLayout.setVisibility(View.GONE);
        newsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<NewsDetails>> loader) {
        //Do Nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.REFRESH_NEWS_ACTION);
            intentFilter.addAction(Constants.REFRESH_PRICE_TICKER_ACTION);
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(refreshNewsListReceiver, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(refreshNewsListReceiver);
    }
}