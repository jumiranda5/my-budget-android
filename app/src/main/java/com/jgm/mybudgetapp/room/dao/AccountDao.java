package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jgm.mybudgetapp.room.entity.Account;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert
    long insert(Account account);

    @Query("SELECT * FROM accounts WHERE active = 1 ORDER BY type ASC, name ASC")
    List<Account> getAccounts();

    @Update
    void update(Account account);

}
