package com.jgm.mybudgetapp.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyDateUtils {
    public MyDateUtils() {}

    public static String[] getMonthName(Context context, int month, int year) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        long dateMilliseconds = calendar.getTimeInMillis();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM", locale);
        String month_name = month_date.format(dateMilliseconds);
        String month_name_upper = month_name.substring(0, 1).toUpperCase() + month_name.substring(1);

        SimpleDateFormat month_date_abbr = new SimpleDateFormat("MMM", locale);
        String month_name_abbr = month_date_abbr.format(dateMilliseconds);
        String month_name_abbr_upper =
                month_name_abbr.substring(0, 1).toUpperCase() + month_name_abbr.substring(1);

        return new String[]{month_name_upper, month_name_abbr_upper};

    }

}
