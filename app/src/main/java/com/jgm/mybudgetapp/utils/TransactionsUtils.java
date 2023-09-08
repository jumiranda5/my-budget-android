package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.TransactionResponse;

public class TransactionsUtils {

    public static void setTotalsTextViews(Context context, int type,
                                          TextView mTotal, TextView mDue, TextView mPaid,
                                          float total, float due, float paid) {
        // Set total in currency format
        String totalCurrency = NumberUtils.getCurrencyFormat(context, total)[2];
        String totalCurrencyPositive = totalCurrency.replace("-", "");
        mTotal.setText(totalCurrencyPositive);

        String dueLabel;
        String paidLabel;
        if (type == Tags.TYPE_OUT) {
            dueLabel = context.getString(R.string.label_due);
            paidLabel = context.getString(R.string.label_paid2);
        }
        else {
            dueLabel = context.getString(R.string.label_receivables);
            paidLabel = context.getString(R.string.label_received);
        }

        // Set due in currency format
        String dueCurrency = NumberUtils.getCurrencyFormat(context, due)[2];
        String dueCurrencyPositive = dueCurrency.replace("-", "");
        String dueText = dueLabel + " " + dueCurrencyPositive;
        mDue.setText(dueText);

        // Set paid in currency format
        String paidCurrency = NumberUtils.getCurrencyFormat(context, paid)[2];
        String paidCurrencyPositive = paidCurrency.replace("-", "");
        String paidText = paidLabel + " " + paidCurrencyPositive;
        mPaid.setText(paidText);
    }

    public static void setDueAndPaidIconColor(Context context, ImageView paidIcon, ImageView dueIcon,
                                              float paid, float due) {

        if (paid != 0) paidIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.check_circle_color));
        else paidIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.medium_emphasis_text));

        if (due != 0) dueIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.danger));
        else dueIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.medium_emphasis_text));

    }

    public static TransactionResponse setAccumulated(Context context, float value, int month, int year) {
        return new TransactionResponse(0,
                Tags.TYPE_OUT,
                context.getString(R.string.label_accumulated),
                value,
                year, month, 1,
                0, 0, 0, true,
                1, 1, null,
                "",
                23,
                71);
    }

}
