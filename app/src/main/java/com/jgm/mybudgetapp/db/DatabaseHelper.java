package com.jgm.mybudgetapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "budget.db";
    private static final int DB_VERSION = 3;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.Categories.SQL_CREATE_TABLE);
        db.execSQL(DatabaseContract.CreditCards.SQL_CREATE_TABLE);
        db.execSQL(DatabaseContract.Accounts.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL(DatabaseContract.CreditCards.SQL_CREATE_TABLE);
        if (oldVersion < 3) db.execSQL(DatabaseContract.Accounts.SQL_CREATE_TABLE);
    }
}
