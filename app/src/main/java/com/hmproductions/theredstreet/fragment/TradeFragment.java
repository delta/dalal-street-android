package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.PlaceOrderRequest;
import dalalstreet.api.actions.PlaceOrderResponse;

import static com.hmproductions.theredstreet.utils.StockUtils.getOrderTypeFromName;
import static com.hmproductions.theredstreet.utils.StockUtils.getQuantityFromCompanyName;
import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

/* Uses PlaceOrder() to place buy or ask order */
public class TradeFragment extends Fragment {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner companySpinner;

    @BindView(R.id.order_select_spinner)
    MaterialBetterSpinner orderSpinner;

    @BindView(R.id.buySell_progressBar)
    ProgressBar buySellProgressBar;

    @BindView(R.id.radioGroupStock)
    RadioGroup stockRadioGroup;

    @BindView(R.id.noOfStocks_editText)
    EditText noOfStocksEditText;

    @BindView(R.id.orderPrice_editText)
    EditText orderPriceEditText;

    public TradeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView= inflater.inflate(R.layout.fragment_buy_sell, container, false);

        if (getActivity() != null)
            getActivity().setTitle("Buy / Sell");

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);

        ArrayAdapter<String> companiesAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line, StockUtils.getCompanyNamesArray());
        ArrayAdapter<String> orderSelectAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.orderType));

        orderSpinner.setAdapter(orderSelectAdapter);
        companySpinner.setAdapter(companiesAdapter);

        orderSpinner.setOnItemClickListener((aV, view, i, l) -> orderPriceEditText.setEnabled(!aV.getItemAtPosition(i).toString().equals("Marker order")));

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
        } else if (orderPriceEditText.isEnabled() && orderPriceEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Enter the order price", Toast.LENGTH_SHORT).show();
        } else if (stockRadioGroup.getCheckedRadioButtonId() == R.id.ask_radioButton) {
            int validQuantity = getQuantityFromCompanyName(MainActivity.ownedStockDetails, companySpinner.getText().toString());
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

    public void addTransaction(){

        buySellProgressBar.setVisibility(View.VISIBLE);

        PlaceOrderResponse orderResponse = actionServiceBlockingStub.placeOrder(
                PlaceOrderRequest
                        .newBuilder()
                        .setIsAsk(stockRadioGroup.getCheckedRadioButtonId() == R.id.ask_radioButton)
                        .setStockId(getStockIdFromCompanyName(companySpinner.getText().toString()))
                        .setOrderType(getOrderTypeFromName(orderSpinner.getText().toString()))
                        .setPrice(Integer.parseInt(orderPriceEditText.getText().toString()))
                        .setStockQuantity(Integer.parseInt(noOfStocksEditText.getText().toString()))
                        .build());

        buySellProgressBar.setVisibility(View.INVISIBLE);

        switch (orderResponse.getStatusCode().getNumber()) {
            case 0:
                Toast.makeText(getContext(), "Transaction added", Toast.LENGTH_SHORT).show();
                break;

            case 1:
                Toast.makeText(getContext(), "Internal server error", Toast.LENGTH_SHORT).show();
                break;

            case 2:
                Toast.makeText(getContext(), "Market Closed", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(getContext(), "Limit exceeded", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
