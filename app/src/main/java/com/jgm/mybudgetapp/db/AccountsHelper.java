package com.jgm.mybudgetapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jgm.mybudgetapp.objects.Account;

import java.util.ArrayList;
import java.util.Objects;

import com.jgm.mybudgetapp.db.DatabaseContract.Accounts;

public class AccountsHelper {

    public AccountsHelper() {}

    private static final String LOG = "debug-db-accounts";

    public static long create(SQLiteDatabase db, Account account) {

        ContentValues values = new ContentValues();
        values.put(Accounts.COLUMN_NAME, account.getName());
        values.put(Accounts.COLUMN_COLOR, account.getColorId());
        values.put(Accounts.COLUMN_ICON, account.getIconId());
        values.put(Accounts.COLUMN_TYPE, account.getType());

        try {
            Log.d(LOG, "row successfully inserted on Accounts table");
            return db.insert(Accounts.TABLE_NAME, null, values);
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
            return -1;
        }
    }

    public static ArrayList<Account> readAll(SQLiteDatabase db) {
        ArrayList<Account> accounts = new ArrayList<>();

        final String[] allColumns = {
                Accounts._ID,
                Accounts.COLUMN_NAME,
                Accounts.COLUMN_COLOR,
                Accounts.COLUMN_ICON,
                Accounts.COLUMN_TYPE,
                Accounts.COLUMN_ACTIVE
        };

        try {
            String selection = DatabaseContract.Accounts.COLUMN_ACTIVE + "=?";
            String[] selectionArgs = new String[]{ "1" };

            Cursor cursor = db.query(Accounts.TABLE_NAME,
                    allColumns, selection, selectionArgs, null, null,
                    Accounts.COLUMN_TYPE + " ASC, " + Accounts.COLUMN_NAME + " ASC");

            int _id = cursor.getColumnIndex(Accounts._ID);
            int _name = cursor.getColumnIndex(Accounts.COLUMN_NAME);
            int _color = cursor.getColumnIndex(Accounts.COLUMN_COLOR);
            int _icon = cursor.getColumnIndex(Accounts.COLUMN_ICON);
            int _type = cursor.getColumnIndex(Accounts.COLUMN_TYPE);
            int _active = cursor.getColumnIndex(Accounts.COLUMN_ACTIVE);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(_id);
                String name = cursor.getString(_name);
                int colorId = cursor.getInt(_color);
                int iconId = cursor.getInt(_icon);
                int type = cursor.getInt(_type);
                int active = cursor.getInt(_active);

                boolean isActive = active != 0;

                Account account = new Account(id, name, colorId, iconId, type, isActive);
                accounts.add(account);
                cursor.moveToNext();

            }
            cursor.close();

            Log.d(LOG, "Successfully read Accounts table: " + accounts.size());
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
        }



        return accounts;
    }

    public static void updateRow(SQLiteDatabase db, Account account) {

        try {
            int active;
            if (account.isActive()) active = 1;
            else active = 0;

            ContentValues cv = new ContentValues();
            cv.put("name", account.getName());
            cv.put("color_id", account.getColorId());
            cv.put("icon_id", account.getIconId());
            cv.put("type", account.getType());
            cv.put("active", active);

            db.update(Accounts.TABLE_NAME, cv, Accounts._ID + " = " + account.getId(), null);

            Log.d(LOG, "Successfully updated Account, id: " + account.getId());
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
        }

    }

    public static void deleteRow(SQLiteDatabase db, int id) {
        try {
            db.delete(Accounts.TABLE_NAME, Accounts._ID + " = " + id, null);
            Log.d(LOG, "Successfully deleted Account, id: " + id);
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
        }

    }

}
