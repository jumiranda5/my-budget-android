package com.jgm.mybudgetapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jgm.mybudgetapp.objects.Category;

import java.util.ArrayList;
import java.util.Objects;

public class CategoriesHelper {

    public CategoriesHelper() {}

    private static final String LOG = "debug-database";

    public static long create(SQLiteDatabase db, Category category) {

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Categories.COLUMN_NAME, category.getName());
        values.put(DatabaseContract.Categories.COLUMN_COLOR, category.getColorId());
        values.put(DatabaseContract.Categories.COLUMN_ICON, category.getIconId());
        values.put(DatabaseContract.Categories.COLUMN_ACTIVE, 1);

        try {
            Log.d(LOG, "row successfully inserted on Categories table");
            return db.insert(DatabaseContract.Categories.TABLE_NAME, null, values);
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
            return -1;
        }
    }

    public static ArrayList<Category> readAll(SQLiteDatabase db) {
        ArrayList<Category> categories = new ArrayList<>();

        final String[] allColumns = {
                DatabaseContract.Categories._ID,
                DatabaseContract.Categories.COLUMN_NAME,
                DatabaseContract.Categories.COLUMN_COLOR,
                DatabaseContract.Categories.COLUMN_ICON,
                DatabaseContract.Categories.COLUMN_ACTIVE
        };

        try {
            String selection = DatabaseContract.Categories.COLUMN_ACTIVE + "=?";
            String[] selectionArgs = new String[]{ "1" };

            Cursor cursor = db.query(DatabaseContract.Categories.TABLE_NAME,
                    allColumns, selection, selectionArgs, null, null,
                    DatabaseContract.Categories.COLUMN_NAME + " ASC");

            int _id = cursor.getColumnIndex(DatabaseContract.Categories._ID);
            int _name = cursor.getColumnIndex(DatabaseContract.Categories.COLUMN_NAME);
            int _color = cursor.getColumnIndex(DatabaseContract.Categories.COLUMN_COLOR);
            int _icon = cursor.getColumnIndex(DatabaseContract.Categories.COLUMN_ICON);
            int _active = cursor.getColumnIndex(DatabaseContract.Categories.COLUMN_ACTIVE);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(_id);
                String name = cursor.getString(_name);
                int colorId = cursor.getInt(_color);
                int iconId = cursor.getInt(_icon);
                int active = cursor.getInt(_active);

                boolean isActive = active != 0;

                Category category = new Category(id, name, colorId, iconId, isActive);
                categories.add(category);
                cursor.moveToNext();

            }
            cursor.close();

            Log.d(LOG, "Successfully read Categories table: " + categories.size());
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
        }

        return categories;
    }

    public static void updateRow(SQLiteDatabase db, Category category) {

        try {
            int active;
            if (category.isActive()) active = 1;
            else active = 0;

            ContentValues cv = new ContentValues();
            cv.put("name", category.getName());
            cv.put("color", category.getColorId());
            cv.put("icon", category.getIconId());
            cv.put("active", active);

            db.update(DatabaseContract.Categories.TABLE_NAME, cv,
                    DatabaseContract.Categories._ID + " = " + category.getId(), null);

            Log.d(LOG, "Successfully updated Account, id: " + category.getId());
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
        }

    }

    public static void deleteRow(SQLiteDatabase db, int id) {
        try {
            db.delete(DatabaseContract.Categories.TABLE_NAME, DatabaseContract.Categories._ID + " = " + id, null);
            Log.d(LOG, "Successfully deleted Category, id: " + id);
        }
        catch (Exception e) {
            Log.e(LOG, Objects.requireNonNull(e.getMessage()));
        }
    }
}

