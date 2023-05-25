package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.icu.util.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class NumberUtils {
    public NumberUtils() {}

    public static float roundFloat(float value) {
        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_DOWN);
        return bd.floatValue();
    }

    public static String getCurrencyFormat(Context context, float value) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        Currency currency = Currency.getInstance(locale);

        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());

        return format.format(value);
    }
}
