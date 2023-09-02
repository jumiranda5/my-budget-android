package com.jgm.mybudgetapp.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.dao.CardDao;
import com.jgm.mybudgetapp.room.dao.CategoryDao;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.room.entity.Transaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Category.class, Account.class,
        CreditCard.class, Transaction.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AccountDao AccountDao();
    public abstract CategoryDao CategoryDao();
    public abstract CardDao CardDao();
    public abstract TransactionDao TransactionDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "budget_app")
                            .build();


                }
            }
        }
        return INSTANCE;
    }

    /* migration docs: https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=pt-br  */

}
