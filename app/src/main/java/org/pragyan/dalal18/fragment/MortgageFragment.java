package org.pragyan.dalal18.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.ui.MainActivity;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.StockUtils;

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

import static org.pragyan.dalal18.utils.StockUtils.getCompanyNameFromStockId;
import static org.pragyan.dalal18.utils.StockUtils.getQuantityOwnedFromCompanyName;

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

    @BindView(R.id.mortgageRate_textView)
    TextView mortgageRateTextView;

    @BindView(R.id.mortgageRateText_textView)
    TextView mortgageRateTextTextView;

    @BindView(R.id.depositPrice_textView)
    TextView depositPriceTextView;

    @BindView(R.id.depositPriceText_textView)
    TextView depositPriceTextTextView;

    @BindView(R.id.mortgage_button)
    Button mortgageButton;

    int stocksOwned = 0, stocksMortgaged = 0, stocksTransaction, lastStockId = 1;
    Spinner companySpinner;
    String [] companiesArray;

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

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
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext()) ).build().inject(this);
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
            int currentPrice = StockUtils.getPriceFromStockId(MainActivity.Companion.getGlobalStockDetails(), lastStockId);

            if (id == R.id.mortgage_radioButton) {
                mortgageButton.setText(R.string.mortgage_uppercase);
                mortgageRateTextTextView.setText(R.string.mortgage_deposit_rate);

                String temp = " :  " + String.valueOf(Constants.MORTGAGE_DEPOSIT_RATE) + "%";
                mortgageRateTextView.setText(temp);

                depositPriceTextTextView.setText(R.string.deposit_rate_per_stock);

                temp = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Constants.MORTGAGE_DEPOSIT_RATE * currentPrice / 100);
                depositPriceTextView.setText(temp);

            } else {
                mortgageButton.setText(R.string.retrieve_uppercase);
                mortgageRateTextTextView.setText(R.string.mortgage_retrival_rate);

                depositPriceTextTextView.setText(R.string.retrieval_rate_per_stock);

                String temp = " :  " + String.valueOf(Constants.MORTGAGE_RETRIEVE_RATE) + "%";
                mortgageRateTextView.setText(temp);

                temp = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Constants.MORTGAGE_RETRIEVE_RATE * currentPrice / 100);
                depositPriceTextView.setText(temp);
            }
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

                } else {
                    Toast.makeText(getContext(), mortgageStocksResponse.getStatusMessage(), Toast.LENGTH_SHORT).show();
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

                } else {
                    Toast.makeText(getContext(), retrieveStocksResponse.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "You don't have sufficient stocks mortgaged", Toast.LENGTH_SHORT).show();
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
//            return new MortgageDetailsLoader(getContext(), actionServiceBlockingStub);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<GetMortgageDetailsResponse> loader, GetMortgageDetailsResponse response) {

        loadingDialog.dismiss();

        if (response == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        int stockId = StockUtils.getStockIdFromCompanyName(companiesArray[companySpinner.getSelectedItemPosition()]);
        companySpinner.setEnabled(true);

        if (response.getMortgageMapMap().get(stockId) != null)
            stocksMortgaged = response.getMortgageMapMap().get(stockId);
        else
            stocksMortgaged = 0;

        String mortgageString = " :  " + String.valueOf(stocksMortgaged);
        mortgagedTextView.setText(mortgageString);

        stocksOwned = getQuantityOwnedFromCompanyName(MainActivity.Companion.getOwnedStockDetails(), getCompanyNameFromStockId(stockId));

        String ownedString = " :  " + String.valueOf(stocksOwned);
        ownedTextView.setText(ownedString);

        lastStockId = stockId;
        int currentPrice = StockUtils.getPriceFromStockId(MainActivity.Companion.getGlobalStockDetails(), stockId);

        String tempString = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(currentPrice);
        currentPriceTextView.setText(tempString);

        if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.mortgage_radioButton) {
            mortgageButton.setText(R.string.mortgage_uppercase);
            mortgageRateTextTextView.setText(R.string.mortgage_deposit_rate);

            String temp = " :  " + String.valueOf(Constants.MORTGAGE_DEPOSIT_RATE) + "%";
            mortgageRateTextView.setText(temp);

            depositPriceTextTextView.setText(R.string.deposit_rate_per_stock);

            temp = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Constants.MORTGAGE_DEPOSIT_RATE * currentPrice / 100);
            depositPriceTextView.setText(temp);

        } else if (mortgageRadioGroup.getCheckedRadioButtonId() == R.id.retrieve_radioButton){
            mortgageButton.setText(R.string.retrieve_uppercase);
            mortgageRateTextTextView.setText(R.string.mortgage_retrival_rate);

            depositPriceTextTextView.setText(R.string.retrieval_rate_per_stock);

            String temp = " :  " + String.valueOf(Constants.MORTGAGE_RETRIEVE_RATE) + "%";
            mortgageRateTextView.setText(temp);

            temp = " :  " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Constants.MORTGAGE_RETRIEVE_RATE * currentPrice / 100);
            depositPriceTextView.setText(temp);
        }
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