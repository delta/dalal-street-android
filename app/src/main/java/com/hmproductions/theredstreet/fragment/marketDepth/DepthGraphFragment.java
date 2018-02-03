package com.hmproductions.theredstreet.fragment.marketDepth;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.actions.GetStockHistoryResponse;
import dalalstreet.api.datastreams.StockHistory;
import dalalstreet.api.models.StockHistoryOuterClass;

import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

public class DepthGraphFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetStockHistoryResponse>{

    private static final String COMPANY_NAME_KEY = "company-name-key";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @BindView(R.id.graph_company_spinner)
    MaterialBetterSpinner companiesSpinner;

    @BindView(R.id.graph_current_stock_price_layout)
    RelativeLayout currentStockLayout;

    @BindView(R.id.graph_prev_day_close_stock_price)
    TextView prevDayCloseText;

    @BindView(R.id.graph_current_stock_price_textView)
    TextView currentStockPriceText;

    @BindView(R.id.graph_arrow_image_view)
    ImageView arrowImage;

    @BindView(R.id.market_depth_chart)
    LineChart lineChart;

    ArrayList<String> xVals = new ArrayList<>();
    ArrayList<Entry> yVals = new ArrayList<>();
    ArrayList<com.hmproductions.theredstreet.data.StockHistory> stockHistoryList = new ArrayList<>();
    ArrayList<com.hmproductions.theredstreet.data.StockHistory> trimmedStockHistoryList = new ArrayList<>();

    String currentCompany;

    ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    AlertDialog loadingDialog;
    LineDataSet set1;

    public DepthGraphFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_depth_graph, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Market Depth");
        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_depth);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.company_spinner_item, StockUtils.getCompanyNamesArray());
        companiesSpinner.setAdapter(arrayAdapter);
        
        getData();


        lineChart.setHighlightEnabled(true);
        lineChart.setTouchEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setGridColor(getResources().getColor(R.color.black_background));
        lineChart.setBackgroundColor(getResources().getColor(R.color.black_background));
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);



        companiesSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            currentCompany = companiesSpinner.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString(COMPANY_NAME_KEY, currentCompany);
            xVals.clear();
            yVals.clear();
            Log.e("SAN","size : " + xVals.size() + "  " + yVals.size());
            if(!lineChart.isEmpty()){
                lineChart.invalidate();
                lineChart.clear();
            }
            lineChart.clearFocus();

            getActivity().getSupportLoaderManager().restartLoader(Constants.STOCK_HISTORY_LOADER_ID, bundle, this);
        });

        return rootView;
    }

    private void getData() {


    }

    @Override
    public Loader<GetStockHistoryResponse> onCreateLoader(int id, Bundle args) {
        if (getContext() != null)
            return new StockHistoryLoader(getContext(), actionServiceBlockingStub
                    , getStockIdFromCompanyName(args.getString(COMPANY_NAME_KEY)));
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetStockHistoryResponse> loader, GetStockHistoryResponse data) {

        if (data == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        for (Map.Entry<String, StockHistoryOuterClass.StockHistory> map : data.getStockHistoryMapMap().entrySet()) {
            com.hmproductions.theredstreet.data.StockHistory tempStockHistory =
                    new com.hmproductions.theredstreet.data.StockHistory(convertToDate(map.getKey())
                            ,map.getValue().getClose());
            stockHistoryList.add(tempStockHistory);
        }
        sortList(stockHistoryList);
        Collections.reverse(stockHistoryList);

        for(int i=0 ; i<stockHistoryList.size() ;i++){
        }

        if(stockHistoryList.size() >= 10){
            for (int i=0 ; i<10 ; i++){
                trimmedStockHistoryList.add(stockHistoryList.get(i));
            }
        }else {
            trimmedStockHistoryList.addAll(stockHistoryList);
        }

        Collections.reverse(trimmedStockHistoryList);
        for (int i=0 ; i<trimmedStockHistoryList.size() ; i++){
            xVals.add(parseDateString(convertToString(trimmedStockHistoryList.get(i).getStockDate())));
            yVals.add(new Entry(trimmedStockHistoryList.get(i).getStockClose(),i));
           // Log.e("SAN","X value : " + xVals + "  y value : " + yVals);
        }



        set1 = new LineDataSet(yVals, "Stock Price");
        set1.setFillAlpha(110);
        set1.setLineWidth(1f);
        set1.setColor(getResources().getColor(android.R.color.white));
        set1.setCircleColor(getResources().getColor(android.R.color.black));
        set1.setHighLightColor(getResources().getColor(R.color.divider_line_gray));
        set1.setDrawFilled(false);
        
        LineData lineData = new LineData(xVals,set1);

        lineChart.setData(lineData);
        lineChart.invalidate();
        lineChart.setDrawGridBackground(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);

        Legend legend = lineChart.getLegend();
        legend.setTextColor(getResources().getColor(android.R.color.white));
        XLabels xAxis = lineChart.getXLabels();
        xAxis.setTextColor(getResources().getColor(android.R.color.white));
        xAxis.setPosition(XLabels.XLabelPosition.BOTTOM);
        YLabels yAxis1 = lineChart.getYLabels();
        yAxis1.setTextColor(getResources().getColor(android.R.color.white));


    }

    @Override
    public void onLoaderReset(Loader<GetStockHistoryResponse> loader) {

    }

    private String parseDateString(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String outputPattern = "hh:mm a MMM dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    private Date convertToDate(String stringDate){
        DateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private String convertToString(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);
        String stringDate = df.format(date);
        return stringDate;
    }

    private void sortList(ArrayList<com.hmproductions.theredstreet.data.StockHistory> list) {
        Collections.sort(list, new Comparator<com.hmproductions.theredstreet.data.StockHistory>() {
            public int compare(com.hmproductions.theredstreet.data.StockHistory val1,
                               com.hmproductions.theredstreet.data.StockHistory val2) {

                Date date1 = val1.getStockDate();
                Date date2 = val2.getStockDate();
                return date1.compareTo(date2);
            }
        });
    }
}