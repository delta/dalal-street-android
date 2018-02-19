package org.pragyan.dalal18.ui;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.GlobalStockDetails;
import org.pragyan.dalal18.data.StockDetails;
import org.pragyan.dalal18.data.Subscription;
import org.pragyan.dalal18.data.Subscription.SubscriptionType;
import org.pragyan.dalal18.fragment.CompanyFragment;
import org.pragyan.dalal18.fragment.HomeFragment;
import org.pragyan.dalal18.fragment.LeaderboardFragment;
import org.pragyan.dalal18.fragment.MortgageFragment;
import org.pragyan.dalal18.fragment.NewsFragment;
import org.pragyan.dalal18.fragment.OrdersFragment;
import org.pragyan.dalal18.fragment.PortfolioFragment;
import org.pragyan.dalal18.fragment.StockExchangeFragment;
import org.pragyan.dalal18.fragment.TradeFragment;
import org.pragyan.dalal18.fragment.TransactionsFragment;
import org.pragyan.dalal18.fragment.marketDepth.MarketDepthFragment;
import org.pragyan.dalal18.loaders.SubscriptionLoader;
import org.pragyan.dalal18.notifications.NotificationFragment;
import org.pragyan.dalal18.notifications.NotificationService;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.MiscellaneousUtils;
import org.pragyan.dalal18.utils.StockUtils;
import org.pragyan.dalal18.utils.TinyDB;

import java.text.DecimalFormat;
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

import static org.pragyan.dalal18.ui.LoginActivity.EMAIL_KEY;
import static org.pragyan.dalal18.ui.LoginActivity.PASSWORD_KEY;

/* Subscribes to GetTransactions*/
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<List<Subscription>>,
        ConnectionUtils.OnNetworkDownHandler {

    private static final long DRAWER_DURATION = 450;

    private static final String LAST_TRANSACTION_ID = "last_transaction_id";
    private static final String LAST_NOTIFICATION_ID = "last_notification_id";

    public static final String CASH_WORTH_KEY = "cash-worth-key";
    public static final String TOTAL_WORTH_KEY = "total-worth-key";
    public static final String STOCKS_OWNED_KEY = "stocks-owned-key";
    public static final String GLOBAL_STOCKS_KEY = "global-stocks-key";

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

    public static List<StockDetails> ownedStockDetails = new ArrayList<>();
    public static List<GlobalStockDetails> globalStockDetails = new ArrayList<>();
    private List<SubscriptionId> subscriptionIds = new ArrayList<>();

    private static boolean shouldUnsubscribe = true;

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

    AlertDialog helpDialog, logoutDialog;

    Intent notifIntent;

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

        TinyDB tinyDB = new TinyDB(this);
        tinyDB.remove(Constants.NOTIFICATION_SHARED_PREF);
        tinyDB.remove(Constants.NOTIFICATION_NEWS_SHARED_PREF);
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

        getSupportLoaderManager().restartLoader(Constants.SUBSCRIPTION_LOADER_ID, null, this);

        StartMakingButtonsTransparent();
        updateStockWorthViaStreamUpdates();

        if (!getIntent().getBooleanExtra(SplashActivity.MARKET_OPEN_KEY, false)) {
            new AlertDialog.Builder(this)
                    .setTitle("Market Closed")
                    .setMessage("Please check notifications for market opening time. Sorry for the inconvenience.")
                    .setCancelable(true)
                    .setPositiveButton("CLOSE", (dI, i) -> dI.dismiss())
                    .show();
        }


        notifIntent = new Intent(this, NotificationService.class);
        startService(notifIntent);
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
                helpDialog = builder.create();
                return true;

            case R.id.action_logout:
                AlertDialog.Builder logOutBuilder = new AlertDialog.Builder(this);
                logOutBuilder
                        .setMessage("Do you want to logout ?")
                        .setPositiveButton("Logout", (dialogInterface, i) -> logout())
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                        .setTitle("Confirm Logout")
                        .setCancelable(true)
                        .show();
                logoutDialog = logOutBuilder.create();
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

            case R.id.notifications_action:
                fragment = new NotificationFragment();
                break;
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

        Handler handler = new Handler();
        if (shouldUnsubscribe) {
            handler.post(() -> {
                for (SubscriptionId currentSubscriptionId : subscriptionIds) {
                    streamServiceBlockingStub.unsubscribe(
                            UnsubscribeRequest.newBuilder().setSubscriptionId(currentSubscriptionId).build());
                }
            });
        }

        handler.postDelayed(() -> {
            LogoutResponse logoutResponse = actionServiceBlockingStub.logout(LogoutRequest.newBuilder().build());

            if (logoutResponse.getStatusCode().getNumber() == 0) {

                Intent stopNotifIntent = new Intent(Constants.STOP_NOTIFICATION_ACTION);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(stopNotifIntent);

                preferences
                        .edit()
                        .putString(EMAIL_KEY, null)
                        .putString(PASSWORD_KEY, null)
                        .putString(LoginActivity.SESSION_KEY,null)
                        .apply();
            } else {
                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                onNetworkDownError();
                return;
            }

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }

    public void updateValues() {

        int cashWorth = getIntent().getIntExtra(CASH_WORTH_KEY, -1);
        int totalWorth = getIntent().getIntExtra(TOTAL_WORTH_KEY, -1);
        int stockWorth = totalWorth - cashWorth;

        DecimalFormat formatter = new DecimalFormat("##,##,###");
        cashTextView.setText(formatter.format(cashWorth));
        stockTextView.setText(formatter.format(stockWorth));
        totalTextView.setText(formatter.format(totalWorth));
    }

    // Subscribes to transaction stream and gets updates (TESTED)
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

                            updateOwnedStockIdAndQuantity(transaction.getStockId(), Math.abs(transaction.getStockQuantity()), transaction.getStockQuantity() > 0);

                            Intent intent = new Intent(Constants.REFRESH_WORTH_TEXTVIEW_ACTION);
                            intent.putExtra(TOTAL_WORTH_KEY, transaction.getTotal());
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Constants.REFRESH_OWNED_STOCKS_ACTION));

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

        DecimalFormat formatter = new DecimalFormat("##,##,###");
        stockTextView.setText(formatter.format(netStockWorth));

        String temp = cashTextView.getText().toString();
        temp = temp.replace(",", "");
        totalTextView.setText(formatter.format(netStockWorth + Integer.parseInt(temp)));
    }

    private void changeTextViewValue(TextView textView, int value, boolean increase) {
        String temp = textView.getText().toString();
        temp = temp.replace(",", "");
        int previousValue = Integer.parseInt(temp);
        textView.setText(new DecimalFormat("##,##,###").format(previousValue + (increase ? value : -1 * value)));
    }

    // Starts making drawer button translucent
    private void StartMakingButtonsTransparent() {

        new Thread() {
            @Override
            public void run() {

                while (drawerEdgeButton.getAlpha() > 0.70) {
                    try {
                        Thread.sleep(175);
                        runOnUiThread(() -> drawerEdgeButton.setAlpha((float) (drawerEdgeButton.getAlpha() - 0.01)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.REFRESH_DIVIDEND_ACTION);
        intentFilter.addAction(Constants.REFRESH_WORTH_TEXTVIEW_ACTION);
        intentFilter.addAction(Constants.UPDATE_WORTH_VIA_STREAM_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCashStockReceiver, new IntentFilter(intentFilter));
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.edit().remove(LAST_TRANSACTION_ID).apply();
        preferences.edit().remove(LAST_NOTIFICATION_ID).apply();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCashStockReceiver);
        if (helpDialog != null) {
            helpDialog.dismiss();
        }
        if (logoutDialog != null) {
            logoutDialog.dismiss();
        }
    }

    @Override
    public Loader<List<Subscription>> onCreateLoader(int id, Bundle args) {
        return new SubscriptionLoader(this, streamServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(Loader<List<Subscription>> loader, List<Subscription> data) {

        if (data == null) {
            onNetworkDownError();
            return;
        }

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

    @Override
    public void onNetworkDownError() {
        shouldUnsubscribe = false;
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    // Unsubscribes from all the streams subscribed in this activity.
    @Override
    protected void onDestroy() {
        stopService(notifIntent);
        preferences.edit().remove(LAST_TRANSACTION_ID).apply();
        preferences.edit().remove(LAST_NOTIFICATION_ID).apply();
        super.onDestroy();
    }
}