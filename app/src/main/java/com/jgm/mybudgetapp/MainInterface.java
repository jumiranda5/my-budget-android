package com.jgm.mybudgetapp;

import com.jgm.mybudgetapp.objects.Account;
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;

import java.util.ArrayList;

public interface MainInterface {

    // Navigation
    void openExpenses();
    void openIncome();
    void openCategoriesList(boolean isEdit);
    void openExpensesCategories();
    void openIncomeCategories();
    void openCategoryForm(boolean isEdit, Category category, int position);
    void openAccounts();
    void openAccountDetails(Account account, int position);
    void openAccountForm(boolean isEdit, Account account, int position);
    void openYear();
    void openCardDetails(Card card, int position);
    void openCardForm(boolean isEdit, Card card, int position);
    void openTransactionForm();
    void navigateBack();

    // Dialogs
    void showConfirmationDialog(String message);
    void handleConfirmation();
    void showTransactionDialog();
    void showColorPickerDialog();
    void handleColorSelection(Color color);
    void showIconPickerDialog();
    void handleIconSelection(Icon icon);

    // Settings
    void switchDarkMode(boolean isDark);

    // Categories
    void setSelectedCategory(Category category);


    // DATABASE

    void getAccountsData();
    void insertAccountData(Account account);
    void editAccountData(int position, Account account);

    void getCategoriesData();
    void insertCategoryData(Category category);
    void editCategoryData(int position, Category category);

    void getCreditCardsData();
    void insertCreditCardData(Card card);
    void editCreditCardData(int position, Card card);

}
