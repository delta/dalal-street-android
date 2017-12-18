package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;


/**
 * A simple {@link Fragment} subclass.
 */
public class Mortgage extends Fragment {


    MaterialBetterSpinner materialBetterSpinner;
    RadioButton defaultButton;
    Button mortgageButton;
    EditText stocks;
    TextInputLayout stock_input;
    TextView owned,mortgaged;

    int stocks_owned,stocks_mortgaged,stock_transaction;



    public Mortgage() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView=inflater.inflate(R.layout.fragment_mortgage, container, false);
        getActivity().setTitle("Mortgage");

        materialBetterSpinner=(MaterialBetterSpinner)rootView.findViewById(R.id.mortgage_companies_spinner);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.companies));
        materialBetterSpinner.setAdapter(arrayAdapter);

        stocks=(EditText)rootView.findViewById(R.id.mortgage_stock);
        stock_input=(TextInputLayout)rootView.findViewById(R.id.mortgage_stock_input);

        owned=(TextView)rootView.findViewById(R.id.stocks_owned);
        mortgaged=(TextView)rootView.findViewById(R.id.stocks_mortgaged);




        defaultButton=(RadioButton)rootView.findViewById(R.id.radioButton_sell);
        defaultButton.setChecked(true);

        mortgageButton=(Button)rootView.findViewById(R.id.mortgage);
        mortgageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(stocks.getText().toString().trim().isEmpty()) {
                    stock_input.setError("enter the number of stocks");
                    stocks.requestFocus();
                }
                else{
                    stock_input.setErrorEnabled(false);
                    if(materialBetterSpinner.getText().toString().trim().isEmpty()){
                        Toast.makeText(getActivity(), "select a company", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        stock_transaction= Integer.parseInt(stocks.getText().toString());
                        if(defaultButton.isChecked()){

                            if(stock_transaction<=stocks_owned) {
                                stocks_owned -= stock_transaction; //todo : update in service
                                stocks_mortgaged += stock_transaction;

                                owned.setText("Number of stocks you own : "+ String.valueOf(stocks_owned));
                                mortgaged.setText("Number of stocks in mortgage : "+ String.valueOf(stocks_mortgaged));
                                stocks.setText("");
                            }
                            else{
                                Toast.makeText(getActivity(), "You dont have sufficient stocks", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {

                            if(stock_transaction<=stocks_mortgaged&&stocks_mortgaged>=0) {
                                stocks_owned += stock_transaction;   //todo : update in service
                                stocks_mortgaged -= stock_transaction;

                                stocks.setText("");
                                owned.setText("Number of stocks you own : "+ String.valueOf(stocks_owned));
                                mortgaged.setText("Number of stocks in mortgage : "+ String.valueOf(stocks_mortgaged));
                            }
                            else{
                                Toast.makeText(getActivity(), "You dont have sufficient stocks", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

            }
        });

        publish();

        return rootView;
    }

    public void setValues(){

        stocks_owned=30;            //todo : get from service
        stocks_mortgaged=45;

    }

    public void publish(){
        setValues();

        owned.setText("Number of stocks you own : "+ String.valueOf(stocks_owned));
        mortgaged.setText("Number of stocks in mortgage : "+ String.valueOf(stocks_mortgaged));


    }

}
