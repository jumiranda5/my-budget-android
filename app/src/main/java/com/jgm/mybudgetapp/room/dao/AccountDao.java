package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.MapInfo;
import androidx.room.Query;
import androidx.room.Update;

import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Transaction;

import java.util.List;
import java.util.Map;

@Dao
public interface AccountDao {

    @Insert
    long insert(Account account);

    @Query("SELECT * FROM accounts WHERE active = 1 ORDER BY type, name ASC")
    List<Account> getAccounts();

    @Update
    void update(Account account);

    // Get accounts with totals by type
    @MapInfo(valueColumn = "total")
    @Query("SELECT accounts.*, SUM(transactions.amount) AS total " +
            "FROM accounts " +
            "LEFT JOIN transactions ON transactions.accountId = accounts.id " +
            "GROUP BY accounts.id " +
            "ORDER BY type, name ASC")
    Map<Account, String> getAccountsWithTotals();


}
