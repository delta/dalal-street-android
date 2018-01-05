package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.hmproductions.theredstreet.MiscellaneousUtils;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.loaders.LoginLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import proto.DalalActionServiceGrpc;
import proto.Login;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Login.LoginResponse> {

    private static final int LOADER_ID = 101;

    EditText emailEditText,  passwordEditText;
    private Login.LoginRequest loginRequest;

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
            loginRequest = Login.LoginRequest
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
    public Loader<Login.LoginResponse> onCreateLoader(int id, Bundle args) {
        return new LoginLoader(this, loginRequest);
    }

    @Override
    public void onLoadFinished(Loader<Login.LoginResponse> loader, Login.LoginResponse data) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("username", emailEditText.getText().toString());
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoaderReset(Loader<Login.LoginResponse> loader) {
        emailEditText.setText("");
        passwordEditText.setText("");
    }
}