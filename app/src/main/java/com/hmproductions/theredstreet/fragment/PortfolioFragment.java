package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hmproductions.theredstreet.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {

    ListView listView;

    ArrayList<PortfolioValues> portfolioValues;
    ArrayList<String> stockDetails;

    public PortfolioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_portfolio, container, false);

        getActivity().setTitle("PortfolioFragment");


        listView = rootView.findViewById(R.id.stock_list);

        publish();

        return rootView;
    }

    public void setValues(){
          //todo : get from service


        stockDetails=new ArrayList<>();
        stockDetails.clear();
        portfolioValues=new ArrayList<>();
        portfolioValues.clear();
        portfolioValues.add(new PortfolioValues("Github",30,20));
        portfolioValues.add(new PortfolioValues("Apple",20,80));
        portfolioValues.add(new PortfolioValues("Yahoo",45,100));
        portfolioValues.add(new PortfolioValues("HDFC",30,20));
        portfolioValues.add(new PortfolioValues("LG",15,60));
        portfolioValues.add(new PortfolioValues("Sony",25,75));
        portfolioValues.add(new PortfolioValues("Infosys",50,35));

        for (int i=0;i<portfolioValues.size();i++){
            stockDetails.add(portfolioValues.get(i).stock_details);
        }



    }

    public void publish(){

        setValues();
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,stockDetails);
        listView.setAdapter(arrayAdapter);


    }

    public class PortfolioValues{

        String company,stock_details;
        int noOfStock,value;

        public PortfolioValues(String company, int noOfStock, int value) {
            this.company = company;
            this.noOfStock = noOfStock;
            this.value = value;

            constructStockDetails();

        }

        public void constructStockDetails(){
            stock_details=company+" : "+ String.valueOf(noOfStock)+" ( ₹"+ String.valueOf(value)+" per stock)";
        }
    }

}
