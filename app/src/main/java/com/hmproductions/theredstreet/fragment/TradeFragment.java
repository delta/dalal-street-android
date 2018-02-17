package com.hmproductions.theredstreet.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.loaders.TradeLoader;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.PlaceOrderRequest;
import dalalstreet.api.actions.PlaceOrderResponse;

import static com.hmproductions.theredstreet.utils.Constants.TRADE_LOADER_ID;
import static com.hmproductions.theredstreet.utils.StockUtils.getOrderTypeFromName;
import static com.hmproductions.theredstreet.utils.StockUtils.getQuantityOwnedFromCompanyName;
import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

/* Uses PlaceOrder() to place buy or ask order */
public class TradeFragment extends Fragment implements LoaderManager.LoaderCallbacks<PlaceOrderResponse>{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner companySpinner;

    @BindView(R.id.order_select_spinner)
    MaterialBetterSpinner orderSpinner;

    @BindView(R.id.radioGroupStock)
    RadioGroup stockRadioGroup;

    @BindView(R.id.noOfStocks_editText)
    EditText noOfStocksEditText;

    @BindView(R.id.orderPrice_editText)
    EditText orderPriceEditText;

    @BindView(R.id.currentStockPrice_textView)
    TextView currentPriceTextView;

    @BindView(R.id.stocksOwned_textView)
    TextView stocksOwnedTextView;

    private AlertDialog loadingDialog;
    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    public TradeFragment() {
        // Required empty public constructor
    }

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
            String tempString = "Placing Order...";
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(tempString);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView= inflater.inflate(R.layout.fragment_trade, container, false);

        if (getActivity() != null)
            getActivity().setTitle("Trade");

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);

        ArrayAdapter<String> companiesAdapter = new ArrayAdapter<>(getActivity(), R.layout.order_spinner_item, StockUtils.getCompanyNamesArray());
        ArrayAdapter<String> orderSelectAdapter = new ArrayAdapter<>(getActivity(), R.layout.order_spinner_item,getResources().getStringArray(R.array.orderType));

        orderSpinner.setAdapter(orderSelectAdapter);
        companySpinner.setAdapter(companiesAdapter);

        stockRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            Button bidAskButton = rootView.findViewById(R.id.bidAsk_button);
            bidAskButton.setText(id==R.id.bid_radioButton?"BID":"ASK");
        });

        companySpinner.setOnItemClickListener((adapterView, view, position, l) -> {
            int stocksOwned = StockUtils.getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, companySpinner.getText().toString());
            String tempString = " :  " + String.valueOf(stocksOwned);
            stocksOwnedTextView.setText(tempString);

            tempString = " : " + Constants.RUPEE_SYMBOL + " " +
                    String.valueOf(StockUtils.getPriceFromStockId(MainActivity.globalStockDetails, StockUtils.getStockIdFromCompanyName(companySpinner.getText().toString())));
            currentPriceTextView.setText(tempString);
        });

        orderSpinner.setOnItemClickListener((aV, view, i, l) -> {
            if (aV.getItemAtPosition(i).toString().equals("Market Order")) {
                orderPriceEditText.setVisibility(View.GONE);
            } else {
                orderPriceEditText.setVisibility(View.VISIBLE);
            }
        });

        return rootView;
    }

    @OnClick(R.id.bidAsk_button)
    void onBidAskButtonClick() {

        if(companySpinner.getText().toString().trim().isEmpty()){
            Toast.makeText(getActivity(), "Select a company", Toast.LENGTH_SHORT).show();
        }
        else if(orderSpinner.getText().toString().trim().isEmpty()){
            Toast.makeText(getActivity(), "Select an Order", Toast.LENGTH_SHORT).show();
        }
        else if(noOfStocksEditText.getText().toString().trim().isEmpty()){
            Toast.makeText(getActivity(), "Enter the number of stocks", Toast.LENGTH_SHORT).show();
        }
        else if (stockRadioGroup.getCheckedRadioButtonId() == -1){
            Toast.makeText(getActivity(), "Select order type", Toast.LENGTH_SHORT).show();
        } else if (orderPriceEditText.getVisibility() == View.VISIBLE && orderPriceEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Enter the order price", Toast.LENGTH_SHORT).show();
        } else if (stockRadioGroup.getCheckedRadioButtonId() == R.id.ask_radioButton) {
            int validQuantity = getQuantityOwnedFromCompanyName(MainActivity.ownedStockDetails, companySpinner.getText().toString());
            int askingQuantity = Integer.parseInt(noOfStocksEditText.getText().toString());

            if (askingQuantity > validQuantity) {
                Toast.makeText(getContext(), "You don't have sufficient stocks", Toast.LENGTH_SHORT).show();
            } else {
                addTransaction();
            }
        }
        else{
            addTransaction();
        }
    }

    private void addTransaction() {
        if (getActivity() != null)
            getActivity().getSupportLoaderManager().restartLoader(TRADE_LOADER_ID, null, this);
    }

    @Override
    public Loader<PlaceOrderResponse> onCreateLoader(int id, Bundle args) {

        loadingDialog.show();

        int price = orderPriceEditText.getVisibility()==View.GONE?0:Integer.parseInt(orderPriceEditText.getText().toString());
        PlaceOrderRequest orderRequest = PlaceOrderRequest
                .newBuilder()
                .setIsAsk(stockRadioGroup.getCheckedRadioButtonId() == R.id.ask_radioButton)
                .setStockId(getStockIdFromCompanyName(companySpinner.getText().toString()))
                .setOrderType(getOrderTypeFromName(orderSpinner.getText().toString()))
                .setPrice(price)
                .setStockQuantity(Integer.parseInt(noOfStocksEditText.getText().toString()))
                .build();

        if (getContext() != null)
            return new TradeLoader(getContext(), actionServiceBlockingStub, orderRequest);

        return null;
    }

    @Override
    public void onLoadFinished(Loader<PlaceOrderResponse> loader, PlaceOrderResponse orderResponse) {

        loadingDialog.dismiss();

        if (orderResponse == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        if (orderResponse.getStatusCodeValue() == 0) {
            Toast.makeText(getContext(), "Order Placed", Toast.LENGTH_SHORT).show();
            noOfStocksEditText.setText("");
            orderPriceEditText.setText("");
        } else {
            Toast.makeText(getContext(), orderResponse.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<PlaceOrderResponse> loader) {
        // Do nothing
    }
}