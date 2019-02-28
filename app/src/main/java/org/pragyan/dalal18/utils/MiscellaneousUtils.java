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

    /* Main Server Certificate */

    // TODO (Release) : Update server crt with main server
    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGVzCCBT+gAwIBAgIQAaErP9OTRnmFLsW/CUGW3TANBgkqhkiG9w0BAQsFADCB\n" +
            "kDELMAkGA1UEBhMCR0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4G\n" +
            "A1UEBxMHU2FsZm9yZDEaMBgGA1UEChMRQ09NT0RPIENBIExpbWl0ZWQxNjA0BgNV\n" +
            "BAMTLUNPTU9ETyBSU0EgRG9tYWluIFZhbGlkYXRpb24gU2VjdXJlIFNlcnZlciBD\n" +
            "QTAeFw0xODA4MjQwMDAwMDBaFw0xOTA4MjQyMzU5NTlaMFsxITAfBgNVBAsTGERv\n" +
            "bWFpbiBDb250cm9sIFZhbGlkYXRlZDEeMBwGA1UECxMVRXNzZW50aWFsU1NMIFdp\n" +
            "bGRjYXJkMRYwFAYDVQQDDA0qLnByYWd5YW4ub3JnMIIBIjANBgkqhkiG9w0BAQEF\n" +
            "AAOCAQ8AMIIBCgKCAQEAxgxvjBwmAjA1rHY+fs3/GqstQzJvu2bQGemSxIvgUKaR\n" +
            "iwe+lWclaOmYYKZOtdUWgxcD3rN3lTe76Ui5kRvnr6mRXExoDx6g+GkGyA1cFVPG\n" +
            "DFXFwNNxkNS1eGe6J7MJa2ResJCTwPVC1CupSWw2unWcaCDuXKhVCyPq3Ym0gw4t\n" +
            "iJWVVZ7K1b6CfzTQbT/hp+cKNnD92uX1p91+3eF9SILsOlumpFG8f0DYGzUc46yI\n" +
            "xL1QYiylR8+WLtmM/HLZ3cjkQ8boYXO3zcVVCFZjdotR5BG+6f1uN1YXqWzTyRKz\n" +
            "eTZPh3MR6ZavN4L0O6V7ZKWzdOfIrEJtfRmdMio1kQIDAQABo4IC3zCCAtswHwYD\n" +
            "VR0jBBgwFoAUkK9qOpRaC9iQ6hJWc99DtDoo2ucwHQYDVR0OBBYEFGdeyQKhYcB2\n" +
            "lm4eMZuVdU1F5EvdMA4GA1UdDwEB/wQEAwIFoDAMBgNVHRMBAf8EAjAAMB0GA1Ud\n" +
            "JQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBPBgNVHSAESDBGMDoGCysGAQQBsjEB\n" +
            "AgIHMCswKQYIKwYBBQUHAgEWHWh0dHBzOi8vc2VjdXJlLmNvbW9kby5jb20vQ1BT\n" +
            "MAgGBmeBDAECATBUBgNVHR8ETTBLMEmgR6BFhkNodHRwOi8vY3JsLmNvbW9kb2Nh\n" +
            "LmNvbS9DT01PRE9SU0FEb21haW5WYWxpZGF0aW9uU2VjdXJlU2VydmVyQ0EuY3Js\n" +
            "MIGFBggrBgEFBQcBAQR5MHcwTwYIKwYBBQUHMAKGQ2h0dHA6Ly9jcnQuY29tb2Rv\n" +
            "Y2EuY29tL0NPTU9ET1JTQURvbWFpblZhbGlkYXRpb25TZWN1cmVTZXJ2ZXJDQS5j\n" +
            "cnQwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmNvbW9kb2NhLmNvbTAlBgNVHREE\n" +
            "HjAcgg0qLnByYWd5YW4ub3JnggtwcmFneWFuLm9yZzCCAQQGCisGAQQB1nkCBAIE\n" +
            "gfUEgfIA8AB2AO5Lvbd1zmC64UJpH6vhnmajD35fsHLYgwDEe4l6qP3LAAABZWw9\n" +
            "DeEAAAQDAEcwRQIhAPFTcod1kLHQAH7XrHhQNK6Dn7ObO6kWc2oIK0d47/WxAiA2\n" +
            "L/DgZ0SCrCEOGdtXevipFpjQINiEcdly+K1H66nsiwB2AHR+2oMxrTMQkSGcziVP\n" +
            "QnDCv/1eQiAIxjc1eeYQe8xWAAABZWw9DqcAAAQDAEcwRQIgOKrbnHbVUtIFjCr3\n" +
            "V8IFg0gvUK3FGyV8W+UJ3DsVSTUCIQDSf2rRBWMYN/pP7qWcm9aZC72sR9XDPwy+\n" +
            "/OGsxMR7SDANBgkqhkiG9w0BAQsFAAOCAQEAGAjFI99dlrLQvyhHOMQPONR0tF3B\n" +
            "RRRr/3jpzP+VaJb3qpmpAFeXGevOFOOeH2vooBTfdjPphzuqRNNa7SjAgmYDyN6i\n" +
            "2HsCnm1mxPVMYiyykecdiCxGGfzN7+YvY/RQiy19XvpGXmvAdLxzEhaojRIysSEV\n" +
            "23R1ivf8vmYzsQLJxhJAsgluohKgIl0BdqkjJ0Gi1TnmFxQksfHyxQH8y6jqB1rT\n" +
            "9ZwfH95QNgv7S916dAGIAzHEG8Uv+5T1zrHn9OT4gQLODuwvTz+39dTat75kTxWh\n" +
            "v5bTWiCJ6Kt0iOuIke6rnyYF9BqIn772NLEhYY7qS5wy/r2FZu7qhMh/aA==\n" +
            "-----END CERTIFICATE-----";

}
