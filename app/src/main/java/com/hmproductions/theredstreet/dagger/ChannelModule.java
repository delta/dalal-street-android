package com.hmproductions.theredstreet.dagger;

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

import dagger.Module;
import dagger.Provides;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

@Module
public class ChannelModule {

    //private static final String HOST = "10.1.76.143";
    private static final String HOST = "192.168.43.230";
    private static final int PORT = 8000;

    @Provides
    @DalalStreetApplicationScope
    public ManagedChannel getManagedChannel() {

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

        return channel;
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
