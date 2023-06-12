package com.jgm.mybudgetapp.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jgm.mybudgetapp.objects.Transaction;

import java.util.Objects;

public class TransactionsHelper {

    public TransactionsHelper() {}

    public static long create(SQLiteDatabase db, Transaction transaction) {

        int paid;
        if (transaction.isPaid()) paid = 1;
        else paid = 0;

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Transactions.COLUMN_TYPE, transaction.getType());
        values.put(DatabaseContract.Transactions.COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(DatabaseContract.Transactions.COLUMN_AMOUNT, transaction.getAmount());
        values.put(DatabaseContract.Transactions.COLUMN_YEAR, transaction.getYear());
        values.put(DatabaseContract.Transactions.COLUMN_MONTH, transaction.getMonth());
        values.put(DatabaseContract.Transactions.COLUMN_DAY, transaction.getDay());
        values.put(DatabaseContract.Transactions.COLUMN_CATEGORY_ID, transaction.getCategoryId());
        values.put(DatabaseContract.Transactions.COLUMN_PAID, paid);
        values.put(DatabaseContract.Transactions.COLUMN_REPEAT, transaction.getRepeat());

        if (transaction.getAccountId() != 0)
            values.put(DatabaseContract.Transactions.COLUMN_ACCOUNT_ID, transaction.getAccountId());

        if (transaction.getCardId() != 0)
            values.put(DatabaseContract.Transactions.COLUMN_CARD_ID, transaction.getCardId());

        if (transaction.getRepeat() > 1) {
            values.put(DatabaseContract.Transactions.COLUMN_REPEAT_COUNT, transaction.getRepeatCount());
            values.put(DatabaseContract.Transactions.COLUMN_REPEAT_ID, transaction.getRepeatId());
        }

        try {
            Log.d("debug-database", "row successfully inserted on Transactions table");
            return db.insert(DatabaseContract.Transactions.TABLE_NAME, null, values);
        }
        catch (Exception e) {
            Log.e("debug-database", Objects.requireNonNull(e.getMessage()));
            return -1;
        }
    }

}
