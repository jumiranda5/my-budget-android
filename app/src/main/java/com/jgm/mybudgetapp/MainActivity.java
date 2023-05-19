package com.jgm.mybudgetapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.jgm.mybudgetapp.adapters.TextTabsAdapter;
import com.jgm.mybudgetapp.navUtils.FragmentTag;
import com.jgm.mybudgetapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Logs
    private static final String LOG_NAV = "debug-nav";
    private static final String LOG_LIFECYCLE = "debug-lifecycle";

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
        setFragment(mHome, homeTag);
        currentFragment = homeTag;

        settingsButton.setOnClickListener(v -> openSettings());
        fab.setOnClickListener(v -> openTransactionsForm());

    }

    /* ===============================================================================
                                         FRAGMENTS
     =============================================================================== */

    /*
        Main fragments => home, cards, accounts, settings, categories, transactions form, transactions
        Secondary fragments => card form, card details, account form, account details

        * Main fragments are not destroyed => only show/hide

        * Secondary fragments are not inserted on the mFragmentList
        * Secondary fragments are destroyed after leaving a main fragment
          - card form and card details are destroyed after leaving cards
          - account form and account details are destroyed after leaving accounts
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
                || !tag.equals(accountFormTag)) mFragmentList.add(new FragmentTag(fragment, tag));

        Log.d(LOG_NAV, "Fragment loaded: " + tag);
    }

    private void destroyFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();

        mFragmentTagList.remove(tag);

        Log.d(LOG_NAV, "Fragment removed: " + tag);
    }

    private void setFragment(Fragment fragment, String tag) {
        if (fragment == null) {
            Log.d(LOG_NAV, "Fragment is null => init: " + tag);
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

        Log.d(LOG_NAV, "Set visibility for: " + tag);

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

            // Update toolbar and bottom nav
            updateBottomNav(newTopFragmentTag);

        }
        else if( backStackCount == 1 ){
            Log.d(LOG_NAV, "EXIT");
            super.onBackPressed();
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
            openHome();
            return false;
        });

        itemAccounts.setOnMenuItemClickListener(item -> {
            openAccounts();
            return false;
        });

        itemCards.setOnMenuItemClickListener(item -> {
            openCreditCards();
            return false;
        });

        itemCategories.setOnMenuItemClickListener(item -> {
            openCategories();
            return false;
        });

    }

    private void updateBottomNav(String tag) {
        switch (tag) {
            case categoriesTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_categories);
                break;
            case accountsTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_accounts);
                break;
            case cardsTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_cards);
                break;
            case homeTag:
                bottomNavigationView.setSelectedItemId(R.id.menu_home);
                break;
        }
    }

    /* ===============================================================================
                                     FRAGMENT NAVIGATION
     =============================================================================== */

    private void openHome() {
        if (!currentFragment.equals(homeTag)) {
            Log.d(LOG_NAV, "OPEN HOME");
            resetFragmentStack();
            currentFragment = homeTag;
            setFragment(mHome, homeTag);
            updateBottomNav(homeTag);
        }
    }

    private void openCategories() {
        if (!currentFragment.equals(categoriesTag)) {
            Log.d(LOG_NAV, "OPEN CREDIT CATEGORIES");
            currentFragment = categoriesTag;
            setFragment(mCategories, categoriesTag);
            updateBottomNav(categoriesTag);
        }
    }

    private void openCreditCards() {
        if (!currentFragment.equals(cardsTag)) {
            Log.d(LOG_NAV, "OPEN CREDIT CARDS");
            currentFragment = cardsTag;
            setFragment(mCreditCards, cardsTag);
            updateBottomNav(cardsTag);
        }
    }

    private void openAccounts() {
        if (!currentFragment.equals(accountsTag)) {
            Log.d(LOG_NAV, "OPEN ACCOUNTS");
            currentFragment = accountsTag;
            setFragment(mAccounts, accountsTag);
            updateBottomNav(accountsTag);
        }
    }

    private void openSettings() {
        if (!currentFragment.equals(settingsTag)) {
            Log.d(LOG_NAV, "OPEN SETTINGS");
            currentFragment = settingsTag;
            setFragment(mSettings, settingsTag);
            updateBottomNav(settingsTag);
        }
    }

    private void openTransactionsForm() {
        if (!currentFragment.equals(transactionFormTag)) {
            Log.d(LOG_NAV, "OPEN TRANSACTIONS FORM");
            currentFragment = transactionFormTag;
            setFragment(mTransactionForm, transactionFormTag);
        }
    }

}