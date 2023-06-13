package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jgm.mybudgetapp.room.entity.CreditCard;

import java.util.List;

@Dao
public interface CardDao {

    @Insert
    long insert(CreditCard creditCard);

    @Query("SELECT * FROM cards WHERE active = 1 ORDER BY billingDay ASC")
    List<CreditCard> getCreditCards();

    @Update
    void update(CreditCard creditCard);

}
