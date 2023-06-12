package com.jgm.mybudgetapp.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jgm.mybudgetapp.objects.Account;
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.objects.Transaction;

import java.util.ArrayList;

public class DataManager {

    private SQLiteDatabase mDatabase;
    private final DatabaseHelper mDatabaseHelper;
    private final Context mContext;

    public DataManager(Context context){
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }

    //open

    public void open() throws SQLException {
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    //close
    public void close(){
        mDatabase.close();
    }


    /* ================ ACCOUNTS CRUD  ================== */

    public long createAccount(Account account) {
        return AccountsHelper.create(mDatabase, account);
    }

    public ArrayList<Account> getAllAccounts() {
        return AccountsHelper.readAll(mDatabase);
    }

    public void updateAccount(Account account) {
        AccountsHelper.updateRow(mDatabase, account);
    }

    public void deleteAccount(int id) {
        AccountsHelper.deleteRow(mDatabase, id);
    }


    /* ================ CATEGORIES CRUD  ================== */

    public long createCategory(Category category) {
        return CategoriesHelper.create(mDatabase, category);
    }

    public ArrayList<Category> getAllCategories() {
        return CategoriesHelper.readAll(mDatabase);
    }

    public void updateCategory(Category category) {
        CategoriesHelper.updateRow(mDatabase, category);
    }

    public void deleteCategory(int id) {
        CategoriesHelper.deleteRow(mDatabase, id);
    }


    /* ================ CREDIT CARDS CRUD  ================== */

    public long createCreditCard(Card card) {
        return CreditCardsHelper.create(mDatabase, card);
    }

    public ArrayList<Card> getAllCreditCards() {
        return CreditCardsHelper.readAll(mDatabase);
    }

    public void updateCreditCard(Card card) {
        CreditCardsHelper.updateRow(mDatabase, card);
    }

    public void deleteCreditCard(int id) {
        CreditCardsHelper.deleteRow(mDatabase, id);
    }


    /* ================ TRANSACTIONS CRUD  ================== */

    public long createTransaction(Transaction transaction) {return TransactionsHelper.create(mDatabase, transaction); }

}
