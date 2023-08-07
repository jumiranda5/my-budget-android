package com.jgm.mybudgetapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.adapters.YearMonthAdapter;
import com.jgm.mybudgetapp.databinding.ActivityYearBinding;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.objects.MonthTotal;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.YearResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YearActivity extends AppCompatActivity {

    private static final String LOG = "debug-year";

    // VARS
    private MyDate mCurrentDate;
    private int year;
    private TransactionDao transactionDao;

    // UI
    private ActivityYearBinding binding;
    private TextView mBalance, mIncome, mExpense, mYear;
    private ImageView mChart;
    private ImageButton mBack, mNextYear, mPrevYear;
    private RecyclerView mBalanceList, mIncomeList, mExpenseList;

    private void setBinding() {
        mYear = binding.toolbarYear;
        mBalance = binding.yearBalanceTotal;
        mIncome = binding.yearIncomeTotal;
        mExpense = binding.yearExpenseTotal;
        mChart = binding.yearChart;
        mBack = binding.yearBackButton;
        mNextYear = binding.nextYearButton;
        mPrevYear = binding.prevYearButton;
        mBalanceList = binding.yearBalanceList;
        mIncomeList = binding.yearIncomeList;
        mExpenseList = binding.yearExpenseList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityYearBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        transactionDao = AppDatabase.getDatabase(this).TransactionDao();

        mCurrentDate = MyDateUtils.getCurrentDate(this);
        year = mCurrentDate.getYear();

        initToolbar();
        getYearData();

    }

    // Set toolbar year and buttons
    private void initToolbar() {
        mYear.setText(String.valueOf(year));
        mBack.setOnClickListener(v -> onBackPressed());
        mNextYear.setOnClickListener(v -> setNextYear());
        mPrevYear.setOnClickListener(v -> setPrevYear());
    }

    private void setPrevYear() {
        year--;
        mYear.setText(String.valueOf(year));
        Log.d(LOG, "set prev year: " + year);
        getYearData();
    }

    private void setNextYear() {
        year++;
        mYear.setText(String.valueOf(year));
        Log.d(LOG, "set prev year: " + year);
        getYearData();
    }


    // Get data from db

    private void getYearData() {
        Log.d(LOG, "=> getYearData");

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            YearResponse totals = transactionDao.getYearTotals(year);
            List<MonthResponse> yearMonths = transactionDao.getYearBalance(year);

            handler.post(() -> {
                Log.d(LOG, "Data successfully retrieved");
                setBalanceTotal(totals, yearMonths);
                setIncomeTotals(totals, yearMonths);
                setExpenseTotals(totals, yearMonths);
                setChart(yearMonths);
            });
        });
    }

    // Set balance total
    private void setBalanceTotal(YearResponse totals, List<MonthResponse> yearMonths) {
        float balance = totals.getBalance();
        String balanceString = NumberUtils.getCurrencyFormat(this, balance)[2];
        mBalance.setText(balanceString);
        initRecyclerView(mBalanceList, yearMonths, 0);
    }

    // Set income total
    private void setIncomeTotals(YearResponse totals, List<MonthResponse> yearMonths) {
        float income = totals.getIncome();
        String incomeString = NumberUtils.getCurrencyFormat(this, income)[2];
        mIncome.setText(incomeString);
        initRecyclerView(mIncomeList, yearMonths, 1);
    }

    // Set expense total
    private void setExpenseTotals(YearResponse totals, List<MonthResponse> yearMonths) {
        float expense = totals.getExpenses();
        String expenseString = NumberUtils.getCurrencyFormat(this, expense)[2];
        mExpense.setText(expenseString);
        initRecyclerView(mExpenseList, yearMonths, -1);
    }

    // Set chart
    private void setChart(List<MonthResponse> response) {

        Log.d(LOG, "== setYearChart: " + response.size());

        mChart.post(() -> {
            ArrayList<MonthTotal> yearList = new ArrayList<>();
            float[] expenses = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
            float[] income =   {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

            // get expenses and income values from response
            for (int i = 0; i < response.size(); i++) {
                // get values
                float monthExpenses = Math.abs(response.get(i).getExpenses());
                float monthIncome = response.get(i).getIncome();
                int month = response.get(i).getMonth();

                // set month values
                int index = month - 1;
                expenses[index] = monthExpenses;
                income[index] = monthIncome;
            }

            // Build list to draw chart
            int month = 1;
            float highestBar = 0f;
            while (month < 13) {

                String[] monthName = MyDateUtils.getMonthName(this, month, year);
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

            mChart.setImageTintList(null);
            Charts.setYearTotalChart(this, mChart, yearList, highestBar, 300, 150);
        });

    }

    // Lists views
    private void initRecyclerView(RecyclerView recyclerView, List<MonthResponse> months, int type) {

        List<MonthResponse> yearMonths = new ArrayList<>();
        float[] expenses = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        float[] income =   {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

        // get expenses and income values from response
        for (int i = 0; i < months.size(); i++) {
            // get values
            float monthExpenses = Math.abs(months.get(i).getExpenses());
            float monthIncome = months.get(i).getIncome();
            int month = months.get(i).getMonth();

            // set month values
            int index = month - 1;
            expenses[index] = monthExpenses;
            income[index] = monthIncome;
        }

        Log.d(LOG, "expenses: " + Arrays.toString(expenses));
        Log.d(LOG, "income: " + Arrays.toString(income));

        int month = 1;
        while (month < 13) {

            float monthExpenses = NumberUtils.roundFloat(expenses[month-1]);
            float monthIncome = NumberUtils.roundFloat(income[month-1]);

            MonthResponse monthResponse = new MonthResponse(month, monthIncome, monthExpenses);
            yearMonths.add(monthResponse);

            month++;
        }

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(listLayoutManager);
        YearMonthAdapter adapter = new YearMonthAdapter(this, yearMonths, year, type);
        recyclerView.setAdapter(adapter);
    }

}