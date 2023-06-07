package com.jgm.mybudgetapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jgm.mybudgetapp.objects.Card;

import java.util.ArrayList;
import java.util.Objects;

public class CreditCardsHelper {

    public CreditCardsHelper() {}

    public static long create(SQLiteDatabase db, Card card) {

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CreditCards.COLUMN_NICKNAME, card.getName());
        values.put(DatabaseContract.CreditCards.COLUMN_COLOR, card.getColorId());
        values.put(DatabaseContract.CreditCards.COLUMN_BILLING_DAY, card.getBillingDay());
        values.put(DatabaseContract.CreditCards.COLUMN_ACTIVE, 1);

        try {
            Log.d("debug-database", "row successfully inserted on CreditCards table");
            return db.insert(DatabaseContract.CreditCards.TABLE_NAME, null, values);
        }
        catch (Exception e) {
            Log.e("debug-database", Objects.requireNonNull(e.getMessage()));
            return -1;
        }
    }

    public static ArrayList<Card> readAll(SQLiteDatabase db) {
        ArrayList<Card> cards = new ArrayList<>();

        final String[] allColumns = {
                DatabaseContract.CreditCards._ID,
                DatabaseContract.CreditCards.COLUMN_NICKNAME,
                DatabaseContract.CreditCards.COLUMN_COLOR,
                DatabaseContract.CreditCards.COLUMN_BILLING_DAY,
                DatabaseContract.CreditCards.COLUMN_ACTIVE
        };

        try {
            String selection = DatabaseContract.CreditCards.COLUMN_ACTIVE + "=?";
            String[] selectionArgs = new String[]{ "1" };

            Cursor cursor = db.query(DatabaseContract.CreditCards.TABLE_NAME,
                    allColumns, selection, selectionArgs, null, null,
                    DatabaseContract.CreditCards.COLUMN_BILLING_DAY + " ASC");

            int _id = cursor.getColumnIndex(DatabaseContract.CreditCards._ID);
            int _name = cursor.getColumnIndex(DatabaseContract.CreditCards.COLUMN_NICKNAME);
            int _color = cursor.getColumnIndex(DatabaseContract.CreditCards.COLUMN_COLOR);
            int _billingDay = cursor.getColumnIndex(DatabaseContract.CreditCards.COLUMN_BILLING_DAY);
            int _active = cursor.getColumnIndex(DatabaseContract.CreditCards.COLUMN_ACTIVE);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(_id);
                String name = cursor.getString(_name);
                int colorId = cursor.getInt(_color);
                int billingDay = cursor.getInt(_billingDay);
                int active = cursor.getInt(_active);

                boolean isActive = active != 0;

                Card card = new Card(id, name, colorId, billingDay, isActive);
                cards.add(card);

                cursor.moveToNext();

            }
            cursor.close();

            Log.d("debug-database-cards", "Successfully read CreditCards table: " + cards.size());
        }
        catch (Exception e) {
            Log.e("debug-database-cards", Objects.requireNonNull(e.getMessage()));
        }



        return cards;
    }

    public static void updateRow(SQLiteDatabase db, Card card) {

        try {
            int active;
            if (card.isActive()) active = 1;
            else active = 0;

            ContentValues cv = new ContentValues();
            cv.put("nickname", card.getName());
            cv.put("color", card.getColorId());
            cv.put("billing_day", card.getBillingDay());
            cv.put("is_active", active);

            db.update(DatabaseContract.CreditCards.TABLE_NAME, cv,
                    DatabaseContract.CreditCards._ID + " = " + card.getId(), null);

            Log.d("debug-database-cards", "Successfully updated Credit Card, id: " + card.getId());
        }
        catch (Exception e) {
            Log.e("debug-database-cards", Objects.requireNonNull(e.getMessage()));
        }

    }

    public static void deleteRow(SQLiteDatabase db, int id) {
        try {
            db.delete(DatabaseContract.CreditCards.TABLE_NAME, DatabaseContract.CreditCards._ID + " = " + id, null);
            Log.d("debug-database", "Successfully deleted Credit Card, id: " + id);
        }
        catch (Exception e) {
            Log.e("debug-database", Objects.requireNonNull(e.getMessage()));
        }
    }
}
