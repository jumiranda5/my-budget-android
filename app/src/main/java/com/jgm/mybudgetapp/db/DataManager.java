package com.jgm.mybudgetapp.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jgm.mybudgetapp.objects.Category;

import java.util.ArrayList;

public class DataManager {

    private SQLiteDatabase mDatabase;
    private final DatabaseHelper mDatabaseHelper;
    private final Context mContext;

    public DataManager(Context context){
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }

    //open

    public void open() throws SQLException {
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    //close
    public void close(){
        mDatabase.close();
    }


    /* ================ ACCOUNTS CRUD  ================== */



    /* ================ CATEGORIES CRUD  ================== */

    public long createCategory(Category category) {
        return CategoriesHelper.create(mDatabase, category);
    }

    public ArrayList<Category> getAllCategories() {
        return CategoriesHelper.readAll(mDatabase);
    }

    public void updateCategory(Category category) {
        CategoriesHelper.updateRow(mDatabase, category);
    }

    public void deleteCategory(int id) {
        CategoriesHelper.deleteRow(mDatabase, id);
    }


    /* ================ CREDIT CARDS CRUD  ================== */



    /* ================ TRANSACTIONS CRUD  ================== */
}
