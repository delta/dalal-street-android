package org.pragyan.dalal18.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MiscellaneousUtils {

    public static String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
        String outputPattern = "MMM dd hh:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

        String str = null;

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputFormat.parse(time));
            calendar.add(Calendar.MINUTE, 330); /* Adding +05:30 GMT */
            str = outputFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    public static String sessionId = "dalalStreetSessionId";
    public static String username = null;

    /* Main Server Certificate */

    // TODO (Release) : Update server crt with main server
    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFUzCCBDugAwIBAgISA7gbLYfDaQTNplsoucExhVJdMA0GCSqGSIb3DQEBCwUA\n" +
            "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
            "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0xOTEyMjcwODM2NTlaFw0y\n" +
            "MDAzMjYwODM2NTlaMBkxFzAVBgNVBAMTDmRlbHRhLm5pdHQuZWR1MIIBIjANBgkq\n" +
            "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx6vzAfYGQpYOkZ1iIZS127XNbZJAoQ5b\n" +
            "AnlTJRFg1l1Q78yqIBGDLAeAvZkPLqNGhqg2V72w9L7KodVOmxWTKwDYL02sTgT6\n" +
            "ANYRPQyxYZVH9LIRVOd1cKGkjuyoueO4TxbuwlCSnk82JHnwJYqwicVT9+eopzGm\n" +
            "bz5Am4pN3joNEKveIOOqo+hPahgfQD0RZBtz61oQq6I+Vig64fRZ1pZQUi/u+9II\n" +
            "C1OwQow1UG6mpWltjuyiG/drLP/l3rpGV3s77I+wTZNRL7pAvv9jGbqhtpoB2Dez\n" +
            "q/ZLJ5IEcwiptwPETdVLkM0GveyWSldFFkHC5BRIwY5KPlPVpZVC7QIDAQABo4IC\n" +
            "YjCCAl4wDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEF\n" +
            "BQcDAjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQvEDXDh33FwaLf1n2XqzJaPBqH\n" +
            "KjAfBgNVHSMEGDAWgBSoSmpjBH3duubRObemRWXv86jsoTBvBggrBgEFBQcBAQRj\n" +
            "MGEwLgYIKwYBBQUHMAGGImh0dHA6Ly9vY3NwLmludC14My5sZXRzZW5jcnlwdC5v\n" +
            "cmcwLwYIKwYBBQUHMAKGI2h0dHA6Ly9jZXJ0LmludC14My5sZXRzZW5jcnlwdC5v\n" +
            "cmcvMBkGA1UdEQQSMBCCDmRlbHRhLm5pdHQuZWR1MEwGA1UdIARFMEMwCAYGZ4EM\n" +
            "AQIBMDcGCysGAQQBgt8TAQEBMCgwJgYIKwYBBQUHAgEWGmh0dHA6Ly9jcHMubGV0\n" +
            "c2VuY3J5cHQub3JnMIIBAwYKKwYBBAHWeQIEAgSB9ASB8QDvAHUA8JWkWfIA0YJA\n" +
            "EC0vk4iOrUv+HUfjmeHQNKawqKqOsnMAAAFvRrda+gAABAMARjBEAiAdX6rlSIOc\n" +
            "g7MH7TV/HuoRKLpowtdciiSILW61c0NuvQIgRJdj/5MyPC41iKWxz7IFxknVqZ3H\n" +
            "IuQv7B9gJICd70wAdgCyHgXMi6LNiiBOh2b5K7mKJSBna9r6cOeySVMt74uQXgAA\n" +
            "AW9Gt1zoAAAEAwBHMEUCIQCz1A++oLUw85Iw7dni4Ni5lJdbZEmUp5/qHC8PXywH\n" +
            "qwIgPUXaCCpFzb6beIlmlpikTmATs+tvGabUlAifyjfP6SQwDQYJKoZIhvcNAQEL\n" +
            "BQADggEBAB08A5LaUvenY1RPx07igUkmZ8lCR7y40MIoVENKmaTOGSVQ8K0ndAyH\n" +
            "6/SY9JwPEjCtbIGJ9MMalLfSnubW9Cg4Jo7ThLpAVmEbzIVebEOZFhk6sZ8Jn+Ep\n" +
            "6L2NFiOs5S+JCOKF8yu0ZMG+f4TnWSvAW3toVPXgsyqEeXmlzDOsu7VX7d6TYlbi\n" +
            "rGsYcqweuB3hl7SU/Q4X9/NkbiBZ6Bn2rHYXlRSfRfYBYeDPISlFsCz2ztASkuLb\n" +
            "QWU1RJij2MdnuXml2y7uFW5gg848QJmrWTgCT1geKqc60JX7uSDpVAyaNcO9CF1z\n" +
            "YPfy9kmiD0yM/Zb7bqHGX2ks7SDVrqw=\n" +
            "-----END CERTIFICATE-----\n";
}
