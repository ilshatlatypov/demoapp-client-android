package ru.jvdev.demoapp.client.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.jvdev.demoapp.client.android.R;

/**
 * Created by ilshat on 13.09.16.
 */
public class DateUtils {

    @SuppressLint("SimpleDateFormat")
    private static DateFormat DF = new SimpleDateFormat("d MMMM");

    private DateUtils() {}

    public static boolean isToday(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return isLaterByDays(cal, now, 0);
    }

    public static boolean isTomorrow(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return isLaterByDays(cal, now, 1);
    }

    public static boolean isDayAfterTomorrow(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return isLaterByDays(cal, now, 2);
    }

    /**
     * Checks if c1 later c2 by some number of days
     */
    private static boolean isLaterByDays(Calendar c1, Calendar c2, int days) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH) + days;
    }

    public static String dateToString(Context context, Date taskDate) {
        String dateAsStr;
        if (DateUtils.isToday(taskDate)) {
            dateAsStr = context.getString(R.string.prompt_today);
        } else if (DateUtils.isTomorrow(taskDate)) {
            dateAsStr = context.getString(R.string.prompt_tomorrow);
        } else if (DateUtils.isDayAfterTomorrow(taskDate)) {
            dateAsStr = context.getString(R.string.prompt_day_after_tomorrow);
        } else {
            dateAsStr = DF.format(taskDate);
        }
        return dateAsStr;
    }
}
