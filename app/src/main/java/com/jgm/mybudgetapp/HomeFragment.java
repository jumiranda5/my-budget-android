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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private static final String LOG_LIFECYCLE = "debug-lifecycle-home";

    // UI
    private FragmentHomeBinding binding;
    private ConstraintLayout cardIncome, cardExpenses, cardSavings,
            cardCash, cardChecking, cardIncomeCategories,
            cardExpensesCategories, cardYear;
    private ImageView incomeChart, expensesChart, yearChart;
    private RecyclerView incomeCategoryListView, expensesCategoryListView;
    private TextView mBalanceText, mIncomeText, mExpensesText,
                     mCash, mChecking, mSavings;

    private void bindViews() {
        cardIncome = binding.homeCardIncome;
        cardExpenses = binding.homeCardExpenses;
        cardSavings = binding.homeCardSavings;
        cardCash = binding.homeCardCash;
        cardChecking = binding.homeCardChecking;
        cardIncomeCategories = binding.homeCardIncomeCategories;
        cardExpensesCategories = binding.homeCardExpensesCategories;
        cardYear = binding.homeCardYear;
        incomeChart = binding.homeCategoriesIn;
        expensesChart = binding.homeCategoriesOut;
        yearChart = binding.homeYearChart;
        incomeCategoryListView = binding.homeIncomeCategoriesList;
        expensesCategoryListView = binding.homeExpensesCategoriesList;
        mBalanceText = binding.homeMonthBalance;
        mIncomeText = binding.homeIncome;
        mExpensesText = binding.homeExpenses;
        mCash = binding.homeCash;
        mChecking = binding.homeChecking;
        mSavings = binding.homeSavings;
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
            MyDate today = MyDateUtils.getCurrentDate(mContext);
            getHomeData(today.getMonth(), today.getYear());
        }

    }

    /* ===============================================================================
                                        NAVIGATION
     =============================================================================== */

    private void initNavigation() {
        cardIncome.setOnClickListener(v -> mInterface.openIncome());
        cardExpenses.setOnClickListener(v -> mInterface.openExpenses());
        cardSavings.setOnClickListener(v -> mInterface.openAccounts());
        cardCash.setOnClickListener(v -> mInterface.openAccounts());
        cardChecking.setOnClickListener(v -> mInterface.openAccounts());
        cardExpensesCategories.setOnClickListener(v -> mInterface.openExpensesCategories());
        cardIncomeCategories.setOnClickListener(v -> mInterface.openIncomeCategories());
        cardYear.setOnClickListener(v -> mInterface.openYear());
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    public void getHomeData(int month, int year) {

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbReadExecutor.execute(() -> {

            Balance balance = transactionDao.getHomeBalance(month, year);
            HomeAccounts homeAccounts = transactionDao.getAccountsTotals();
            List<HomeCategory> incomeCategories = transactionDao.getHomeCategories(month, year, 1);
            List<HomeCategory> expensesCategories = transactionDao.getHomeCategories(month, year, -1);
            List<MonthResponse> yearBalance = transactionDao.getYearBalance(year);

            handler.post(() -> {
                setBalanceData(balance);
                setAccountsData(homeAccounts);
                setIncomeCategories(incomeCategories);
                setExpensesCategories(expensesCategories);
                setYearChart(yearBalance, year);
            });

        });
    }

    private void setBalanceData(Balance balance) {
        String formattedBalance = NumberUtils.getCurrencyFormat(mContext, balance.getBalance())[2];
        String formattedIncome = NumberUtils.getCurrencyFormat(mContext, balance.getIncome())[2];
        String formattedExpenses = NumberUtils.getCurrencyFormat(mContext, balance.getExpenses())[2];
        mBalanceText.setText(formattedBalance);
        mExpensesText.setText(formattedExpenses);
        mIncomeText.setText(formattedIncome);
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

        incomeChart.post(() -> {
            incomeChart.setImageTintList(null);
            Charts.setCategoriesChart(mContext, percents, incomeChart, 100, 10);
        });

    }

    private void setExpensesCategories(List<HomeCategory> categories) {

        initCategoriesExpensesList(categories);
        ArrayList<CategoryPercent> percents = getCategoriesPercents(categories);

        expensesChart.post(() -> {
            expensesChart.setImageTintList(null);
            Charts.setCategoriesChart(mContext, percents, expensesChart, 100, 10);
        });

    }

    private void setYearChart(List<MonthResponse> response, int year) {

        // Todo => chart is not drawing correctly... ???

        yearChart.post(() -> {
            ArrayList<MonthTotal> yearList = new ArrayList<>();
            float[] expenses = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
            float[] income =   {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

            // get expenses and income values from response
            for (int i = 0; i < response.size(); i++) {
                float monthExpenses = Math.abs(response.get(i).getExpenses());
                float monthIncome = response.get(i).getIncome();
                int month = response.get(i).getMonth();

                int index = month - 1;
                expenses[index] = monthExpenses;
                income[index] = monthIncome;
            }

            int month = 1;
            float higherBar = 0f;
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

                if (monthExpenses > higherBar) higherBar = monthExpenses;
                if (monthIncome > higherBar) higherBar = monthIncome;

                month++;
            }

            yearChart.setImageTintList(null);
            Charts.setYearTotalChart(mContext, yearChart, yearList, higherBar, 300, 100);
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
        incomeCategoryListView.setLayoutManager(listLayoutManager);
        incomeCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        incomeCategoryListView.setAdapter(adapter);
    }

    private void initCategoriesExpensesList(List<HomeCategory> categories) {

        List<HomeCategory> cat;

        if (categories.size() > 3) cat = categories.subList(0, 3);
        else cat = categories;

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        expensesCategoryListView.setLayoutManager(listLayoutManager);
        expensesCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        expensesCategoryListView.setAdapter(adapter);
    }


}