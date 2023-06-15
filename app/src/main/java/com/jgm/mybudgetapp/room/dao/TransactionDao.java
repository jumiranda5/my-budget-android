package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jgm.mybudgetapp.objects.Balance;
import com.jgm.mybudgetapp.objects.HomeAccounts;
import com.jgm.mybudgetapp.objects.HomeCategory;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.room.entity.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions")
    List<Transaction> getAllTransactions();

    @Update
    void update(Transaction transaction);

    // Account fragment
    @Query("SELECT * FROM transactions " +
            "WHERE accountId = :accountId " +
            "AND year = :year " +
            "AND month = :month ")
    List<Transaction> getAccountTransactions(int accountId, int year, int month);

    // Home fragment

    @Query("SELECT SUM (amount) AS balance, " +
            "SUM(CASE WHEN type = '1' THEN amount ELSE 0 END) AS income, " +
            "SUM(CASE WHEN type = '-1' THEN amount ELSE 0 END) AS expenses " +
            "FROM transactions " +
            "WHERE year = :year AND month = :month")
    Balance getHomeBalance(int month, int year);

    @Query("SELECT SUM(CASE WHEN accounts.type = 0 THEN transactions.amount ELSE 0 END) AS cash, " +
            "    SUM(CASE WHEN accounts.type = 1 THEN transactions.amount ELSE 0 END) AS checking, " +
            "    SUM(CASE WHEN accounts.type = 2 THEN transactions.amount ELSE 0 END) AS savings " +
            "FROM accounts " +
            "INNER JOIN transactions ON accounts.id = transactions.accountId " +
            "WHERE transactions.paid = 1")
    HomeAccounts getAccountsTotals();

    @Query("SELECT SUM(transactions.amount) AS total, categories.name AS category, categories.colorId " +
            "FROM transactions " +
            "JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE transactions.year = :year AND transactions.month = :month AND transactions.type = :type " +
            "GROUP BY categories.id " +
            "ORDER BY total")
    List<HomeCategory> getHomeCategories(int month, int year, int type);

    @Query("SELECT month, " +
            "    SUM(CASE WHEN type = '1' THEN amount ELSE 0 END) AS income, " +
            "    SUM(CASE WHEN type = '-1' THEN amount ELSE 0 END) AS expenses " +
            "    FROM transactions " +
            "    WHERE year = :year" +
            "    GROUP BY month;")
    List<MonthResponse> getYearBalance(int year);

}
