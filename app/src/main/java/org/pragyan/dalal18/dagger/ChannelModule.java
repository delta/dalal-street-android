package org.pragyan.dalal18.dagger;

import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.squareup.okhttp.ConnectionSpec;

import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.MiscellaneousUtils;

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
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

@Module (includes = ContextModule.class)
class ChannelModule {

    @Provides
    @DalalStreetApplicationScope
    ManagedChannel getManagedChannel(Context context) {

        return OkHttpChannelBuilder
                    .forAddress(Constants.HOST, Constants.PORT)
                    .sslSocketFactory(getSocketFactory())
                    .connectionSpec(ConnectionSpec.MODERN_TLS)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

    }

    private SSLSocketFactory getSocketFactory() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            // returns array of certificate which are trusted
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext tlsContext = SSLContext.getInstance("TLS");
            tlsContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager

            return tlsContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
