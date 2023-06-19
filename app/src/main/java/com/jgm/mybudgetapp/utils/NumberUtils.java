package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.icu.util.Currency;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class NumberUtils {
    public NumberUtils() {}

    public static float roundFloat(float value) {
        try {
            BigDecimal bd = new BigDecimal(Float.toString(value));
            bd = bd.setScale(2, RoundingMode.HALF_DOWN);
            return bd.floatValue();
        }
        catch (Exception e) {
            Log.w("debug-number", "division by zero => " + e);
            return 0.00f;
        }
    }

    public static String[] getCurrencyFormat(Context context, float value) {
        // Todo => get locale and currency from shared prefs
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        Currency currency = Currency.getInstance(locale);
        String currencySymbol = currency.getSymbol(locale);

        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());

        String valueString = format.format(value);
        String number = valueString.replaceAll("([^0-9|.,-])", "");

        return new String[]{currencySymbol, number, valueString};
    }
}
