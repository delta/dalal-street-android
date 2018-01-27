package com.hmproductions.theredstreet.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.MarketDepthRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.loaders.CompanyProfileLoader;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;
import com.hmproductions.theredstreet.data.MarketDepth;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.actions.GetCompanyProfileResponse;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.MarketDepthUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.datastreams.UnsubscribeRequest;
import dalalstreet.api.datastreams.UnsubscribeResponse;
import dalalstreet.api.models.Stock;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.theredstreet.utils.StockUtils.getStockIdFromCompanyName;

public class MarketDepthFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetCompanyProfileResponse> {

    private static final String COMPANY_NAME_KEY = "company-name-key";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner companiesSpinner;

    @BindView(R.id.bid_depth_card)
    CardView bidDepthCard;

    @BindView(R.id.bid_depth_rv)
    RecyclerView bidDepthRv;

    @BindView(R.id.ask_depth_card)
    CardView askDepthCard;

    @BindView(R.id.ask_depth_rv)
    RecyclerView askDepthRv;

    @BindView(R.id.current_stock_price_layout)
    RelativeLayout currentStockLayout;

    @BindView(R.id.prev_day_close_stock_price)
    TextView prevDayCloseText;

    @BindView(R.id.current_stock_price_textView)
    TextView currentStockPriceText;

    @BindView(R.id.arrow_image_view)
    ImageView arrowImage;

    String currentCompany;

    ArrayList<MarketDepth> bidArrayList,askArrayList;

    MarketDepthRecyclerAdapter bidDepthAdapter,askDepthAdapter;
    private SubscriptionId subscriptionId = null,prevSubscriptionId = null;
    AlertDialog loadingDialog;

    private BroadcastReceiver refreshMarketDepth = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            askDepthCard.setVisibility(View.VISIBLE);
            bidDepthCard.setVisibility(View.VISIBLE);
            bidDepthAdapter.swapData(bidArrayList);
            askDepthAdapter.swapData(askArrayList);
            loadingDialog.dismiss();
        }
    };

    public MarketDepthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_market_depth, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Market Depth");
        if (getContext() != null){
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.fetching_depth);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line, StockUtils.getCompanyNamesArray());
        companiesSpinner.setAdapter(arrayAdapter);

        bidArrayList = new ArrayList<>();
        askArrayList = new ArrayList<>();

        bidDepthRv.setLayoutManager(new LinearLayoutManager(getContext()));
        bidDepthRv.setHasFixedSize(false);
        bidDepthAdapter = new MarketDepthRecyclerAdapter(getContext(),bidArrayList);
        bidDepthRv.setAdapter(bidDepthAdapter);
        bidDepthRv.setNestedScrollingEnabled(false);

        askDepthRv.setLayoutManager(new LinearLayoutManager(getContext()));
        askDepthRv.setHasFixedSize(false);
        askDepthAdapter = new MarketDepthRecyclerAdapter(getContext(),askArrayList);
        askDepthRv.setAdapter(askDepthAdapter);
        askDepthRv.setNestedScrollingEnabled(false);

        companiesSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            currentCompany = companiesSpinner.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString(COMPANY_NAME_KEY, currentCompany);
            bidArrayList.clear();
            askArrayList.clear();
            getValues(currentCompany);
            unSubscribe(prevSubscriptionId);
            currentStockLayout.setVisibility(View.INVISIBLE);
            askDepthCard.setVisibility(View.INVISIBLE);
            bidDepthCard.setVisibility(View.INVISIBLE);
            loadingDialog.show();
            getActivity().getSupportLoaderManager().restartLoader(Constants.COMPANY_PROFILE_LOADER_ID, bundle, this);
        });



        return rootView;
    }

    public void getValues(String currentCompany) {

        prevSubscriptionId = subscriptionId;
        streamServiceStub.subscribe(
                SubscribeRequest
                        .newBuilder()
                        .setDataStreamType(DataStreamType.MARKET_DEPTH)
                        .setDataStreamId(String.valueOf(getStockIdFromCompanyName(currentCompany)))
                        .build(), new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0){
                            subscriptionId = value.getSubscriptionId();
                            getMarketDepth(subscriptionId);
                        } else{
                            Toast.makeText(getContext(), "Server internal error", Toast.LENGTH_SHORT).show();
                        }

                        onCompleted();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void getMarketDepth(SubscriptionId subscriptionId) {

        streamServiceStub
                .getMarketDepthUpdates(subscriptionId, new StreamObserver<MarketDepthUpdate>() {
            @Override
            public void onNext(MarketDepthUpdate value) {

                for(Map.Entry<Integer,Integer> map : value.getBidDepthMap().entrySet()){
                    MarketDepth temp = new MarketDepth(map.getKey(),map.getValue());
                    Log.e("SAN", "bid  : " + map.getKey() + "  " + map.getValue());
                    bidArrayList.add(temp);
                }

                for(Map.Entry<Integer,Integer> map : value.getAskDepthMap().entrySet()){
                    MarketDepth temp = new MarketDepth(map.getKey(),map.getValue());
                    askArrayList.add(temp);
                }

                for(Map.Entry<Integer,Integer> map : value.getBidDepthDiffMap().entrySet()){
                    int price = map.getKey();
                    int volume = map.getValue();
                    if(!containsBid(price,volume)){
                        bidArrayList.add(new MarketDepth(price,volume));
                    }
                }

                for(Map.Entry<Integer,Integer> map : value.getAskDepthDiffMap().entrySet()){
                    int price = map.getKey();
                    int volume = map.getValue();
                    if(!containsAsk(price,volume)){
                        askArrayList.add(new MarketDepth(price,volume));
                    }
                }
                Intent marketDepthIntent = new Intent(Constants.REFRESH_MARKET_DEPTH);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(marketDepthIntent);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {


            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unSubscribe(subscriptionId);
    }

    public void unSubscribe(SubscriptionId prevSubscriptionId){

        if(prevSubscriptionId != null){
            streamServiceStub.unsubscribe(
                    UnsubscribeRequest.newBuilder()
                            .setSubscriptionId(prevSubscriptionId)
                            .build(),
                    new StreamObserver<UnsubscribeResponse>() {
                        @Override
                        public void onNext(UnsubscribeResponse value) {
                            if (!(value.getStatusCode().getNumber() == 0))
                                Toast.makeText(getContext(), "Server internal error", Toast.LENGTH_SHORT).show();
                            onCompleted();
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onCompleted() {

                        }
                    });

        }

    }

    @Override
    public Loader<GetCompanyProfileResponse> onCreateLoader(int id, Bundle args) {

        if (getContext() != null)
            return new CompanyProfileLoader(getContext(), actionServiceBlockingStub, getStockIdFromCompanyName(args.getString(COMPANY_NAME_KEY)));
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetCompanyProfileResponse> loader, GetCompanyProfileResponse companyProfileResponse) {

        Stock currentStock = companyProfileResponse.getStockDetails();

        int currentPrice = currentStock.getCurrentPrice();
        int prevDayClose = currentStock.getPreviousDayClose();

        currentStockLayout.setVisibility(View.VISIBLE);
        String currentStockPrice = "Current Stock Price : " + Constants.RUPEE_SYMBOL + String.valueOf(currentPrice);
        currentStockPriceText.setText(currentStockPrice);
        String prevDayClosePrice = Constants.RUPEE_SYMBOL + String.valueOf(prevDayClose);
        prevDayCloseText.setText(String.valueOf(prevDayClosePrice));
        if(currentPrice >= prevDayClose){
            arrowImage.setImageResource(R.drawable.up_arrow);
        }else {
            arrowImage.setImageResource(R.drawable.down_arrow);
        }

    }

    @Override
    public void onLoaderReset(Loader<GetCompanyProfileResponse> loader) {
        // Do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.REFRESH_MARKET_DEPTH);
        if(getContext() != null){
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshMarketDepth, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null){
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshMarketDepth);
        }
    }

    boolean containsBid(int price, int newVolume) {
        for(int i=0 ; i<bidArrayList.size() ; i++){
            if(bidArrayList.get(i).getPrice() == price){
                int tempVol = bidArrayList.get(i).getVolume() + newVolume;
                bidArrayList.set(i,new MarketDepth(price,tempVol));
                return true;
            }
        }
        return false;
    }

    boolean containsAsk(int price,int newVolume) {
        for(int i=0 ; i<askArrayList.size() ; i++){
            if(askArrayList.get(i).getPrice() == price){
                int tempVol = askArrayList.get(i).getVolume() + newVolume;
                askArrayList.set(i,new MarketDepth(price,tempVol));
                return true;
            }
        }
        return false;
    }

}