package com.jgm.mybudgetapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Query("SELECT * FROM categories WHERE active = 1 ORDER BY name ASC")
    List<Category> getCategories();

    @Update
    void update(Category category);

    @Query("SELECT COUNT(id) FROM categories AS count")
    int getCategoriesCount();

    @Query("SELECT * FROM categories LIMIT 1")
    Category getDefaultCategory();

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getCategoryById(int id);

    @Query("DELETE FROM categories")
    void clearCategoriesTable();

}
