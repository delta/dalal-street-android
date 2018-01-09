package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.ui.MainActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMortgageDetailsRequest;
import dalalstreet.api.actions.GetMortgageDetailsResponse;
import dalalstreet.api.actions.MortgageStocksRequest;
import dalalstreet.api.actions.MortgageStocksResponse;
import dalalstreet.api.actions.RetrieveMortgageStocksRequest;
import dalalstreet.api.actions.RetrieveMortgageStocksResponse;
import dalalstreet.api.models.Transaction;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/* Uses GetMortgageDetails() for setting stocksMortgaged (int data member)
*  Uses MortgageStocks() to mortgage stocks
*  Uses RetrieveStocksFromMortgage() to get back mortgaged stocks */

public class MortgageFragment extends Fragment {

    private static final String NO_OF_STOCKS_OWNED_STRING = "Number of stocks you own :";
    private final static String NO_OF_STOCKS_MORTGAGE_STRING = "Number of stocks in mortgage : ";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    Metadata metadata;

    @BindView(R.id.mortgageRadioGroup)
    RadioGroup mortgageRadioGroup;

    @BindView(R.id.stocks_editText)
    EditText stocksEditText;

    @BindView(R.id.stocksOwned_textView)
    TextView ownedTextView;

    @BindView(R.id.stocksMortgaged_textView)
    TextView mortgagedTextView;

    int stocksOwned = 0, stocksMortgaged = 0, stocksTransaction;
    Spinner companySpinner;
    TextView cashTextView, stockTextView;

    public MortgageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_mortgage, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);
        if (getActivity() != null) getActivity().setTitle("Mortgage Stocks");

        // TODO : (IMP) Remove unnecessary change of textView
        stockTextView = container.getRootView().findViewById(R.id.stockWorth_textView);
        cashTextView = container.getRootView().findViewById(R.id.cashWorth_textView);

        companySpinner = rootView.findViewById(R.id.mortgage_companies_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.companies));
        companySpinner.setAdapter(arrayAdapter);

        companySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateValues(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String ownedString = NO_OF_STOCKS_OWNED_STRING + "N/A";
        ownedTextView.setText(ownedString);

        String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + "N/A";
        mortgagedTextView.setText(mortgageString);

        return rootView;
    }

    @OnClick(R.id.mortgage_button)
    void onMortgageButtonClick() {

        if (stocksEditText.getText().toString().trim().isEmpty()) {
            stocksEditText.setError("Enter the number of stocks");
            stocksEditText.requestFocus();
            return;
        } else {
            stocksEditText.setError(null);
            stocksEditText.clearFocus();
        }

        stocksTransaction = Integer.parseInt(stocksEditText.getText().toString().trim());

        if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.mortgage_radioButton) {

            if (stocksTransaction <= stocksOwned) {
                stocksOwned -= stocksTransaction;
                stocksMortgaged += stocksTransaction;

                String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
                ownedTextView.setText(ownedString);

                String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
                mortgagedTextView.setText(mortgageString);

                stocksEditText.setText("");

                MortgageStocksResponse mortgageStocksResponse = actionServiceBlockingStub.mortgageStocks(
                        MortgageStocksRequest
                                .newBuilder()
                                .setStockId(companySpinner.getSelectedItemPosition())
                                .setStockQuantity(Integer.parseInt(stocksEditText.getText().toString()))
                                .build()
                );

                if (mortgageStocksResponse.getStatusCode().getNumber() == 0) {

                    Transaction currentTransaction = mortgageStocksResponse.getTransaction();
                    int mortgageValue = currentTransaction.getStockQuantity() * currentTransaction.getPrice();

                    cashTextView.setText(String.valueOf(Integer.parseInt(cashTextView.getText().toString()) + mortgageValue));
                    stockTextView.setText(String.valueOf(Integer.parseInt(stockTextView.getText().toString()) - mortgageValue));

                    MainActivity.ownedStockDetails.get(companySpinner.getSelectedItemPosition()).setQuantity(stocksOwned);

                } else if (mortgageStocksResponse.getStatusCode().getNumber() == 2) {
                    Toast.makeText(getContext(), "Market is Closed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Inconsistent data server error", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "You dont have sufficient stocks", Toast.LENGTH_SHORT).show();
            }

        } else if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.retrieve_radioButton) {

            if (stocksTransaction <= stocksMortgaged && stocksMortgaged >= 0) {
                stocksOwned += stocksTransaction;
                stocksMortgaged -= stocksTransaction;

                String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
                ownedTextView.setText(ownedString);

                String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
                mortgagedTextView.setText(mortgageString);

                stocksEditText.setText("");

                RetrieveMortgageStocksResponse retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                        RetrieveMortgageStocksRequest
                                .newBuilder()
                                // TODO : Check getSelectedItemPositionValue() if it is 0 or 1 for first position
                                .setStockId(companySpinner.getSelectedItemPosition())
                                .setStockQuantity(Integer.parseInt(stocksEditText.getText().toString()))
                                .build()
                );

                if (retrieveStocksResponse.getStatusCode().getNumber() == 0) {

                    Transaction currentTransaction = retrieveStocksResponse.getTransaction();
                    int mortgageValue = currentTransaction.getStockQuantity() * currentTransaction.getPrice();

                    cashTextView.setText(String.valueOf(Integer.parseInt(cashTextView.getText().toString()) - mortgageValue));
                    stockTextView.setText(String.valueOf(Integer.parseInt(stockTextView.getText().toString()) + mortgageValue));

                    MainActivity.ownedStockDetails.get(companySpinner.getSelectedItemPosition()).setQuantity(stocksOwned);

                } else if (retrieveStocksResponse.getStatusCode().getNumber() == 2) {
                    Toast.makeText(getContext(), "Market is Closed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Inconsistent data server error", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "You dont have sufficient stocks mortgaged", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Select mortgage or retrieve", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateValues(int position) {

        MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata);

        GetMortgageDetailsResponse response = actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build());

        stocksMortgaged = response.getMortgageMapMap().get(position);
        String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
        mortgagedTextView.setText(mortgageString);

        stocksOwned = MainActivity.ownedStockDetails.get(position).getQuantity();
        String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
        ownedTextView.setText(ownedString);
    }
}