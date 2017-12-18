package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

public class BuySellFragment extends Fragment {

    MaterialBetterSpinner companySpinner,orderSpinner;
    RadioButton defultButton;
    TextInputLayout noOfStocksInput,orderPriceInput;
    EditText noOfStocks,orderPrice;
    Button bidOrAsk;
    ProgressBar progressBar;

    public BuySellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_buy_sell, container, false);
        getActivity().setTitle("Buy / Sell");


        companySpinner= rootView.findViewById(R.id.company_spinner);
        orderSpinner= rootView.findViewById(R.id.order_select_spinner);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.companies));
        ArrayAdapter<String> arrayAdapter1=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.orders));
        orderSpinner.setAdapter(arrayAdapter1);
        companySpinner.setAdapter(arrayAdapter);


        bidOrAsk= rootView.findViewById(R.id.bid_ask);
        noOfStocks= rootView.findViewById(R.id.no_of_stocks);
        orderPrice= rootView.findViewById(R.id.order_price);
        noOfStocksInput= rootView.findViewById(R.id.no_of_stocks_input);
        orderPriceInput= rootView.findViewById(R.id.order_price_input);
        defultButton= rootView.findViewById(R.id.radioButton_bid);
        progressBar= rootView.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.INVISIBLE);
        defultButton.setChecked(true);


        defultButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    bidOrAsk.setText("Bid");
                }
                else{
                    bidOrAsk.setText("Ask");

                }
            }
        });

        orderSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).toString().equals("Marker order")){
                    orderPrice.setEnabled(false);

                }
                else {
                    orderPrice.setEnabled(true);

                }
            }
        });

        bidOrAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(companySpinner.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "select a company", Toast.LENGTH_SHORT).show();
                }
                else if(orderSpinner.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "select an order", Toast.LENGTH_SHORT).show();
                }
                else if(noOfStocks.getText().toString().trim().isEmpty()){
                    noOfStocksInput.setError("enter the number of stocks");
                    orderPriceInput.setErrorEnabled(false);
                }
                else if (orderPrice.isEnabled()&&orderPrice.getText().toString().trim().isEmpty()){
                    noOfStocksInput.setErrorEnabled(false);
                        if (defultButton.isChecked())
                            orderPriceInput.setError("enter the bid value");
                        else
                            orderPriceInput.setError("enter the ask value");

                }
                else{
                    orderPriceInput.setErrorEnabled(false);
                    noOfStocksInput.setErrorEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    addtransaction();
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "transaction added", Toast.LENGTH_SHORT).show();
                }


            }
        });

        return rootView;
    }

    public void addtransaction(){
        //todo : add transaction
    }

}
