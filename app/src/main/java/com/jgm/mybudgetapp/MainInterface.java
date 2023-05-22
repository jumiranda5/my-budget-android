package com.jgm.mybudgetapp;

public interface MainInterface {

    // Navigation
    void openExpenses();
    void openIncome();
    void openExpensesCategories();
    void openIncomeCategories();
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

}
