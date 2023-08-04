package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.jgm.mybudgetapp.utils.CategoryUtils;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.ListSort;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private final static String LOG_HOME = "debug-home";

    // UI
    private FragmentHomeBinding binding;
    private MaterialCardView mCardIncome, mCardExpenses, mCardAccounts, mCardProgress,
            mCardIncomeCategories, mCardExpensesCategories, mCardYear, mCardPending;
    private ImageView mIncomeChart, mExpensesChart, mYearChart;
    private RecyclerView mIncomeCategoryListView, mExpensesCategoryListView;
    private TextView mBalanceText, mIncomeText, mExpensesText,
            mCash, mChecking, mSavings, mCashSymbol, mCheckingSymbol, mSavingsSymbol,
            mPendingMsg, mProgress, mProgressText, mYearLabel;
    private CircularProgressIndicator mBalanceProgress;

    private void bindViews() {
        mCardIncome = binding.homeCardIncome;
        mCardExpenses = binding.homeCardExpenses;
        mCardAccounts = binding.homeCardAccounts;
        mCardIncomeCategories = binding.homeCardIncomeCategories;
        mCardExpensesCategories = binding.homeCardExpensesCategories;
        mCardYear = binding.homeCardYear;
        mIncomeChart = binding.homeCategoriesIn;
        mExpensesChart = binding.homeCategoriesOut;
        mYearChart = binding.homeYearChart;
        mIncomeCategoryListView = binding.homeIncomeCategoriesList;
        mExpensesCategoryListView = binding.homeExpensesCategoriesList;
        mBalanceText = binding.homeMonthBalance;
        mIncomeText = binding.homeIncome;
        mExpensesText = binding.homeExpenses;
        mCash = binding.homeCash;
        mChecking = binding.homeChecking;
        mSavings = binding.homeSavings;
        mPendingMsg = binding.homePendingText;
        mCardPending = binding.homePending;
        mCardProgress = binding.homeCardExpensesProgress;
        mBalanceProgress = binding.homeMonthProgress;
        mProgress = binding.homeMonthProgressText;
        mProgressText = binding.homeProgressText;
        mCashSymbol = binding.homeCashCurrencySymbol;
        mCheckingSymbol = binding.homeCheckingCurrencySymbol;
        mSavingsSymbol = binding.homeSavingsCurrencySymbol;
        mYearLabel = binding.homeYearLabel;
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
            MyDate today = MyDateUtils.getCurrentDate(mContext);
            getHomeData(today.getMonth(), today.getYear());
        }

    }

    /* ===============================================================================
                                        NAVIGATION
     =============================================================================== */

    private void initNavigation() {
        mCardPending.setOnClickListener(v -> mInterface.open(Tags.pendingTag));
        mCardIncome.setOnClickListener(v -> mInterface.open(Tags.transactionsInTag));
        mCardExpenses.setOnClickListener(v -> mInterface.open(Tags.transactionsOutTag));
        mCardProgress.setOnClickListener(v -> mInterface.open(Tags.transactionsOutTag));
        mCardAccounts.setOnClickListener(v -> mInterface.open(Tags.accountsTag));
        mCardExpensesCategories.setOnClickListener(v -> mInterface.openExpensesCategories());
        mCardIncomeCategories.setOnClickListener(v -> mInterface.openIncomeCategories());
        mCardYear.setOnClickListener(v -> mInterface.open(Tags.yearTag));
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

            handler.post(() -> {
                Log.d(LOG_HOME, "Data successfully retrieved");

                // set accumulated category
                CategoryResponse accumulatedCategory = new CategoryResponse(accumulated, getString(R.string.label_accumulated), 16, 71);
                if (accumulated > 0) {
                    incomeCategories.add(0, accumulatedCategory);
                    incomeCategories.sort(ListSort.categoryResponseComparator);
                }
                else if (accumulated < 0) {
                    expensesCategories.add(0, accumulatedCategory);
                    expensesCategories.sort(ListSort.categoryResponseComparator);
                }

                setPendingMessage(pendingCount);
                setBalanceData(balance, accumulated);
                setAccountsData(homeAccounts);
                setIncomeCategories(incomeCategories);
                setExpensesCategories(expensesCategories);
                setYearChart(yearBalance, year);
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

    private void setBalanceData(Balance balance, float accumulated) {

        Log.d(LOG_HOME, "== setBalanceData: \n" +
                "balance = " + balance.getBalance() + "\n" +
                "accumulated: " + accumulated + "\n" +
                "income: " + balance.getIncome() + "\n" +
                "expenses: " + balance.getExpenses());

        float monthBalance = balance.getBalance() + accumulated;
        float monthExpenses = balance.getExpenses();
        float monthIncome = balance.getIncome();
        setNumbersDisplaySizeAndColor(monthBalance, monthIncome, monthExpenses);

        if (accumulated < 0.0f) monthExpenses = monthExpenses + accumulated;
        else monthIncome = monthIncome + accumulated;

        String formattedBalance = NumberUtils.getCurrencyFormat(mContext, monthBalance)[2];
        String formattedIncome = NumberUtils.getCurrencyFormat(mContext, monthIncome)[2];
        String formattedExpenses = NumberUtils.getCurrencyFormat(mContext, monthExpenses)[3];
        mBalanceText.setText(formattedBalance);
        mExpensesText.setText(formattedExpenses);
        mIncomeText.setText(formattedIncome);

        // Progress
        float percentage = (Math.abs(monthExpenses)/Math.abs(monthIncome)) * 100;
        if (percentage < 0) percentage = 0;
        if (percentage > 999) percentage = 100;
        int progress = (int) percentage;

        String progressString = progress + "%";
        mProgress.setText(progressString);
        mBalanceProgress.setProgress(progress);

        String progressText = getString(R.string.msg_progress_part1) + " "
                + progressString + " " + getString(R.string.msg_progress_part2);
        mProgressText.setText(progressText);

        Log.d(LOG_HOME, "percentage: " + percentage);
        Log.d(LOG_HOME, "progress: " + progress);

    }

    private void setNumbersDisplaySizeAndColor(float balance, float income, float expense) {
        // size
        if (balance > 999999.99) mBalanceText.setTextAppearance(R.style.ShrinkHomeBalance);
        if (income > 999999.99 || expense > 999999.99) {
            mIncomeText.setTextAppearance(R.style.ShrinkHomeIncome);
            mExpensesText.setTextAppearance(R.style.ShrinkHomeIncome);
        }

        // color
        if (balance < 0) mBalanceText.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        else mBalanceText.setTextColor(ContextCompat.getColor(mContext, R.color.income));
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
            Charts.setCategoriesChart(mContext, percents, mIncomeChart, size, indicator, true);
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
            Charts.setCategoriesChart(mContext, percents, mExpensesChart, size, indicator, true);
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
            Charts.setYearTotalChart(mContext, mYearChart, yearList, highestBar, 300, 100);
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