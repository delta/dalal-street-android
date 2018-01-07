package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import com.hmproductions.theredstreet.loaders.LoginLoader;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<LoginResponse> {

    private static final int LOADER_ID = 101;

    EditText emailEditText,  passwordEditText;
    private LoginRequest loginRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

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
        return new LoginLoader(this, loginRequest);
    }

    @Override
    public void onLoadFinished(Loader<LoginResponse> loader, LoginResponse data) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("username", emailEditText.getText().toString());
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoaderReset(Loader<LoginResponse> loader) {
        emailEditText.setText("");
        passwordEditText.setText("");
    }
}