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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.loaders.CompanyProfileLoader;
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.BuyStocksFromExchangeRequest;
import dalalstreet.api.actions.BuyStocksFromExchangeResponse;
import dalalstreet.api.actions.GetCompanyProfileResponse;
import dalalstreet.api.models.Stock;

/* Uses GetCompanyProfile() for getting stock info
*  Uses BuyStocksFromExchange() to buy appropriate stocks */
public class StockExchangeFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetCompanyProfileResponse> {

    private static final String COMPANY_STOCK_ID_KEY = "company-stock-id-key";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @BindView(R.id.noOfStocks_editText)
    EditText noOfStocksEditText;

    @BindView(R.id.currentStockPrice_textView)
    TextView currentStockPriceTextView;

    @BindView(R.id.dailyHigh_textView)
    TextView dailyHighTextView;

    @BindView(R.id.dailyLow_textView)
    TextView dailyLowTextView;

    @BindView(R.id.stocksInMarket_textView)
    TextView stocksInMarketTextView;

    @BindView(R.id.stocksInExchange_textView)
    TextView stockInExchangeTextView;

    private Stock currentStock;
    private int lastSelectedStockId;
    private AlertDialog loadingDialog;

    Spinner companySpinner;
    String [] companiesArray;

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    private BroadcastReceiver refreshStockPricesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.REFRESH_STOCK_PRICES_ACTION)) {
                Bundle bundle = new Bundle();
                bundle.putInt(COMPANY_STOCK_ID_KEY, lastSelectedStockId);
                getActivity().getSupportLoaderManager().restartLoader(Constants.COMPANY_PROFILE_LOADER_ID, bundle, StockExchangeFragment.this);
            }
        }
    };

    public StockExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down hnadler.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null){
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            String tempString = "Getting stocks details...";
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(tempString);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stock_exchange, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Stock Exchange");
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        companiesArray = StockUtils.getCompanyNamesArray();

        companySpinner = rootView.findViewById(R.id.company_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.company_spinner_item, companiesArray);
        companySpinner.setAdapter(arrayAdapter);

        companySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int stockId = StockUtils.getStockIdFromCompanyName(companiesArray[position]);
                Bundle bundle = new Bundle();
                bundle.putInt(COMPANY_STOCK_ID_KEY, stockId);
                lastSelectedStockId = stockId;
                getActivity().getSupportLoaderManager().restartLoader(Constants.COMPANY_PROFILE_LOADER_ID, bundle, StockExchangeFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootView;
    }

    @OnClick(R.id.buyExchange_button)
    void onBuyExchangeButtonClick() {

        if (noOfStocksEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Enter the number of Stocks", Toast.LENGTH_SHORT).show();
        } else {

            if (!(ConnectionUtils.getConnectionInfo(getContext()))) {
                networkDownHandler.onNetworkDownError();
                return;
            }

            if (Integer.parseInt(noOfStocksEditText.getText().toString().trim()) <= currentStock.getStocksInExchange()) {

                BuyStocksFromExchangeResponse response = actionServiceBlockingStub.buyStocksFromExchange(
                        BuyStocksFromExchangeRequest
                                .newBuilder()
                                .setStockId(lastSelectedStockId)
                                .setStockQuantity(Integer.parseInt(noOfStocksEditText.getText().toString()))
                                .build()
                );

                switch (response.getStatusCode().getNumber()) {

                    case 0:
                        Toast.makeText(getContext(), "Stocks bought", Toast.LENGTH_SHORT).show();

                        if (getActivity() != null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(COMPANY_STOCK_ID_KEY, lastSelectedStockId);
                            getActivity().getSupportLoaderManager().restartLoader(Constants.COMPANY_PROFILE_LOADER_ID, bundle, this);
                        }
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

            } else {
                Toast.makeText(getActivity(), "Insufficient stocks in market", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<GetCompanyProfileResponse> onCreateLoader(int id, Bundle args) {
        loadingDialog.show();

        dailyHighTextView.setText("");
        dailyLowTextView.setText("");
        currentStockPriceTextView.setText("");
        stocksInMarketTextView.setText("");
        stockInExchangeTextView.setText("");

        if (getContext() != null)
            return new CompanyProfileLoader(getContext(), actionServiceBlockingStub, args.getInt(COMPANY_STOCK_ID_KEY));
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetCompanyProfileResponse> loader, GetCompanyProfileResponse companyProfileResponse) {

        loadingDialog.dismiss();

        if (companyProfileResponse == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        String temporaryTextViewString;
        currentStock = companyProfileResponse.getStockDetails();

        temporaryTextViewString = ": ₹" + String.valueOf(currentStock.getCurrentPrice());
        currentStockPriceTextView.setText(temporaryTextViewString);

        temporaryTextViewString = ": ₹" + String.valueOf(currentStock.getDayHigh());
        dailyHighTextView.setText(temporaryTextViewString);

        temporaryTextViewString = ": ₹" + String.valueOf(currentStock.getDayLow());
        dailyLowTextView.setText(temporaryTextViewString);

        temporaryTextViewString = ": " + String.valueOf(currentStock.getStocksInMarket());
        stocksInMarketTextView.setText(temporaryTextViewString);

        temporaryTextViewString = ": " + String.valueOf(currentStock.getStocksInExchange());
        stockInExchangeTextView.setText(temporaryTextViewString);
    }

    @Override
    public void onLoaderReset(Loader<GetCompanyProfileResponse> loader) {
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