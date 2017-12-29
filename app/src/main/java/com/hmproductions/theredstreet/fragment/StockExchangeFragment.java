package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.ExchangeValuesDetails;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StockExchangeFragment extends Fragment {

    ArrayList<ExchangeValuesDetails> exchangeValues = new ArrayList<>();
    private ExchangeValuesDetails currentExchange;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner companySpinner;

    @BindView(R.id.noOfStocks_editText)
    EditText noOfStocksEditText;

    @BindView(R.id.currentStockPrice_textView)
    TextView currentStockPriceTextView;

    @BindView(R.id.dailyHigh_textView)
    TextView daily_high;

    @BindView(R.id.dailyLow_textView)
    TextView daily_low;

    @BindView(R.id.stocksInMarket_textView)
    TextView stocks_in_market;

    @BindView(R.id.stocksInExchange_textView)
    TextView stocks_in_exchange;

    public StockExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stock_exchange, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Stock Exchange");

        companySpinner = rootView.findViewById(R.id.company_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.companies));
        companySpinner.setAdapter(arrayAdapter);

        updateValues();

        companySpinner.setOnItemClickListener((adapterView, view, position, id) -> {

            String temporaryTextViewString;
            currentExchange = exchangeValues.get(position);

            temporaryTextViewString = "Current stock price : " + String.valueOf(currentExchange.getStockValue());
            currentStockPriceTextView.setText(temporaryTextViewString);

            temporaryTextViewString = "Daily high : " + String.valueOf(currentExchange.getDailyHigh());
            daily_high.setText(temporaryTextViewString);

            temporaryTextViewString = "Daily low : " + String.valueOf(currentExchange.getDailyLow());
            daily_low.setText(temporaryTextViewString);

            temporaryTextViewString = "Stocks in market : " + String.valueOf(currentExchange.getStocksInMarket());
            stocks_in_market.setText(temporaryTextViewString);

            temporaryTextViewString = "Stocks in exchange : " + String.valueOf(currentExchange.getStocksInExchange());
            stocks_in_exchange.setText(temporaryTextViewString);

        });

        return rootView;
    }

    @OnClick(R.id.buyExchange_button)
    void onBuyExchangeButtonClick() {
        if (companySpinner.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.pick_a_company), Toast.LENGTH_SHORT).show();
        } else if (noOfStocksEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Enter the number of Stocks", Toast.LENGTH_SHORT).show();
        } else {
            if (Integer.parseInt(noOfStocksEditText.getText().toString().trim()) < currentExchange.getStocksInMarket()) {
                Toast.makeText(getActivity(), "Stocks Bought", Toast.LENGTH_SHORT).show();
                // TODO : Buy stocks
            } else {
                Toast.makeText(getActivity(), "Insufficient stocks in market", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateValues() {

        //TODO : Get from service
        exchangeValues.clear();

        exchangeValues.add(new ExchangeValuesDetails("Github", 50, 45, 55, 100, 100));
        exchangeValues.add(new ExchangeValuesDetails("Intel", 50, 45, 55, 100, 100));
        exchangeValues.add(new ExchangeValuesDetails("Apple", 50, 45, 55, 100, 100));
        exchangeValues.add(new ExchangeValuesDetails("Yahoo", 50, 45, 55, 100, 100));
        exchangeValues.add(new ExchangeValuesDetails("HDFC", 50, 45, 55, 100, 100));
        exchangeValues.add(new ExchangeValuesDetails("LG", 50, 45, 55, 100, 100));
        exchangeValues.add(new ExchangeValuesDetails("Infosys", 50, 45, 55, 100, 100));
    }
}