package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jgm.mybudgetapp.adapters.HomeCategoryAdapter;
import com.jgm.mybudgetapp.databinding.FragmentHomeBinding;
import com.jgm.mybudgetapp.objects.Balance;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.HomeAccounts;
import com.jgm.mybudgetapp.objects.HomeCategory;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.objects.MonthTotal;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private static final String LOG_LIFECYCLE = "debug-lifecycle-home";

    // UI
    private FragmentHomeBinding binding;
    private ConstraintLayout mCardIncome, mCardExpenses, mCardSavings,
            mCardCash, mCardChecking, mCardIncomeCategories,
            mCardExpensesCategories, mCardYear, mCardPending;
    private ImageView mIncomeChart, mExpensesChart, mYearChart;
    private RecyclerView mIncomeCategoryListView, mExpensesCategoryListView;
    private TextView mBalanceText, mIncomeText, mExpensesText,
                     mCash, mChecking, mSavings, mYearBalance, mPendingMsg,
                     mCategoriesIncomeLabel, mCategoriesExpensesLabel, mProgressText;
    private CircularProgressIndicator mBalanceProgress;

    private void bindViews() {
        mCardIncome = binding.homeCardIncome;
        mCardExpenses = binding.homeCardExpenses;
        mCardSavings = binding.homeCardSavings;
        mCardCash = binding.homeCardCash;
        mCardChecking = binding.homeCardChecking;
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
        mYearBalance = binding.homeYearBalance;
        mPendingMsg = binding.homePendingText;
        mCardPending = binding.homePending;
        mCategoriesExpensesLabel = binding.homeExpensesCategoriesLabel;
        mCategoriesIncomeLabel = binding.homeIncomeCategoriesLabel;
        mBalanceProgress = binding.homeMonthProgress;
        mProgressText = binding.homeMonthProgressText;
    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mInterface = (MainInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        bindViews();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initNavigation();

        if (savedInstanceState == null) {
            Log.d(LOG_LIFECYCLE, "saved instance is null => init home data");
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
        mCardSavings.setOnClickListener(v -> mInterface.open(Tags.accountsTag));
        mCardCash.setOnClickListener(v -> mInterface.open(Tags.accountsTag));
        mCardChecking.setOnClickListener(v -> mInterface.open(Tags.accountsTag));
        mCardExpensesCategories.setOnClickListener(v -> mInterface.openExpensesCategories());
        mCardIncomeCategories.setOnClickListener(v -> mInterface.openIncomeCategories());
        mCardYear.setOnClickListener(v -> mInterface.open(Tags.yearTag));
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    public void getHomeData(int month, int year) {

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            MyDate today = MyDateUtils.getCurrentDate(mContext);
            int pendingCount = transactionDao.getPendingCount(today.getDay(), today.getMonth(), today.getYear());

            float prevTotal = transactionDao.getAccumulated(month, year);
            Balance balance = transactionDao.getHomeBalance(month, year);
            HomeAccounts homeAccounts = transactionDao.getAccountsTotals();
            List<HomeCategory> incomeCategories = transactionDao.getHomeCategories(month, year, 1);
            List<HomeCategory> expensesCategories = transactionDao.getHomeCategories(month, year, -1);
            List<MonthResponse> yearBalance = transactionDao.getYearBalance(year);

            handler.post(() -> {
                setBalanceData(balance, prevTotal);
                setAccountsData(homeAccounts);
                setIncomeCategories(incomeCategories);
                setExpensesCategories(expensesCategories);
                setYearChart(yearBalance, year);
                setBacklogMessage(pendingCount);
            });

        });
    }

    private void setBacklogMessage(int count) {
        Log.d(Tags.LOG_DB, "Pending => " + count);

        if (count == 0) mCardPending.setVisibility(View.GONE);
        else {
            mCardPending.setVisibility(View.VISIBLE);
            String msg = getString(R.string.msg_backlogs_part1) + " " + count + " ";
            if (count == 1) {
                String msg1 = msg + getString(R.string.msg_backlogs_part2);
                mPendingMsg.setText(msg1);
            }
            else {
                String msg2 = msg + getString(R.string.msg_backlogs_part2_plural);
                mPendingMsg.setText(msg2);
            }
        }
    }

    private void setBalanceData(Balance balance, float prevTotal) {

        float monthBalance = balance.getBalance() + prevTotal;
        float monthExpenses = balance.getExpenses();
        float monthIncome = balance.getIncome();

        if (prevTotal < 0.0f) monthExpenses = monthExpenses + prevTotal;
        else monthIncome = monthIncome + prevTotal;

        String formattedBalance = NumberUtils.getCurrencyFormat(mContext, monthBalance)[2];
        String formattedIncome = NumberUtils.getCurrencyFormat(mContext, monthIncome)[2];
        String formattedExpenses = NumberUtils.getCurrencyFormat(mContext, monthExpenses)[2];
        mBalanceText.setText(formattedBalance);
        mExpensesText.setText(formattedExpenses);
        mIncomeText.setText(formattedIncome);

        // Progress
        float percentage = (Math.abs(monthExpenses)/Math.abs(monthIncome)) * 100;
        if (percentage < 0) percentage = 0;
        int progress = (int) percentage;
        String progressText = progress + "%";
        mProgressText.setText(progressText);
        mBalanceProgress.setProgress(progress);

        Log.d("debug-home", "======> " + Math.abs(monthExpenses) + "/" + Math.abs(monthIncome));
        Log.d("debug-home", "======> " + percentage);
        Log.d("debug-home", "======> " + progress);

    }

    private void setAccountsData(HomeAccounts homeAccounts) {
        String formattedCash = NumberUtils.getCurrencyFormat(mContext, homeAccounts.getCash())[2];
        String formattedChecking = NumberUtils.getCurrencyFormat(mContext, homeAccounts.getChecking())[2];
        String formattedSavings = NumberUtils.getCurrencyFormat(mContext, homeAccounts.getSavings())[2];
        mCash.setText(formattedCash);
        mChecking.setText(formattedChecking);
        mSavings.setText(formattedSavings);
    }

    private void setIncomeCategories(List<HomeCategory> categories) {

        initCategoriesIncomeList(categories);
        ArrayList<CategoryPercent> percents = getCategoriesPercents(categories);

        mIncomeChart.post(() -> {
            if (percents.size() > 0) {
                mCategoriesIncomeLabel.setVisibility(View.GONE);
                mIncomeChart.setImageTintList(null);
                Charts.setCategoriesChart(mContext, percents, mIncomeChart, 100, 10);
            }
        });

    }

    private void setExpensesCategories(List<HomeCategory> categories) {

        initCategoriesExpensesList(categories);
        ArrayList<CategoryPercent> percents = getCategoriesPercents(categories);

        mExpensesChart.post(() -> {
            if (percents.size() > 0) {
                mCategoriesExpensesLabel.setVisibility(View.GONE);
                mExpensesChart.setImageTintList(null);
                Charts.setCategoriesChart(mContext, percents, mExpensesChart, 100, 10);
            }
        });

    }

    private void setYearChart(List<MonthResponse> response, int year) {

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

            // Get year balance
            float yearBalance = NumberUtils.roundFloat(incomeTotal) - NumberUtils.roundFloat(expensesTotal);
            String yearBalanceCurrency = NumberUtils.getCurrencyFormat(mContext, yearBalance)[2];
            mYearBalance.setText(yearBalanceCurrency);

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

    private ArrayList<CategoryPercent> getCategoriesPercents(List<HomeCategory> categories) {
        float total = 0.0f;
        ArrayList<CategoryPercent> percents = new ArrayList<>();

        // Get total from list
        for(int i =0; i < categories.size(); i++){
            total = total + categories.get(i).getTotal();
        }

        // Set percentage for each category
        for(int i =0; i < categories.size(); i++){
            float percent =
                    NumberUtils.roundFloat((categories.get(i).getTotal() * 100) / total);
            HomeCategory category = categories.get(i);
            CategoryPercent categoryPercent =
                    new CategoryPercent(0,category.getCategory(), category.getColorId(), 0);
            categoryPercent.setPercent(percent);
            percents.add(categoryPercent);
        }

        return percents;
    }

    private void initCategoriesIncomeList(List<HomeCategory> categories) {

        List<HomeCategory> cat;

        if (categories.size() > 3) cat = categories.subList(0, 3);
        else cat = categories;

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mIncomeCategoryListView.setLayoutManager(listLayoutManager);
        mIncomeCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        mIncomeCategoryListView.setAdapter(adapter);
    }

    private void initCategoriesExpensesList(List<HomeCategory> categories) {

        List<HomeCategory> cat;

        if (categories.size() > 3) cat = categories.subList(0, 3);
        else cat = categories;

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mExpensesCategoryListView.setLayoutManager(listLayoutManager);
        mExpensesCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        mExpensesCategoryListView.setAdapter(adapter);
    }


}