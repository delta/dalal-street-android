package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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

import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.MiscellaneousUtils;
import com.hmproductions.theredstreet.utils.StockUtils;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.StockDetails;
import com.hmproductions.theredstreet.fragment.BuySellFragment;
import com.hmproductions.theredstreet.fragment.CompanyProfileFragment;
import com.hmproductions.theredstreet.fragment.HomeFragment;
import com.hmproductions.theredstreet.fragment.LeaderboardFragment;
import com.hmproductions.theredstreet.fragment.MortgageFragment;
import com.hmproductions.theredstreet.fragment.NewsFragment;
import com.hmproductions.theredstreet.fragment.OrdersFragment;
import com.hmproductions.theredstreet.fragment.PortfolioFragment;
import com.hmproductions.theredstreet.fragment.StockExchangeFragment;
import com.hmproductions.theredstreet.fragment.TransactionsFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.DalalStreamServiceGrpc;
import dalalstreet.api.actions.LogoutRequest;
import dalalstreet.api.actions.LogoutResponse;
import dalalstreet.api.datastreams.DataStreamType;
import dalalstreet.api.datastreams.MarketEventUpdate;
import dalalstreet.api.datastreams.StockExchangeDataPoint;
import dalalstreet.api.datastreams.StockExchangeUpdate;
import dalalstreet.api.datastreams.StockPricesUpdate;
import dalalstreet.api.datastreams.SubscribeRequest;
import dalalstreet.api.datastreams.SubscribeResponse;
import dalalstreet.api.datastreams.SubscriptionId;
import dalalstreet.api.datastreams.TransactionUpdate;
import dalalstreet.api.models.TransactionType;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.theredstreet.ui.LoginActivity.EMAIL_KEY;
import static com.hmproductions.theredstreet.ui.LoginActivity.PASSWORD_KEY;

/* Subscribes to GetTransactions*/
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
    SharedPreferences preferences;

    private TextView usernameTextView;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    public static List<StockDetails> ownedStockDetails;
    public static List<GlobalStockDetails> globalStockDetails;
    private SubscriptionId transactionsSubscriptionId, stockPricesSubscriptionId, stockExchangeSubscriptionId, marketEventsSubscriptionId;

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

        Log.v(":::", "global stock details list size = " + String.valueOf(globalStockDetails.size()));
        updateValues();

        subscribeToTransactionsStream();
        subscribeToStockPricesStream();
        subscribeToStockExchangeStream();
        subscribeToMarketEventsUpdateStream();

        StartMakingButtonsTransparent();
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
                logout();
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
                fragment = new CompanyProfileFragment();
                break;
            case R.id.nav_news:
                fragment = new NewsFragment();
                break;
            case R.id.nav_buy_sell:
                fragment = new BuySellFragment();
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

    private void subscribeToTransactionsStream() {

        streamServiceStub.subscribe(
                SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.TRANSACTIONS).setDataStreamId("").build(),
                new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0)
                            transactionsSubscriptionId = value.getSubscriptionId();
                        else
                            Toast.makeText(MainActivity.this , "Server internal error", Toast.LENGTH_SHORT).show();
                        onCompleted();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                }
        );

        streamServiceStub.getTransactionUpdates(transactionsSubscriptionId,
                new StreamObserver<TransactionUpdate>() {
                    @Override
                    public void onNext(TransactionUpdate value) {
                        // TODO : Fill this method
                        if (value.getTransaction().getType() == TransactionType.DIVIDEND_TRANSACTION) {
                            int previousValue = Integer.parseInt(cashTextView.getText().toString());
                            cashTextView.setText(String.valueOf(previousValue + value.getTransaction().getTotal()));
                        } else if (value.getTransaction().getType() == TransactionType.ORDER_FILL_TRANSACTION) {

                        } else if (value.getTransaction().getType() == TransactionType.FROM_EXCHANGE_TRANSACTION) {

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

    private void subscribeToMarketEventsUpdateStream() {

        streamServiceStub.subscribe(
                SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MARKET_EVENTS).setDataStreamId("").build(),
                new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0)
                            marketEventsSubscriptionId = value.getSubscriptionId();
                        else
                            Toast.makeText(MainActivity.this , "Server internal error", Toast.LENGTH_SHORT).show();
                        onCompleted();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                }
        );

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

    private void subscribeToStockPricesStream() {
        streamServiceStub.subscribe(
                SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_PRICES).setDataStreamId("").build(),
                new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0)
                            stockPricesSubscriptionId = value.getSubscriptionId();
                        else
                            Toast.makeText(MainActivity.this , "Server internal error", Toast.LENGTH_SHORT).show();
                        onCompleted();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                }
        );

        streamServiceStub.getStockPricesUpdates(stockPricesSubscriptionId,
                new StreamObserver<StockPricesUpdate>() {
                    @Override
                    public void onNext(StockPricesUpdate value) {
                        for (int i=0 ; i<value.getPricesCount() ; ++i) {
                            MainActivity.globalStockDetails.get(i).setPrice(value.getPricesMap().get(i));
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

    private void subscribeToStockExchangeStream() {
        streamServiceStub.subscribe(
                SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_EXCHANGE).setDataStreamId("").build(),
                new StreamObserver<SubscribeResponse>() {
                    @Override
                    public void onNext(SubscribeResponse value) {
                        if (value.getStatusCode().getNumber() == 0)
                            stockExchangeSubscriptionId = value.getSubscriptionId();
                        else
                            Toast.makeText(MainActivity.this , "Server internal error", Toast.LENGTH_SHORT).show();
                        onCompleted();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                }
        );

        streamServiceStub.getStockExchangeUpdates(stockExchangeSubscriptionId,
                new StreamObserver<StockExchangeUpdate>() {
                    @Override
                    public void onNext(StockExchangeUpdate value) {

                        for (int x=0 ; x<value.getStocksInExchangeCount() ; ++x) {
                            StockExchangeDataPoint currentDataPoint = value.getStocksInExchangeMap().get(x);
                            MainActivity.globalStockDetails.get(x).setPrice(currentDataPoint.getPrice());
                            MainActivity.globalStockDetails.get(x).setQuantityInMarket(currentDataPoint.getStocksInMarket());
                            MainActivity.globalStockDetails.get(x).setQuantityInExchange(currentDataPoint.getStocksInExchange());
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

    // Starts making drawer button translucent
    private void StartMakingButtonsTransparent() {

        new Thread() {
            @Override
            public void run() {
                try {
                    while (drawerEdgeButton.getAlpha() > 0.60) {
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
}