package org.pragyan.dalal18.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MiscellaneousUtils {

    public static String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ssX";
        String outputPattern = "MMM dd hh:mm a";
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

    public static int getNumberOfPlayersOnline(long timeInMillis, int startTime24Hr, int endTime24Hr) {

        int numberOfPlayers = 400;
        double multiplier = 1;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        timeInMillis /= 10000;
        numberOfPlayers += getMagicNumber(timeInMillis);

        /* If current time is not within 1 hour of event starting/ending, multiplier is 0.2
           If current time is within 1 hour of event, mulitpler is 0.5
           If current time is within the event, then
                For first and last hour, multiplier is 1 (default)
                Otherwise multipler is 1.5
         */

        if (calendar.get(Calendar.HOUR_OF_DAY) + 1 < startTime24Hr || calendar.get(Calendar.HOUR_OF_DAY) - 1 > endTime24Hr)
            multiplier = 0.2;
        if (calendar.get(Calendar.HOUR_OF_DAY) == startTime24Hr - 1 || calendar.get(Calendar.HOUR_OF_DAY) == endTime24Hr)
            multiplier = 0.5;
        if (calendar.get(Calendar.HOUR_OF_DAY) < endTime24Hr - 1 && calendar.get(Calendar.HOUR_OF_DAY) > startTime24Hr)
            multiplier = 1.5;

        return (int) (numberOfPlayers * multiplier);
    }

    // Returns product of 3rd power of individual digits
    private static int getMagicNumber(long num) {
        long answer = 1;
        while (num > 0) {
            if (num % 10 != 0)
                answer *= Math.pow(num % 10, 3);
            num /= 10;
        }

        while (answer > 1000) answer /= 10;
        return (int) (answer % 200);
    }

    public static String sessionId = "dalalStreetSessionId";
    public static String username = null;

    /*public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
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
            "5+8T\n" +
            "-----END CERTIFICATE-----";

    /*  Test Server Certificate*/

    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIID1DCCArygAwIBAgIJAKcNrDdYzYDuMA0GCSqGSIb3DQEBCwUAMH8xCzAJBgNV\n" +
            "BAYTAklOMQswCQYDVQQIDAJUTjEPMA0GA1UEBwwGVHJpY2h5MQ4wDAYDVQQKDAVE\n" +
            "ZWx0YTEOMAwGA1UECwwFRGFsYWwxCzAJBgNVBAMMAkRMMSUwIwYJKoZIhvcNAQkB\n" +
            "FhZwYWlha3NoYXk5OThAZ21haWwuY29tMB4XDTE5MDIyNzEyMTQyM1oXDTIwMDIy\n" +
            "NzEyMTQyM1owfzELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAlROMQ8wDQYDVQQHDAZU\n" +
            "cmljaHkxDjAMBgNVBAoMBURlbHRhMQ4wDAYDVQQLDAVEYWxhbDELMAkGA1UEAwwC\n" +
            "REwxJTAjBgkqhkiG9w0BCQEWFnBhaWFrc2hheTk5OEBnbWFpbC5jb20wggEiMA0G\n" +
            "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCj0+2odbJm5w41VIVSaiRniPut/6jo\n" +
            "eE/mYkvfFb91UCNr05BGFMHzPV8TP4rkbv8JtwVwB6peUC431aiNT6q/DG4sOnNV\n" +
            "v3sdPOPJ24lGmItcuHtb/xj2NG6JblbGw2cIGb2AC8N7GMJmlopthJvezK7nkRuA\n" +
            "aHSqtsT3A1do1o/wEV1ozewsN1iNCJAmK6Iw9eJDJyec+2AB4B/2wAyPVzCQybar\n" +
            "ie8YqCseN+YJ2JN341fBiTDV60yWxU6pIXyZDijEyfOqOc8A6pRfVhhMiJSdx+U7\n" +
            "f4/j/c3hmMqEkHTfXkaKL1iRA8kJnhKDP4AV7QpYw7oZoYuB+hfpR3mnAgMBAAGj\n" +
            "UzBRMB0GA1UdDgQWBBQGgHjDtfiNZxIJn9tYpN5Id3x7UjAfBgNVHSMEGDAWgBQG\n" +
            "gHjDtfiNZxIJn9tYpN5Id3x7UjAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEB\n" +
            "CwUAA4IBAQAOJST5+fH9Z2L+q6u+IW45YYqNLpgjPlMujPKiOckgM77zZCQNTQGA\n" +
            "IhwloGOGcr5L0iRo58tVxejPYwxIIA8BKyeBtKwY63MBiGCzKs9N7UaePtPSdfmJ\n" +
            "CwN16umYelH14W4tsnxAqTGptO4VQDHyZ948EZa+S6GlYB7Cq4zZlwuCzoypQD0i\n" +
            "IO63SoCC5OQOdcy+qV4Ma5h5Metgcrb7sx3YWzcxEoUXTxCcYDUffa0OY3SDFe8P\n" +
            "JiHTaCaMeu/jzZQD7AlLbP476wML1k4bvn3156GsfBYFexxoL+Mg6pCckvYp+iHM\n" +
            "N4SLKvQzH2koDdS9bxANXqto26y3rsuV\n" +
            "-----END CERTIFICATE-----";

}
