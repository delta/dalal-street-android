package com.hmproductions.theredstreet.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.hmproductions.theredstreet.MiscellaneousUtils;

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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

public class LoginLoader extends AsyncTaskLoader<LoginResponse> {

    private static final String HOST = "10.1.76.143";
    private static final int PORT = 8000;

    private LoginRequest loginRequest;

    public LoginLoader(Context context, LoginRequest loginRequest) {
        super(context);
        this.loginRequest = loginRequest;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public LoginResponse loadInBackground() {

        ManagedChannel channel = null;
        try {
            channel = OkHttpChannelBuilder
                    .forAddress(HOST, PORT)
                    .sslSocketFactory(getSocketFactory())
                    .hostnameVerifier((hostname, session) -> true) // TODO : Fix this verification
                    .build();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }

        DalalActionServiceGrpc.DalalActionServiceBlockingStub loginStub = DalalActionServiceGrpc.newBlockingStub(channel);
        LoginResponse loginResponse = loginStub.login(loginRequest);

        Log.v(":::", "authenticated as " + loginResponse.getUser().getName() + "status code = " + String.valueOf(loginResponse.getStatusCode()));

        if (channel != null) {
            channel.shutdown();
        }

        return loginResponse;
    }

    private SSLSocketFactory getSocketFactory() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException {

        byte[] der = MiscellaneousUtils.SERVER_CERT.getBytes();
        ByteArrayInputStream crtInputStream = new ByteArrayInputStream(der);

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);

        CertificateFactory cert_factory = CertificateFactory.getInstance("X509");
        Certificate cert = cert_factory.generateCertificate(crtInputStream);
        trustStore.setCertificateEntry("cert", cert);

        TrustManagerFactory trust_manager_factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());trust_manager_factory.init(trustStore);
        TrustManager[] trust_manager = trust_manager_factory.getTrustManagers();

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(trustStore, null);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        SSLContext tlsContext = SSLContext.getInstance("TLS");
        tlsContext.init(keyManagers, trust_manager, null);

        return tlsContext.getSocketFactory();
    }
}
