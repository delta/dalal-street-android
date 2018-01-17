package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.hmproductions.theredstreet.loaders.MortgageDetailsLoader;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetMortgageDetailsResponse;
import dalalstreet.api.actions.MortgageStocksRequest;
import dalalstreet.api.actions.MortgageStocksResponse;
import dalalstreet.api.actions.RetrieveMortgageStocksRequest;
import dalalstreet.api.actions.RetrieveMortgageStocksResponse;

import static com.hmproductions.theredstreet.utils.StockUtils.getCompanyNameFromStockId;
import static com.hmproductions.theredstreet.utils.StockUtils.getQuantityOwnedFromCompanyName;

/* Uses GetMortgageDetails() for setting stocksMortgaged (int data member)
*  Uses MortgageStocks() to mortgage stocks
*  Uses RetrieveStocksFromMortgage() to get back mortgaged stocks */

public class MortgageFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetMortgageDetailsResponse>{

    private static final String NO_OF_STOCKS_OWNED_STRING = "Number of stocks you own : ";
    private final static String NO_OF_STOCKS_MORTGAGE_STRING = "Number of stocks in mortgage : ";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

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

        companySpinner = rootView.findViewById(R.id.mortgage_companies_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, StockUtils.getCompanyNamesArray());
        companySpinner.setAdapter(arrayAdapter);

        companySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getActivity().getSupportLoaderManager().restartLoader(Constants.MORTGAGE_DETAILS_LOADER_ID, null, MortgageFragment.this);
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
            stocksEditText.setError("Stocks quantity missing");
            stocksEditText.requestFocus();
            return;
        } else {
            stocksEditText.setError(null);
            stocksEditText.clearFocus();
        }

        stocksTransaction = Integer.parseInt(stocksEditText.getText().toString().trim());

        if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.mortgage_radioButton) {

            if (stocksTransaction <= stocksOwned) {
                MortgageStocksResponse mortgageStocksResponse = actionServiceBlockingStub.mortgageStocks(
                        MortgageStocksRequest
                                .newBuilder()
                                .setStockId(companySpinner.getSelectedItemPosition()+1)
                                .setStockQuantity(Integer.parseInt(stocksEditText.getText().toString()))
                                .build()
                );

                if (mortgageStocksResponse.getStatusCode().getNumber() == 0) {
                    Toast.makeText(getContext(), "Transaction successful", Toast.LENGTH_SHORT).show();

                    stocksOwned -= stocksTransaction;
                    stocksMortgaged += stocksTransaction;

                    String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
                    ownedTextView.setText(ownedString);

                    String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
                    mortgagedTextView.setText(mortgageString);

                    stocksEditText.setText("");

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
                RetrieveMortgageStocksResponse retrieveStocksResponse = actionServiceBlockingStub.retrieveMortgageStocks(
                        RetrieveMortgageStocksRequest
                                .newBuilder()
                                .setStockId(companySpinner.getSelectedItemPosition()+1)
                                .setStockQuantity(Integer.parseInt(stocksEditText.getText().toString()))
                                .build()
                );

                if (retrieveStocksResponse.getStatusCode().getNumber() == 0) {
                    Toast.makeText(getContext(), "Transaction successful", Toast.LENGTH_SHORT).show();

                    stocksOwned += stocksTransaction;
                    stocksMortgaged -= stocksTransaction;

                    String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
                    ownedTextView.setText(ownedString);

                    String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
                    mortgagedTextView.setText(mortgageString);

                    stocksEditText.setText("");

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

    @Override
    public Loader<GetMortgageDetailsResponse> onCreateLoader(int id, Bundle args) {
        companySpinner.setEnabled(false);
        if (getContext() != null) {
            return new MortgageDetailsLoader(getContext(), actionServiceBlockingStub);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<GetMortgageDetailsResponse> loader, GetMortgageDetailsResponse response) {

        int stockId = companySpinner.getSelectedItemPosition() + 1;
        companySpinner.setEnabled(true);

        if (response.getMortgageMapMap().get(stockId) != null)
            stocksMortgaged = response.getMortgageMapMap().get(stockId);
        else
            stocksMortgaged = 0;

        String mortgageString = NO_OF_STOCKS_MORTGAGE_STRING + String.valueOf(stocksMortgaged);
        mortgagedTextView.setText(mortgageString);

        stocksOwned = getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, getCompanyNameFromStockId(stockId));

        String ownedString = NO_OF_STOCKS_OWNED_STRING + String.valueOf(stocksOwned);
        ownedTextView.setText(ownedString);
    }

    @Override
    public void onLoaderReset(Loader<GetMortgageDetailsResponse> loader) {

    }
}