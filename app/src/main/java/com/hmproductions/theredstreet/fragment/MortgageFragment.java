package com.hmproductions.theredstreet.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    @BindView(R.id.currentPrice_textView)
    TextView currentPriceTextView;

    @BindView(R.id.mortgageDeposit_textView)
    TextView mortgageDepositTextView;

    @BindView(R.id.mortgageRetrieve_textView)
    TextView mortgageRetrieveTextView;

    int stocksOwned = 0, stocksMortgaged = 0, stocksTransaction;
    Spinner companySpinner;
    String [] companiesArray;
    private AlertDialog loadingDialog;

    public MortgageFragment() {
        // Required empty public constructor
    }

    private BroadcastReceiver refreshStockPricesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_STOCK_PRICES_ACTION)) {
                getActivity().getSupportLoaderManager().restartLoader(Constants.MORTGAGE_DETAILS_LOADER_ID, null, MortgageFragment.this);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null){
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            String tempString = "Getting mortgage details...";
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(tempString);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_mortgage, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);
        if (getActivity() != null) getActivity().setTitle("Mortgage Stocks");

        companiesArray = StockUtils.getCompanyNamesArray();

        companySpinner = rootView.findViewById(R.id.mortgage_companies_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.company_spinner_item, companiesArray);
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

        mortgageRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            Button mortgageButton = rootView.findViewById(R.id.mortgage_button);
            mortgageButton.setText(id==R.id.mortgage_radioButton?"MORTGAGE":"RETRIEVE");
        });

        String ownedString = "N/A";
        ownedTextView.setText(ownedString);

        String mortgageString = "N/A";
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

                    String ownedString = " :  " + String.valueOf(stocksOwned);
                    ownedTextView.setText(ownedString);

                    String mortgageString = " :  " + String.valueOf(stocksMortgaged);
                    mortgagedTextView.setText(mortgageString);

                    stocksEditText.setText("");

                } else if (mortgageStocksResponse.getStatusCode().getNumber() == 2) {
                    Toast.makeText(getContext(), "Market is Closed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Inconsistent data server error", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "Insufficient Stocks", Toast.LENGTH_SHORT).show();
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

                    String ownedString = " :  " + String.valueOf(stocksOwned);
                    ownedTextView.setText(ownedString);

                    String mortgageString = " :  " + String.valueOf(stocksMortgaged);
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
        loadingDialog.show();
        companySpinner.setEnabled(false);

        if (getContext() != null) {
            return new MortgageDetailsLoader(getContext(), actionServiceBlockingStub);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<GetMortgageDetailsResponse> loader, GetMortgageDetailsResponse response) {

        loadingDialog.dismiss();

        int stockId = StockUtils.getStockIdFromCompanyName(companiesArray[companySpinner.getSelectedItemPosition()]);
        companySpinner.setEnabled(true);

        if (response.getMortgageMapMap().get(stockId) != null)
            stocksMortgaged = response.getMortgageMapMap().get(stockId);
        else
            stocksMortgaged = 0;

        String mortgageString = " :  " + String.valueOf(stocksMortgaged);
        mortgagedTextView.setText(mortgageString);

        stocksOwned = getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, getCompanyNameFromStockId(stockId));

        String ownedString = " :  " + String.valueOf(stocksOwned);
        ownedTextView.setText(ownedString);

        int currentPrice = StockUtils.getPriceFromStockId(MainActivity.globalStockDetails, stockId);
        String tempString = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(currentPrice);
        currentPriceTextView.setText(tempString);

        tempString = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Constants.MORTGAGE_DEPOSIT_RATE * currentPrice / 100);
        mortgageDepositTextView.setText(tempString);

        tempString = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Constants.MORTGAGE_RETRIEVE_RATE * currentPrice / 100);
        mortgageRetrieveTextView.setText(tempString);
    }

    @Override
    public void onLoaderReset(Loader<GetMortgageDetailsResponse> loader) {
        // Do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    refreshStockPricesReceiver, new IntentFilter(Constants.REFRESH_STOCK_PRICES_ACTION)
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshStockPricesReceiver);
        }
    }
}