package com.hmproductions.theredstreet.ui;

import android.content.Intent;
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
import com.hmproductions.theredstreet.loaders.LoginLoader;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<LoginResponse> {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub stub;

    private static final int LOADER_ID = 101;
    static final String USERNAME_KEY = "username-key";
    private static final String EMAIL_KEY = "email-key";

    EditText emailEditText,  passwordEditText;
    private LoginRequest loginRequest;
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
    }

    @OnClick(R.id.play_button)
    void onLoginButtonClick() {
        setLogin();
    }

    public void setLogin() {

        // TODO : Login request
        if (validateEmail() && validatePassword()) {
            loginRequest = LoginRequest
                    .newBuilder()
                    .setEmail(emailEditText.getText().toString().trim())
                    .setPassword(passwordEditText.getText().toString())
                    .build();
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
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

        return new LoginLoader(this, loginRequest, stub);
    }

    @Override
    public void onLoadFinished(Loader<LoginResponse> loader, LoginResponse loginResponse) {

        signingInAlertDialog.dismiss();

        // TODO : Fix internal error
        if (loginResponse.getStatusCode().getNumber() == 0 || loginResponse.getStatusCode().getNumber() == 1) {
            MiscellaneousUtils.sessionId = loginResponse.getSessionId();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(USERNAME_KEY, loginResponse.getUser().getName());
            intent.putExtra(EMAIL_KEY, loginResponse.getUser().getEmail());
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