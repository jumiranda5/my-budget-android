package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.util.Log;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.util.ArrayList;

public class Populate {

    private final static String LOG_POPULATE = "debug-populate";


    // Populate accounts table with default accounts
    public static void initDefaultAccounts(Context context) {

        Log.d(LOG_POPULATE, "=> Init default accounts");

        boolean hasInitialAccounts = SettingsPrefs.getSettingsPrefsBoolean(context, Tags.keyInitialAccounts);
        Log.d(LOG_POPULATE, "hasInitialAccounts: " + hasInitialAccounts);

        if (!hasInitialAccounts) {

            Log.d(LOG_POPULATE, "Check if accounts table is empty");

            // get accounts count => if 0 => populate
            AppDatabase db = AppDatabase.getDatabase(context);
            AppDatabase.dbExecutor.execute(() -> {

                int count = db.AccountDao().getAccountsCount();
                Log.d(Tags.LOG_DB, "Get accounts count: " + count);
                Log.d(LOG_POPULATE, "Accounts count: " + count);

                if (count == 0) {
                    Log.d(LOG_POPULATE, "Populate table with defaults");

                    ArrayList<Account> list = new ArrayList<>();

                    Account cash = new Account(context.getString(R.string.account_cash), 21, 67, 0, true);
                    Account checking = new Account(context.getString(R.string.account_checking), 14, 68, 1, true);
                    Account savings = new Account(context.getString(R.string.account_savings), 20, 69, 2, true);

                    list.add(cash);
                    list.add(checking);
                    list.add(savings);

                    for (int i = 0; i < list.size(); i++) {
                        Account newAccount = list.get(i);
                        AppDatabase.getDatabase(context).AccountDao().insert(newAccount);
                        Log.d(Tags.LOG_DB, "Account inserted: " + newAccount.getName());
                    }

                    SettingsPrefs.setSettingsPrefsBoolean(context, Tags.keyInitialAccounts, true);

                }
                else SettingsPrefs.setSettingsPrefsBoolean(context, Tags.keyInitialAccounts, true);

            });
        }
    }

    // Populate categories table with default categories
    public static void initDefaultCategories(Context context) {

        Log.d(LOG_POPULATE, "=> Init default categories");

        boolean hasInitialCategories = SettingsPrefs.getSettingsPrefsBoolean(context, Tags.keyInitialCategories);
        Log.d(LOG_POPULATE, "hasInitialAccounts: " + hasInitialCategories);

        if (!hasInitialCategories) {

            Log.d(LOG_POPULATE, "Check if categories table is empty");

            // get categories count => if 0 => populate
            AppDatabase db = AppDatabase.getDatabase(context);
            AppDatabase.dbExecutor.execute(() -> {

                int count = db.CategoryDao().getCategoriesCount();
                Log.d(Tags.LOG_DB, "Get categories count: " + count);
                Log.d(LOG_POPULATE, "Categories count: " + count);

                if (count == 0) {
                    Log.d(LOG_POPULATE, "Populate table with defaults");

                    ArrayList<Category> list = new ArrayList<>();

                    Category c0 = new Category(context.getString(R.string.category_default), 16, 0, false);
                    Category c1 = new Category(context.getString(R.string.category_home), 3, 6, true);
                    Category c2 = new Category(context.getString(R.string.category_health), 5, 34, true);
                    Category c3 = new Category(context.getString(R.string.category_groceries), 14, 9, true);
                    Category c4 = new Category(context.getString(R.string.category_transport), 10, 29, true);
                    Category c5 = new Category(context.getString(R.string.category_leisure), 9, 46, true);
                    Category c6 = new Category(context.getString(R.string.category_education), 7, 15, true);
                    Category c7 = new Category(context.getString(R.string.category_work), 4, 11, true);
                    Category c8 = new Category(context.getString(R.string.category_pharmacy), 6, 10, true);
                    Category c9 = new Category(context.getString(R.string.category_closet), 2, 17, true);

                    list.add(c0);
                    list.add(c1);
                    list.add(c2);
                    list.add(c3);
                    list.add(c4);
                    list.add(c5);
                    list.add(c6);
                    list.add(c7);
                    list.add(c8);
                    list.add(c9);

                    for (int i = 0; i < list.size(); i++) {
                        db.CategoryDao().insert(list.get(i));
                        Log.d(Tags.LOG_DB, "Category inserted: " + list.get(i).getName());
                    }

                    SettingsPrefs.setSettingsPrefsBoolean(context, Tags.keyInitialCategories, true);
                }
                else SettingsPrefs.setSettingsPrefsBoolean(context, Tags.keyInitialCategories, true);

            });
        }
    }
}
