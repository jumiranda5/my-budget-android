package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.util.Log;

import com.jgm.mybudgetapp.objects.MyDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyDateUtils {
    public MyDateUtils() {}

    public static MyDate getCurrentDate(Context context) {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // month in date lib is from 0-11 => return as 1-12
        month++;

        String monthName = getMonthName(context, month, year)[0];
        MyDate date = new MyDate(day, month, year);
        date.setMonthName(monthName);

        return date;
    }

    public static String getFormattedFieldDate(Context context, int year, int month, int day) {
        Log.d("debug-date", "date-picker month => " + month);

        // month in date lib is from 0-11
        month--;

        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        long dateMilliseconds = calendar.getTimeInMillis();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DATE_FIELD, locale);
        return dateFormat.format(dateMilliseconds);
    }

    public static String[] getMonthName(Context context, int month, int year) {

        // month in date lib is from 0-11
        month--;

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

    public static MyDate getDateFromMilliseconds(Context context, long milliseconds) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", locale);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("EEE", locale);
        String dateString = simpleDateFormat.format(milliseconds);
        String weekday = simpleDateFormat2.format(milliseconds);

        Log.d("debug-date", dateString);

        int day = Integer.parseInt(dateString.split("/")[0]);
        int month = Integer.parseInt(dateString.split("/")[1]);
        int year = Integer.parseInt(dateString.split("/")[2]);

        MyDate myDate = new MyDate(day, month, year);
        myDate.setWeekday(weekday);

        return myDate;
    }

    public static int[] getNextTransactionDate(int currentMonth, int currentYear) {

        int nextMonth;
        int nextYear;

        if (currentMonth == 12) {
            nextMonth = 1;
            nextYear = currentYear + 1;
        }
        else {
            nextMonth = currentMonth + 1;
            nextYear = currentYear;
        }

        return new int[] { nextMonth, nextYear };
    }

}
