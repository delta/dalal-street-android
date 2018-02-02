package com.hmproductions.theredstreet.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.StockDetails;
import com.hmproductions.theredstreet.data.Subscription;
import com.hmproductions.theredstreet.data.Subscription.SubscriptionType;
import com.hmproductions.theredstreet.fragment.CompanyFragment;
import com.hmproductions.theredstreet.fragment.HomeFragment;
import com.hmproductions.theredstreet.fragment.LeaderboardFragment;
import com.hmproductions.theredstreet.fragment.MarketDepthFragment;
import com.hmproductions.theredstreet.fragment.MortgageFragment;
import com.hmproductions.theredstreet.fragment.NewsFragment;
import com.hmproductions.theredstreet.fragment.OrdersFragment;
import com.hmproductions.theredstreet.fragment.PortfolioFragment;
import com.hmproductions.theredstreet.fragment.StockExchangeFragment;
import com.hmproductions.theredstreet.fragment.TradeFragment;
import com.hmproductions.theredstreet.fragment.TransactionsFragment;
import com.hmproductions.theredstreet.loaders.SubscriptionLoader;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.MiscellaneousUtils;
import com.hmproductions.theredstreet.utils.StockUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.actions.LogoutRequest;
import dalalstreet.api.actions.LogoutResponse;
import dalalstreet.api.datastreams.MarketEventUpdate;
import dalalstreet.api.datastreams.StockExchangeDataPoint;
import dalalstreet.api.datastreams.StockExchangeUpdate;
import dalalstreet.api.datastreams.StockPricesUpdate;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.datastreams.TransactionUpdate;
import dalalstreet.api.datastreams.UnsubscribeRequest;
import dalalstreet.api.models.TransactionType;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.theredstreet.ui.LoginActivity.EMAIL_KEY;
import static com.hmproductions.theredstreet.ui.LoginActivity.PASSWORD_KEY;

/* Subscribes to GetTransactions*/
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<List<Subscription>>{

    private static final long DRAWER_DURATION = 450;
    public static final String CASH_WORTH_KEY = "cash-worth-key";
    public static final String TOTAL_WORTH_KEY = "total-worth-key";
    public static final String STOCKS_OWNED_KEY = "stocks-owned-key";
    public static final String GLOBAL_STOCKS_KEY = "global-stocks-key";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceStub streamServiceStub;

    @Inject
    DalalStreamServiceGrpc.DalalStreamServiceBlockingStub streamServiceBlockingStub;

    @Inject
    SharedPreferences preferences;

    private TextView usernameTextView;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    public static List<StockDetails> ownedStockDetails;
    public static List<GlobalStockDetails> globalStockDetails;
    private List<SubscriptionId> subscriptionIds = new ArrayList<>();

    @BindView(R.id.stockWorth_textView)
    TextView stockTextView;

    @BindView(R.id.cashWorth_textView)
    TextView cashTextView;

    @BindView(R.id.totalWorth_textView)
    TextView totalTextView;

    @BindView(R.id.home_toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_edge_button)
    Button drawerEdgeButton;

    private BroadcastReceiver refreshCashStockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Constants.REFRESH_WORTH_TEXTVIEW_ACTION)) {
                changeTextViewValue(stockTextView, intent.getIntExtra(TOTAL_WORTH_KEY, 0), false);
                changeTextViewValue(cashTextView, intent.getIntExtra(TOTAL_WORTH_KEY, 0), true);
            } else if (intent.getAction().equals(Constants.REFRESH_DIVIDEND_ACTION)) {
                changeTextViewValue(totalTextView, intent.getIntExtra(TOTAL_WORTH_KEY, 0), true);
                changeTextViewValue(cashTextView, intent.getIntExtra(TOTAL_WORTH_KEY, 0), true);
            } else if (intent.getAction().equals(Constants.UPDATE_WORTH_VIA_STREAM_ACTION)) {
                updateStockWorthViaStreamUpdates();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BindDrawerViews();
        SetupNavigationDrawer();

        OpenAndCloseDrawer();

        getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new HomeFragment()).commit();

        ownedStockDetails = getIntent().getParcelableArrayListExtra(STOCKS_OWNED_KEY);
        globalStockDetails = getIntent().getParcelableArrayListExtra(GLOBAL_STOCKS_KEY);
        StockUtils.createCompanyArrayFromGlobalStockDetails();

        updateValues();

        getSupportLoaderManager().restartLoader(Constants.SUBSCRIPTION_LOADER, null, this);

        StartMakingButtonsTransparent();
        updateStockWorthViaStreamUpdates();
    }

    private void BindDrawerViews() {

        // Binding view for drawer navigation
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationViewLeft);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        // Binding views for display name and email */
        usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.username_textView);
    }

    // Adding and setting up Navigation drawer
    private void SetupNavigationDrawer() {

        usernameTextView.setText(getIntent().getStringExtra(LoginActivity.USERNAME_KEY));
        MiscellaneousUtils.username = getIntent().getStringExtra(LoginActivity.USERNAME_KEY);

        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Opening and closing drawer for UX
    private void OpenAndCloseDrawer() {
        drawerLayout.openDrawer(GravityCompat.START, true);
        new Handler().postDelayed(() -> drawerLayout.closeDrawer(GravityCompat.START, true), DRAWER_DURATION);
    }

    @OnClick(R.id.drawer_edge_button)
    void onDrawerButtonClick() {
        drawerLayout.openDrawer(GravityCompat.START, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setCustomTitle(getLayoutInflater().inflate(R.layout.help_title, null))
                        .setView(getLayoutInflater().inflate(R.layout.help_box, null))
                        .setCancelable(true)
                        .show();
                return true;

            case R.id.action_logout:
                new AlertDialog.Builder(this)
                        .setMessage("Do you want to logout ?")
                        .setPositiveButton("Logout", (dialogInterface, i) -> logout())
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                        .setTitle("Confirm Logout")
                        .setCancelable(true)
                        .show();

                return true;

            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_exchange:
                fragment = new StockExchangeFragment();
                break;
            case R.id.nav_company_profile:
                fragment = new MarketDepthFragment();
                break;
            case R.id.nav_news:
                fragment = new NewsFragment();
                break;
            case R.id.nav_buy_sell:
                fragment = new TradeFragment();
                break;
            case R.id.nav_mortgage:
                fragment = new MortgageFragment();
                break;
            case R.id.nav_my_orders:
                fragment = new OrdersFragment();
                break;
            case R.id.nav_transactions:
                fragment = new TransactionsFragment();
                break;
            case R.id.nav_portfolio:
                fragment = new PortfolioFragment();
                break;
            case R.id.nav_companies:
                fragment = new CompanyFragment();
                break;
            case R.id.nav_leaderboard:
                fragment = new LeaderboardFragment();
                break;

        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, fragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void logout() {

        LogoutResponse logoutResponse = actionServiceBlockingStub.logout(LogoutRequest.newBuilder().build());

        if (logoutResponse.getStatusCode().getNumber() == 0) {
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();

            preferences
                    .edit()
                    .putString(EMAIL_KEY, null)
                    .putString(PASSWORD_KEY, null)
                    .apply();
        } else {
            Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateValues() {

        int cashWorth = getIntent().getIntExtra(CASH_WORTH_KEY, -1);
        int totalWorth = getIntent().getIntExtra(TOTAL_WORTH_KEY, -1);
        int stockWorth = totalWorth - cashWorth;

        cashTextView.setText(String.valueOf(cashWorth));
        stockTextView.setText(String.valueOf(stockWorth));
        totalTextView.setText(String.valueOf(totalWorth));
    }

    // Subscribes to transaction stream and gets updates (TESTED) TODO : Check order fill stream
    private void subscribeToTransactionsStream(SubscriptionId transactionsSubscriptionId) {

        streamServiceStub.getTransactionUpdates(transactionsSubscriptionId,
                new StreamObserver<TransactionUpdate>() {
                    @Override
                    public void onNext(TransactionUpdate value) {

                        dalalstreet.api.models.Transaction transaction = value.getTransaction();

                        if (transaction.getType() == TransactionType.DIVIDEND_TRANSACTION) {

                            Intent intent = new Intent(Constants.REFRESH_DIVIDEND_ACTION);
                            intent.putExtra(TOTAL_WORTH_KEY, transaction.getTotal());
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                        } else if (transaction.getType() == TransactionType.ORDER_FILL_TRANSACTION) {

                            Log.v(":::", "order fill with total=" + String.valueOf(transaction.getTotal()));
                            updateOwnedStockIdAndQuantity(transaction.getStockId(), Math.abs(transaction.getStockQuantity()), transaction.getStockQuantity() > 0);

                            Intent intent = new Intent(Constants.REFRESH_WORTH_TEXTVIEW_ACTION);
                            intent.putExtra(TOTAL_WORTH_KEY, transaction.getTotal());
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                        } else if (transaction.getType() == TransactionType.FROM_EXCHANGE_TRANSACTION) {

                            updateOwnedStockIdAndQuantity(transaction.getStockId(), transaction.getStockQuantity(), true);

                            Intent intent = new Intent(Constants.REFRESH_WORTH_TEXTVIEW_ACTION);
                            intent.putExtra(TOTAL_WORTH_KEY, transaction.getTotal());
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                        } else if (transaction.getType() == TransactionType.MORTGAGE_TRANSACTION) {

                            updateOwnedStockIdAndQuantity(transaction.getStockId(), transaction.getStockQuantity(), transaction.getStockQuantity() > 0);

                            Intent intent = new Intent(Constants.REFRESH_WORTH_TEXTVIEW_ACTION);
                            intent.putExtra(TOTAL_WORTH_KEY, transaction.getTotal());
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    // Subscribes to market events stream and gets updates (TESTED)
    private void subscribeToMarketEventsUpdateStream(SubscriptionId marketEventsSubscriptionId) {

        streamServiceStub.getMarketEventUpdates(marketEventsSubscriptionId,
                new StreamObserver<MarketEventUpdate>() {
                    @Override
                    public void onNext(MarketEventUpdate value) {
                        Intent refreshNewsIntent = new Intent(Constants.REFRESH_NEWS_ACTION);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(refreshNewsIntent);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    // Subscribes to stock prices stream and gets updates (TESTED)
    private void subscribeToStockPricesStream(SubscriptionId stockPricesSubscriptionId) {
        streamServiceStub.getStockPricesUpdates(stockPricesSubscriptionId,
                new StreamObserver<StockPricesUpdate>() {
                    @Override
                    public void onNext(StockPricesUpdate value) {
                        for (int i = 1; i <= Constants.NUMBER_OF_COMPANIES; ++i) {
                            if (value.getPricesMap().containsKey(i)) {
                                globalStockDetails.get(i - 1).setPrice(value.getPricesMap().get(i));
                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Constants.REFRESH_PRICE_TICKER_ACTION));
                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Constants.REFRESH_STOCK_PRICES_ACTION));
                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Constants.UPDATE_WORTH_VIA_STREAM_ACTION));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    // Subscribes to stock exchange stream and gets updates globalStockDetails (TESTED)
    private void subscribeToStockExchangeStream(SubscriptionId stockExchangeSubscriptionId) {

        streamServiceStub.getStockExchangeUpdates(stockExchangeSubscriptionId,
                new StreamObserver<StockExchangeUpdate>() {
                    @Override
                    public void onNext(StockExchangeUpdate value) {
                        Map<Integer, StockExchangeDataPoint> stockExchangeDataPointMap = value.getStocksInExchangeMap();

                        for (int x = 1; x <= Constants.NUMBER_OF_COMPANIES; ++x) {
                            if (stockExchangeDataPointMap.containsKey(x)) {
                                StockExchangeDataPoint currentDataPoint = value.getStocksInExchangeMap().get(x);

                                int position = -1;
                                for (int i = 0; i < globalStockDetails.size(); ++i) {
                                    if (x == globalStockDetails.get(i).getStockId()) {
                                        position = i;
                                        break;
                                    }
                                }
                                MainActivity.globalStockDetails.get(position).setPrice(currentDataPoint.getPrice());
                                MainActivity.globalStockDetails.get(position).setQuantityInMarket(currentDataPoint.getStocksInMarket());
                                MainActivity.globalStockDetails.get(position).setQuantityInExchange(currentDataPoint.getStocksInExchange());
                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Constants.REFRESH_STOCKS_EXCHANGE_ACTION));
                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Constants.UPDATE_WORTH_VIA_STREAM_ACTION));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    // Method called when user's stock quantity changes (called from Transactions stream update)
    private void updateOwnedStockIdAndQuantity(int stockId, int stockQuantity, boolean increase) {

        boolean isPresentInList = false;

        for (StockDetails currentOwnedStockDetails : ownedStockDetails) {
            if (currentOwnedStockDetails.getStockId() == stockId) {
                int newQuantity;

                if (increase) newQuantity = currentOwnedStockDetails.getQuantity() + stockQuantity;
                else newQuantity = currentOwnedStockDetails.getQuantity() - stockQuantity;

                isPresentInList = true;

                currentOwnedStockDetails.setQuantity(newQuantity);
                break;
            }
        }

        if (!isPresentInList && increase) {
            ownedStockDetails.add(new StockDetails(stockId, stockQuantity));
        }
    }

    // Method is called when stock price update is received
    private void updateStockWorthViaStreamUpdates() {
        int netStockWorth = 0, rate = 0;

        for (StockDetails currentOwnedDetails : ownedStockDetails) {
            for (GlobalStockDetails details : globalStockDetails) {
                if (details.getStockId() == currentOwnedDetails.getStockId()) {
                    rate = details.getPrice();
                    break;
                }
            }
            netStockWorth += currentOwnedDetails.getQuantity() * rate;
        }

        stockTextView.setText(String.valueOf(netStockWorth));
        totalTextView.setText(String.valueOf(netStockWorth + Integer.parseInt(cashTextView.getText().toString())));
    }

    private void changeTextViewValue(TextView textView, int value, boolean increase) {
        int previousValue = Integer.parseInt(textView.getText().toString());
        textView.setText(String.valueOf(previousValue + (increase ? value : -1 * value)));
    }

    // Starts making drawer button translucent
    private void StartMakingButtonsTransparent() {

        new Thread() {
            @Override
            public void run() {
                try {
                    while (drawerEdgeButton.getAlpha() > 0.70) {
                        Thread.sleep(175);
                        runOnUiThread(() -> drawerEdgeButton.setAlpha((float) (drawerEdgeButton.getAlpha() - 0.01)));
                    }
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception");
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Unsubscribes from all the streams subscribed in this activity.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (SubscriptionId currentSubscriptionId : subscriptionIds) {
            streamServiceBlockingStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(currentSubscriptionId).build());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.REFRESH_DIVIDEND_ACTION);
        intentFilter.addAction(Constants.REFRESH_WORTH_TEXTVIEW_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCashStockReceiver, new IntentFilter(intentFilter));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCashStockReceiver);
    }

    @Override
    public Loader<List<Subscription>> onCreateLoader(int id, Bundle args) {
        return new SubscriptionLoader(this, streamServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(Loader<List<Subscription>> loader, List<Subscription> data) {

        for (Subscription currentSubscription : data) {

            subscriptionIds.add(currentSubscription.getSubscriptionId());

            if (currentSubscription.getType() == SubscriptionType.TRANSACTIONS)
                subscribeToTransactionsStream(currentSubscription.getSubscriptionId());

            if (currentSubscription.getType() == SubscriptionType.MARKET_EVENTS)
                subscribeToMarketEventsUpdateStream(currentSubscription.getSubscriptionId());

            if (currentSubscription.getType() == SubscriptionType.STOCK_EXCHANGE)
                subscribeToStockExchangeStream(currentSubscription.getSubscriptionId());

            if (currentSubscription.getType() == SubscriptionType.STOCK_PRICES)
                subscribeToStockPricesStream(currentSubscription.getSubscriptionId());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Subscription>> loader) {
        // Do nothing
    }
}