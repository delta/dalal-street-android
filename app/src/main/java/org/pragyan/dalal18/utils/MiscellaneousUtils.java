package org.pragyan.dalal18.utils;

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
            str = outputFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String sessionId = "dalalStreetSessionId";
    public static String username = null;

}
