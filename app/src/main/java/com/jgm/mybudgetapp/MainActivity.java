package com.jgm.mybudgetapp;

import static com.jgm.mybudgetapp.utils.Tags.LOG_NAV;
import static com.jgm.mybudgetapp.utils.Tags.accountDetailsTag;
import static com.jgm.mybudgetapp.utils.Tags.accountFormTag;
import static com.jgm.mybudgetapp.utils.Tags.accountsTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesFormTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesListTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesTag;
import static com.jgm.mybudgetapp.utils.Tags.homeTag;
import static com.jgm.mybudgetapp.utils.Tags.pendingTag;
import static com.jgm.mybudgetapp.utils.Tags.settingsTag;
import static com.jgm.mybudgetapp.utils.Tags.transactionFormTag;
import static com.jgm.mybudgetapp.utils.Tags.transactionsInTag;
import static com.jgm.mybudgetapp.utils.Tags.transactionsOutTag;
import static com.jgm.mybudgetapp.utils.Tags.yearTag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jgm.mybudgetapp.databinding.ActivityMainBinding;
import com.jgm.mybudgetapp.dialogs.ColorPickerDialog;
import com.jgm.mybudgetapp.dialogs.IconPickerDialog;
import com.jgm.mybudgetapp.dialogs.MethodPickerDialog;
import com.jgm.mybudgetapp.dialogs.TransactionDialog;
import com.jgm.mybudgetapp.fragmentsMain.AccountDetailsFragment;
import com.jgm.mybudgetapp.fragmentsMain.AccountFormFragment;
import com.jgm.mybudgetapp.fragmentsMain.AccountsFragment;
import com.jgm.mybudgetapp.fragmentsMain.CategoriesFormFragment;
import com.jgm.mybudgetapp.fragmentsMain.CategoriesListFragment;
import com.jgm.mybudgetapp.fragmentsMain.HomeFragment;
import com.jgm.mybudgetapp.fragmentsMain.PendingFragment;
import com.jgm.mybudgetapp.fragmentsMain.TransactionFormFragment;
import com.jgm.mybudgetapp.fragmentsMain.TransactionsInFragment;
import com.jgm.mybudgetapp.fragmentsMain.TransactionsOutFragment;
import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.MotionUtils;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainInterface {

    // Constants
    private static final String LOG_MAIN = "debug-main";
    private static final String STATE_FRAGMENT = "current-fragment";
    private static final String STATE_TAG_LIST = "fragment-tag-list";
    private static final String STATE_DAY = "day";
    private static final String STATE_MONTH = "month";
    private static final String STATE_YEAR = "year";
    private static final int TRANSITION_TIME = 150;

    // FRAGMENTS
    private AccountsFragment mAccounts;
    private AccountFormFragment mAccountForm;
    private AccountDetailsFragment mAccountDetails;
    private CategoriesListFragment mCategoriesList;
    private CategoriesFormFragment mCategoriesForm;
    private HomeFragment mHome;
    private TransactionFormFragment mTransactionForm;
    private TransactionsOutFragment mTransactionsOut;
    private TransactionsInFragment mTransactionsIn;
    private PendingFragment mPending;

    // Vars
    private ArrayList<String> mFragmentTagList = new ArrayList<>();
    private String currentFragment;
    private boolean isExpenseMethodDialog;
    private TransactionResponse transactionDialogItem;
    private int transactionDialogItemPosition;
    private MyDate selectedDate;
    private MyDate today;
    private TransactionResponse selectedTransaction;
    private int categoryTab = 1;
    private AccountTotal accountDetails;
    boolean isEdit = false;
    private Account accountToEdit;
    private int formType;
    private TransactionResponse transactionToEdit;
    private PaymentMethod transactionPaymentMethod;
    private Category categoryToEdit;
    private int categoryPosition;

    // UI
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private ImageButton settingsButton, mNextMonth, mPrevMonth;
    private TextView mToolbarMonth, mToolbarYear;
    private FloatingActionButton mAdd;
    private MotionLayout mMotion;
    private ConstraintLayout bottomNavContainer;

    private void setBinding() {
        toolbar = binding.mainToolbar;
        bottomNavigationView = binding.bottomNavigationView;
        bottomNavContainer = binding.mainBottomNavContainer;
        settingsButton = binding.settingsButton;
        mToolbarMonth = binding.toolbarMonth;
        mToolbarYear = binding.toolbarYear;
        mNextMonth = binding.toolbarNextMonthButton;
        mPrevMonth = binding.toolbarPrevMonthButton;
        mAdd = binding.buttonAdd;
        mMotion = binding.mainMotionContainer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(Tags.LOG_LIFECYCLE, "Main Activity onCreate");

        // set dark/light mode
        if (savedInstanceState == null) {
            Log.d(LOG_MAIN, "Set dark/light mode");
            boolean isDark = SettingsPrefs.getSettingsPrefsBoolean(this, "isDark");
            switchDarkMode(isDark);
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        if (savedInstanceState == null) {
            Log.d(LOG_MAIN, "savedInstance is null => init splash screen delay, get date " +
                    "and populate accounts and categories tables if empty");
            selectedDate = MyDateUtils.getCurrentDate(this);
            setFragment(homeTag);
        }
        else {
            // Set toolbar date
            Log.d(LOG_MAIN, "savedInstance NOT null => get saved info and set toolbar date");
            int day = savedInstanceState.getInt(STATE_DAY);
            int month = savedInstanceState.getInt(STATE_MONTH);
            int year = savedInstanceState.getInt(STATE_YEAR);
            selectedDate = new MyDate(day,month,year);
            selectedDate.setMonthName(MyDateUtils.getMonthName(this, month, year)[0]);
        }

        today = MyDateUtils.getCurrentDate(this);

        initBottomBar();
        initToolbar();

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentFragment = savedInstanceState.getString(STATE_FRAGMENT);
        mFragmentTagList = savedInstanceState.getStringArrayList(STATE_TAG_LIST);

        Log.i(Tags.LOG_LIFECYCLE, "onRestoreInstanceState => current fragment: " + currentFragment);

        for (int i = 0; i < mFragmentTagList.size(); i++) {
            reReferenceFragment(mFragmentTagList.get(i));
        }
        updateBottomNavOnBackPressed(currentFragment);
        setToolbarVisibilities(currentFragment);

        // init fragment data
        updateMonthOnCurrentFragment();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(Tags.LOG_LIFECYCLE, "Main Activity onSaveInstanceState");
        outState.putString(STATE_FRAGMENT, currentFragment);
        outState.putStringArrayList(STATE_TAG_LIST, mFragmentTagList);
        outState.putInt(STATE_DAY, selectedDate.getDay());
        outState.putInt(STATE_MONTH, selectedDate.getMonth());
        outState.putInt(STATE_YEAR, selectedDate.getYear());
    }

    /* ==== SETTINGS ==== */

    private void switchDarkMode(boolean isDark) {
        Log.d(LOG_MAIN, "-- Interface => switchDarkMode");
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (isDark) {
            Log.d(LOG_MAIN, "Dark Mode");
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            Log.d(LOG_MAIN, "Light Mode");
            if (currentNightMode != Configuration.UI_MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /* ===============================================================================
                                         TOOLBAR
     =============================================================================== */

    private void initToolbar() {
        Log.d(LOG_MAIN, "=> Init toolbar");
        // set toolbar date
        mToolbarMonth.setText(selectedDate.getMonthName());
        mToolbarYear.setText(String.valueOf(selectedDate.getYear()));
        setToolbarMonthStyle();

        // init buttons
        settingsButton.setOnClickListener(v -> open(settingsTag));
        mNextMonth.setOnClickListener(v -> setToolbarNextMonth());
        mPrevMonth.setOnClickListener(v -> setToolbarPrevMonth());
//        mAdd.setOnClickListener(v -> openTransactionForm(Tags.TYPE_OUT, false, null, null));
    }

    private void setToolbarNextMonth() {
        MyDate nextDate = MyDateUtils.getNextMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = nextDate;
        mToolbarMonth.setText(nextDate.getMonthName());
        mToolbarYear.setText(String.valueOf(nextDate.getYear()));
        setToolbarMonthStyle();
        updateMonthOnCurrentFragment();

        Log.d(LOG_MAIN, "Next month: " + nextDate.getMonthName());
    }

    private void setToolbarPrevMonth() {
        MyDate prevDate = MyDateUtils.getPrevMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = prevDate;
        mToolbarMonth.setText(prevDate.getMonthName());
        mToolbarYear.setText(String.valueOf(prevDate.getYear()));
        setToolbarMonthStyle();
        updateMonthOnCurrentFragment();

        Log.d(LOG_MAIN, "Previous month: " + prevDate.getMonthName());
    }

    private void setToolbarMonthStyle() {
        if (selectedDate.getMonth() != today.getMonth()
                || selectedDate.getYear() != today.getYear()) {
            mToolbarMonth.setTextAppearance(R.style.ToolbarNotCurrentMonth);
        }
        else mToolbarMonth.setTextAppearance(R.style.ToolbarMonth);
    }

    private void updateMonthOnCurrentFragment() {
        if (mHome != null)
            mHome.getHomeData(selectedDate.getMonth(), selectedDate.getYear());
        else if (mTransactionsOut != null)
            mTransactionsOut.getExpensesData(selectedDate.getMonth(), selectedDate.getYear());
        else if (mTransactionsIn != null)
            mTransactionsIn.getIncomeData(selectedDate.getMonth(), selectedDate.getYear());
        else if (mAccounts != null && currentFragment.equals(accountsTag))
            mAccounts.updateListOnDateChange(selectedDate);
        else if (mAccountDetails != null && currentFragment.equals(accountDetailsTag))
            mAccountDetails.updateAccountOnMonthChange(selectedDate);
    }

    private void setToolbarVisibilities(String tag) {

        if (tag.equals(homeTag)
                || tag.equals(transactionsOutTag)
                || tag.equals(transactionsInTag)
                || tag.equals(accountsTag)
                || tag.equals(accountDetailsTag)) {

            Log.d(LOG_MAIN, "Toolbar visible");
            toolbar.setVisibility(View.VISIBLE);
        }
        else {
            Log.d(LOG_MAIN, "Toolbar gone");
            toolbar.setVisibility(View.GONE);
        }

    }

    /* ===============================================================================
                                        BOTTOM BAR
     =============================================================================== */

    private void initBottomBar() {
        Log.d(LOG_MAIN, "=> Init bottom bar");

        MenuItem itemHome = bottomNavigationView.getMenu().getItem(0);
        MenuItem itemAccounts = bottomNavigationView.getMenu().getItem(1);
        MenuItem itemIncome = bottomNavigationView.getMenu().getItem(2);
        MenuItem itemExpense = bottomNavigationView.getMenu().getItem(3);
        setAddButton(homeTag);

        itemHome.setOnMenuItemClickListener(item -> {
            if (!currentFragment.equals(homeTag)) open(homeTag);
            return false;
        });

        itemAccounts.setOnMenuItemClickListener(item -> {
            if (!currentFragment.equals(accountsTag)) open(accountsTag);
            return false;
        });

        itemIncome.setOnMenuItemClickListener(item -> {
            if (!currentFragment.equals(transactionsInTag)) open(transactionsInTag);
            return false;
        });

        itemExpense.setOnMenuItemClickListener(item -> {
            if (!currentFragment.equals(transactionsOutTag)) open(transactionsOutTag);
            return false;
        });

    }

    private void updateBottomNavOnBackPressed(String tag) {

        Log.d(LOG_MAIN, "=> Update bottom nav => " + tag);

        switch (tag) {
            case homeTag:
                Log.d(LOG_MAIN, "set home selected");
                bottomNavigationView.setSelectedItemId(R.id.menu_home);
                break;
            case accountsTag:
                Log.d(LOG_MAIN, "set accounts selected");
                bottomNavigationView.setSelectedItemId(R.id.menu_accounts);
                break;
            case transactionsOutTag:
                Log.d(LOG_MAIN, "set expenses selected");
                bottomNavigationView.setSelectedItemId(R.id.menu_expense);
                break;
            case transactionsInTag:
                Log.d(LOG_MAIN, "set income selected");
                bottomNavigationView.setSelectedItemId(R.id.menu_income);
                break;
        }

    }

    private void hideBottomNav() {
        Log.d(LOG_MAIN, "Hide bottom nav");
        bottomNavContainer.setVisibility(View.GONE);
    }

    private void showBottomNav() {
        Log.d(LOG_MAIN, "Show bottom nav");
        bottomNavContainer.setVisibility(View.VISIBLE);
    }

    private void setAddButton(String tag) {
        // padding is lost on setBackground => get padding
        int padding = mAdd.getPaddingTop();
        Log.d("padding", "add button padding: " + padding);

        switch (tag) {
            case accountsTag:
                mAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.savings));
                mAdd.setOnClickListener(v -> openAccountForm(false, null));
                break;
            case transactionsInTag:
                mAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.income));
                mAdd.setOnClickListener(v -> openTransactionForm(Tags.TYPE_IN, false, null, null));
                break;
            default:
                mAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.expense));
                mAdd.setOnClickListener(v -> openTransactionForm(Tags.TYPE_OUT, false, null, null));

        }

        // reset padding
        mAdd.setPadding(padding, padding, padding, padding);

        // show button
        mAdd.show();
    }


    /* ===============================================================================
                                 NAV FRAGMENTS TRANSITIONS
     =============================================================================== */

    private void initTransitionOut(String nextFragment) {

        Log.d(LOG_NAV, "init transition animation OUT from " + currentFragment + " to " + nextFragment);

        mMotion.setTransitionDuration(TRANSITION_TIME);

        boolean isFromMain = (currentFragment.equals(homeTag)
                || currentFragment.equals(accountsTag)
                || currentFragment.equals(transactionsOutTag)
                || currentFragment.equals(transactionsInTag));

        boolean isToMain = (nextFragment.equals(homeTag)
                || nextFragment.equals(accountsTag)
                || nextFragment.equals(transactionsOutTag)
                || nextFragment.equals(transactionsInTag));

        if (isFromMain && isToMain) mAdd.hide();

        MotionUtils.setTransition(mMotion, currentFragment, nextFragment);

        mMotion.transitionToEnd();

    }

    private void initTransitionIn(String nextFragment) {

        Log.d(LOG_NAV, "init transition animation IN from " + currentFragment + " to " + nextFragment);

        mMotion.setTransitionDuration(TRANSITION_TIME);

        boolean isFromMain = (currentFragment.equals(homeTag)
                || currentFragment.equals(accountsTag)
                || currentFragment.equals(transactionsOutTag)
                || currentFragment.equals(transactionsInTag));

        boolean isToMain = (nextFragment.equals(homeTag)
                || nextFragment.equals(accountsTag)
                || nextFragment.equals(transactionsOutTag)
                || nextFragment.equals(transactionsInTag));

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    mMotion.setProgress(1);

                    MotionUtils.setTransition(mMotion, nextFragment, currentFragment);

                    if (isToMain) showBottomNav();
                    else hideBottomNav();

                    setToolbarVisibilities(nextFragment);
                    openFragment(nextFragment);
                    updateOpenedFragment(nextFragment);

                    // Open new fragment after transitioning out
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> {
                                mMotion.transitionToStart();
                                if (isFromMain && isToMain) setAddButton(nextFragment);
                            }, 100);


                }, TRANSITION_TIME);

    }

    private void updateOpenedFragment(String tag) {
        switch (tag) {
            case accountDetailsTag:
                if (mAccountDetails != null) mAccountDetails.setAccount(accountDetails, selectedDate);
                break;
            case accountFormTag:
                if (mAccountForm != null) {
                    mAccountForm.setFormType(isEdit);
                    if (isEdit) mAccountForm.setAccount(accountToEdit);
                }
                break;
            case transactionFormTag:
                if (mTransactionForm != null)
                    mTransactionForm.setFormType(formType, isEdit, transactionToEdit, transactionPaymentMethod);
                break;
            case categoriesListTag:
                if (mCategoriesList != null) mCategoriesList.setListType(isEdit);
                break;
            case categoriesFormTag:
                if (mCategoriesForm != null) {
                    mCategoriesForm.setFormType(isEdit);
                    mCategoriesForm.setCategoryToEdit(categoryToEdit, categoryPosition);
                }
                break;
        }
    }


    /* =============================================================================================
                                             INTERFACE
     ============================================================================================ */


    // ------------------------------------ NAVIGATION --------------------------------------------

    @Override
    public void open(String tag) {
        Log.d(LOG_MAIN, "-- Interface => open: " + tag);

        switch (tag) {
            case yearTag:
                startActivity(new Intent(MainActivity.this, YearActivity.class));
                break;
            case categoriesTag:
                Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
                intent.putExtra("tab", categoryTab);
                intent.putExtra("day", selectedDate.getDay());
                intent.putExtra("month", selectedDate.getMonth());
                intent.putExtra("year", selectedDate.getYear());
                startActivity(intent);
                break;
            case settingsTag:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case categoriesListTag:
            case categoriesFormTag:
                initTransitionIn(tag);
                break;
            default:
                initTransitionOut(tag);
                initTransitionIn(tag);
                break;
        }
    }

    @Override
    public void openCategories(int tab) {
        categoryTab = tab;
        open(categoriesTag);
    }

    @Override
    public void openCategoriesList(boolean isEdit) {
        Log.d(LOG_MAIN, "-- Interface => open categories list");
        open(categoriesListTag);
    }

    @Override
    public void openCategoryForm(boolean isEdit, Category category, int position) {
        Log.d(LOG_MAIN, "-- Interface => open category form");
        this.isEdit = isEdit;
        categoryToEdit = category;
        categoryPosition = position;
        open(categoriesFormTag);
    }

    @Override
    public void openAccountDetails(AccountTotal accountTotal) {
        Log.d(LOG_MAIN, "-- Interface => open account details");
        accountDetails = accountTotal;
        open(accountDetailsTag);
    }

    @Override
    public void openAccountForm(boolean isEdit, Account account) {
        Log.d(LOG_MAIN, "-- Interface => open account form");
        this.isEdit = isEdit;
        accountToEdit = account;
        open(accountFormTag);
    }

    @Override
    public void openTransactionForm(
            int type, boolean isEdit, TransactionResponse transaction, PaymentMethod paymentMethod) {
        Log.d(LOG_MAIN, "-- Interface => open transaction form");
        formType = type;
        this.isEdit = isEdit;
        transactionToEdit = transaction;
        transactionPaymentMethod = paymentMethod;
        open(transactionFormTag);
    }

    @Override
    public void navigateBack() {
        Log.d(LOG_MAIN, "-- Interface => navigate back");
        onBackPressed();
    }


    // --------------------------------------- DIALOGS --------------------------------------------

    @Override
    public void showConfirmationDialog(String message, String title, int icon) {
        Log.d(LOG_MAIN, "-- Interface => show confirmation dialog");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this)
                .setIcon(icon)
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
        Log.d(LOG_MAIN, "-- Interface => handle confirmation");
        if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null)
            mCategoriesForm.handleArchiveConfirmation();
        else if (currentFragment.equals(accountFormTag) && mAccountForm != null)
            mAccountForm.handleArchiveConfirmation();
    }

    @Override
    public void showTransactionDialog(TransactionResponse transaction) {
        Log.d(LOG_MAIN, "-- Interface => show transaction dialog");
        selectedTransaction = transaction;
        BottomSheetDialogFragment transactionDialog = new TransactionDialog();
        transactionDialog.show(getSupportFragmentManager(), "TRANSACTION DIALOG");
    }

    @Override
    public TransactionResponse getSelectedTransactionData() {
        Log.d(LOG_MAIN, "-- Interface => getSelectedTransactionData");
        return selectedTransaction;
    }

    @Override
    public void handleTransactionDeleted(int id) {
        Log.d(LOG_MAIN, "-- Interface => handleTransactionDeleted");
        if (mTransactionsOut != null) mTransactionsOut.updateOnTransactionDeleted(id);
        else if (mTransactionsIn != null) mTransactionsIn.updateOnTransactionDeleted(id);
    }

    @Override
    public void showColorPickerDialog() {
        Log.d(LOG_MAIN, "-- Interface => Show color picker dialog");
        BottomSheetDialogFragment colorPicker = new ColorPickerDialog();
        colorPicker.show(getSupportFragmentManager(), "colorPicker");
    }

    @Override
    public void handleColorSelection(Color color) {
        Log.d(LOG_MAIN, "-- Interface => handleColorSelection");
        if (currentFragment.equals(accountFormTag) && mAccountForm != null) {
            mAccountForm.setSelectedColor(color);
        }
        else if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null) {
            mCategoriesForm.setSelectedColor(color);
        }
    }

    @Override
    public void showIconPickerDialog() {
        Log.d(LOG_MAIN, "-- Interface => showIconPickerDialog");
        BottomSheetDialogFragment iconPicker = new IconPickerDialog();
        iconPicker.show(getSupportFragmentManager(), "iconPicker");
    }

    @Override
    public void handleIconSelection(Icon icon) {
        Log.d(LOG_MAIN, "-- Interface => handleIconSelection "  + icon.getIconName());
        if (currentFragment.equals(categoriesFormTag) && mCategoriesForm != null) mCategoriesForm.setSelectedIcon(icon);
    }

    @Override
    public void showDatePickerDialog() {
        Log.d(LOG_MAIN, "-- Interface => showDatePickerDialog");
        // todo: set selected date on edit form...
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
    public void showMethodPickerDialog(boolean isExpense, TransactionResponse item, int position) {
        Log.d(LOG_MAIN, "-- Interface => showMethodPickerDialog");
        isExpenseMethodDialog = isExpense;
        transactionDialogItem = item;
        transactionDialogItemPosition = position;
        BottomSheetDialogFragment methodDialog = new MethodPickerDialog();
        methodDialog.show(getSupportFragmentManager(), "methodPicker");
    }

    @Override
    public boolean getMethodDialogType() {
        Log.d(LOG_MAIN, "-- Interface => getMethodDialogType");
        return isExpenseMethodDialog;
    }

    @Override
    public void setSelectedPaymentMethod(PaymentMethod paymentMethod) {
        Log.d(LOG_MAIN, "-- Interface => setSelectedPaymentMethod");
        if (mTransactionForm != null) mTransactionForm.setSelectedPaymentMethod(paymentMethod);
        else if (mTransactionsOut != null && transactionDialogItem != null)
            mTransactionsOut.updateOnCreditCardPaid(transactionDialogItem, paymentMethod, transactionDialogItemPosition);
        else if (mPending != null) mPending.updatePaidCreditCard(paymentMethod, transactionDialogItemPosition);
    }


    /* ==== TRANSACTIONS ==== */

    @Override
    public void updateTotal(float value, boolean isPaid) {
        if (mTransactionsOut != null) mTransactionsOut.updateTotal(value, isPaid);
        else if (mTransactionsIn != null) mTransactionsIn.updateTotal(value, isPaid);
    }


    /* ==== CATEGORIES ==== */

    @Override
    public void setSelectedCategory(Category category) {
        Log.d(LOG_MAIN, "-- Interface => setSelectedCategory: " + category.getName());
        if (mTransactionForm != null) mTransactionForm.setSelectedCategory(category);
        onBackPressed();
    }

    @Override
    public void handleCategoryInserted(Category category) {
        Log.d(LOG_MAIN, "-- Interface => handleCategoryInserted: " + category.getName());
        if (mCategoriesList != null) mCategoriesList.updateListAfterDbInsertion(category);
    }


    /* ==== ACCOUNTS ==== */

    @Override
    public void updateAccountInserted(Account account, boolean isEdit, int position) {
        Log.d(LOG_MAIN, "-- Interface => updateAccountInserted: " + account.getName());
        if (!isEdit){
            if (mAccounts != null) mAccounts.updateUiAfterInsertion(account);
        }
        else {
            // Changed account, but didn't archive
            if (account.isActive() && mAccountDetails != null) {
                Log.d(LOG_MAIN, "-- Interface => updateAccountEdited");
                mAccountDetails.updateAccountAfterEdit(account);
            }
            // Archived => close details page
            else onBackPressed();
        }
    }


    /* ====  DATE ==== */
    @Override
    public MyDate getDate() {
        Log.d(LOG_MAIN, "-- Interface => getDate: " +
                selectedDate.getDay() + "/" + selectedDate.getMonthName() + "/" + selectedDate.getYear());
        return selectedDate;
    }

    @Override
    public void setSelectedToolbarDate(int day, int month, int year) {
        MyDate newDate = new MyDate(day, month, year);
        String monthName = MyDateUtils.getMonthName(this, month, year)[0];
        newDate.setMonthName(monthName);
        selectedDate = newDate;

        // set toolbar date
        mToolbarMonth.setText(selectedDate.getMonthName());
        mToolbarYear.setText(String.valueOf(selectedDate.getYear()));
        setToolbarMonthStyle();
    }


    /* =============================================================================================
                                        FRAGMENT NAVIGATION
     ============================================================================================ */

    private void openFragment(String tag) {
        Log.d(Tags.LOG_NAV, "== openFragment / " + tag + " | " + "currentFragment " + currentFragment);
        if (!currentFragment.equals(tag)) {
            if (tag.equals(homeTag)) resetFragmentStack();
            setFragment(tag);
        }
    }

    private void resetFragmentStack() {
        mFragmentTagList.clear();
        mFragmentTagList = new ArrayList<>();
        Log.d(Tags.LOG_NAV, "Reset fragment stack: " + mFragmentTagList.size());
    }

    private void setFragment(String tag) {

        Log.d(Tags.LOG_NAV, "== setFragment / " + tag);

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
            case categoriesListTag:
                mCategoriesList = new CategoriesListFragment();
                fragment = mCategoriesList;
                break;
            case categoriesFormTag:
                mCategoriesForm = new CategoriesFormFragment();
                fragment = mCategoriesForm;
                break;
            case homeTag:
                mHome = new HomeFragment();
                fragment = mHome;
                break;
            case transactionsOutTag:
                mTransactionsOut = new TransactionsOutFragment();
                fragment = mTransactionsOut;
                break;
            case transactionsInTag:
                mTransactionsIn = new TransactionsInFragment();
                fragment = mTransactionsIn;
                break;
            case transactionFormTag:
                mTransactionForm = new TransactionFormFragment();
                fragment = mTransactionForm;
                break;
            case pendingTag:
                mPending = new PendingFragment();
                fragment = mPending;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tag);
        }

        if (tag.equals(categoriesListTag)
                || tag.equals(categoriesFormTag)
                || tag.equals(accountFormTag)) {
            addFragment(fragment, tag);
        }
        else {
            replaceFragment(fragment, tag);
        }

    }

    private void replaceFragment(Fragment fragment, String tag) {

        Log.d(Tags.LOG_NAV, "== replaceFragment: new fragment = " + tag + " old fragment = " + currentFragment);

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

        Log.d(Tags.LOG_NAV, "New fragment loaded: " + tag);

    }

    private void addFragment(Fragment fragment, String tag) {

        Log.d(Tags.LOG_NAV, "== Fragment to be added: " + tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_content_frame, fragment, tag);
        transaction.commit();

        // hide prev fragment
        Log.d(Tags.LOG_NAV, "Hide prev fragment: " + currentFragment);
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        switch (tag) {
            case categoriesListTag:
                transaction2.hide(mTransactionForm);
                break;
            case categoriesFormTag:
                transaction2.hide(mCategoriesList);
                break;
            case accountFormTag:
                if (currentFragment.equals(accountsTag)) transaction2.hide(mAccounts);
                if (currentFragment.equals(accountDetailsTag)) transaction2.hide(mAccountDetails);
                break;
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
            case categoriesListTag:
                if (tag.equals(transactionFormTag)) transaction.show(mTransactionForm);
                break;
            case categoriesFormTag:
                transaction.show(mCategoriesList);
                break;
            case accountFormTag:
                if (tag.equals(accountsTag)) transaction.show(mAccounts);
                if (tag.equals(accountDetailsTag)) transaction.show(mAccountDetails);
                break;
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
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        switch (tag) {
            case categoriesListTag:
                transaction2.show(mTransactionForm);
                break;
            case categoriesFormTag:
                transaction2.show(mCategoriesList);
                break;
            case accountFormTag:
                if (currentFragment.equals(accountsTag)) transaction2.show(mAccounts);
                if (currentFragment.equals(accountDetailsTag)) transaction2.show(mAccountDetails);
                break;
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

            // destroy and show hidden newTopFragment when necessary
            if (currentFragment.equals(accountFormTag)
                    || currentFragment.equals(categoriesFormTag)
                    || currentFragment.equals(categoriesListTag)) {

                MotionUtils.setTransition(mMotion, topFragmentTag, newTopFragmentTag);
                initTransitionOut(topFragmentTag);

                new Handler(Looper.getMainLooper()).postDelayed(
                        () -> {
                            mMotion.setProgress(1);

                            // destroy prev fragment
                            switch (topFragmentTag) {
                                case categoriesListTag:
                                    destroyFragment(mCategoriesList, categoriesListTag);
                                    break;
                                case categoriesFormTag:
                                    destroyFragment(mCategoriesForm, categoriesFormTag);
                                    break;
                                case accountFormTag:
                                    destroyFragment(mAccountForm, accountFormTag);
                                    break;
                            }

                            setToolbarVisibilities(newTopFragmentTag);
                            showHiddenFragment(newTopFragmentTag, topFragmentTag);

                        }, TRANSITION_TIME);

            }
            else if (newTopFragmentTag.equals(homeTag)
                    || newTopFragmentTag.equals(accountsTag)
                    || newTopFragmentTag.equals(transactionsOutTag)
                    || newTopFragmentTag.equals(transactionsInTag)) {

                updateBottomNavOnBackPressed(newTopFragmentTag);

            }
            else {
                initTransitionOut(newTopFragmentTag);
                initTransitionIn(newTopFragmentTag);
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
            case accountsTag:
                mAccounts = (AccountsFragment) getSupportFragmentManager().findFragmentByTag(accountsTag);
                break;
            case accountFormTag:
                mAccountForm = (AccountFormFragment) getSupportFragmentManager().findFragmentByTag(accountFormTag);
                break;
            case accountDetailsTag:
                mAccountDetails = (AccountDetailsFragment) getSupportFragmentManager().findFragmentByTag(accountDetailsTag);
                break;
            case categoriesListTag:
                mCategoriesList = (CategoriesListFragment) getSupportFragmentManager().findFragmentByTag(categoriesListTag);
                break;
            case categoriesFormTag:
                mCategoriesForm = (CategoriesFormFragment) getSupportFragmentManager().findFragmentByTag(categoriesFormTag);
                break;
            case homeTag:
                mHome = (HomeFragment) getSupportFragmentManager().findFragmentByTag(homeTag);
                break;
            case transactionsOutTag:
                mTransactionsOut = (TransactionsOutFragment) getSupportFragmentManager().findFragmentByTag(transactionsOutTag);
                break;
            case transactionsInTag:
                mTransactionsIn = (TransactionsInFragment) getSupportFragmentManager().findFragmentByTag(transactionsInTag);
                break;
            case transactionFormTag:
                mTransactionForm = (TransactionFormFragment) getSupportFragmentManager().findFragmentByTag(transactionFormTag);
                break;
            case pendingTag:
                mPending = (PendingFragment) getSupportFragmentManager().findFragmentByTag(pendingTag);
                break;
        }
    }

    private void deReferenceFragment(String tag) {

        Log.d(Tags.LOG_NAV, "========= de-reference: " + tag);

        if (tag != null) {
            switch (tag) {
                case accountsTag: mAccounts = null; break;
                case accountFormTag: mAccountForm = null; break;
                case accountDetailsTag: mAccountDetails = null;break;
                case categoriesListTag: mCategoriesList = null; break;
                case categoriesFormTag: mCategoriesForm = null; break;
                case homeTag: mHome = null; break;
                case transactionsOutTag: mTransactionsOut = null; break;
                case transactionsInTag: mTransactionsIn = null; break;
                case transactionFormTag: mTransactionForm = null; break;
                case pendingTag: mPending = null; break;
            }
        }

    }

}