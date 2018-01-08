package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import com.hmproductions.theredstreet.MiscellaneousUtils;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.StockDetails;
import com.hmproductions.theredstreet.loaders.LoginLoader;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<LoginResponse> {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub stub;

    @Inject
    SharedPreferences preferences;

    private static final int LOADER_ID = 101;
    public static final String USERNAME_KEY = "username-key";
    public static final String EMAIL_KEY = "email-key";
    public static final String SESSION_ID_KEY = "session-id-key";
    private static final String PASSWORD_KEY = "password-key";

    EditText emailEditText,  passwordEditText;
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

        emailEditText = findViewById(R.id.email_editText);
        passwordEditText = findViewById(R.id.password_editText);

        String email = preferences.getString(EMAIL_KEY, null);
        String password = preferences.getString(PASSWORD_KEY, null);
        if (email != null && !email.equals("")) {

            Bundle bundle = new Bundle();
            bundle.putString(EMAIL_KEY, email);
            bundle.putString(PASSWORD_KEY, password);
            getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
        }
    }

    @OnClick(R.id.play_button)
    void onLoginButtonClick() {
        if (validateEmail() && validatePassword()) {
            Bundle bundle = new Bundle();
            bundle.putString(EMAIL_KEY, emailEditText.getText().toString());
            bundle.putString(PASSWORD_KEY, passwordEditText.getText().toString());
            getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
        }
    }

    private boolean validateEmail() {

        if (emailEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter an email ID", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!emailEditText.getText().toString().contains("@")) {
            Toast.makeText(this, "Please enter a valid email ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        if (passwordEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public Loader<LoginResponse> onCreateLoader(int id, Bundle args) {
        signingInAlertDialog = new AlertDialog.Builder(this)
                .setView(R.layout.progress_dialog)
                .setCancelable(false)
                .create();
        signingInAlertDialog.show();

        LoginRequest loginRequest = LoginRequest
                .newBuilder()
                .setEmail(args.getString(EMAIL_KEY))
                .setPassword(args.getString(PASSWORD_KEY))
                .build();

        return new LoginLoader(this, loginRequest, stub);
    }

    @Override
    public void onLoadFinished(Loader<LoginResponse> loader, LoginResponse loginResponse) {

        signingInAlertDialog.dismiss();

        // TODO : Fix internal error
        if (loginResponse.getStatusCode().getNumber() == 0 || loginResponse.getStatusCode().getNumber() == 1) {

            MiscellaneousUtils.sessionId = loginResponse.getSessionId();
            preferences.edit()
                    .putString(EMAIL_KEY, loginResponse.getUser().getEmail())
                    .putString(PASSWORD_KEY, passwordEditText.getText().toString())
                    .apply();

            ArrayList<StockDetails> list = new ArrayList<>();
            Map<Integer, Integer> stocksOwnedMap = loginResponse.getStocksOwnedMap();

            for (int i=0 ; i<stocksOwnedMap.size() ; ++i) {
                list.add(new StockDetails(i, stocksOwnedMap.get(i)));
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(USERNAME_KEY, loginResponse.getUser().getName());
            intent.putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.getUser().getCash());
            intent.putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.getUser().getTotal());
            intent.putParcelableArrayListExtra(MainActivity.STOCKS_OWNED_KEY, list);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoginResponse> loader) {
        emailEditText.setText("");
        passwordEditText.setText("");
    }
}