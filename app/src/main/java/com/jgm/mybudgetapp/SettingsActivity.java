package com.jgm.mybudgetapp;

import static com.jgm.mybudgetapp.utils.Tags.accountDetailsTag;
import static com.jgm.mybudgetapp.utils.Tags.accountFormTag;
import static com.jgm.mybudgetapp.utils.Tags.cardFormTag;
import static com.jgm.mybudgetapp.utils.Tags.cardsTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesFormTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesListTag;
import static com.jgm.mybudgetapp.utils.Tags.homeTag;
import static com.jgm.mybudgetapp.utils.Tags.settingsTag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.jgm.mybudgetapp.dialogs.ColorPickerDialog;
import com.jgm.mybudgetapp.dialogs.IconPickerDialog;
import com.jgm.mybudgetapp.fragmentsMain.CategoriesFormFragment;
import com.jgm.mybudgetapp.fragmentsMain.CategoriesListFragment;
import com.jgm.mybudgetapp.fragmentsSettings.CreditCardFormFragment;
import com.jgm.mybudgetapp.fragmentsSettings.CreditCardsFragment;
import com.jgm.mybudgetapp.fragmentsSettings.SettingsFragment;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements SettingsInterface {

    // todo: something is wrong with dark mode after screen rotation
    // todo: credit card archive is returning to settings. Should return to card list...
    // todo: add loader on review ads button click

    // Constants
    private static final String LOG_SETTINGS = "debug-settings";
    private static final String STATE_FRAGMENT = "current-fragment";
    private static final String STATE_TAG_LIST = "fragment-tag-list";

    // FRAGMENTS

    private CategoriesListFragment mCategoriesList;
    private CategoriesFormFragment mCategoriesForm;
    private CreditCardsFragment mCreditCards;
    private CreditCardFormFragment mCreditCardForm;
    private SettingsFragment mSettings;

    // Vars
    private ArrayList<String> mFragmentTagList = new ArrayList<>();
    private String currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(Tags.LOG_LIFECYCLE, "Settings Activity onCreate");

        // set dark/light mode
        if (savedInstanceState == null) {
            Log.d(LOG_SETTINGS, "Set dark/light mode");
            boolean isDark = SettingsPrefs.getSettingsPrefsBoolean(this, "isDark");
            switchDarkMode(isDark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            Log.d(LOG_SETTINGS, "savedInstance is null");
            setFragment(settingsTag);
        }
        else {
            Log.d(LOG_SETTINGS, "savedInstance NOT null => todo...");
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentFragment = savedInstanceState.getString(STATE_FRAGMENT);
        mFragmentTagList = savedInstanceState.getStringArrayList(STATE_TAG_LIST);

        Log.i(Tags.LOG_LIFECYCLE, "onRestoreInstanceState => current fragment: " + currentFragment);

        for (int i = 0; i < mFragmentTagList.size(); i++) {
            reReferenceFragment(mFragmentTagList.get(i));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(Tags.LOG_LIFECYCLE, "Main Activity onSaveInstanceState");
        outState.putString(STATE_FRAGMENT, currentFragment);
        outState.putStringArrayList(STATE_TAG_LIST, mFragmentTagList);
    }


    /* ===============================================================================
                                         INTERFACE
     =============================================================================== */

    @Override
    public void switchDarkMode(boolean isDark) {
        Log.d(LOG_SETTINGS, "=> switchDarkMode");
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (isDark) {
            Log.d(LOG_SETTINGS, "Dark Mode");
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            Log.d(LOG_SETTINGS, "Light Mode");
            if (currentNightMode != Configuration.UI_MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void open(String tag) {
        Log.d(LOG_SETTINGS, "-- Interface => open");
        openFragment(tag);
    }


    @Override
    public void navigateBack() {
        Log.d(LOG_SETTINGS, "-- Interface => navigate back");
        onBackPressed();
    }

    @Override
    public void openCategoriesList(boolean isEdit) {
        Log.d(LOG_SETTINGS, "-- Interface => open categories list");
        openFragment(categoriesListTag);
        if (mCategoriesList != null) mCategoriesList.setListType(isEdit);
    }

    @Override
    public void openCategoryForm(boolean isEdit, Category category, int position) {
        Log.d(LOG_SETTINGS, "-- Interface => open category form");
        openFragment(categoriesFormTag);
        if (mCategoriesForm != null) {
            mCategoriesForm.setFormType(isEdit);
            mCategoriesForm.setCategoryToEdit(category, position);
        }
    }

    @Override
    public void handleCategoryInserted(Category category) {
        Log.d(LOG_SETTINGS, "-- Interface => handleCategoryInserted: " + category.getName());
        if (mCategoriesList != null) mCategoriesList.updateListAfterDbInsertion(category);
    }

    @Override
    public void handleCategoryEdited(int position, Category category) {
        Log.d(LOG_SETTINGS, "-- Interface => handleCategoryEdited: " + category.getName() + " pos: " + position);
        if (mCategoriesList != null) {
            if (category.isActive()) mCategoriesList.updateListAfterEdit(position, category);
            else mCategoriesList.updateListAfterDelete(position);
        }
    }

    /*  CREDIT CARDS  */

    @Override
    public void openCardForm(boolean isEdit, CreditCard card, int position) {
        Log.d(LOG_SETTINGS, "-- Interface => open card form");
        openFragment(cardFormTag);
        if (mCreditCardForm != null) {
            mCreditCardForm.setFormType(isEdit);
            if (isEdit) mCreditCardForm.setCreditCard(card, position);
        }
    }

    @Override
    public void handleCreditCardInserted(CreditCard card) {
        Log.d(LOG_SETTINGS, "-- Interface => handleCreditCardInserted: " + card.getName());
        if (mCreditCards != null) mCreditCards.updateUiAfterInsertion(card);
    }

    @Override
    public void handleCreditCardEdited(int position, CreditCard card) {
        Log.d(LOG_SETTINGS, "-- Interface => handleCreditCardEdited: " + card.getName() + " pos: " + position);
        if (card.isActive() && mCreditCards != null) {
            mCreditCards.updateListAfterEdit(position, card);
        }
        else {
            // close cards details fragment and update main
            onBackPressed();
            mCreditCards.updateListAfterDelete(position);
        }
    }

    /*  ARCHIVE  */

    @Override
    public void showConfirmationDialog(String message, String title, int iconDrawable) {
        Log.d(LOG_SETTINGS, "-- Interface => show confirmation dialog");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SettingsActivity.this)
                .setIcon(iconDrawable)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.action_confirm), (dialog, which) -> {
                    handleConfirmation();
                    dialog.dismiss();
                });
        builder.create();
        builder.show();
    }

    @Override
    public void handleConfirmation() {
        if (currentFragment.equals(cardFormTag) && mCreditCardForm != null)
            mCreditCardForm.handleArchiveConfirmation();
        else if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null)
            mCategoriesForm.handleArchiveConfirmation();
        else if (currentFragment.equals(settingsTag) && mSettings != null)
            mSettings.clearDatabase();
    }

    @Override
    public void showColorPickerDialog() {
        Log.d(LOG_SETTINGS, "-- Interface => Show color picker dialog");
        BottomSheetDialogFragment colorPicker = new ColorPickerDialog();
        colorPicker.show(getSupportFragmentManager(), "colorPicker");
    }

    @Override
    public void handleColorSelection(Color color) {
        Log.d(LOG_SETTINGS, "-- Interface => handleColorSelection");
        if (currentFragment.equals(cardFormTag) && mCreditCardForm != null) {
            mCreditCardForm.setSelectedColor(color);
        }
        else if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null) {
            mCategoriesForm.setSelectedColor(color);
        }
    }

    @Override
    public void showIconPickerDialog() {
        Log.d(LOG_SETTINGS, "-- Interface => showIconPickerDialog");
        BottomSheetDialogFragment iconPicker = new IconPickerDialog();
        iconPicker.show(getSupportFragmentManager(), "iconPicker");
    }

    @Override
    public void handleIconSelection(Icon icon) {
        Log.d(LOG_SETTINGS, "-- Interface => handleIconSelection "  + icon.getIconName());
        if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null) mCategoriesForm.setSelectedIcon(icon);
    }


    /* ===============================================================================
                                     FRAGMENT NAVIGATION
     =============================================================================== */

    private void openFragment(String tag) {
        Log.d(Tags.LOG_NAV, "== openFragment / " + tag + " | " + "currentFragment " + currentFragment);
        if (!currentFragment.equals(tag)) setFragment(tag);
    }

    private void setFragment(String tag) {

        Log.d(Tags.LOG_NAV, "== setFragment / " + tag);

        Fragment fragment;

        switch (tag) {
            case categoriesListTag:
                mCategoriesList = new CategoriesListFragment();
                fragment = mCategoriesList;
                break;
            case categoriesFormTag:
                mCategoriesForm = new CategoriesFormFragment();
                fragment = mCategoriesForm;
                break;
            case cardsTag:
                mCreditCards = new CreditCardsFragment();
                fragment = mCreditCards;
                break;
            case cardFormTag:
                mCreditCardForm = new CreditCardFormFragment();
                fragment = mCreditCardForm;
                break;
            case settingsTag:
                mSettings = new SettingsFragment();
                fragment = mSettings;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tag);
        }

        if (tag.equals(categoriesListTag) || tag.equals(categoriesFormTag) || tag.equals(cardFormTag)) {
            Log.d(Tags.LOG_NAV, "Add fragment: " + tag);
            addFragment(fragment, tag);
        }
        else {
            Log.d(Tags.LOG_NAV, "Replace fragment: " + tag);
            replaceFragment(fragment, tag);
        }

    }

    private void replaceFragment(Fragment fragment, String tag) {

        Log.d(Tags.LOG_NAV, "== replaceFragment: new fragment = " + tag + " old fragment = " + currentFragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.settings_content_frame, fragment, tag);
        transaction.commit();

        // Set replaced fragment to null
        deReferenceFragment(currentFragment);

        // Update custom navigation stack
        mFragmentTagList.remove(tag);
        mFragmentTagList.add(tag);

        // Set current fragment tag
        currentFragment = tag;

        Log.d(Tags.LOG_NAV, "New fragment loaded: " + tag);

    }

    private void addFragment(Fragment fragment, String tag) {

        Log.d(Tags.LOG_NAV, "== Fragment to be added: " + tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.settings_content_frame, fragment, tag);
        transaction.commit();

        // hide prev fragment
        Log.d(Tags.LOG_NAV, "Hide prev fragment: " + currentFragment);
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        switch (tag) {
            case categoriesListTag: transaction2.hide(mSettings); break;
            case categoriesFormTag: transaction2.hide(mCategoriesList); break;
            case cardFormTag: transaction2.hide(mCreditCards); break;
        }
        transaction2.commit();

        // Update custom navigation stack
        mFragmentTagList.remove(tag);
        mFragmentTagList.add(tag);

        // Set current fragment tag
        currentFragment = tag;
        Log.d(Tags.LOG_NAV, "New fragment loaded: " + tag);
    }

    private void showHiddenFragment(String tag, String prevTag) {
        Log.d(Tags.LOG_NAV, "== Show hidden fragment: " + tag);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (prevTag) {
            case categoriesListTag: transaction.show(mSettings); break;
            case categoriesFormTag: transaction.show(mCategoriesList); break;
            case cardFormTag: transaction.show(mCreditCards); break;
        }

        transaction.commit();
        currentFragment = tag;
    }

    private void destroyFragment(Fragment fragment, String tag) {
        Log.d(Tags.LOG_NAV, "== Destroy fragment: " + tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();

        // show prev fragment
        Log.d(Tags.LOG_NAV, "Show hidden fragment: " + currentFragment);
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        switch (tag) {
            case categoriesListTag: transaction2.show(mSettings); break;
            case categoriesFormTag: transaction2.show(mCategoriesList); break;
            case cardFormTag: transaction2.show(mCreditCards); break;
        }
        transaction2.commit();
    }

    @Override
    public void onBackPressed() {

        Log.d(Tags.LOG_NAV, "== onBackPressed");

        int backStackCount = mFragmentTagList.size();
        String topFragmentTag = mFragmentTagList.get(backStackCount - 1);

        if(backStackCount > 1){
            // Get previous fragment from stack
            String newTopFragmentTag = mFragmentTagList.get(backStackCount - 2);
            Log.d(Tags.LOG_NAV, "current fragment (remove): " + topFragmentTag);
            Log.d(Tags.LOG_NAV, "previous fragment (return to): " + newTopFragmentTag);

            mFragmentTagList.remove(topFragmentTag);

            // remove or replace top fragment
            switch (topFragmentTag) {
                case categoriesListTag:
                    destroyFragment(mCategoriesList, categoriesListTag);
                    break;
                case categoriesFormTag:
                    destroyFragment(mCategoriesForm, categoriesFormTag);
                    break;
                case cardFormTag:
                    destroyFragment(mCreditCardForm, cardFormTag);
                    break;
                default:
                    setFragment(newTopFragmentTag);
                    break;
            }

            // show hidden newTopFragment
            if (topFragmentTag.equals(accountDetailsTag)
                    || topFragmentTag.equals(accountFormTag)
                    || topFragmentTag.equals(cardFormTag)
                    || topFragmentTag.equals(categoriesFormTag)
                    || topFragmentTag.equals(categoriesListTag)) {

                Log.d(Tags.LOG_NAV, "show hidden newTopFragment");
                showHiddenFragment(newTopFragmentTag, topFragmentTag);

            }

        }
        else if( backStackCount == 1 ){
            Log.d(Tags.LOG_NAV, "EXIT");
            super.onBackPressed();
        }
    }

    // for screen rotation...
    private void reReferenceFragment(String tag) {

        Log.d(Tags.LOG_NAV, "========= re-reference: " + tag);

        switch (tag) {
            case categoriesListTag:
                mCategoriesList = (CategoriesListFragment) getSupportFragmentManager().findFragmentByTag(categoriesListTag);
                break;
            case categoriesFormTag:
                mCategoriesForm = (CategoriesFormFragment) getSupportFragmentManager().findFragmentByTag(categoriesFormTag);
                break;
            case cardsTag:
                mCreditCards = (CreditCardsFragment) getSupportFragmentManager().findFragmentByTag(cardsTag);
                break;
            case cardFormTag:
                mCreditCardForm = (CreditCardFormFragment) getSupportFragmentManager().findFragmentByTag(cardFormTag);
                break;
            case settingsTag:
                mSettings = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(settingsTag);
                break;
        }
    }

    private void deReferenceFragment(String tag) {

        Log.d(Tags.LOG_NAV, "========= de-reference: " + tag);

        if (tag != null) {
            switch (tag) {
                case categoriesListTag: mCategoriesList = null; break;
                case categoriesFormTag: mCategoriesForm = null; break;
                case cardsTag: mCreditCards = null; break;
                case cardFormTag: mCreditCardForm = null; break;
                case settingsTag: mSettings = null; break;
            }
        }

    }

}