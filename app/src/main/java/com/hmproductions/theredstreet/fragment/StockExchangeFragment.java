package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;


public class StockExchangeFragment extends Fragment {


    ArrayList<ExchangeValues> exchangeValues;

    MaterialBetterSpinner companySpinner;

    TextInputLayout noOfStocksInput;
    EditText noOfStocks;

    TextView stock_value,daily_high,daily_low,stocks_in_market,stocks_in_exchange;

    Button buy;

    public StockExchangeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_stock_exchange, container, false);

        getActivity().setTitle("Stock Exchange");

        companySpinner=(MaterialBetterSpinner)rootView.findViewById(R.id.company_spinner_exchange);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.companies));
        companySpinner.setAdapter(arrayAdapter);
        companySpinner.setSelection(0);

        buy=(Button)rootView.findViewById(R.id.buy_exchange);
        stock_value=(TextView)rootView.findViewById(R.id.stock_price_current);
        daily_high=(TextView)rootView.findViewById(R.id.daily_high);
        daily_low=(TextView)rootView.findViewById(R.id.daily_low);
        stocks_in_market=(TextView)rootView.findViewById(R.id.stocks_in_market);
        stocks_in_exchange=(TextView)rootView.findViewById(R.id.stocks_in_exchange);

        noOfStocks=(EditText)rootView.findViewById(R.id.stocks_exchange);
        noOfStocksInput=(TextInputLayout)rootView.findViewById(R.id.stocks_exchange_input);



        publish();

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(companySpinner.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Select a company", Toast.LENGTH_SHORT).show();
                    noOfStocksInput.setErrorEnabled(false);
                }
                else if(noOfStocks.getText().toString().trim().isEmpty()){
                    noOfStocksInput.setError("enter the number of stocks");
                }
                else {
                    noOfStocksInput.setErrorEnabled(false);
                    Toast.makeText(getActivity(), "stocks bought", Toast.LENGTH_SHORT).show();
                }
            }
        });



        return rootView;
    }

    public void setValues(){

        //todo : get from service
        exchangeValues=new ArrayList<>();

        exchangeValues.add(new ExchangeValues("Github",50,45,55,100,100));
        exchangeValues.add(new ExchangeValues("Intel",50,45,55,100,100));
        exchangeValues.add(new ExchangeValues("Apple",50,45,55,100,100));
        exchangeValues.add(new ExchangeValues("Yahoo",50,45,55,100,100));
        exchangeValues.add(new ExchangeValues("HDFC",50,45,55,100,100));
        exchangeValues.add(new ExchangeValues("LG",50,45,55,100,100));
        exchangeValues.add(new ExchangeValues("Infosys",50,45,55,100,100));



    }

    public void publish(){

        setValues();

        final String stock_price="Stock price : ";
        companySpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                for (ExchangeValues k : exchangeValues){
                    if(adapterView.getItemAtPosition(i).toString().equals(k.company)){
                        stock_value.setText("Current stock price : "+ String.valueOf(k.stockValue));
                        daily_high.setText("Daily high : "+ String.valueOf(k.dailyHigh));
                        daily_low.setText("Daily low : "+ String.valueOf(k.dailyLow));
                        stocks_in_market.setText("Stocks in market : "+ String.valueOf(k.stocksInMarket));
                        stocks_in_exchange.setText("Stocks in exchange : "+ String.valueOf(k.stocksInExchange));
                    }
                }

            }
        });



    }

    public class ExchangeValues{

        int stockValue,dailyHigh,dailyLow,stocksInMarket,stocksInExchange;
        String company;

        public ExchangeValues(String company, int stockValue, int dailyHigh, int dailyLow, int stocksInMarket, int stocksInExchange) {
            this.stockValue = stockValue;
            this.dailyHigh = dailyHigh;
            this.dailyLow = dailyLow;
            this.stocksInMarket = stocksInMarket;
            this.stocksInExchange = stocksInExchange;
            this.company = company;
        }

        public ExchangeValues() {
        }
    }

}
