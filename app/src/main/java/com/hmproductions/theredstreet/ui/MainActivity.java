package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
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
import dalalstreet.api.actions.LogoutRequest;
import dalalstreet.api.actions.LogoutResponse;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final long DRAWER_DURATION = 450;
    public static final String CASH_WORTH_KEY = "cash-worth-key";
    public static final String TOTAL_WORTH_KEY = "total-worth-key";
    public static final String STOCKS_OWNED_KEY = "stocks-owned-key";
    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    SharedPreferences preferences;

    @Inject
    Metadata metadata;

    private TextView usernameTextView;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    public static List<StockDetails> ownedStockDetails;

    @BindView(R.id.stockWorth_textView)
    TextView stockTextView;

    @BindView(R.id.cashWorth_textView)
    TextView cashTextView;

    @BindView(R.id.totalWorth_textView)
    TextView totalTextView;

    @BindView(R.id.home_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);
        ButterKnife.bind(this);
        MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BindDrawerViews();
        SetupNavigationDrawer();

        OpenAndCloseDrawer();

        getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new MortgageFragment()).commit();

        ownedStockDetails = getIntent().getParcelableArrayListExtra(STOCKS_OWNED_KEY);
        updateValues();
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

        if (logoutResponse.getStatusCode().getNumber() == 0 || logoutResponse.getStatusCode().getNumber() == 1) {
            Toast.makeText(this, logoutResponse.getStatusMessage(), Toast.LENGTH_SHORT).show();

            preferences
                    .edit()
                    .putString(LoginActivity.USERNAME_KEY, null)
                    .putString(LoginActivity.SESSION_ID_KEY, null)
                    .putString(LoginActivity.EMAIL_KEY, null)
                    .putInt(CASH_WORTH_KEY, -1)
                    .putInt(TOTAL_WORTH_KEY, -1)
                    .apply();
        } else {
            Toast.makeText(this, "Connection error", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CASH_WORTH_KEY, Integer.parseInt(cashTextView.getText().toString()));
        editor.putInt(TOTAL_WORTH_KEY, Integer.parseInt(totalTextView.getText().toString()));
        editor.apply();
    }
}
