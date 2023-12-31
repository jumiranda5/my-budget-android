package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.HomeCategoryAdapter;
import com.jgm.mybudgetapp.databinding.FragmentHomeBinding;
import com.jgm.mybudgetapp.objects.Balance;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.objects.HomeAccounts;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.objects.MonthTotal;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.CategoryUtils;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.ListSort;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private final static String LOG_HOME = "debug-home";

    // UI
    private FragmentHomeBinding binding;
    private MaterialCardView mCardIncome, mCardExpenses, mCardSavings, mCardAccounts,
            mCardIncomeCategories, mCardExpensesCategories, mCardYear, mCardPending;
    private ImageView mIncomeChart, mExpensesChart, mYearChart;
    private RecyclerView mIncomeCategoryListView, mExpensesCategoryListView;
    private TextView mBalance, mIncome, mExpenses, mSavingsTransfer, mAccumulated,
            mIncomeSymbol, mExpensesSymbol, mSavingsTransferSymbol, mAccumulatedSymbol,
            mCash, mChecking, mSavings, mCashSymbol, mCheckingSymbol, mSavingsSymbol,
            mPendingMsg, mProgress, mProgressText, mYearLabel;
    private CircularProgressIndicator mBalanceProgress;
    private LinearLayout mBalanceContainer;
    private ConstraintLayout mContainer;

    private void bindViews() {
        mContainer = binding.homeContentContainer;
        // Balance
        mBalanceContainer = binding.homeMonthContainer;
        mBalance = binding.homeMonthBalance;
        mIncome = binding.homeIncome;
        mExpenses = binding.homeExpenses;
        mSavingsTransfer = binding.homeSavingsBalance;
        mAccumulated = binding.homeAccumulated;
        mIncomeSymbol = binding.homeIncomeCurrencySymbol;
        mExpensesSymbol = binding.homeExpenseCurrencySymbol;
        mSavingsTransferSymbol = binding.homeSavingsBalanceCurrencySymbol;
        mAccumulatedSymbol = binding.homeAccumulatedCurrencySymbol;

        // Cards
        mCardIncome = binding.homeCardIncome;
        mCardExpenses = binding.homeCardExpenses;
        mCardSavings = binding.homeCardSavings;
        mCardAccounts = binding.homeCardAccounts;
        mCardIncomeCategories = binding.homeCardIncomeCategories;
        mCardExpensesCategories = binding.homeCardExpensesCategories;
        mCardYear = binding.homeCardYear;

        // Progress
        mBalanceProgress = binding.homeMonthProgress;
        mProgress = binding.homeMonthProgressText;

        // Accounts
        mCash = binding.homeCash;
        mChecking = binding.homeChecking;
        mSavings = binding.homeSavings;
        mCashSymbol = binding.homeCashCurrencySymbol;
        mCheckingSymbol = binding.homeCheckingCurrencySymbol;
        mSavingsSymbol = binding.homeSavingsCurrencySymbol;
        mProgressText = binding.homeProgressText;

        // Categories
        mIncomeChart = binding.homeCategoriesIn;
        mExpensesChart = binding.homeCategoriesOut;
        mIncomeCategoryListView = binding.homeIncomeCategoriesList;
        mExpensesCategoryListView = binding.homeExpensesCategoriesList;

        // Year
        mYearChart = binding.homeYearChart;
        mYearLabel = binding.homeYearLabel;

        // Pending
        mPendingMsg = binding.homePendingText;
        mCardPending = binding.homePending;

    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(Tags.LOG_LIFECYCLE, "Home fragment onAttach");
        mContext = context;
        mInterface = (MainInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(Tags.LOG_LIFECYCLE, "Home fragment onCreateView");
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        bindViews();
        mContainer.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(Tags.LOG_LIFECYCLE, "Home fragment onViewCreated");

        initNavigation();

        if (savedInstanceState == null) {
            Log.d(LOG_HOME, "saved instance is null => init home data");
            mCardPending.setVisibility(View.GONE);
            MyDate date;
            try {
                date = mInterface.getDate();
                Log.d(LOG_HOME, "date successfully retrieved from activity");
            }
            catch (Exception e) {
                date = MyDateUtils.getCurrentDate(mContext);
                Log.e(LOG_HOME, e.getMessage());
            }
            getHomeData(date.getMonth(), date.getYear());
        }

    }

    /* ===============================================================================
                                        NAVIGATION
     =============================================================================== */

    private void initNavigation() {
        mCardPending.setOnClickListener(v -> mInterface.openPendingFragment());
        mCardIncome.setOnClickListener(v -> mInterface.openBottomNavFragment(Tags.transactionsInTag));
        mCardExpenses.setOnClickListener(v -> mInterface.openBottomNavFragment(Tags.transactionsOutTag));
        mCardSavings.setOnClickListener(v -> mInterface.openBottomNavFragment(Tags.accountsTag));
        mCardAccounts.setOnClickListener(v -> mInterface.openBottomNavFragment(Tags.accountsTag));
        mCardExpensesCategories.setOnClickListener(v -> mInterface.openCategoriesActivity(1));
        mCardIncomeCategories.setOnClickListener(v -> mInterface.openCategoriesActivity(0));
        mCardYear.setOnClickListener(v -> mInterface.openYearActivity());
    }


    /* ===============================================================================
                                          DATA
     =============================================================================== */

    public void getHomeData(int month, int year) {
        Log.d(LOG_HOME, "== getHomeData: month = " + month + ", year = " + year);

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            Log.d(LOG_HOME, "Retrieving data from db...");

            MyDate today = MyDateUtils.getCurrentDate(mContext);
            int pendingCount = transactionDao.getPendingCount(today.getDay(), today.getMonth(), today.getYear());

            float accumulated = transactionDao.getAccumulated(month, year);
            Balance balance = transactionDao.getHomeBalance(month, year);
            HomeAccounts homeAccounts = transactionDao.getAccountsTotals();
            List<CategoryResponse> incomeCategories = transactionDao.getCategoriesWithTotals(month, year, 1);
            List<CategoryResponse> expensesCategories = transactionDao.getCategoriesWithTotals(month, year, -1);
            List<MonthResponse> yearBalance = transactionDao.getYearBalance(year);
            float savings = transactionDao.getMonthSavings(month, year);


            handler.post(() -> {
                Log.d(LOG_HOME, "Data successfully retrieved");

                // set accumulated category
                CategoryResponse accumulatedCategory = new CategoryResponse(0, accumulated, getString(R.string.label_accumulated), 16, 71);
                if (accumulated > 0) {
                    incomeCategories.add(0, accumulatedCategory);
                    incomeCategories.sort(ListSort.categoryResponseComparator);
                }
                else if (accumulated < 0) {
                    expensesCategories.add(0, accumulatedCategory);
                    expensesCategories.sort(ListSort.categoryResponseComparator);
                }

                setPendingMessage(pendingCount);
                setBalanceData(balance, accumulated, savings);
                setAccountsData(homeAccounts);
                setIncomeCategories(incomeCategories);
                setExpensesCategories(expensesCategories);
                setYearChart(yearBalance, year);

                mContainer.setVisibility(View.VISIBLE);
            });

        });
    }

    private void setPendingMessage(int count) {
        Log.d(Tags.LOG_DB, "== Pending => " + count);

        if (count == 0) mCardPending.setVisibility(View.GONE);
        else {
            mCardPending.setVisibility(View.VISIBLE);
            String msg = getString(R.string.msg_pending_part1) + " " + count + " ";
            if (count == 1) {
                String msg1 = msg + getString(R.string.msg_pending_part2);
                mPendingMsg.setText(msg1);
            }
            else {
                String msg2 = msg + getString(R.string.msg_pending_part2_plural);
                mPendingMsg.setText(msg2);
            }
        }
    }

    private void setBalanceData(Balance balance, float accumulated, float savings) {

        Log.d(LOG_HOME, "== setBalanceData: \n" +
                "balance = " + balance.getBalance() + "\n" +
                "accumulated: " + accumulated + "\n" +
                "income: " + balance.getIncome() + "\n" +
                "expenses: " + balance.getExpenses() + "\n" +
                "savings: " + savings);

        float monthBalance = balance.getBalance() + accumulated - savings;
        float monthExpenses = balance.getExpenses();
        float monthIncome = balance.getIncome();

        // size and color
        if (monthBalance > 99999999999.99) mBalance.setTextAppearance(R.style.ShrinkHomeBalance);
        if (monthBalance < 0) {
            mBalanceContainer.setBackgroundColor(ContextCompat.getColor(mContext, R.color.bg_expense_container));
            mBalance.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else {
            mBalanceContainer.setBackgroundColor(ContextCompat.getColor(mContext, R.color.bg_income_container));
            mBalance.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }

        if (accumulated < 0) {
            mAccumulated.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            mAccumulatedSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else if (accumulated > 0) {
            mAccumulated.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            mAccumulatedSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }
        mSavingsTransfer.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
        mSavingsTransferSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.savings));

        String formattedBalance = NumberUtils.getCurrencyFormat(mContext, monthBalance)[2];
        String[] formattedIncome = NumberUtils.getCurrencyFormat(mContext, monthIncome);
        String[] formattedExpenses = NumberUtils.getCurrencyFormat(mContext, monthExpenses);
        String[] formattedAccumulated = NumberUtils.getCurrencyFormat(mContext, accumulated);
        String[] formattedSavings = NumberUtils.getCurrencyFormat(mContext, savings);

        mBalance.setText(formattedBalance);
        mExpenses.setText(formattedExpenses[1]);
        mIncome.setText(formattedIncome[1]);
        mSavingsTransfer.setText(formattedSavings[1]);
        mAccumulated.setText(formattedAccumulated[1]);
        mExpensesSymbol.setText(formattedExpenses[0]);
        mIncomeSymbol.setText(formattedIncome[0]);
        mSavingsTransferSymbol.setText(formattedSavings[0]);
        mAccumulatedSymbol.setText(formattedAccumulated[0]);

        setIncomeProgress(monthIncome, monthExpenses, accumulated);

    }

    private void setIncomeProgress(float income, float expense, float accumulated) {

        if (accumulated > 0) income = income + accumulated;
        else if (accumulated < 0) expense = expense + accumulated;

        // Progress
        float percentage = (Math.abs(expense)/Math.abs(income)) * 100;
        if (percentage < 0) percentage = 0;
        if (percentage > 999) percentage = 100;
        int progress = Math.round(percentage);

        String progressString = progress + "%";
        mProgress.setText(progressString);
        mBalanceProgress.setProgress(progress);

        String progressText = getString(R.string.msg_progress_part1) + " "
                + progressString + " " + getString(R.string.msg_progress_part2);
        mProgressText.setText(progressText);

        Log.d(LOG_HOME, "percentage: " + percentage);
        Log.d(LOG_HOME, "progress: " + progress);

    }

    private void setAccountsData(HomeAccounts homeAccounts) {
        Log.d(LOG_HOME, "== setAccountsData");

        // set values

        String[] formattedCash = NumberUtils.getCurrencyFormat(mContext, homeAccounts.getCash());
        mCash.setText(formattedCash[1]);
        mCashSymbol.setText(formattedCash[0]);

        String[] formattedChecking = NumberUtils.getCurrencyFormat(mContext, homeAccounts.getChecking());
        mChecking.setText(formattedChecking[1]);
        mCheckingSymbol.setText(formattedChecking[0]);

        String[] formattedSavings = NumberUtils.getCurrencyFormat(mContext, homeAccounts.getSavings());
        mSavings.setText(formattedSavings[1]);
        mSavingsSymbol.setText(formattedSavings[0]);

        // set color
        if (homeAccounts.getCash() < 0) {
            mCash.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            mCashSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else {
            mCash.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            mCashSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }

        if (homeAccounts.getChecking() < 0) {
            mChecking.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            mCheckingSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else {
            mChecking.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            mCheckingSymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }
    }

    private void setIncomeCategories(List<CategoryResponse> categories) {

        Log.d(LOG_HOME, "== setIncomeCategories: " + categories.size());

        initCategoriesIncomeList(categories);
        ArrayList<CategoryPercent> percents = CategoryUtils.getCategoriesPercents(categories);

        mIncomeChart.post(() -> {
            int size = mContext.getResources().getInteger(R.integer.home_pie_size_int);
            int indicator = mContext.getResources().getInteger(R.integer.home_pie_indicator_int);
            mIncomeChart.setImageTintList(null);
            Charts.setCategoriesChart(mContext, percents, mIncomeChart, size, indicator, false);
        });
    }

    private void setExpensesCategories(List<CategoryResponse> categories) {

        Log.d(LOG_HOME, "== setExpensesCategories: " + categories.size());

        initCategoriesExpensesList(categories);
        ArrayList<CategoryPercent> percents = CategoryUtils.getCategoriesPercents(categories);

        mExpensesChart.post(() -> {
            int size = mContext.getResources().getInteger(R.integer.home_pie_size_int);
            int indicator = mContext.getResources().getInteger(R.integer.home_pie_indicator_int);
            mExpensesChart.setImageTintList(null);
            Charts.setCategoriesChart(mContext, percents, mExpensesChart, size, indicator, false);
        });

    }

    private void setYearChart(List<MonthResponse> response, int year) {

        Log.d(LOG_HOME, "== setYearChart: " + response.size());

        String yearLabelText = getString(R.string.label_year) + " - " + year;
        mYearLabel.setText(yearLabelText);

        mYearChart.post(() -> {
            ArrayList<MonthTotal> yearList = new ArrayList<>();
            float[] expenses = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
            float[] income =   {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
            float expensesTotal = 0f;
            float incomeTotal = 0f;

            // get expenses and income values from response
            for (int i = 0; i < response.size(); i++) {
                // get values
                float monthExpenses = Math.abs(response.get(i).getExpenses());
                float monthIncome = response.get(i).getIncome();
                int month = response.get(i).getMonth();

                // sum
                expensesTotal = expensesTotal + monthExpenses;
                incomeTotal = incomeTotal + monthIncome;

                // set month values
                int index = month - 1;
                expenses[index] = monthExpenses;
                income[index] = monthIncome;
            }

            // Build list to draw chart
            int month = 1;
            float highestBar = 0f;
            while (month < 13) {

                String[] monthName = MyDateUtils.getMonthName(mContext, month, year);
                float monthExpenses = NumberUtils.roundFloat(expenses[month-1]);
                float monthIncome = NumberUtils.roundFloat(income[month-1]);

                MonthTotal monthTotal = new MonthTotal(
                        year, month,
                        monthName[0],
                        monthName[1],
                        monthExpenses,
                        monthIncome);

                yearList.add(monthTotal);

                if (monthExpenses > highestBar) highestBar = monthExpenses;
                if (monthIncome > highestBar) highestBar = monthIncome;

                month++;
            }

            mYearChart.setImageTintList(null);
            Charts.setYearTotalChart(mContext, mYearChart, yearList, highestBar, 300, 120);
        });

    }

    /* ===============================================================================
                                    CATEGORIES CONTAINERS
     =============================================================================== */

    private void initCategoriesIncomeList(List<CategoryResponse> categories) {

        mIncomeCategoryListView.suppressLayout(false);
        Log.d(LOG_HOME, "== initCategoriesIncomeList: " + categories.size());

        List<CategoryResponse> cat;

        if (categories.size() > 3) cat = categories.subList(0, 3);
        else cat = categories;

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mIncomeCategoryListView.setLayoutManager(listLayoutManager);
        mIncomeCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        mIncomeCategoryListView.setAdapter(adapter);
        mIncomeCategoryListView.suppressLayout(true);
    }

    private void initCategoriesExpensesList(List<CategoryResponse> categories) {

        mExpensesCategoryListView.suppressLayout(false);
        Log.d(LOG_HOME, "== initCategoriesExpensesList: " + categories.size());

        List<CategoryResponse> cat;

        if (categories.size() > 3) cat = categories.subList(0, 3);
        else cat = categories;

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mExpensesCategoryListView.setLayoutManager(listLayoutManager);
        mExpensesCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        mExpensesCategoryListView.setAdapter(adapter);
        mExpensesCategoryListView.suppressLayout(true);
    }
}