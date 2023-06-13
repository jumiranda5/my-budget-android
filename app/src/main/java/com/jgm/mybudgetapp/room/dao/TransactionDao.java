package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

}
