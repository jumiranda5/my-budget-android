package com.jgm.mybudgetapp;

import static com.jgm.mybudgetapp.utils.Tags.adLockTag;
import static com.jgm.mybudgetapp.utils.Tags.yearTag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jgm.mybudgetapp.adapters.YearMonthAdapter;
import com.jgm.mybudgetapp.databinding.ActivityYearBinding;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.objects.MonthTotal;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.YearResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YearActivity extends AppCompatActivity implements AdInterface {

    private static final String LOG = "debug-year";
    private static final String STATE_AD = "ad";
    private static final String STATE_YEAR = "year";

    private AdLockFragment mAdLock;

    private int year;
    private TransactionDao transactionDao;
    private boolean isAdFragment;
    private boolean isPremium = false;

    // UI
    private ActivityYearBinding binding;
    private TextView mBalance, mBalanceCurrency,
            mIncome, mIncomeCurrency,
            mExpense, mExpenseCurrency,
            mYear;
    private Group mMainGroup;
    private ImageView mChart;
    private ImageButton mBack, mNextYear, mPrevYear;
    private RecyclerView mBalanceList, mIncomeList, mExpenseList;

    private void setBinding() {
        mMainGroup = binding.groupYearMain;
        mYear = binding.toolbarYear;
        mBalance = binding.yearBalanceTotal;
        mIncome = binding.yearIncomeTotal;
        mExpense = binding.yearExpenseTotal;
        mBalanceCurrency = binding.yearBalanceCurrencySymbol;
        mIncomeCurrency = binding.yearIncomeCurrencySymbol;
        mExpenseCurrency = binding.yearExpenseCurrencySymbol;
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

        Log.w(LOG, "====> activity on create");

        binding = ActivityYearBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        mMainGroup.setVisibility(View.GONE);

        transactionDao = AppDatabase.getDatabase(this).TransactionDao();

        // VARS
        MyDate mCurrentDate = MyDateUtils.getCurrentDate(this);

        if (savedInstanceState == null) year = mCurrentDate.getYear();
        else year = savedInstanceState.getInt(STATE_YEAR);

        initToolbar();

        if (savedInstanceState == null) {
            isPremium = SettingsPrefs.getSettingsPrefsBoolean(this, Tags.keyIsPremium);
            long lockTimer = MyDateUtils.getLockTimer(this, yearTag);
            if (!isPremium && lockTimer == 0) setAdLock();
            else getYearData();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Tags.LOG_LIFECYCLE, "Main Activity onResume");
        isPremium = SettingsPrefs.getSettingsPrefsBoolean(this, Tags.keyIsPremium);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.w(LOG, "====> activity on restore");

        isAdFragment = savedInstanceState.getBoolean(STATE_AD);

        if (isAdFragment) {
            mAdLock = (AdLockFragment) getSupportFragmentManager().findFragmentByTag(adLockTag);
        }
        else getYearData();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_YEAR, year);
        outState.putBoolean(STATE_AD, isAdFragment);
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
                mMainGroup.setVisibility(View.VISIBLE);
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
        String[] balanceInfo = NumberUtils.getCurrencyFormat(this, balance);
        String currency = balanceInfo[0];
        String total = balanceInfo[1];
        mBalanceCurrency.setText(currency);
        mBalance.setText(total);
        if (balance < 0) {
            mBalanceCurrency.setTextColor(ContextCompat.getColor(this, R.color.expense));
            mBalance.setTextColor(ContextCompat.getColor(this, R.color.expense));
        }
        initRecyclerView(mBalanceList, yearMonths, 0);
    }

    // Set income total
    private void setIncomeTotals(YearResponse totals, List<MonthResponse> yearMonths) {
        float income = totals.getIncome();
        String[] incomeInfo = NumberUtils.getCurrencyFormat(this, income);
        String currency = incomeInfo[0];
        String total = incomeInfo[1];
        mIncomeCurrency.setText(currency);
        mIncome.setText(total);
        initRecyclerView(mIncomeList, yearMonths, 1);
    }

    // Set expense total
    private void setExpenseTotals(YearResponse totals, List<MonthResponse> yearMonths) {
        float expense = totals.getExpenses();
        String[] expenseInfo = NumberUtils.getCurrencyFormat(this, expense);
        String currency = expenseInfo[0];
        String total = expenseInfo[1];
        mExpenseCurrency.setText(currency);
        mExpense.setText(total);
        initRecyclerView(mExpenseList, yearMonths, -1);
    }

    // Set chart
    private void setChart(List<MonthResponse> response) {

        Log.d(LOG, "== setYearChart: " + response.size());

        mChart.post(() -> {
            mChart.setImageDrawable(null);
            mChart.setImageTintList(null);

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


    /* ===============================================================================
                                     AD FRAGMENT
     =============================================================================== */

    private void setAdLock() {
        loadAdLockFragment();
        isAdFragment = true;
    }

    private void loadAdLockFragment() {
        Log.d(LOG, "loadAdLockFragment");
        mAdLock = AdLockFragment.newInstance(yearTag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.year_content_frame, mAdLock, Tags.adLockTag);
        transaction.commit();
    }

    private void destroyAdLockFragment() {
        Log.d(LOG, "destroyAdLockFragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(mAdLock);
        transaction.commit();
    }

    @Override
    public void onAdFragmentDismiss(boolean isRewardGranted) {
        Log.d(LOG, "onAdFragmentDismiss");
        try {
            if (isRewardGranted) {
                destroyAdLockFragment();
                getYearData();
                isAdFragment = false;
            }
            else onBackPressed();
        }
        catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /*
        Same crash from main activity (onAdFragmentDismiss)
     */
}