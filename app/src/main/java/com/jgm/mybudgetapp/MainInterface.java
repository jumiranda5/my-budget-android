package com.jgm.mybudgetapp;

import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.room.entity.Transaction;

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
    void openAccountDetails(AccountTotal accountTotal, int position);
    void openAccountForm(boolean isEdit, Account account, int position);
    void openYear();
    void openCardDetails(CreditCard card, int position);
    void openCardForm(boolean isEdit, CreditCard card, int position);
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
    void showDatePickerDialog();
    void showMethodPickerDialog(boolean isExpense);
    ArrayList<PaymentMethod> getMethodsList();
    void setSelectedPaymentMethod(PaymentMethod paymentMethod);

    // Settings
    void switchDarkMode(boolean isDark);

    // Categories
    void setSelectedCategory(Category category);

    // Accounts
    void updateAccountInserted(Account account, boolean isEdit, int position);

    // DATABASE

    void getCategoriesData();
    void insertCategoryData(Category category);
    void editCategoryData(int position, Category category);

    void getCreditCardsData();
    void insertCreditCardData(CreditCard card);
    void editCreditCardData(int position, CreditCard card);

    // Date
    MyDate getDate();

}
