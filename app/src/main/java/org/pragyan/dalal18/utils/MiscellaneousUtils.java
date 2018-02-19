package org.pragyan.dalal18.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MiscellaneousUtils {

    public static float convertDpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String outputPattern = "hh:mm a   MMM dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

        String str = null;

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputFormat.parse(time));
            calendar.add(Calendar.HOUR_OF_DAY, 5);
            calendar.add(Calendar.MINUTE, 30);
            str = outputFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String sessionId = "dalalStreetSessionId";
    public static String username = null;

    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFBTCCA+2gAwIBAgISA+OThMLh+yVPNP5jen9UpZLLMA0GCSqGSIb3DQEBCwUA\n" +
            "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
            "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0xODAyMTgxNDQ3MDRaFw0x\n" +
            "ODA1MTkxNDQ3MDRaMBwxGjAYBgNVBAMTEWRhbGFsLnByYWd5YW4ub3JnMIIBIjAN\n" +
            "BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvGT5r53rUVLDgTRitxcf76Y+3p0O\n" +
            "AVVDR+EZdSwlGNFyaZeBc3cwPUodevPImvhfCDvL5LoJAwIwpIQ/Ufqdcb6I2Duh\n" +
            "27QcxDXAId3PQPSrV6ubAAxeLUNRoqvGoxJ5hebUSuVmwfIvx4seO/f1VqD8gqiR\n" +
            "cZFkOag+sRRYGqwxWlaTcTQ6r1RZmW5FxSfvwUHOLYWzcc1pdP1XBWZ4n0KEdgCa\n" +
            "N8ht7XDp3xRlZp+Oo+Avu6TnyJsOGob5YuxbzZC3S1sHDvtGY6czmaxOCCT1IRC9\n" +
            "8q16RhC9aYL7iouEH2XOK/QRYXS5oKXQoQmaAlKIkYpUE3Cor5GrJwN9UwIDAQAB\n" +
            "o4ICETCCAg0wDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr\n" +
            "BgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTLe29ihoYDX3nePTMkeKR+\n" +
            "8Y7axjAfBgNVHSMEGDAWgBSoSmpjBH3duubRObemRWXv86jsoTBvBggrBgEFBQcB\n" +
            "AQRjMGEwLgYIKwYBBQUHMAGGImh0dHA6Ly9vY3NwLmludC14My5sZXRzZW5jcnlw\n" +
            "dC5vcmcwLwYIKwYBBQUHMAKGI2h0dHA6Ly9jZXJ0LmludC14My5sZXRzZW5jcnlw\n" +
            "dC5vcmcvMBwGA1UdEQQVMBOCEWRhbGFsLnByYWd5YW4ub3JnMIH+BgNVHSAEgfYw\n" +
            "gfMwCAYGZ4EMAQIBMIHmBgsrBgEEAYLfEwEBATCB1jAmBggrBgEFBQcCARYaaHR0\n" +
            "cDovL2Nwcy5sZXRzZW5jcnlwdC5vcmcwgasGCCsGAQUFBwICMIGeDIGbVGhpcyBD\n" +
            "ZXJ0aWZpY2F0ZSBtYXkgb25seSBiZSByZWxpZWQgdXBvbiBieSBSZWx5aW5nIFBh\n" +
            "cnRpZXMgYW5kIG9ubHkgaW4gYWNjb3JkYW5jZSB3aXRoIHRoZSBDZXJ0aWZpY2F0\n" +
            "ZSBQb2xpY3kgZm91bmQgYXQgaHR0cHM6Ly9sZXRzZW5jcnlwdC5vcmcvcmVwb3Np\n" +
            "dG9yeS8wDQYJKoZIhvcNAQELBQADggEBACepTdQrvrR+BKRZHhkhExrtbYELPgeF\n" +
            "X5TUz3yKeUCmf1CJfgyCuRBAPrrDU1LKlImRbUGnAxt+IHk3GgmdbbImyL/8x22j\n" +
            "DYHdsy/3x4wkAMMDeOiDI84KWKLNGXYCUx9XMT394djNpAtynhuINFgTKpi1U1aw\n" +
            "JxHjr5qGna4LLoL2OyUtLZnLPfrhBdbe1EfQTQ5TZgr5g6rgtGIX2o/g3ORLYUGM\n" +
            "EYrt2uzl6wJHiNDrIyEmr1YepVDyNiYaSt0S6T72qd68eBeDZ5q6rI1bQcsrp1/N\n" +
            "4yxybWqAkiH/0e4JsG3sXIABMClz0rcoS4hq6wdin8K45mxnFBZaK1Q=\n" +
            "-----END CERTIFICATE-----";

    /*  Test Server Certificate

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
            "5+8T\n-----END CERTIFICATE-----";*/
}
