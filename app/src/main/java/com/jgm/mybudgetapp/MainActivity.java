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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.jgm.mybudgetapp.dialogs.ColorPickerDialog;
import com.jgm.mybudgetapp.dialogs.ConfirmationDialog;
import com.jgm.mybudgetapp.dialogs.IconPickerDialog;
import com.jgm.mybudgetapp.dialogs.MethodPickerDialog;
import com.jgm.mybudgetapp.dialogs.TransactionDialog;
import com.jgm.mybudgetapp.databinding.ActivityMainBinding;
import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.dao.CardDao;
import com.jgm.mybudgetapp.room.dao.CategoryDao;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainInterface {

    // Constants
    private static final String LOG_NAV = "debug-nav";
    private static final String LOG_LIFECYCLE = "debug-lifecycle";

    private static final String LOG_DB = "debug-database";
    private static final String STATE_FRAGMENT = "current-fragment";
    private static final String STATE_TAG_LIST = "fragment-tag-list";
    private static final String STATE_DAY = "day";
    private static final String STATE_MONTH = "month";
    private static final String STATE_YEAR = "year";

    // Params
    private static final String PARAM_OUT = "OUT";
    private static final String PARAM_IN = "IN";

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
    private TransactionsOutFragment mTransactions;
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
    private boolean isExpenseMethodDialog;
    private MyDate selectedDate;
    private MyDate today;
    private TransactionResponse selectedTransaction;

    // Db
    private AccountDao mAccountDao;
    private CardDao mCardDao;
    private CategoryDao mCategoryDao;
    private TransactionDao mTransactionDao;

    // UI
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private ImageButton settingsButton, mNextMonth, mPrevMonth;
    private TextView mToolbarMonth, mToolbarYear;

    private void setBinding() {
        toolbar = binding.mainToolbar;
        bottomNavigationView = binding.bottomNavigationView;
        settingsButton = binding.settingsButton;
        mToolbarMonth = binding.toolbarMonth;
        mToolbarYear = binding.toolbarYear;
        mNextMonth = binding.toolbarNextMonthButton;
        mPrevMonth = binding.toolbarPrevMonthButton;
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

        if (savedInstanceState == null) {
            setFragment(homeTag);
            selectedDate = MyDateUtils.getCurrentDate(this);
        }
        else {
            // Set toolbar date
            int day = savedInstanceState.getInt(STATE_DAY);
            int month = savedInstanceState.getInt(STATE_MONTH);
            int year = savedInstanceState.getInt(STATE_YEAR);
            selectedDate = new MyDate(day,month,year);
            selectedDate.setMonthName(MyDateUtils.getMonthName(this, month, year)[0]);
        }

        today = MyDateUtils.getCurrentDate(this);

        initDatabase();
        initBottomBar();
        initToolbar();

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentFragment = savedInstanceState.getString(STATE_FRAGMENT);
        mFragmentTagList = savedInstanceState.getStringArrayList(STATE_TAG_LIST);

        Log.d(LOG_LIFECYCLE, "onRestoreInstanceState => current fragment: " + currentFragment);

        for (int i = 0; i < mFragmentTagList.size(); i++) {
            reReferenceFragment(mFragmentTagList.get(i));
        }
        updateBottomNav(currentFragment);
        setToolbarVisibilities(currentFragment);

        // Change toolbar background if viewpager
        if (currentFragment.equals(categoriesTag)) toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_toolbar_no_border));
        else toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_toolbar));

        // init fragment data
        updateMonthOnCurrentFragment();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_LIFECYCLE, "Main Activity onSaveInstanceState");
        outState.putString(STATE_FRAGMENT, currentFragment);
        outState.putStringArrayList(STATE_TAG_LIST, mFragmentTagList);
        outState.putInt(STATE_DAY, selectedDate.getDay());
        outState.putInt(STATE_MONTH, selectedDate.getMonth());
        outState.putInt(STATE_YEAR, selectedDate.getYear());
    }

    /* ==========================================================================
                                      DATABASE
    ========================================================================== */

    private void initDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        mAccountDao = db.AccountDao();
        mCardDao = db.CardDao();
        mCategoryDao = db.CategoryDao();
        mTransactionDao = db.TransactionDao();
        initDefaultAccounts();
        initDefaultCategories();
    }

    private void initDefaultAccounts() {
        // todo => and if list is empty
        boolean hasInitialAccounts = SettingsPrefs.getSettingsPrefsBoolean(this, "hasInitialAccounts");
        if (!hasInitialAccounts) {

            Log.d(LOG_DB, "== INIT ACCOUNT DEFAULT LIST");

            ArrayList<Account> list = new ArrayList<>();

            Account cash = new Account(getString(R.string.account_cash), 21, 67, 0, true);
            Account checking = new Account(getString(R.string.account_checking), 14, 68, 1, true);
            Account savings = new Account(getString(R.string.account_savings), 20, 69, 2, true);

            list.add(cash);
            list.add(checking);
            list.add(savings);

            for (int i = 0; i < list.size(); i++) {
                Account newAccount = list.get(i);
                AppDatabase.dbWriteExecutor.execute(() -> {
                    mAccountDao.insert(newAccount);
                });
            }

            SettingsPrefs.setSettingsPrefsBoolean(this, "hasInitialAccounts", true);

        }
    }

    private void initDefaultCategories() {

        // todo => and if list is empty
        boolean hasInitialCategories = SettingsPrefs.getSettingsPrefsBoolean(this, "hasInitialCategories");

        if (!hasInitialCategories) {

            Log.d(LOG_DB, "== INIT CATEGORIES DEFAULT LIST");

            ArrayList<com.jgm.mybudgetapp.room.entity.Category> list = new ArrayList<>();

            Category c1 = new Category(getString(R.string.category_home), 3, 6, true);
            Category c2 = new Category(getString(R.string.category_health), 5, 34, true);
            Category c3 = new Category(getString(R.string.category_groceries), 14, 9, true);
            Category c4 = new Category(getString(R.string.category_transport), 11, 29, true);
            Category c5 = new Category(getString(R.string.category_leisure), 1, 46, true);
            Category c6 = new Category(getString(R.string.category_education), 7, 15, true);
            Category c7 = new Category(getString(R.string.category_work), 4, 11, true);

            list.add(c1);
            list.add(c2);
            list.add(c3);
            list.add(c4);
            list.add(c5);
            list.add(c6);
            list.add(c7);

            for (int i = 0; i < list.size(); i++) {
                insertCategoryData(list.get(i));
            }

            SettingsPrefs.setSettingsPrefsBoolean(this, "hasInitialCategories", true);
        }

    }


    /* ===============================================================================
                                         TOOLBAR
     =============================================================================== */

    private void initToolbar() {
        // set toolbar date
        mToolbarMonth.setText(selectedDate.getMonthName());
        mToolbarYear.setText(String.valueOf(selectedDate.getYear()));
        setToolbarMonthStyle();

        // init buttons
        settingsButton.setOnClickListener(v -> openFragment(settingsTag));
        mNextMonth.setOnClickListener(v -> setToolbarNextMonth());
        mPrevMonth.setOnClickListener(v -> setToolbarPrevMonth());
    }

    private void setToolbarNextMonth() {
        MyDate nextDate = MyDateUtils.getNextMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = nextDate;
        mToolbarMonth.setText(nextDate.getMonthName());
        mToolbarYear.setText(String.valueOf(nextDate.getYear()));
        setToolbarMonthStyle();
        updateMonthOnCurrentFragment();
    }

    private void setToolbarPrevMonth() {
        MyDate prevDate = MyDateUtils.getPrevMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = prevDate;
        mToolbarMonth.setText(prevDate.getMonthName());
        mToolbarYear.setText(String.valueOf(prevDate.getYear()));
        setToolbarMonthStyle();
        updateMonthOnCurrentFragment();
    }

    private void setToolbarMonthStyle() {
        if (selectedDate.getMonth() != today.getMonth()
                || selectedDate.getYear() != today.getYear()) {
            mToolbarMonth.setTextAppearance(R.style.ToolbarNotCurrentMonth);
        }
        else mToolbarMonth.setTextAppearance(R.style.ToolbarMonth);
    }

    private void updateMonthOnCurrentFragment() {
        if (mHome != null) mHome.getHomeData(selectedDate.getMonth(), selectedDate.getYear());
        else if (mTransactions != null) mTransactions.getExpensesData(selectedDate.getMonth(), selectedDate.getYear());
    }

    private void setToolbarVisibilities(String tag) {

        if (tag.equals(homeTag) || tag.equals(categoriesTag) || tag.equals(transactionsTag)) {
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
        MenuItem itemAdd = bottomNavigationView.getMenu().getItem(2);
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

        itemAdd.setOnMenuItemClickListener(item -> {
            openFragment(transactionFormTag);
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
            default:
                hideBottomNav();
        }
    }


    private void hideBottomNav() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    private void showBottomNav() {
        bottomNavigationView.setVisibility(View.VISIBLE);
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
                                         INTERFACE
     =============================================================================== */


    /* ========================  NAVIGATION ======================== */


    @Override
    public void openExpenses() {
        openFragment(transactionsTag);
    }

    @Override
    public void openIncome() {
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
    public void openCategoryForm(boolean isEdit, Category category, int position) {
        openFragment(categoriesFormTag);
        if (mCategoriesForm != null) {
            mCategoriesForm.setFormType(isEdit);
            mCategoriesForm.setCategoryToEdit(category, position);
        }
    }

    @Override
    public void openAccounts() {
        openFragment(accountsTag);
    }

    @Override
    public void openAccountDetails(AccountTotal accountTotal, int position) {
        openFragment(accountDetailsTag);
        if (mAccountDetails != null) mAccountDetails.setAccount(accountTotal, position);
    }

    @Override
    public void openAccountForm(boolean isEdit, Account account, int position) {
        openFragment(accountFormTag);
        if (mAccountForm != null) {
            mAccountForm.setFormType(isEdit);
            if (isEdit) mAccountForm.setAccount(account, position);
        }
    }

    @Override
    public void openYear() {
        openFragment(yearTag);
    }

    @Override
    public void openCardDetails(CreditCard card, int position) {
        openFragment(cardDetailsTag);
        if (mCreditCardDetails != null) mCreditCardDetails.setCreditCard(card, position);
    }

    @Override
    public void openCardForm(boolean isEdit, CreditCard card, int position) {
        openFragment(cardFormTag);
        if (mCreditCardForm != null) {
            mCreditCardForm.setFormType(isEdit);
            if (isEdit) mCreditCardForm.setCreditCard(card, position);
        }
    }

    @Override
    public void openTransactionForm(
            boolean isEdit, TransactionResponse transaction, PaymentMethod paymentMethod) {

        setFragment(transactionFormTag);

        if (mTransactionForm != null) {
            mTransactionForm.setFormType(isEdit, transaction, paymentMethod);
        }

    }

    @Override
    public void navigateBack() {
        onBackPressed();
    }


    /* ========================  DIALOGS ======================== */


    @Override
    public void showConfirmationDialog(String message) {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance(message);
        confirmationDialog.show(fm, "CONFIRMATION_DIALOG");
    }

    @Override
    public void handleConfirmation() {
        if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null)
            mCategoriesForm.handleArchiveConfirmation();
        else if (currentFragment.equals(cardFormTag) && mCreditCardForm != null)
            mCreditCardForm.handleArchiveConfirmation();
        else if (currentFragment.equals(accountFormTag) && mAccountForm != null)
            mAccountForm.handleArchiveConfirmation();
    }

    @Override
    public void showTransactionDialog(TransactionResponse transaction) {
        selectedTransaction = transaction;
        BottomSheetDialogFragment transactionDialog = new TransactionDialog();
        transactionDialog.show(getSupportFragmentManager(), "TRANSACTION DIALOG");
    }

    @Override
    public TransactionResponse getSelectedTransactionData() {
        Log.d("debug-add", "should have the data...");
        Log.d("debug-add", selectedTransaction.getDescription());
        return selectedTransaction;
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
            mCreditCardForm.setSelectedColor(color);
        }
        else if (currentFragment.equals(accountFormTag) && mAccountForm != null) {
            mAccountForm.setSelectedColor(color);
        }
        else if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null) {
            mCategoriesForm.setSelectedColor(color);
        }
    }

    @Override
    public void showIconPickerDialog() {
        Log.d(LOG_NAV, "Show icon picker dialog");
        BottomSheetDialogFragment iconPicker = new IconPickerDialog();
        iconPicker.show(getSupportFragmentManager(), "iconPicker");
    }

    @Override
    public void handleIconSelection(Icon icon) {
        Log.d("debug-icon-picker", "Icon: " + icon.getIconName());
        if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null) mCategoriesForm.setSelectedIcon(icon);
    }

    @Override
    public void showDatePickerDialog() {
        Log.d(LOG_NAV, "Show date picker dialog");
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "datePicker");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // convert to local timezone
            long zonedSelection = MyDateUtils.getLocalDateTimeMilliseconds(selection);
            if (mTransactionForm != null) mTransactionForm.setSelectedDate(zonedSelection);
        });
    }

    @Override
    public void showMethodPickerDialog(boolean isExpense) {
        Log.d(LOG_NAV, "Show method picker dialog");
        isExpenseMethodDialog = isExpense;
        MethodPickerDialog methodDialog = new MethodPickerDialog();
        methodDialog.show(getSupportFragmentManager(), "methodPicker");
    }

    @Override
    public ArrayList<PaymentMethod> getMethodsList() {

        // Create a payment methods list
        ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();

        AppDatabase.dbReadExecutor.execute(() -> {
            // get accounts and credit cards from db
            // Using the main thread to be able to use the return statement
            List<Account> accountsList = mAccountDao.getAccounts();

            for (int i = 0; i < accountsList.size(); i++) {
                Account account = accountsList.get(i);
                PaymentMethod paymentMethod = new PaymentMethod(
                        account.getId(),
                        account.getType(),
                        account.getName(),
                        account.getColorId(),
                        account.getIconId(),
                        0);
                paymentMethods.add(paymentMethod);
            }
        });

        if (isExpenseMethodDialog) {
            AppDatabase.dbReadExecutor.execute(() -> {
                // Get credit cards from db if type is expense

                List<CreditCard> cardsList = mCardDao.getCreditCards();
                for (int i = 0; i < cardsList.size(); i++) {
                    CreditCard card = cardsList.get(i);
                    PaymentMethod paymentMethod = new PaymentMethod(
                            card.getId(),
                            Tags.METHOD_CARD,
                            card.getName(),
                            card.getColorId(),
                            Tags.CARD_ICON_ID,
                            card.getBillingDay());
                    paymentMethods.add(paymentMethod);
                }

                Log.d(LOG_DB, "Methods list size: " + paymentMethods.size());
            });
        }

        // todo => wait executions...

        return paymentMethods;
    }

    @Override
    public void setSelectedPaymentMethod(PaymentMethod paymentMethod) {
        mTransactionForm.setSelectedPaymentMethod(paymentMethod);
    }

    /* ========================  SETTINGS ======================== */


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


    /* ========================  CATEGORIES ======================== */


    @Override
    public void setSelectedCategory(Category category) {
        mTransactionForm.setSelectedCategory(category);
        onBackPressed();
    }


    /* ========================  DATABASE - ACCOUNTS ======================== */

    @Override
    public void updateAccountInserted(Account account, boolean isEdit, int position) {
        if (!isEdit){
            if (mAccounts != null) mAccounts.updateUiAfterInsertion(account);
        }
        else {
            // Changed account, but didn't archive
            if (account.isActive()) {
                Log.d("debug-account", "Should back press once");
                if (mAccounts != null) mAccounts.updateListAfterEdit(position, account);
                if (mAccountDetails != null) mAccountDetails.updateAccountAfterEdit(account);
            }
            // Archived
            else {
                // close details page and update main
                Log.d("debug-account", "Should back press twice");
                onBackPressed();
                if (mAccounts != null) mAccounts.updateListAfterDelete(position);
            }
        }
    }


    /* ========================  DATABASE - CATEGORIES ======================== */

    @Override
    public void getCategoriesData() {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbReadExecutor.execute(() -> {

            List<Category> categoriesList = mCategoryDao.getCategories();

            handler.post(() -> {
                mCategoriesList.updateListAfterDbRead(categoriesList);
                Log.d(LOG_DB, "Done reading all categories from db: " + categoriesList.size());
            });
        });
    }

    @Override
    public void insertCategoryData(Category category) {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbWriteExecutor.execute(() -> {

            int id = (int) mCategoryDao.insert(category);
            category.setId(id);

            handler.post(() -> {
                if (mCategoriesList != null) mCategoriesList.updateListAfterDbInsertion(category);
                Log.d(LOG_DB, "Category saved in db... update ui");
            });

        });
    }

    @Override
    public void editCategoryData(int position, Category category) {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbWriteExecutor.execute(() -> {

            mCategoryDao.update(category);

            handler.post(() -> {
                Log.d(LOG_DB, "category updated on db... update ui");
                if (mCategoriesList != null) {
                    if (category.isActive()) mCategoriesList.updateListAfterEdit(position, category);
                    else mCategoriesList.updateListAfterDelete(position);
                }
            });

        });
    }

    /* ========================  DATABASE - CREDIT CARDS ======================== */

    @Override
    public void getCreditCardsData() {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbReadExecutor.execute(() -> {

            List<CreditCard> cardsList = mCardDao.getCreditCards();

            handler.post(() -> {
                if (mCreditCards != null) mCreditCards.updateListAfterDbRead(cardsList);
                Log.d("debug-database", "Done reading all credit cards from db: " + cardsList.size());
            });
        });
    }

    @Override
    public void insertCreditCardData(CreditCard card) {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbWriteExecutor.execute(() -> {

            int id = (int) mCardDao.insert(card);
            card.setId(id);

            handler.post(() -> {
                if (mCreditCards != null) mCreditCards.updateUiAfterInsertion(card);
                Log.d("debug-db", "Credit Card saved in db... update ui");
            });
        });
    }

    @Override
    public void editCreditCardData(int position, CreditCard card) {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbWriteExecutor.execute(() -> {

            mCardDao.update(card);

            handler.post(() -> {
                if (card.isActive()) {
                    if (mCreditCards != null) mCreditCards.updateListAfterEdit(position, card);
                    if (mCreditCardDetails != null) mCreditCardDetails.updateCreditCardAfterEdit(card);
                }
                else {
                    // close cards details fragment and update main
                    onBackPressed();
                    mCreditCards.updateListAfterDelete(position);
                }
                Log.d(LOG_DB, "credit card updated on db... update ui");
            });

        });
    }

    @Override
    public MyDate getDate() {
        return selectedDate;
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
                mTransactions = new TransactionsOutFragment();
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

        if (tag.equals(categoriesListTag)
                || tag.equals(categoriesFormTag)
                || tag.equals(cardFormTag)
                || tag.equals(cardDetailsTag)
                || tag.equals(accountFormTag)
                || tag.equals(accountDetailsTag)) addFragment(fragment, tag);
        else replaceFragment(fragment, tag);

    }

    private void replaceFragment(Fragment fragment, String tag) {

        Log.d(LOG_NAV, "Fragment to be replaced: " + currentFragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, fragment, tag);
        transaction.commit();

        // Change toolbar background if viewpager
        if (tag.equals(categoriesTag)) toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_toolbar_no_border));
        else toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_toolbar));

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
        switch (tag) {
            case categoriesListTag:
                if (currentFragment.equals(transactionFormTag)) transaction2.hide(mTransactionForm);
                if (currentFragment.equals(settingsTag)) transaction2.hide(mSettings);
                break;
            case categoriesFormTag:
                transaction2.hide(mCategoriesList);
                break;
            case cardFormTag:
                if (currentFragment.equals(cardsTag)) transaction2.hide(mCreditCards);
                if (currentFragment.equals(cardDetailsTag)) transaction2.hide(mCreditCardDetails);
                break;
            case cardDetailsTag:
                transaction2.hide(mCreditCards);
                break;
            case accountFormTag:
                if (currentFragment.equals(accountsTag)) transaction2.hide(mAccounts);
                if (currentFragment.equals(accountDetailsTag)) transaction2.hide(mAccountDetails);
                break;
            case accountDetailsTag:
                transaction2.hide(mAccounts);
                break;
        }
        transaction2.commit();

        // Update custom navigation stack
        mFragmentTagList.remove(tag);
        mFragmentTagList.add(tag);

        // Set current fragment tag
        currentFragment = tag;
        Log.d(LOG_NAV, "Fragment added: " + tag);
    }

    private void destroyFragment(Fragment fragment, String tag) {
        Log.d(LOG_NAV, "Fragment to be removed: " + tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();

        // show prev fragment
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        switch (tag) {
            case categoriesListTag:
                if (currentFragment.equals(transactionFormTag)) transaction2.show(mTransactionForm);
                if (currentFragment.equals(settingsTag)) transaction2.show(mSettings);
                break;
            case categoriesFormTag:
                transaction2.show(mCategoriesList);
                break;
            case cardFormTag:
                if (currentFragment.equals(cardsTag)) transaction2.show(mCreditCards);
                if (currentFragment.equals(cardDetailsTag)) transaction2.show(mCreditCardDetails);
                break;
            case cardDetailsTag:
                transaction2.show(mCreditCards);
                break;
            case accountFormTag:
                if (currentFragment.equals(accountsTag)) transaction2.show(mAccounts);
                if (currentFragment.equals(accountDetailsTag)) transaction2.show(mAccountDetails);
                break;
            case accountDetailsTag:
                transaction2.show(mAccounts);
                break;
        }
        transaction2.commit();
    }

    private void reReferenceFragment(String tag) {

        Log.d(LOG_NAV, "========= re-reference: " + currentFragment);

        switch (tag) {
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
                mTransactions = (TransactionsOutFragment) getSupportFragmentManager().findFragmentByTag(transactionsTag);
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
            Log.d(LOG_NAV, "Top fragment: " + topFragmentTag);
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
                case cardDetailsTag:
                    destroyFragment(mCreditCardDetails, cardDetailsTag);
                    break;
                case accountFormTag:
                    destroyFragment(mAccountForm, accountFormTag);
                    break;
                case accountDetailsTag:
                    destroyFragment(mAccountDetails, accountDetailsTag);
                    break;
                default:
                    setFragment(newTopFragmentTag);
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