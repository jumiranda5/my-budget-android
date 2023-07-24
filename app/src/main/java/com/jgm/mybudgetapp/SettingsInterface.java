package com.jgm.mybudgetapp;

import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.room.entity.CreditCard;

public interface SettingsInterface {

    void switchDarkMode(boolean isDark);
    void open(String tag);
    void navigateBack();

    // Categories
    void openCategoriesList(boolean isEdit);
    void openCategoryForm(boolean isEdit, Category category, int position);
    void handleCategoryInserted(Category category);
    void handleCategoryEdited(int position, Category category);

    // Credit Cards
    void openCardForm(boolean isEdit, CreditCard card, int position);
    void handleCreditCardInserted(CreditCard card);
    void handleCreditCardEdited(int position, CreditCard card);

    // Dialogs
    void showConfirmationDialog(String message, String title, int iconDrawable);
    void handleConfirmation();
    void showColorPickerDialog();
    void handleColorSelection(Color color);
    void showIconPickerDialog();
    void handleIconSelection(Icon icon);

}
