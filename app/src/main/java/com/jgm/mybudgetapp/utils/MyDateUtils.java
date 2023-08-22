package com.jgm.mybudgetapp.utils;

import static com.jgm.mybudgetapp.utils.Tags.transactionFormTag;

import android.content.Context;
import android.util.Log;

import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;

public class MyDateUtils {
    public MyDateUtils() {}

    public static MyDate getCurrentDate(Context context) {

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = now.format(formatter);

        int day = Integer.parseInt(formattedDate.split("-")[2]);
        int month = Integer.parseInt(formattedDate.split("-")[1]);
        int year = Integer.parseInt(formattedDate.split("-")[0]);

        String monthName = getMonthName(context, month, year)[0];
        MyDate date = new MyDate(day, month, year);
        date.setMonthName(monthName);

        return date;
    }

    public static MyDate getNextMonth(Context context, int currentMonth, int currentYear) {
        int nextMonth = currentMonth;
        int nextYear = currentYear;

        if (currentMonth == 12) {
            nextMonth = 1;
            nextYear++;
        }
        else {
            nextMonth++;
        }

        MyDate dateObj = new MyDate(1, nextMonth, nextYear);
        dateObj.setMonthName(getMonthName(context, nextMonth, nextYear)[0]);

        return dateObj;
    }

    public static MyDate getPrevMonth(Context context, int currentMonth, int currentYear) {
        int prevMonth = currentMonth;
        int prevYear = currentYear;

        if (currentMonth == 1) {
            prevMonth = 12;
            prevYear--;
        }
        else {
            prevMonth--;
        }

        MyDate dateObj = new MyDate(1, prevMonth, prevYear);
        dateObj.setMonthName(getMonthName(context, prevMonth, prevYear)[0]);

        return dateObj;
    }

    public static String getFormattedFieldDate(Context context, int year, int month, int day) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        LocalDate localDate = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);

        return localDate.format(formatter);
    }

    public static String[] getMonthName(Context context, int month, int year) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);

        LocalDate localDate = LocalDate.of(year, month, 1);
        Month monthObj = localDate.getMonth();
        String monthFull = monthObj.getDisplayName(TextStyle.FULL, locale);
        String monthShort = monthObj.getDisplayName(TextStyle.SHORT, locale);

        String month_full_upper = monthFull.substring(0, 1).toUpperCase() + monthFull.substring(1);
        String month_short_upper = monthShort.substring(0, 1).toUpperCase() + monthShort.substring(1);

        return new String[]{month_full_upper, month_short_upper};

    }

    public static MyDate getDateFromMilliseconds(Context context, long milliseconds) {

        Locale locale = context.getResources().getConfiguration().getLocales().get(0);

        LocalDate localDate = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);

        int year = Integer.parseInt(formattedDate.split("-")[0]);
        int month = Integer.parseInt(formattedDate.split("-")[1]);
        int day = Integer.parseInt(formattedDate.split("-")[2]);

        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        String weekday = dayOfWeek.getDisplayName(TextStyle.SHORT, locale);

        MyDate myDate = new MyDate(day, month, year);
        myDate.setWeekday(weekday);

        return myDate;
    }

    public static String[] getDayOfWeek(Context context, int day, int month, int year) {

        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        LocalDate localDate = LocalDate.of(year, month, day);
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        String dayShort = dayOfWeek.getDisplayName(TextStyle.SHORT, locale);
        String dayFull = dayOfWeek.getDisplayName(TextStyle.FULL, locale);

        dayFull = dayFull.replace("-feira", "");

        String day_full_upper = dayFull.substring(0, 1).toUpperCase() + dayFull.substring(1);

        return new String[] {dayShort, day_full_upper};
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

    public static long getLocalDateTimeMilliseconds(long selection) {

        // create an object with a matching timezone
        LocalDateTime utcDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(selection), ZoneOffset.UTC);

        // create an object converting from UTC to the device´s timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.of(utcDate, ZoneId.systemDefault());

        // get the local date in millis
        return zonedDateTime.toInstant().toEpochMilli();

    }

    public static long getLockTimer(Context context, String tag) {

        long reward = SettingsPrefs.getSettingsPrefsMilliseconds(context, tag);
        Log.d("debug-ads", "reward time: " + reward);
        if (reward == 0) return 0;
        else {
            long now = Instant.now().toEpochMilli();
            long lockTimer = now - reward;
            long hour = 60 * 60000;
            Log.d("debug-ads", "lock time: " + lockTimer);
            Log.d("debug-ads", "now: " + now);
            if (lockTimer > hour) {
                lockTimer = 0;
                SettingsPrefs.setSettingsPrefsMilliseconds(context, tag, 0);
            }
            Log.d("debug-ads", "timer: " + lockTimer);
            return lockTimer;
        }
    }

}
