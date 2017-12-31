package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class MortgageFragment extends Fragment {

    private static final String NO_OF_STOCKS_OWNED_STRING = "Number of stocks you own :";
    private final static String NO_OF_STOCKS_MORTGAGE_STRING = "Number of stocks in mortgage : ";

    MaterialBetterSpinner materialBetterSpinner;

    @BindView(R.id.mortgageRadioGroup)
    RadioGroup mortgageRadioGroup;

    @BindView(R.id.stocks_editText)
    EditText stocksEditText;

    @BindView(R.id.stocksOwned_textView)
    TextView ownedTextView;

    @BindView(R.id.stocksMortgaged_textView)
    TextView mortgagedTextView;

    int stocksOwned, stocksMortgaged, stocksTransaction;

    public MortgageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_mortgage, container, false);

        ButterKnife.bind(this, rootView);
        if (getActivity() != null) getActivity().setTitle("MortgageFragment");

        materialBetterSpinner = rootView.findViewById(R.id.mortgage_companies_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.companies));
        materialBetterSpinner.setAdapter(arrayAdapter);

        updateValues();

        String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
        ownedTextView.setText(ownedString);

        String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksOwned);
        mortgagedTextView.setText(mortgageString);

        return rootView;
    }

    @OnClick(R.id.mortgage_button)
    void onMortgageButtonClick() {

        if (stocksEditText.getText().toString().trim().isEmpty()) {
            stocksEditText.setError("Enter the number of stocks");
            Toast.makeText(getContext(), "Incomplete", Toast.LENGTH_SHORT).show();
            stocksEditText.requestFocus();
            return;
        } else {
            stocksEditText.setError(null);
            stocksEditText.clearFocus();
        }

        if (materialBetterSpinner.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Select a company", Toast.LENGTH_SHORT).show();
            return;
        }

        stocksTransaction = Integer.parseInt(stocksEditText.getText().toString().trim());

        if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.mortgage_radioButton) {

            if (stocksTransaction <= stocksOwned) {
                stocksOwned -= stocksTransaction; //todo : update in service
                stocksMortgaged += stocksTransaction;

                String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
                ownedTextView.setText(ownedString);

                String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
                mortgagedTextView.setText(mortgageString);

                stocksEditText.setText("");

            } else {
                Toast.makeText(getActivity(), "You dont have sufficient stocks", Toast.LENGTH_SHORT).show();
            }

        } else if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.retrieve_radioButton) {

            if (stocksTransaction <= stocksMortgaged && stocksMortgaged >= 0) {
                stocksOwned += stocksTransaction;   //todo : update in service
                stocksMortgaged -= stocksTransaction;

                String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
                ownedTextView.setText(ownedString);

                String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
                mortgagedTextView.setText(mortgageString);

                stocksEditText.setText("");

            } else {
                Toast.makeText(getActivity(), "You dont have sufficient stocks mortgaged", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateValues() {
        stocksOwned = 30;            //TODO : get from service
        stocksMortgaged = 45;
    }
}