package com.tripexpense.tracker.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    private static final String DEFAULT_FORMAT = "dd MMM yyyy";
    private static final String TIME_FORMAT = "hh:mm a";

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT + " " + TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
