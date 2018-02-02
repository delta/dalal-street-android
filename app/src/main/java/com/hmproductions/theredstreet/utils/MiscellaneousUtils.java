package com.hmproductions.theredstreet.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MiscellaneousUtils {

    public static float convertDpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String sessionId = "dalalStreetSessionId";
    public static String username = null;

    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDjzCCAnegAwIBAgIJAON0T+GsNoM0MA0GCSqGSIb3DQEBCwUAMF4xCzAJBgNV\n" +
            "BAYTAklOMQ8wDQYDVQQHDAZUcmljaHkxDjAMBgNVBAoMBURlbHRhMRowGAYDVQQL\n" +
            "DBFEYWxhbCBTdHJlZXQgVGVhbTESMBAGA1UEAwwJbG9jYWxob3N0MB4XDTE3MTIy\n" +
            "NjIwMzIwNVoXDTI3MTIyNDIwMzIwNVowXjELMAkGA1UEBhMCSU4xDzANBgNVBAcM\n" +
            "BlRyaWNoeTEOMAwGA1UECgwFRGVsdGExGjAYBgNVBAsMEURhbGFsIFN0cmVldCBU\n" +
            "ZWFtMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n" +
            "ggEKAoIBAQCyfpFvDTbonf0U5dwhEQ2yCMvj6gFyq7gEYD0Ch9S2WcO9zo70uVBI\n" +
            "zvKc+gJ4/OHzmlEP33+x2Q9LC9H6pTwWO+ZltM6/u7z7IuI3BAv2O5a7KqAh0B16\n" +
            "nEv6C+47S9eATUgbtalNpbpCEqBJB/zr+vfoAv4k76G840wHA3NJYyGRJ/sfOrPs\n" +
            "ks8TnMCpUIn2HKAaihXBDjx91HCKdkf1RIqBGADN1s6Q75xJq+VjO39BEKs1Wh+C\n" +
            "cg3l8rQ9FotKlVYnfgfpEL5qouY9BgneXsb+ixMBkSCoZlaiJpaEYUZQ9CjbIECy\n" +
            "KL34JEDkNQlGFHI4dnxs8RJtBcVVaYs9AgMBAAGjUDBOMB0GA1UdDgQWBBS9CU6m\n" +
            "LHaqYFG9rvpl1aTwZyvnzzAfBgNVHSMEGDAWgBS9CU6mLHaqYFG9rvpl1aTwZyvn\n" +
            "zzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBp8GM4Ne4sPtDBb6QZ\n" +
            "IWO+fKQkFvSCvD0TXTUxU88QGWzOMyVlCskQo0P1/vjPIylRTVAJWC9J164PnKW2\n" +
            "U7RYgKFVGIfdQmkD6Ul2ip2XVJ7awoXjQO51nNp1uN9UzsHaIoswPe5KcJ/TOZ75\n" +
            "8BByY+l/L8EOXdk+DTq2kPS1mcv6a500Q9JTDAfAM6uUVnwiF4fWtfSUyyRpWl6s\n" +
            "xY01aSxRMQud7e8h/FDMPdTvcFpcnWUB2byRdw7gv5kIjfRsZZSwy/8zODZx/biR\n" +
            "n8lG/ZKeivbUai547FHqdI2qJwv2mElxojG2hLV7sImSg26fuaTyy5+ftP3GH8Yj\n" +
            "5+8T\n-----END CERTIFICATE-----";
}
