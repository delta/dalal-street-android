package com.hmproductions.theredstreet.fragment.marketDepth;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.loaders.StockHistoryLoader;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.actions.GetStockHistoryResponse;

import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

public class DepthGraphFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetStockHistoryResponse>{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @BindView(R.id.chart1)
    LineChart lineChart;

    ArrayList<String> xVals = new ArrayList<>();
    ArrayList<Entry> yVals = new ArrayList<>();

    public DepthGraphFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_depth_graph, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);
        
        getData();

        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");
        xVals.add("hello");

        yVals.add(new Entry(4f, 0));
        yVals.add(new Entry(8f, 1));
        yVals.add(new Entry(6f, 2));
        yVals.add(new Entry(2f, 3));
        yVals.add(new Entry(18f, 4));
        yVals.add(new Entry(9f, 6));
        yVals.add(new Entry(1f, 7));
        yVals.add(new Entry(2f, 8));
        yVals.add(new Entry(3f, 9));
        yVals.add(new Entry(4f, 10));
        yVals.add(new Entry(5f, 11));
        yVals.add(new Entry(6f, 12));
        yVals.add(new Entry(7f, 13));
        yVals.add(new Entry(8f, 14));
        yVals.add(new Entry(9f, 15));
        yVals.add(new Entry(10f, 16));
        yVals.add(new Entry(15f, 17));



        lineChart.setHighlightEnabled(true);
        lineChart.setTouchEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setGridColor(getResources().getColor(R.color.black_background));
        lineChart.setBackgroundColor(getResources().getColor(R.color.black_background));
        lineChart.setScaleEnabled(false);

        LineDataSet set1 = new LineDataSet(yVals, "Stock Price");
        set1.setFillAlpha(110);
        set1.setLineWidth(1f);
        set1.setColor(getResources().getColor(android.R.color.white));
        set1.setCircleColor(getResources().getColor(android.R.color.black));
        set1.setHighLightColor(getResources().getColor(R.color.divider_line_gray));
        set1.setCircleSize(2f);

        LineData data = new LineData(xVals, set1);
        lineChart.setData(data);
        lineChart.invalidate();

        Legend legend = lineChart.getLegend();
        legend.setTextColor(getResources().getColor(android.R.color.white));

        XLabels xAxis = lineChart.getXLabels();
        xAxis.setTextColor(getResources().getColor(android.R.color.white));

        YLabels yLabels = lineChart.getYLabels();
        yLabels.setTextColor(getResources().getColor(android.R.color.white));

        return rootView;
    }

    private void getData() {


    }

    @Override
    public Loader<GetStockHistoryResponse> onCreateLoader(int id, Bundle args) {
        if (getContext() != null)
            return new StockHistoryLoader(getContext(), actionServiceBlockingStub, getStockIdFromCompanyName());
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetStockHistoryResponse> loader, GetStockHistoryResponse data) {

    }

    @Override
    public void onLoaderReset(Loader<GetStockHistoryResponse> loader) {

    }
}