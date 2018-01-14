package com.hmproductions.theredstreet.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.loaders.CompanyProfileLoader;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.BuyStocksFromExchangeRequest;
import dalalstreet.api.actions.BuyStocksFromExchangeResponse;
import dalalstreet.api.actions.GetCompanyProfileResponse;
import dalalstreet.api.models.Stock;

import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

/* Uses GetCompanyProfile() for getting stock info
*  Uses BuyStocksFromExchange() to buy appropriate stocks */
public class StockExchangeFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetCompanyProfileResponse> {

    private static final String STOCK_ID_KEY = "stock-id-key";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner companySpinner;

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

    @BindView(R.id.stockExchange_progressBar)
    ProgressBar stockExchangeProgressBar;

    private Stock currentStock;
    private int lastPositionClick = -1;

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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, StockUtils.getCompanyNamesArray());
        companySpinner.setAdapter(arrayAdapter);

        companySpinner.setOnItemClickListener((adapterView, view, position, id) -> {
            Bundle bundle = new Bundle();
            bundle.putInt(STOCK_ID_KEY, position + 1);
            lastPositionClick = position;
            getActivity().getSupportLoaderManager().restartLoader(Constants.COMPANY_PROFILE_LOADER_ID, bundle, this);
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

                        if (getActivity() != null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(STOCK_ID_KEY, lastPositionClick + 1);
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
        stockExchangeProgressBar.setVisibility(View.VISIBLE);

        String gettingString = "Getting stocks details for " + StockUtils.getCompanyNameFromStockId(args.getInt(STOCK_ID_KEY)) + "...";

        dailyHighTextView.setText("");
        dailyHighTextView.setText("");
        currentStockPriceTextView.setText(gettingString);
        stocksInMarketTextView.setText("");
        stockInExchangeTextView.setText("");

        if (getContext() != null)
            return new CompanyProfileLoader(getContext(), actionServiceBlockingStub, args.getInt(STOCK_ID_KEY));
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetCompanyProfileResponse> loader, GetCompanyProfileResponse companyProfileResponse) {

        stockExchangeProgressBar.setVisibility(View.GONE);

        String temporaryTextViewString;
        currentStock = companyProfileResponse.getStockDetails();

        temporaryTextViewString = "Current stock price : ₹" + String.valueOf(currentStock.getCurrentPrice());
        currentStockPriceTextView.setText(temporaryTextViewString);

        temporaryTextViewString = "Daily high : ₹" + String.valueOf(currentStock.getDayHigh());
        dailyHighTextView.setText(temporaryTextViewString);

        temporaryTextViewString = "Daily low : ₹" + String.valueOf(currentStock.getDayLow());
        dailyLowTextView.setText(temporaryTextViewString);

        temporaryTextViewString = "Stocks in market : " + String.valueOf(currentStock.getStocksInMarket());
        stocksInMarketTextView.setText(temporaryTextViewString);

        temporaryTextViewString = "Stocks in exchange : " + String.valueOf(currentStock.getStocksInExchange());
        stockInExchangeTextView.setText(temporaryTextViewString);
    }

    @Override
    public void onLoaderReset(Loader<GetCompanyProfileResponse> loader) {
        // Do nothing
    }
}