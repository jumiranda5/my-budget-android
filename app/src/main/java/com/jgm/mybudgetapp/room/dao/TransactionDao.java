package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jgm.mybudgetapp.objects.Balance;
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.objects.HomeAccounts;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.objects.TransactionResponse;
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

    @Query("DELETE FROM transactions WHERE id=:id")
    int deleteById(int id);

    @Query("DELETE FROM transactions WHERE repeatId=:repeatId")
    void deleteByRepeatId(long repeatId);

    @Query("UPDATE transactions SET paid = :isPaid WHERE id = :id")
    void updatePaid(int id, boolean isPaid);

    @Query("UPDATE transactions " +
            "SET description = :description, amount = :amount, " +
            "cardId = :cardId, accountId = :accountId, day = :day, categoryId = :categoryId " +
            "WHERE repeatId = :repeatId ")
    void updateAllParcels(long repeatId, float amount, String description,
                       int cardId, int accountId, int categoryId, int day);

    @Query("UPDATE transactions " +
            "SET description = :description, amount = :amount, " +
            "cardId = :cardId, accountId = :accountId, day = :day, categoryId = :categoryId " +
            "WHERE id = :id ")
    void updateParcel(int id, float amount, String description,
                          int cardId, int accountId, int categoryId, int day);

    @Query("UPDATE transactions " +
            "SET paid = :isPaid, accountId = :accountId " +
            "WHERE cardId = :cardId " +
            "AND month = :month " +
            "AND year = :year")
    void updatePaidCard(int cardId, boolean isPaid, int month, int year, int accountId);

    /* ------------------------------------------------------------------------------
                                  TRANSACTIONS FRAGMENT
    ------------------------------------------------------------------------------- */
    @Query("SELECT transactions.*, categories.name AS categoryName, categories.colorId, categories.iconId " +
            "FROM transactions " +
            "JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE transactions.year = :year " +
            "AND transactions.month = :month " +
            "AND transactions.type = :type " +
            "ORDER BY transactions.day, transactions.cardId ")
    List<TransactionResponse> getTransactions(int type, int month, int year);

    @Query("SELECT SUM(transactions.amount) AS total, cards.* " +
            "FROM transactions " +
            "JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.cardId = :cardId " +
            "AND transactions.day = :billingDay " +
            "AND transactions.month = :month " +
            "AND transactions.year = :year ")
    Card getCreditCardWithTotal(int cardId, int billingDay, int month, int year);

    /* ------------------------------------------------------------------------------
                                     ACCOUNT FRAGMENT
    ------------------------------------------------------------------------------- */
    @Query("SELECT * FROM transactions " +
            "WHERE accountId = :accountId " +
            "AND year = :year " +
            "AND month = :month ")
    List<Transaction> getAccountTransactions(int accountId, int year, int month);

    /* ------------------------------------------------------------------------------
                                     PENDING FRAGMENT
    ------------------------------------------------------------------------------- */
    @Query("SELECT transactions.*, categories.name AS categoryName, categories.colorId, categories.iconId " +
            "FROM transactions " +
            "JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE paid = 0 " +
            "AND NOT (day > :day AND month >= :month AND year >= :year) " +
            "ORDER BY year, month, day")
    List<TransactionResponse> getPendingList(int day, int month, int year);


    /* ------------------------------------------------------------------------------
                                     HOME FRAGMENT
    ------------------------------------------------------------------------------- */

    @Query("SELECT COUNT(paid) FROM transactions " +
            "WHERE paid = 0 " +
            "AND NOT (day > :day AND month >= :month AND year >= :year)")
    int getPendingCount(int day, int month, int year);

    @Query("SELECT SUM(amount) FROM transactions WHERE year <= :year and month < :month")
    float getAccumulated(int month, int year);

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

    @Query("SELECT SUM(transactions.amount) AS total, categories.name AS category, categories.colorId, categories.iconId " +
            "FROM transactions " +
            "JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE transactions.year = :year AND transactions.month = :month AND transactions.type = :type " +
            "GROUP BY categories.id " +
            "ORDER BY total")
    List<CategoryResponse> getCategoriesWithTotals(int month, int year, int type);

    @Query("SELECT month, " +
            "    SUM(CASE WHEN type = '1' THEN amount ELSE 0 END) AS income, " +
            "    SUM(CASE WHEN type = '-1' THEN amount ELSE 0 END) AS expenses " +
            "    FROM transactions " +
            "    WHERE year = :year" +
            "    GROUP BY month;")
    List<MonthResponse> getYearBalance(int year);

}
