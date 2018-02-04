package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.StockDetails;
import com.hmproductions.theredstreet.loaders.LoginLoader;
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;
import com.hmproductions.theredstreet.utils.MiscellaneousUtils;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import dalalstreet.api.models.Stock;
import io.grpc.ManagedChannel;

import static com.hmproductions.theredstreet.utils.Constants.LOGIN_LOADER_ID;

public class SplashActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<LoginResponse> {

    /* Not injecting stub directly into this context to prevent empty/null metadata attached to stub since user has not logged in. */
    @Inject
    ManagedChannel channel;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.splash_textView)
    TextView splashText;

    @BindView(R.id.graph_drawer)
    GraphDrawView graphDrawView;

    public static final String USERNAME_KEY = "username-key";
    public static final String EMAIL_KEY = "email-key";
    public static final String MARKET_OPEN_KEY = "market-open-key";
    static final String PASSWORD_KEY = "password-key";

    public Thread drawingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        ButterKnife.bind(this);
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        setupSplashAnimations();

        startLoginProcess(preferences.getString(EMAIL_KEY, null), preferences.getString(PASSWORD_KEY, null));
    }

    private void startLoginProcess(String email, String password) {

        splashText.setText(getString(R.string.signing_in));

        if (ConnectionUtils.getConnectionInfo(this)) {
            if (email != null && !email.equals("")) {

                Bundle bundle = new Bundle();
                bundle.putString(EMAIL_KEY, email);
                bundle.putString(PASSWORD_KEY, password);

                getSupportLoaderManager().restartLoader(LOGIN_LOADER_ID, bundle, this);
            } else {
                preferences
                        .edit()
                        .putString(EMAIL_KEY, null)
                        .putString(PASSWORD_KEY, null)
                        .apply();

                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            new Handler().postDelayed(() -> {
                Snackbar.make(findViewById(android.R.id.content), "Internet Unavailable", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", view -> {
                            startLoginProcess(email,password);
                            splashText.setText(R.string.error_signing_in);
                        })
                        .show();
                splashText.setText(R.string.error_signing_in);
            }, 500);
        }
    }

    private void setupSplashAnimations() {
        drawingThread = new Thread(graphDrawView);
        drawingThread.start();
    }

    @Override
    public Loader<LoginResponse> onCreateLoader(int id, Bundle args) {

            LoginRequest loginRequest = LoginRequest
                    .newBuilder()
                    .setEmail(args.getString(EMAIL_KEY))
                    .setPassword(args.getString(PASSWORD_KEY))
                    .build();

            DalalActionServiceGrpc.DalalActionServiceBlockingStub stub = DalalActionServiceGrpc.newBlockingStub(channel);

        return new LoginLoader(this, loginRequest, stub);
    }

    @Override
    public void onLoadFinished(Loader<LoginResponse> loader, LoginResponse loginResponse) {

        if (loginResponse == null) {
            Snackbar.make(findViewById(android.R.id.content), "Server Down", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", view -> startLoginProcess(preferences.getString(EMAIL_KEY, null), preferences.getString(PASSWORD_KEY, null)))
                    .show();
            splashText.setText(R.string.error_server_down);
            return;
        }

        if (loginResponse.getStatusCode().getNumber() == 0) {

            MiscellaneousUtils.sessionId = loginResponse.getSessionId();

            // Adding user's stock details
            ArrayList<StockDetails> stocksOwnedList = new ArrayList<>();
            Map<Integer, Integer> stocksOwnedMap = loginResponse.getStocksOwnedMap();

            for (int i = 1; i <= Constants.NUMBER_OF_COMPANIES; ++i) {
                if (stocksOwnedMap.containsKey(i)) {
                    stocksOwnedList.add(new StockDetails(i, stocksOwnedMap.get(i)));
                }
            }

            // Adding global stock details
            ArrayList<GlobalStockDetails> globalStockList = new ArrayList<>();
            Map<Integer, Stock> globalStockMap = loginResponse.getStockListMap();

            for (int q = 1; q <= globalStockMap.size(); ++q) {

                Stock currentStockDetails = globalStockMap.get(q);

                if (currentStockDetails != null) {
                    globalStockList.add(new GlobalStockDetails(
                            currentStockDetails.getFullName(),
                            currentStockDetails.getShortName(),
                            q,
                            currentStockDetails.getCurrentPrice(),
                            currentStockDetails.getStocksInMarket(),
                            currentStockDetails.getStocksInExchange(),
                            currentStockDetails.getPreviousDayClose(),
                            currentStockDetails.getUpOrDown() ? 1 : 0));
                }
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(USERNAME_KEY, loginResponse.getUser().getName());
            intent.putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.getUser().getCash());
            intent.putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.getUser().getTotal());
            intent.putExtra(MARKET_OPEN_KEY, loginResponse.getIsMarketOpen());

            intent.putParcelableArrayListExtra(MainActivity.STOCKS_OWNED_KEY, stocksOwnedList);
            intent.putParcelableArrayListExtra(MainActivity.GLOBAL_STOCKS_KEY, globalStockList);

            // Checking for constants
            for (Map.Entry<String,Integer> entry : loginResponse.getConstantsMap().entrySet()) {
                if (entry.getKey().equals("MORTGAGE_DEPOSIT_RATE"))
                    Constants.MORTGAGE_DEPOSIT_RATE = entry.getValue();
                else if (entry.getKey().equals("MORTGAGE_RETRIEVE_RATE"))
                    Constants.MORTGAGE_RETRIEVE_RATE = entry.getValue();
            }

            preferences.edit()
                    .putString(Constants.MARKET_OPEN_TEXT_KEY, loginResponse.getMarketIsOpenHackyNotif())
                    .putString(Constants.MARKET_CLOSED_TEXT_KEY, loginResponse.getMarketIsClosedHackyNotif())
                    .apply();

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();

            preferences
                    .edit()
                    .putString(EMAIL_KEY, null)
                    .putString(PASSWORD_KEY, null)
                    .apply();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drawingThread != null) {
            if (drawingThread.isAlive())
                drawingThread.interrupt();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoginResponse> loader) {
        // Do Nothing
    }
}