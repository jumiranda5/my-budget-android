package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.util.Log;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.CategoryDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.util.ArrayList;

public class Populate {

    public Populate() {}

    public static void initDefaultAccounts(Context context) {
        // todo => and if list is empty
        boolean hasInitialAccounts = SettingsPrefs.getSettingsPrefsBoolean(context, "hasInitialAccounts");
        if (!hasInitialAccounts) {

            Log.d(Tags.LOG_DB, "== INIT ACCOUNT DEFAULT LIST");

            ArrayList<Account> list = new ArrayList<>();

            Account cash = new Account(context.getString(R.string.account_cash), 21, 67, 0, true);
            Account checking = new Account(context.getString(R.string.account_checking), 14, 68, 1, true);
            Account savings = new Account(context.getString(R.string.account_savings), 20, 69, 2, true);

            list.add(cash);
            list.add(checking);
            list.add(savings);

            for (int i = 0; i < list.size(); i++) {
                Account newAccount = list.get(i);
                AppDatabase.dbExecutor.execute(() -> {
                    AppDatabase.getDatabase(context).AccountDao().insert(newAccount);
                });
            }

            SettingsPrefs.setSettingsPrefsBoolean(context, "hasInitialAccounts", true);

        }
    }

    public static void initDefaultCategories(Context context) {

        // todo => and if list is empty
        boolean hasInitialCategories = SettingsPrefs.getSettingsPrefsBoolean(context, "hasInitialCategories");

        if (!hasInitialCategories) {

            Log.d(Tags.LOG_DB, "== INIT CATEGORIES DEFAULT LIST");

            ArrayList<com.jgm.mybudgetapp.room.entity.Category> list = new ArrayList<>();

            Category c1 = new Category(context.getString(R.string.category_home), 3, 6, true);
            Category c2 = new Category(context.getString(R.string.category_health), 5, 34, true);
            Category c3 = new Category(context.getString(R.string.category_groceries), 14, 9, true);
            Category c4 = new Category(context.getString(R.string.category_transport), 11, 29, true);
            Category c5 = new Category(context.getString(R.string.category_leisure), 1, 46, true);
            Category c6 = new Category(context.getString(R.string.category_education), 7, 15, true);
            Category c7 = new Category(context.getString(R.string.category_work), 4, 11, true);

            list.add(c1);
            list.add(c2);
            list.add(c3);
            list.add(c4);
            list.add(c5);
            list.add(c6);
            list.add(c7);

            CategoryDao categoryDao = AppDatabase.getDatabase(context).CategoryDao();
            AppDatabase.dbExecutor.execute(() -> {

                for (int i = 0; i < list.size(); i++) {
                    categoryDao.insert(list.get(i));
                }

            });

            SettingsPrefs.setSettingsPrefsBoolean(context, "hasInitialCategories", true);
        }

    }
}
