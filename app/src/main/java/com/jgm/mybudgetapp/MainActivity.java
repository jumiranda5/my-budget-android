package com.jgm.mybudgetapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jgm.mybudgetapp.dialogs.ColorPickerDialog;
import com.jgm.mybudgetapp.dialogs.ConfirmationDialog;
import com.jgm.mybudgetapp.dialogs.TransactionDialog;
import com.jgm.mybudgetapp.databinding.ActivityMainBinding;
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainInterface {

    // Constants
    private static final String LOG_NAV = "debug-nav";
    private static final String LOG_LIFECYCLE = "debug-lifecycle";
    private static final String STATE_FRAGMENT = "current-fragment";
    private static final String STATE_TAG_LIST = "fragment-tag-list";
    private static final String STATE_FRAGMENT_LIST = "fragment-list";

    // Params
    private static final String PARAM_OUT = "OUT";
    private static final String PARAM_IN = "IN";
    private static final String PARAM_IN_ADD = "IN_ADD";
    private static final String PARAM_IN_EDIT = "IN_EDIT";
    private static final String PARAM_OUT_ADD = "OUT_ADD";
    private static final String PARAM_OUT_EDIT = "OUT_EDIT";
    private static final String PARAM_ADD = "ADD";
    private static final String PARAM_EDIT = "EDIT";

    // FRAGMENTS
    private AccountsFragment mAccounts;
    private AccountFormFragment mAccountForm;
    private AccountDetailsFragment mAccountDetails;
    private CategoriesListFragment mCategoriesList;
    private CategoriesFormFragment mCategoriesForm;
    private CategoriesFragment mCategories;
    private CreditCardsFragment mCreditCards;
    private CreditCardFormFragment mCreditCardForm;
    private CreditCardDetailsFragment mCreditCardDetails;
    private HomeFragment mHome;
    private SettingsFragment mSettings;
    private TransactionFormFragment mTransactionForm;
    private TransactionsFragment mTransactions;
    private YearFragment mYear;

    // Fragment Tags
    private static final String accountsTag = "ACCOUNTS";
    private static final String accountFormTag = "ACCOUNT_FORM";
    private static final String accountDetailsTag = "ACCOUNT_DETAILS";
    private static final String categoriesTag = "CATEGORIES";
    private static final String categoriesFormTag = "CATEGORIES_FORM";
    private static final String categoriesListTag = "CATEGORIES_LIST";
    private static final String cardsTag = "CARDS";
    private static final String cardFormTag = "CARD_FORM";
    private static final String cardDetailsTag = "CARD_DETAILS";
    private static final String homeTag = "HOME";
    private static final String settingsTag = "SETTINGS";
    private static final String transactionFormTag = "TRANSACTION_FORM";
    private static final String transactionsTag = "TRANSACTIONS";
    private static final String yearTag = "YEAR";

    // Vars
    private ArrayList<String> mFragmentTagList = new ArrayList<>();
    private String currentFragment;

    // UI
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    private ImageButton settingsButton;

    private void setBinding() {
        toolbar = binding.mainToolbar;
        bottomNavigationView = binding.bottomNavigationView;
        fab = binding.fab;
        settingsButton = binding.settingsButton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set dark/light mode
        if (savedInstanceState == null) {
            boolean isDark = SettingsPrefs.getSettingsPrefsBoolean(this, "isDark");
            switchDarkMode(isDark);
        }

        super.onCreate(savedInstanceState);

        Log.d(LOG_LIFECYCLE, "Main Activity onCreate");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        if (savedInstanceState == null) setFragment(homeTag);

        initBottomBar();
        initFab();
        settingsButton.setOnClickListener(v -> openFragment(settingsTag));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_LIFECYCLE, "Main Activity onStart");
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        currentFragment = savedInstanceState.getString(STATE_FRAGMENT);
        mFragmentTagList = savedInstanceState.getStringArrayList(STATE_TAG_LIST);
        Log.d(LOG_LIFECYCLE, "onRestoreInstanceState => current fragment: " + currentFragment);
        Log.d(LOG_LIFECYCLE, "current fragment => " + currentFragment);
        reReferenceFragment();
        updateBottomNav(currentFragment);
        setToolbarVisibilities(currentFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_LIFECYCLE, "Main Activity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_LIFECYCLE, "Main Activity onPause");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_LIFECYCLE, "Main Activity onSaveInstanceState");
        outState.putString(STATE_FRAGMENT, currentFragment);
        outState.putStringArrayList(STATE_TAG_LIST, mFragmentTagList);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_LIFECYCLE, "Main Activity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_LIFECYCLE, "Main Activity onDestroy");
    }

    /* ===============================================================================
                                         TOOLBAR
     =============================================================================== */

    private void setToolbarVisibilities(String tag) {

        if (tag.equals(homeTag) || tag.equals(categoriesTag)) {
            toolbar.setVisibility(View.VISIBLE);
            if (tag.equals(homeTag)) settingsButton.setVisibility(View.VISIBLE);
            else settingsButton.setVisibility(View.GONE);
        }
        else {
            toolbar.setVisibility(View.GONE);
        }

    }

    /* ===============================================================================
                                        BOTTOM BAR
     =============================================================================== */

    private void initBottomBar() {
        MenuItem itemHome = bottomNavigationView.getMenu().getItem(0);
        MenuItem itemCategories = bottomNavigationView.getMenu().getItem(1);
        MenuItem itemCards = bottomNavigationView.getMenu().getItem(3);
        MenuItem itemAccounts = bottomNavigationView.getMenu().getItem(4);

        itemHome.setOnMenuItemClickListener(item -> {
            openFragment(homeTag);
            return false;
        });

        itemAccounts.setOnMenuItemClickListener(item -> {
            openFragment(accountsTag);
            return false;
        });

        itemCards.setOnMenuItemClickListener(item -> {
            openFragment(cardsTag);
            return false;
        });

        itemCategories.setOnMenuItemClickListener(item -> {
            openFragment(categoriesTag);
            return false;
        });

    }

    private void updateBottomNav(String tag) {

        showBottomNav();

        switch (tag) {
            case categoriesTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_categories);
                hideBottomNav();
                break;
            case accountsTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_accounts);
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_app_add_account));
                fab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.main_text));
                break;
            case cardsTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_cards);
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_app_add_card));
                fab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.main_text));
                break;
            case homeTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_home);
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_app_add));
                break;
            default:
                hideBottomNav();
        }
    }

    private void initFab() {
        fab.setOnClickListener(v -> {
            switch (currentFragment) {
                case homeTag:
                    openFragment(transactionFormTag);
                    break;
                case accountsTag:
                    openAccountForm(false);
                    break;
                case cardsTag:
                    openCardForm(false);
                    break;
            }
        });
    }

    private void hideBottomNav() {
        bottomNavigationView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }

    private void showBottomNav() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
    }

    /* ===============================================================================
                                     FRAGMENT NAVIGATION
     =============================================================================== */

    private void openFragment(String tag) {
        if (!currentFragment.equals(tag)) {
            Log.d(LOG_NAV, "OPEN " + tag);
            if (tag.equals(homeTag)) resetFragmentStack();
            setFragment(tag);
            setToolbarVisibilities(tag);
            updateBottomNav(tag);
        }
    }

    /* ===============================================================================
                                     INTERFACE NAVIGATION
     =============================================================================== */

    @Override
    public void openExpenses() {
        if (mTransactions != null) mTransactions.setTypeParam(PARAM_OUT);
        openFragment(transactionsTag);
    }

    @Override
    public void openIncome() {
        if (mTransactions != null) mTransactions.setTypeParam(PARAM_IN);
        openFragment(transactionsTag);
    }

    @Override
    public void openExpensesCategories() {
        if (mCategories != null) mCategories.setInitialTab(PARAM_OUT);
        openFragment(categoriesTag);
    }

    @Override
    public void openIncomeCategories() {
        if (mCategories != null) mCategories.setInitialTab(PARAM_IN);
        openFragment(categoriesTag);
    }

    @Override
    public void openCategoriesList(boolean isEdit) {
        openFragment(categoriesListTag);
        if (mCategoriesList != null) mCategoriesList.setListType(isEdit);
    }

    @Override
    public void openCategoryForm(boolean isEdit) {
        openFragment(categoriesFormTag);
        if (mCategoriesForm != null) mCategoriesForm.setFormType(isEdit);
    }

    @Override
    public void openAccounts() {
        openFragment(accountsTag);
    }

    @Override
    public void openAccountDetails() {
        openFragment(accountDetailsTag);
    }

    @Override
    public void openAccountForm(boolean isEdit) {
        if (mAccountForm != null) mAccountForm.setFormType(isEdit);
        openFragment(accountFormTag);
    }

    @Override
    public void openYear() {
        openFragment(yearTag);
    }

    @Override
    public void openCardDetails() {
        openFragment(cardDetailsTag);
    }

    @Override
    public void openCardForm(boolean isEdit) {
        if (mCreditCardForm != null) mCreditCardForm.setFormType(isEdit);
        openFragment(cardFormTag);
    }

    @Override
    public void openTransactionForm() {
        // todo: add param (IN/OUT && ADD/EDIT)
        setFragment(transactionFormTag);
    }

    @Override
    public void navigateBack() {
        onBackPressed();
    }

    /* ===============================================================================
                                     INTERFACE DIALOGS
     =============================================================================== */

    @Override
    public void showConfirmationDialog(String message, int id) {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance(message, id);
        confirmationDialog.show(fm, "CONFIRMATION_DIALOG");
    }

    @Override
    public void handleConfirmation(int id) {
        // todo => update item on db...
        Log.d("debug-dialog", "Id to be updated: " + id);
    }

    @Override
    public void showTransactionDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TransactionDialog transactionDialog = new TransactionDialog();
        transactionDialog.show(fm, "TRANSACTION DIALOG");
    }

    @Override
    public void showColorPickerDialog() {
        Log.d(LOG_NAV, "Show color picker dialog");
        BottomSheetDialogFragment colorPicker = new ColorPickerDialog();
        colorPicker.show(getSupportFragmentManager(), "colorPicker");
    }

    @Override
    public void handleColorSelection(Color color) {
        if (currentFragment.equals(cardFormTag) && mCreditCardForm != null) {
            mCreditCardForm.updateColor(color);
        }
        else if (currentFragment.equals(accountFormTag) && mAccountForm != null) {
            mAccountForm.updateColor(color);
        }
    }

    /* ===============================================================================
                                    INTERFACE SETTINGS
     =============================================================================== */

    @Override
    public void switchDarkMode(boolean isDark) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (isDark) {
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            if (currentNightMode != Configuration.UI_MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /* ===============================================================================
       INTERFACE                        CATEGORIES
     =============================================================================== */

    @Override
    public void setSelectedCategory(Category category) {
        mTransactionForm.setSelectedCategory(category);
        onBackPressed();
    }

    /* ===============================================================================
                                         FRAGMENTS
     =============================================================================== */

    private void resetFragmentStack() {
        mFragmentTagList.clear();
        mFragmentTagList = new ArrayList<>();
        Log.d(LOG_NAV, "Reset fragment stack: " + mFragmentTagList.size());
    }

    private void setFragment(String tag) {

        Log.d(LOG_NAV, "init fragment: " + tag);

        Fragment fragment;

        switch (tag) {
            case accountsTag:
                mAccounts = new AccountsFragment();
                fragment = mAccounts;
                break;
            case accountFormTag:
                mAccountForm = new AccountFormFragment();
                fragment = mAccountForm;
                break;
            case accountDetailsTag:
                mAccountDetails = new AccountDetailsFragment();
                fragment = mAccountDetails;
                break;
            case categoriesTag:
                mCategories = new CategoriesFragment();
                fragment = mCategories;
                break;
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
            case cardDetailsTag:
                mCreditCardDetails = new CreditCardDetailsFragment();
                fragment = mCreditCardDetails;
                break;
            case homeTag:
                mHome = new HomeFragment();
                fragment = mHome;
                break;
            case settingsTag:
                mSettings = new SettingsFragment();
                fragment = mSettings;
                break;
            case transactionsTag:
                mTransactions = new TransactionsFragment();
                fragment = mTransactions;
                break;
            case transactionFormTag:
                mTransactionForm = new TransactionFormFragment();
                fragment = mTransactionForm;
                break;
            case yearTag:
                mYear = new YearFragment();
                fragment = mYear;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tag);
        }

        if (tag.equals(categoriesListTag) || tag.equals(categoriesFormTag)) addFragment(fragment, tag);
        else replaceFragment(fragment, tag);

    }

    private void replaceFragment(Fragment fragment, String tag) {

        Log.d(LOG_NAV, "Fragment to be replaced: " + currentFragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, fragment, tag);
        transaction.commit();

        // Set replaced fragment to null
        deReferenceFragment(currentFragment);

        // Update custom navigation stack
        mFragmentTagList.remove(tag);
        mFragmentTagList.add(tag);

        // Set current fragment tag
        currentFragment = tag;

        Log.d(LOG_NAV, "Fragment loaded: " + tag);

    }

    private void addFragment(Fragment fragment, String tag) {

        Log.d(LOG_NAV, "Fragment to be added: " + currentFragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_content_frame, fragment, tag);
        transaction.commit();

        // hide prev fragment
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        if (tag.equals(categoriesListTag)) {
            if (currentFragment.equals(transactionFormTag)) transaction2.hide(mTransactionForm);
            if (currentFragment.equals(settingsTag)) transaction2.hide(mSettings);
        }
        else if (tag.equals(categoriesFormTag)) transaction2.hide(mCategoriesList);
        transaction2.commit();

        // Update custom navigation stack
        mFragmentTagList.remove(tag);
        mFragmentTagList.add(tag);

        // Set current fragment tag
        currentFragment = tag;
        Log.d(LOG_NAV, "Fragment added: " + tag);
    }

    private void destroyFragment(Fragment fragment, String tag) {
        Log.d(LOG_NAV, "Fragment to be removed: " + currentFragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();

        // show prev fragment
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        if (tag.equals(categoriesListTag)) {
            if (currentFragment.equals(transactionFormTag)) transaction2.show(mTransactionForm);
            if (currentFragment.equals(settingsTag)) transaction2.show(mSettings);
        }
        if (tag.equals(categoriesFormTag)) transaction2.show(mCategoriesList);
        transaction2.commit();
    }

    private void reReferenceFragment() {
        switch (currentFragment) {
            case accountsTag:
                mAccounts = (AccountsFragment) getSupportFragmentManager().findFragmentByTag(accountsTag);
                break;
            case accountFormTag:
                mAccountForm = (AccountFormFragment) getSupportFragmentManager().findFragmentByTag(accountFormTag);
                break;
            case accountDetailsTag:
                mAccountDetails = (AccountDetailsFragment) getSupportFragmentManager().findFragmentByTag(accountDetailsTag);
                break;
            case categoriesTag:
                mCategories = (CategoriesFragment) getSupportFragmentManager().findFragmentByTag(categoriesTag);
                break;
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
            case cardDetailsTag:
                mCreditCardDetails = (CreditCardDetailsFragment) getSupportFragmentManager().findFragmentByTag(cardDetailsTag);
                break;
            case homeTag:
                mHome = (HomeFragment) getSupportFragmentManager().findFragmentByTag(homeTag);
                break;
            case settingsTag:
                mSettings = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(settingsTag);
                break;
            case transactionsTag:
                mTransactions = (TransactionsFragment) getSupportFragmentManager().findFragmentByTag(transactionsTag);
                break;
            case transactionFormTag:
                mTransactionForm = (TransactionFormFragment) getSupportFragmentManager().findFragmentByTag(transactionFormTag);
                break;
            case yearTag:
                mYear = (YearFragment) getSupportFragmentManager().findFragmentByTag(yearTag);
                break;
        }
    }

    private void deReferenceFragment(String tag) {

        if (tag != null) {
            switch (tag) {
                case accountsTag: mAccounts = null; break;
                case accountFormTag: mAccountForm = null; break;
                case accountDetailsTag: mAccountDetails = null;break;
                case categoriesTag: mCategories = null; break;
                case categoriesListTag: mCategoriesList = null; break;
                case categoriesFormTag: mCategoriesForm = null; break;
                case cardsTag: mCreditCards = null; break;
                case cardFormTag: mCreditCardForm = null; break;
                case cardDetailsTag: mCreditCardDetails = null; break;
                case homeTag: mHome = null; break;
                case settingsTag: mSettings = null; break;
                case transactionsTag: mTransactions = null; break;
                case transactionFormTag: mTransactionForm = null; break;
                case yearTag: mYear = null; break;
            }
        }

    }

    @Override
    public void onBackPressed() {

        int backStackCount = mFragmentTagList.size();
        String topFragmentTag = mFragmentTagList.get(backStackCount - 1);

        if(backStackCount > 1){
            // Get previous fragment from stack
            String newTopFragmentTag = mFragmentTagList.get(backStackCount - 2);
            currentFragment = newTopFragmentTag;

            Log.d(LOG_NAV, "Back to: " + newTopFragmentTag);
            mFragmentTagList.remove(topFragmentTag);

            // remove or replace top fragment
            if (topFragmentTag.equals(categoriesListTag)) destroyFragment(mCategoriesList, categoriesListTag);
            else if (topFragmentTag.equals(categoriesFormTag)) destroyFragment(mCategoriesForm, categoriesFormTag);
            else setFragment(newTopFragmentTag);

            // Update toolbar and bottom nav
            setToolbarVisibilities(newTopFragmentTag);
            updateBottomNav(newTopFragmentTag);

        }
        else if( backStackCount == 1 ){
            Log.d(LOG_NAV, "EXIT");
            super.onBackPressed();
        }
    }

}