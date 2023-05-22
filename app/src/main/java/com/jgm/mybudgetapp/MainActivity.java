package com.jgm.mybudgetapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jgm.mybudgetapp.dialogs.ConfirmationDialog;
import com.jgm.mybudgetapp.dialogs.TransactionDialog;
import com.jgm.mybudgetapp.utils.FragmentTag;
import com.jgm.mybudgetapp.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainInterface {

    // Logs
    private static final String LOG_NAV = "debug-nav";
    private static final String LOG_LIFECYCLE = "debug-lifecycle";

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
    private final ArrayList<FragmentTag> mFragmentList = new ArrayList<>();
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
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        initBottomBar();
        setFragment(mHome, homeTag, null);
        currentFragment = homeTag;

        initFab();
        settingsButton.setOnClickListener(v -> openFragment(mSettings, settingsTag, null));

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
            openFragment(mHome, homeTag, null);
            return false;
        });

        itemAccounts.setOnMenuItemClickListener(item -> {
            openFragment(mAccounts, accountsTag, null);
            return false;
        });

        itemCards.setOnMenuItemClickListener(item -> {
            openFragment(mCreditCards, cardsTag, null);
            return false;
        });

        itemCategories.setOnMenuItemClickListener(item -> {
            openFragment(mCategories, categoriesTag, PARAM_OUT);
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
                break;
            case cardsTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_cards);
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_app_add_card));
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
                    openFragment(mTransactionForm, transactionFormTag, PARAM_OUT_ADD);
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

    private void openFragment(Fragment fragment, String tag, String param) {
        if (!currentFragment.equals(tag)) {
            Log.d(LOG_NAV, "OPEN " + tag);
            if (tag.equals(homeTag)) resetFragmentStack();
            currentFragment = tag;
            setFragment(fragment, tag, param);
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
        openFragment(mTransactions, transactionsTag, null);
    }

    @Override
    public void openIncome() {
        if (mTransactions != null) mTransactions.setTypeParam(PARAM_IN);
        openFragment(mTransactions, transactionsTag, null);
    }

    @Override
    public void openExpensesCategories() {
        if (mCategories != null) mCategories.setInitialTab(PARAM_OUT);
        openFragment(mCategories, categoriesTag, PARAM_OUT);
    }

    @Override
    public void openIncomeCategories() {
        if (mCategories != null) mCategories.setInitialTab(PARAM_IN);
        openFragment(mCategories, categoriesTag, PARAM_IN);
    }

    @Override
    public void openAccounts() {
        openFragment(mAccounts, accountsTag, null);
    }

    @Override
    public void openAccountDetails() {
        openFragment(mAccountDetails, accountDetailsTag, null);
    }

    @Override
    public void openAccountForm(boolean isEdit) {
        String param;
        if (isEdit) param = PARAM_EDIT;
        else param = PARAM_ADD;
        if (mAccountForm != null) mAccountForm.setFormType(isEdit);
        openFragment(mAccountForm, accountFormTag, param);
    }

    @Override
    public void openYear() {
        openFragment(mYear, yearTag, null);
    }

    @Override
    public void openCardDetails() {
        openFragment(mCreditCardDetails, cardDetailsTag, null);
    }

    @Override
    public void openCardForm(boolean isEdit) {
        String param;
        if (isEdit) param = PARAM_EDIT;
        else param = PARAM_ADD;
        if (mCreditCardForm != null) mCreditCardForm.setFormType(isEdit);
        openFragment(mCreditCardForm, cardFormTag, param);
    }

    @Override
    public void openTransactionForm() {
        // todo: add param (IN/OUT && ADD/EDIT)
        setFragment(mTransactionForm, transactionFormTag, null);
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

    /* ===============================================================================
                                         FRAGMENTS
     =============================================================================== */

    /*
        Main fragments => home, cards, accounts, settings, categories, transactions form, transactions
        Secondary fragments => card form, card details, account form, account details, year

        * Main fragments are not destroyed => only show/hide

        * Secondary fragments are not inserted on the mFragmentList
        * Secondary fragments are destroyed after leaving a main fragment
          - card form and card details are destroyed after leaving cards
          - account form and account details are destroyed after leaving accounts
          - year is destroyed when closed
     */

    private void resetFragmentStack() {
        mFragmentTagList.clear();
        mFragmentTagList = new ArrayList<>();
        Log.d(LOG_NAV, "Reset fragment stack: " + mFragmentTagList.size());
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_content_frame, fragment, tag);
        transaction.commit();

        mFragmentTagList.add(tag);

        if (!tag.equals(cardFormTag)
                || !tag.equals(cardDetailsTag)
                || !tag.equals(accountDetailsTag)
                || !tag.equals(accountFormTag)
                || !tag.equals(yearTag)) mFragmentList.add(new FragmentTag(fragment, tag));

        Log.d(LOG_NAV, "Fragment loaded: " + tag);
    }

    private void destroyFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();

        mFragmentTagList.remove(tag);

        switch (tag) {
            case cardDetailsTag: mCreditCardDetails = null; break;
            case cardFormTag: mCreditCardForm = null; break;
            case accountDetailsTag: mAccountDetails = null; break;
            case accountFormTag: mAccountForm = null; break;
            case yearTag: mYear = null; break;
        }

        Log.d(LOG_NAV, "Fragment removed: " + tag);
    }

    private void setFragment(Fragment fragment, String tag, String param) {
        if (fragment == null) {
            Log.d(LOG_NAV, "Fragment is null => init: " + tag);
            switch (tag) {
                case accountsTag:
                    mAccounts = new AccountsFragment();
                    fragment = mAccounts;
                    break;
                case accountFormTag:
                    mAccountForm = AccountFormFragment.newInstance(param);
                    fragment = mAccountForm;
                    break;
                case accountDetailsTag:
                    mAccountDetails = new AccountDetailsFragment();
                    fragment = mAccountDetails;
                    break;
                case categoriesTag:
                    mCategories = CategoriesFragment.newInstance(param);
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
                    mCreditCardForm = CreditCardFormFragment.newInstance(param);
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
                    mTransactions = TransactionsFragment.newInstance(param);
                    fragment = mTransactions;
                    break;
                case transactionFormTag:
                    mTransactionForm = TransactionFormFragment.newInstance(param);
                    fragment = mTransactionForm;
                    break;
                case yearTag:
                    mYear = new YearFragment();
                    fragment = mYear;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + tag);
            }
            loadFragment(fragment, tag);
        }
        else {
            Log.d(LOG_NAV, "Fragment not null => update stack: " + tag);
            mFragmentTagList.remove(tag);
            mFragmentTagList.add(tag);
        }
        setFragmentVisibilities(tag);
    }

    private void setFragmentVisibilities(String tag){

        for(int i = 0; i < mFragmentList.size(); i++){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if(tag.equals(mFragmentList.get(i).getTag())) transaction.show((mFragmentList.get(i).getFragment()));
            else transaction.hide((mFragmentList.get(i).getFragment()));

            transaction.commit();
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

            setFragmentVisibilities(newTopFragmentTag);
            mFragmentTagList.remove(topFragmentTag);
            Log.d(LOG_NAV, "Back to: " + newTopFragmentTag);

            switch (topFragmentTag) {
                case yearTag:
                    destroyFragment(mYear, yearTag);
                    break;
                case cardsTag:
                    if (mCreditCardDetails != null) destroyFragment(mCreditCardDetails, cardDetailsTag);
                    if (mCreditCardForm != null) destroyFragment(mCreditCardForm, cardFormTag);
                    break;
                case accountsTag:
                    if (mAccountDetails != null) destroyFragment(mAccountDetails, accountDetailsTag);
                    if (mAccountForm != null) destroyFragment(mAccountForm, accountFormTag);
                    break;
            }

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