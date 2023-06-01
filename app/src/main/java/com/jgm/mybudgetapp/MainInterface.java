package com.jgm.mybudgetapp;

import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.objects.Color;

public interface MainInterface {

    // Navigation
    void openExpenses();
    void openIncome();
    void openCategoriesList(boolean isEdit);
    void openExpensesCategories();
    void openIncomeCategories();
    void openCategoryForm(boolean isEdit);
    void openAccounts();
    void openAccountDetails();
    void openAccountForm(boolean isEdit);
    void openYear();
    void openCardDetails();
    void openCardForm(boolean isEdit);
    void openTransactionForm();
    void navigateBack();

    // Dialogs
    void showConfirmationDialog(String message, int id);
    void handleConfirmation(int id);
    void showTransactionDialog();
    void showColorPickerDialog();
    void handleColorSelection(Color color);

    // Settings
    void switchDarkMode(boolean isDark);

    // Categories
    void setSelectedCategory(Category category);

}
