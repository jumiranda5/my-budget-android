package com.jgm.mybudgetapp;

import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;

public interface MainInterface {

    // Navigation
    void open(String tag);
    void openCategoriesList(boolean isEdit);
    void openExpensesCategories();
    void openIncomeCategories();
    void openCategoryForm(boolean isEdit, Category category, int position);
    void openAccountDetails(AccountTotal accountTotal, int position);
    void openAccountForm(boolean isEdit, Account account, int position);
    void openTransactionForm(int type, boolean isEdit, TransactionResponse transaction, PaymentMethod paymentMethod);
    void navigateBack();

    // Dialogs
    void showConfirmationDialog(String message, String title, int iconDrawable);
    void handleConfirmation();
    void showTransactionDialog(TransactionResponse transaction);
    TransactionResponse getSelectedTransactionData();
    void handleTransactionDeleted(int id);
    void showColorPickerDialog();
    void handleColorSelection(Color color);
    void showIconPickerDialog();
    void handleIconSelection(Icon icon);
    void showDatePickerDialog();
    void showMethodPickerDialog(boolean isExpense, TransactionResponse item, int position);
    boolean getMethodDialogType();
    void setSelectedPaymentMethod(PaymentMethod paymentMethod);

    // Transactions
    void updateTotal(float value, boolean isPaid);

    // Categories
    void setSelectedCategory(Category category);
    void handleCategoryInserted(Category category);

    // Accounts
    void updateAccountInserted(Account account, boolean isEdit, int position);

    // Date
    MyDate getDate();

    void setSelectedToolbarDate(int day, int month, int year);

}
