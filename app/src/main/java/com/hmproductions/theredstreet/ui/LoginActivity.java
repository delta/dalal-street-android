package com.hmproductions.theredstreet.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
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

import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import dalalstreet.api.models.Stock;
import io.grpc.ManagedChannel;

import static com.hmproductions.theredstreet.utils.Constants.LOGIN_LOADER_ID;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<LoginResponse> {

    /* Not injecting stub directly into this context to prevent empty/null metadata attached to stub since user has not logged in. */
    @Inject
    ManagedChannel channel;

    @Inject
    SharedPreferences preferences;

    public static final String USERNAME_KEY = "username-key";
    public static final String EMAIL_KEY = "email-key";
    public static final String MARKET_OPEN_KEY = "market-open-key";
    static final String PASSWORD_KEY = "password-key";

    EditText emailEditText, passwordEditText;
    private AlertDialog signingInAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.app_name));

        signingInAlertDialog = new AlertDialog.Builder(this).setView(R.layout.progress_dialog).setCancelable(false).create();

        emailEditText = findViewById(R.id.email_editText);
        passwordEditText = findViewById(R.id.password_editText);

        if (getIntent().getStringExtra(RegistrationActivity.REGISTER_MESSAGE_KEY) != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Registration Message")
                    .setMessage(getIntent().getStringExtra(RegistrationActivity.REGISTER_MESSAGE_KEY))
                    .setPositiveButton("OKAY", (dI, i) -> dI.dismiss())
                    .setCancelable(false)
                    .show();
        }

        startLoginProcess(false);
    }

    private void startLoginProcess(boolean startedFromServerDown) {

        if (ConnectionUtils.getConnectionInfo(this)) {
            findViewById(R.id.play_button).setEnabled(true);

            if (startedFromServerDown)
                onLoginButtonClick();
        } else {
            findViewById(R.id.play_button).setEnabled(false);
            new Handler().postDelayed(() -> Snackbar
                    .make(findViewById(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", view -> startLoginProcess(true))
                    .show(), 500);
        }
    }

    @OnClick(R.id.play_button)
    void onLoginButtonClick() {
        if (ConnectionUtils.getConnectionInfo(this)) {
            if (validateEmail() && validatePassword()) {
                Bundle bundle = new Bundle();
                bundle.putString(EMAIL_KEY, emailEditText.getText().toString());
                bundle.putString(PASSWORD_KEY, passwordEditText.getText().toString());

                signingInAlertDialog.show();
                getSupportLoaderManager().restartLoader(LOGIN_LOADER_ID, bundle, this);
            }
        } else {
            startLoginProcess(false);
        }
    }

    @OnClick(R.id.clickRegister_textView)
    void onRegisterButtonClick() {
        startActivity(new Intent(this, RegistrationActivity.class));
        finish();
    }

    private boolean validateEmail() {

        if (emailEditText.getText().toString().trim().isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        } else if (!emailEditText.getText().toString().contains("@")) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        if (passwordEditText.getText().toString().trim().isEmpty()) {
            passwordEditText.setError("Enter password");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
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

        signingInAlertDialog.dismiss();

        if (loginResponse == null) {
            Snackbar.make(findViewById(android.R.id.content), "Server Down", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", view -> startLoginProcess(true))
                    .show();
            return;
        }

        if (loginResponse.getStatusCode().getNumber() == 0) {

            MiscellaneousUtils.sessionId = loginResponse.getSessionId();

            if (!passwordEditText.getText().toString().equals("") || !passwordEditText.getText().toString().isEmpty())
                preferences.edit()
                        .putString(EMAIL_KEY, loginResponse.getUser().getEmail())
                        .putString(PASSWORD_KEY, passwordEditText.getText().toString())
                        .apply();

            // Adding user's stock details
            ArrayList<StockDetails> stocksOwnedList = new ArrayList<>(30);
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
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            passwordEditText.setText("");
        }
    }

    @Override
    public void onLoaderReset(Loader<LoginResponse> loader) {
        emailEditText.setText("");
        passwordEditText.setText("");
    }
}