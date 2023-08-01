package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.widget.TextView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.DayGroup;

import java.util.ArrayList;

public class TransactionsUtils {

    public TransactionsUtils() {}

    public static void setTotalsTextViews(Context context,
                                          TextView mTotal, TextView mDue, TextView mPaid,
                                          float total, float due, float paid) {
        // Set total in currency format
        String totalCurrency = NumberUtils.getCurrencyFormat(context, total)[2];
        String totalCurrencyPositive = totalCurrency.replace("-", "");
        mTotal.setText(totalCurrencyPositive);

        // Set due in currency format
        String dueCurrency = NumberUtils.getCurrencyFormat(context, due)[2];
        String dueCurrencyPositive = dueCurrency.replace("-", "");
        String dueText = context.getString(R.string.label_due) + " " + dueCurrencyPositive;
        mDue.setText(dueText);

        // Set paid in currency format
        String paidCurrency = NumberUtils.getCurrencyFormat(context, paid)[2];
        String paidCurrencyPositive = paidCurrency.replace("-", "");
        String paidText = context.getString(R.string.label_paid2) + " " + paidCurrencyPositive;
        mPaid.setText(paidText);
    }

}
