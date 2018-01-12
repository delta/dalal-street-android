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

import com.hmproductions.theredstreet.utils.StockUtils;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.BuyStocksFromExchangeRequest;
import dalalstreet.api.actions.BuyStocksFromExchangeResponse;
import dalalstreet.api.actions.GetCompanyProfileRequest;
import dalalstreet.api.actions.GetCompanyProfileResponse;
import dalalstreet.api.actions.StockHistoryGranularity;
import dalalstreet.api.models.Stock;

import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

/* Uses GetCompanyProfile() for getting stock info
*  Uses BuyStocksFromExchange() to buy appropriate stocks */
public class StockExchangeFragment extends Fragment {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

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

    private Stock currentStock;

    public StockExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stock_exchange, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Stock Exchange");
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        companySpinner = rootView.findViewById(R.id.company_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, StockUtils.companyNamesArray);
        companySpinner.setAdapter(arrayAdapter);

        companySpinner.setOnItemClickListener((adapterView, view, position, id) -> {

            GetCompanyProfileResponse companyProfileResponse = actionServiceBlockingStub.getCompanyProfile(
                    GetCompanyProfileRequest.newBuilder()
                            .setStockId(position)
                            .setGranularity(StockHistoryGranularity.OneDay)
                            .setGetOnlyHistory(false)
                            .build()
            );

            String temporaryTextViewString;
            currentStock = companyProfileResponse.getStockDetails();

            temporaryTextViewString = "Current stock price : " + String.valueOf(currentStock.getCurrentPrice());
            currentStockPriceTextView.setText(temporaryTextViewString);

            temporaryTextViewString = "Daily high : " + String.valueOf(currentStock.getDayHigh());
            daily_high.setText(temporaryTextViewString);

            temporaryTextViewString = "Daily low : " + String.valueOf(currentStock.getDayLow());
            daily_low.setText(temporaryTextViewString);

            temporaryTextViewString = "Stocks in market : " + String.valueOf(currentStock.getStocksInMarket());
            stocks_in_market.setText(temporaryTextViewString);

            temporaryTextViewString = "Stocks in exchange : " + String.valueOf(currentStock.getStocksInExchange());
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
            if (Integer.parseInt(noOfStocksEditText.getText().toString().trim()) <= currentStock.getStocksInExchange()) {

                BuyStocksFromExchangeResponse response = actionServiceBlockingStub.buyStocksFromExchange(
                        BuyStocksFromExchangeRequest
                                .newBuilder()
                                .setStockId(getStockIdFromCompanyName(companySpinner.getText().toString()))
                                .setStockQuantity(Integer.parseInt(noOfStocksEditText.getText().toString()))
                        .build()
                );

                switch (response.getStatusCode().getNumber()) {

                    case 0:
                        Toast.makeText(getContext(), "Stocks bought", Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        Toast.makeText(getContext(), "Internal server error", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        Toast.makeText(getContext(), "Market closed", Toast.LENGTH_SHORT).show();
                        break;

                    case 4:
                        Toast.makeText(getContext(), "Insufficient cash", Toast.LENGTH_SHORT).show();
                        break;

                    case 5:
                        Toast.makeText(getContext(), "Buy limit exceeded", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(getContext(), "Insufficient stocks error", Toast.LENGTH_SHORT).show();
                        break;
                }

                Toast.makeText(getActivity(), "Stocks Bought", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Insufficient stocks in market", Toast.LENGTH_SHORT).show();
            }
        }
    }
}