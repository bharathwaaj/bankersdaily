package in.bankersdaily.util;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import in.bankersdaily.network.ApiClient;

public class FormatDate {

    private static final String ABBREV_DAY = "d ago";
    private static final String ABBREV_HOUR = "h ago";
    private static final String ABBREV_MINUTE = "m ago";

    @SuppressLint("SimpleDateFormat")
    public static String getISODateString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ApiClient.TIME_ZONE));
        return simpleDateFormat.format(date);
    }

    public static String getAbbreviatedTimeSpan(long timeMillis) {
        long span = Math.max(System.currentTimeMillis() - timeMillis, 0);
        if (span >= DateUtils.WEEK_IN_MILLIS) {
            return DateUtils.getRelativeTimeSpanString(timeMillis).toString();
        }
        if (span >= DateUtils.DAY_IN_MILLIS) {
            return (span / DateUtils.DAY_IN_MILLIS) + ABBREV_DAY;
        }
        if (span >= DateUtils.HOUR_IN_MILLIS) {
            long hour = span / DateUtils.HOUR_IN_MILLIS;
            return hour + ABBREV_HOUR;
        }
        long min = span / DateUtils.MINUTE_IN_MILLIS;
        if (min == 0) {
            return "Just now";
        }
        return min + ABBREV_MINUTE;
    }

}
