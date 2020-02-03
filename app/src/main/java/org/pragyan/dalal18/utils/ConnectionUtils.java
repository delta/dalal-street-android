package org.pragyan.dalal18.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ConnectionUtils {

    public static boolean getConnectionInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } else {
            return false;
        }
    }

    public static boolean isReachableByTcp(String host, int port) {
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, 12000);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public interface OnNetworkDownHandler {
        void onNetworkDownError(String message, int fragment);
    }

    public interface SmsVerificationHandler {
        void onNetworkDownError(String message);
        void navigateToOtpVerification(String phoneNumber);
        void navigateToAddPhone();
        String getPhoneNumber();
    }
}
