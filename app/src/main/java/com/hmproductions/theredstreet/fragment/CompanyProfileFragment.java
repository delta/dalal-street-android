package com.hmproductions.theredstreet.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.datastreams.SubscriptionId;

import static com.hmproductions.theredstreet.utils.MiscellaneousUtils.convertDpToPixel;

public class CompanyProfileFragment extends Fragment {

    private static final int ANIMATE_DURATION = 1000;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner materialBetterSpinner;

    @BindView(R.id.bidBarChart)
    BarChart bidBarChart;

    @BindView(R.id.askBarChart)
    BarChart askBarChart;

    ArrayList<BarEntry> bidEntryList = new ArrayList<>();
    ArrayList<BarEntry> askEntryList = new ArrayList<>();
    ArrayList<String> bidEntryLabels = new ArrayList<>();
    ArrayList<String> askEntryLabels = new ArrayList<>();
    ArrayList<Entry> performanceEntries = new ArrayList<>();
    ArrayList<String> performanceLabels = new ArrayList<>();

    BarDataSet bidDataset,askDataset;
    BarData bidBarData, askBarData;

    LineChart performanceLineChart;
    LineData performanceData;
    LineDataSet dataset;

    private SubscriptionId subscriptionId = null;

    public CompanyProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_company_profile, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Company Profile");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line, StockUtils.companyNamesArray);
        materialBetterSpinner.setAdapter(arrayAdapter);
        materialBetterSpinner.setSelection(0);

        performanceLineChart = rootView.findViewById(R.id.history);

        updateValues();

        publish();

        return rootView;
    }

    public void addBidvalues(){

        //TODO : get from service

        bidEntryList.clear();
        bidEntryList.add(new BarEntry(30, 0));
        bidEntryList.add(new BarEntry(40, 1));
        bidEntryList.add(new BarEntry(50, 2));
        bidEntryList.add(new BarEntry(60, 3));
        bidEntryList.add(new BarEntry(70, 4));

        bidEntryLabels.clear();
        bidEntryLabels.add("3077"); //x axis labels
        bidEntryLabels.add("40");
        bidEntryLabels.add("50");
        bidEntryLabels.add("60");
        bidEntryLabels.add("70");
    }

    public void addAskValues(){

        askEntryList.clear();
        askEntryList.add(new BarEntry(30, 0));
        askEntryList.add(new BarEntry(40, 1));
        askEntryList.add(new BarEntry(50, 2));
        askEntryList.add(new BarEntry(60, 3));
        askEntryList.add(new BarEntry(70, 4));

        askEntryLabels.clear();
        askEntryLabels.add("30");
        askEntryLabels.add("40");
        askEntryLabels.add("50");
        askEntryLabels.add("60");
        askEntryLabels.add("70");
    }

    public void addPerformanceValues(){

        //TODO : Get from service
        performanceEntries.clear();
        performanceEntries.add(new Entry(30f,0));
        performanceEntries.add(new Entry(32f,1));
        performanceEntries.add(new Entry(34f,2));
        performanceEntries.add(new Entry(36f,3));
        performanceEntries.add(new Entry(18f,4));

        performanceLabels.clear();
        performanceLabels.add("12th");
        performanceLabels.add("13th");
        performanceLabels.add("14th");
        performanceLabels.add("15th");
        performanceLabels.add("16th");
    }

    public void updateValues() {

//        streamServiceStub.subscribe(
//                SubscribeRequest
//                        .newBuilder()
//                        .setDataStreamType(DataStreamType.MARKET_DEPTH)
//                        .setDataStreamId("marketDepth")
//                        .build(), new StreamObserver<SubscribeResponse>() {
//                    @Override
//                    public void onNext(SubscribeResponse value) {
//                        if (value.getStatusCode().getNumber() == 0)
//                            subscriptionId = value.getSubscriptionId();
//                        else
//                            Toast.makeText(getContext(), "Server internal error", Toast.LENGTH_SHORT).show();
//                        onCompleted();
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//
//                    }
//
//                    @Override
//                    public void onCompleted() {
//
//                    }
//                });
//
//        streamServiceStub.getMarketDepthUpdates(subscriptionId, new StreamObserver<MarketDepthUpdate>() {
//            @Override
//            public void onNext(MarketDepthUpdate value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//
//            }
//        });
//
//        streamServiceStub.unsubscribe(
//                UnsubscribeRequest.newBuilder().setSubscriptionId(subscriptionId).build(),
//                new StreamObserver<UnsubscribeResponse>() {
//                    @Override
//                    public void onNext(UnsubscribeResponse value) {
//                        if (!(value.getStatusCode().getNumber() == 0))
//                            Toast.makeText(getContext(), "Server internal error", Toast.LENGTH_SHORT).show();
//                        onCompleted();
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//
//                    }
//
//                    @Override
//                    public void onCompleted() {
//
//                    }
//                });

        addBidvalues();
        addAskValues();
        addPerformanceValues();
    }

    public void publish(){

        bidDataset=new BarDataSet(bidEntryList,"Bid values");
        bidDataset.setColors(ColorTemplate.PASTEL_COLORS);
        askDataset=new BarDataSet(askEntryList,"Ask values");
        askDataset.setColors(ColorTemplate.PASTEL_COLORS);

        bidBarData=new BarData(bidEntryLabels, bidDataset);
        askBarData=new BarData(askEntryLabels,askDataset);

        bidBarChart.setDrawLegend(false);
        bidBarChart.setDescription("");
        bidBarChart.setDescriptionTextSize(convertDpToPixel(getContext(), 20));
        bidBarChart.setData(bidBarData);
        bidBarChart.animateY(ANIMATE_DURATION);

        askBarChart.setDrawLegend(false);
        askBarChart.setDescription("");
        askBarChart.setDescriptionTextSize(convertDpToPixel(getContext(), 20));
        askBarChart.setData(askBarData);
        askBarChart.animateY(ANIMATE_DURATION);

        dataset=new LineDataSet(performanceEntries,"Company Performance");
        if (getContext() != null)
            dataset.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        dataset.setLineWidth(3);
        dataset.setCircleSize(5);
        performanceData=new LineData(performanceLabels,dataset);
        performanceLineChart.setDrawLegend(false);
        performanceLineChart.setDescription("");
        performanceLineChart.animateX(ANIMATE_DURATION);
        performanceLineChart.setDescriptionTextSize(convertDpToPixel(getContext(), 20));
        performanceLineChart.setData(performanceData);
    }
}