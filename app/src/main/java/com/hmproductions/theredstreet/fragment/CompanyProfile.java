package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
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
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompanyProfile extends Fragment {

    MaterialBetterSpinner materialBetterSpinner;

    BarChart bidBar,askBar;

    ArrayList<BarEntry> bidEntry,askEntry ;
    ArrayList<String> bidEntryLabels,askEntryLabels ;

    BarDataSet bidDataset,askDataset ;
    BarData bidData,askData ;



    LineChart performance;
    ArrayList<Entry> performanceEntries;
    ArrayList<String> performanceLabels;
    LineData performanceData;
    LineDataSet dataset;

    public CompanyProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_company_profile, container, false);
        getActivity().setTitle("Company profile");


        materialBetterSpinner=(MaterialBetterSpinner)rootView.findViewById(R.id.profile_select);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.companies));
        materialBetterSpinner.setAdapter(arrayAdapter);
        materialBetterSpinner.setSelection(0);

        bidBar=(BarChart)rootView.findViewById(R.id.bidChart);
        askBar=(BarChart)rootView.findViewById(R.id.askChart);
        performance=(LineChart)rootView.findViewById(R.id.history);

        publish();




        return rootView;
    }


    public void addBidvalues(){
        bidEntry=new ArrayList<>(); //todo : get from service
        bidEntry.clear();
        bidEntry.add(new BarEntry(30, 0)); //y axis (value,index)
        bidEntry.add(new BarEntry(40, 1));
        bidEntry.add(new BarEntry(50, 2));
        bidEntry.add(new BarEntry(60, 3));
        bidEntry.add(new BarEntry(70, 4));
       // bidEntry.add(new BarEntry(80, 5));

        bidEntryLabels=new ArrayList<>();
        bidEntryLabels.clear();
        bidEntryLabels.add("30"); //x axis labels
        bidEntryLabels.add("40");
        bidEntryLabels.add("50");
        bidEntryLabels.add("60");
        bidEntryLabels.add("70");
        //bidEntryLabels.add("80");
    }


    public void addAskvalues(){
        askEntry=new ArrayList<>();  //todo : get from service
        askEntry.clear();
        askEntry.add(new BarEntry(30, 0));
        askEntry.add(new BarEntry(40, 1));
        askEntry.add(new BarEntry(50, 2));
        askEntry.add(new BarEntry(60, 3));
        askEntry.add(new BarEntry(70, 4));
     //   askEntry.add(new BarEntry(80, 5));

        askEntryLabels=new ArrayList<>();
        askEntryLabels.clear();
        askEntryLabels.add("30");
        askEntryLabels.add("40");
        askEntryLabels.add("50");
        askEntryLabels.add("60");
        askEntryLabels.add("70");
//        askEntryLabels.add("80");
    }

    public void addPerformanceValues(){
        performanceEntries=new ArrayList<>(); //todo : get from service
        performanceEntries.clear();
        performanceEntries.add(new Entry(30f,0));
        performanceEntries.add(new Entry(32f,1));
        performanceEntries.add(new Entry(34f,2));
        performanceEntries.add(new Entry(36f,3));
        performanceEntries.add(new Entry(18f,4));

        performanceLabels=new ArrayList<>();
        performanceLabels.clear();
        performanceLabels.add("12th");
        performanceLabels.add("13th");
        performanceLabels.add("14th");
        performanceLabels.add("15th");
        performanceLabels.add("16th");
    }

    public void setValues(){

        addBidvalues();
        addAskvalues();
        addPerformanceValues();


    }

    public void publish(){

        setValues();

        bidDataset=new BarDataSet(bidEntry,"Bid values");
        bidDataset.setColors(ColorTemplate.PASTEL_COLORS);
        askDataset=new BarDataSet(askEntry,"Ask values");
        askDataset.setColors(ColorTemplate.PASTEL_COLORS);

        bidData=new BarData(bidEntryLabels, bidDataset);
        askData=new BarData(askEntryLabels,askDataset);

        bidBar.setDrawLegend(false);
        bidBar.setDescription("");
        bidBar.setDescriptionTextSize(20);
        bidBar.setData(bidData);
        bidBar.animateY(1000);

        askBar.setDrawLegend(false);
        askBar.setDescription("");
        askBar.setDescriptionTextSize(20);
        askBar.setData(askData);
        askBar.animateY(1000);

        dataset=new LineDataSet(performanceEntries,"Company performance");
        dataset.setColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
        dataset.setLineWidth(3);
        dataset.setCircleSize(5);
        performanceData=new LineData(performanceLabels,dataset);
        performance.setDrawLegend(false);
        performance.setDescription("");
        performance.animateX(1000);
        performance.setDescriptionTextSize(20);
        performance.setData(performanceData);


    }

}
