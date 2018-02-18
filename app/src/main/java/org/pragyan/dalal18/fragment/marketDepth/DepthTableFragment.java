package org.pragyan.dalal18.fragment.marketDepth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.MarketDepthRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.MarketDepth;
import org.pragyan.dalal18.loaders.CompanyProfileLoader;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.StockUtils;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import static org.pragyan.dalal18.utils.StockUtils.getStockIdFromCompanyName;

public class DepthTableFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetCompanyProfileResponse> {

    private static final String COMPANY_NAME_KEY = "company-name-key";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @BindView(R.id.company_spinner)
    MaterialBetterSpinner companiesSpinner;

    @BindView(R.id.bid_depth_layout)
    LinearLayout bidDepthLayout;

    @BindView(R.id.bid_depth_rv)
    RecyclerView bidDepthRv;

    @BindView(R.id.ask_depth_layout)
    LinearLayout askDepthLayout;

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

    @BindView(R.id.depth_table_holder)
    TextView depthTableTv;

    @BindView(R.id.bid_depth_holder)
    TextView bidDepthHolderTv;

    @BindView(R.id.ask_depth_holder)
    TextView askDepthHolderTv;

    String currentCompany;
    ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    ArrayList<MarketDepth> bidArrayList, askArrayList;

    MarketDepthRecyclerAdapter bidDepthAdapter, askDepthAdapter;
    private SubscriptionId subscriptionId = null, prevSubscriptionId = null;
    AlertDialog loadingDialog;

    private BroadcastReceiver refreshMarketDepth = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && isAdded()) {
                if (askArrayList.size() == 0) {
                    askDepthHolderTv.setVisibility(View.VISIBLE);
                } else if (askArrayList.size() > 0) {
                    askDepthHolderTv.setVisibility(View.GONE);
                }

                if (bidArrayList.size() == 0) {
                    bidDepthHolderTv.setVisibility(View.VISIBLE);
                } else if (bidArrayList.size() > 0) {
                    bidDepthHolderTv.setVisibility(View.GONE);
                }
                askDepthLayout.setVisibility(View.VISIBLE);
                bidDepthLayout.setVisibility(View.VISIBLE);
                depthTableTv.setVisibility(View.INVISIBLE);
                sortList(bidArrayList);
                Collections.reverse(bidArrayList);
                sortList(askArrayList);
                bidDepthAdapter.swapData(bidArrayList);
                askDepthAdapter.swapData(askArrayList);
                loadingDialog.dismiss();
            }
        }
    };

    public DepthTableFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_depth_table, container, false);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Market Depth");
        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_depth);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.company_spinner_item, StockUtils.getCompanyNamesArray());
        companiesSpinner.setAdapter(arrayAdapter);

        bidArrayList = new ArrayList<>();
        askArrayList = new ArrayList<>();

        bidDepthRv.setLayoutManager(new LinearLayoutManager(getContext()));
        bidDepthRv.setHasFixedSize(false);
        bidDepthAdapter = new MarketDepthRecyclerAdapter(getContext(), bidArrayList);
        bidDepthRv.setAdapter(bidDepthAdapter);
        bidDepthRv.setNestedScrollingEnabled(false);

        askDepthRv.setLayoutManager(new LinearLayoutManager(getContext()));
        askDepthRv.setHasFixedSize(false);
        askDepthAdapter = new MarketDepthRecyclerAdapter(getContext(), askArrayList);
        askDepthRv.setAdapter(askDepthAdapter);
        askDepthRv.setNestedScrollingEnabled(false);

        companiesSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            currentCompany = companiesSpinner.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString(COMPANY_NAME_KEY, currentCompany);
            bidArrayList.clear();
            askArrayList.clear();
            getValues(currentCompany);
            unsubscribe(prevSubscriptionId);
            currentStockLayout.setVisibility(View.INVISIBLE);
            askDepthLayout.setVisibility(View.INVISIBLE);
            bidDepthLayout.setVisibility(View.INVISIBLE);
            if (getActivity() != null && isAdded()) {
                loadingDialog.show();
                getActivity().getSupportLoaderManager().restartLoader(Constants.COMPANY_PROFILE_LOADER_ID, bundle, this);
            }
        });

        return rootView;
    }

    public void getValues(String currentCompany) {

        prevSubscriptionId = subscriptionId;
        new Thread(() -> streamServiceStub.subscribe(
                SubscribeRequest
                        .newBuilder()
                        .setDataStreamType(DataStreamType.MARKET_DEPTH)
                        .setDataStreamId(String.valueOf(getStockIdFromCompanyName(currentCompany)))
                        .build(),
                new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0) {
                            subscriptionId = value.getSubscriptionId();
                            getMarketDepth(subscriptionId);
                        } else {
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
                })).start();
    }

    private void getMarketDepth(SubscriptionId subscriptionId) {
        streamServiceStub.getMarketDepthUpdates(subscriptionId,
                new StreamObserver<MarketDepthUpdate>() {
                    @Override
                    public void onNext(MarketDepthUpdate value) {

                        for (Map.Entry<Integer, Integer> map : value.getBidDepthMap().entrySet()) {
                            if(map.getKey() > 0 ){
                                int price = map.getKey();
                                int volume = map.getValue();
                                if(price == 0){
                                    price = Integer.MAX_VALUE;
                                }
                                MarketDepth temp = new MarketDepth(price, volume);
                                bidArrayList.add(temp);
                            }
                        }


                        for (Map.Entry<Integer, Integer> map : value.getAskDepthMap().entrySet()) {
                            if(map.getKey() > 0){
                                MarketDepth temp = new MarketDepth(map.getKey(), map.getValue());
                                askArrayList.add(temp);
                            }
                        }

                        for (Map.Entry<Integer, Integer> map : value.getBidDepthDiffMap().entrySet()) {
                            int price = map.getKey();
                            int volume = map.getValue();
                            if(price == 0){
                                price = Integer.MAX_VALUE;
                            }
                            if (!containsBid(price, volume) && price>0) {
                                bidArrayList.add(new MarketDepth(price, volume));
                            }
                        }

                        for (Map.Entry<Integer, Integer> map : value.getAskDepthDiffMap().entrySet()) {
                            int price = map.getKey();
                            int volume = map.getValue();
                            if (!containsAsk(price, volume) && price>0) {
                                askArrayList.add(new MarketDepth(price, volume));
                            }
                        }
                        Intent marketDepthIntent = new Intent(Constants.REFRESH_MARKET_DEPTH);

                        if (getContext() != null)
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
        unsubscribe(subscriptionId);
    }

    public void unsubscribe(SubscriptionId prevSubscriptionId) {

        new Handler().post(() -> {
            if (prevSubscriptionId != null) {
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
        });
    }

    @Override
    public Loader<GetCompanyProfileResponse> onCreateLoader(int id, Bundle args) {

        if (getContext() != null)
            return new CompanyProfileLoader(getContext(), actionServiceBlockingStub
                    , getStockIdFromCompanyName(args.getString(COMPANY_NAME_KEY)));
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetCompanyProfileResponse> loader, GetCompanyProfileResponse companyProfileResponse) {

        if (companyProfileResponse == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        if (getActivity() != null && isAdded()) {
            Stock currentStock = companyProfileResponse.getStockDetails();

            int currentPrice = currentStock.getCurrentPrice();
            int prevDayClose = currentStock.getPreviousDayClose();

            currentStockLayout.setVisibility(View.VISIBLE);
            String currentStockPrice = "Current Stock Price : " + Constants.RUPEE_SYMBOL + String.valueOf(currentPrice);
            currentStockPriceText.setText(currentStockPrice);
            String prevDayClosePrice = Constants.RUPEE_SYMBOL + String.valueOf(Math.abs(currentPrice - prevDayClose));
            prevDayCloseText.setText(String.valueOf(prevDayClosePrice));
            if (currentPrice >= prevDayClose) {
                arrowImage.setImageResource(R.drawable.up_arrow);
            } else {
                arrowImage.setImageResource(R.drawable.down_arrow);
            }
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
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshMarketDepth, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshMarketDepth);
        }
    }

    boolean containsBid(int price, int newVolume) {
        for (int i = 0; i < bidArrayList.size(); i++) {
            if (bidArrayList.get(i).getPrice() == price) {
                int tempVol = bidArrayList.get(i).getVolume() + newVolume;
                bidArrayList.set(i, new MarketDepth(price, tempVol));
                return true;
            }
        }
        return false;
    }

    boolean containsAsk(int price, int newVolume) {
        for (int i = 0; i < askArrayList.size(); i++) {
            if (askArrayList.get(i).getPrice() == price) {
                int tempVol = askArrayList.get(i).getVolume() + newVolume;
                askArrayList.set(i, new MarketDepth(price, tempVol));
                return true;
            }
        }
        return false;
    }

    private void sortList(List<MarketDepth> list) {
        Collections.sort(list, (val1, val2) -> {

            Integer price1 = val1.getPrice();
            Integer price2 = val2.getPrice();
            return price1.compareTo(price2);
        });
    }
}