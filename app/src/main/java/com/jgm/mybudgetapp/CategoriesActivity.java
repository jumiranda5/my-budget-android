package com.jgm.mybudgetapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jgm.mybudgetapp.adapters.CategoriesPagerAdapter;
import com.jgm.mybudgetapp.databinding.ActivityCategoriesBinding;
import com.jgm.mybudgetapp.fragmentsCategories.CategoriesExpensesFragment;
import com.jgm.mybudgetapp.fragmentsCategories.CategoriesIncomeFragment;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private static final String STATE_DAY = "day";
    private static final String STATE_MONTH = "month";
    private static final String STATE_YEAR = "year";
    private static final String STATE_TAB = "tab";

    private static final String LOG = "debug-categories";

    // Fragments
    private CategoriesExpensesFragment expensesFragment;
    private CategoriesIncomeFragment incomeFragment;
    private CategoriesPagerAdapter tabsAdapter;

    // Vars
    private MyDate selectedDate;
    private MyDate today;

    // UI
    private ActivityCategoriesBinding binding;
    private ImageButton mClose, mNextMonth, mPrevMonth;
    private TextView mToolbarMonth, mToolbarYear;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private void setBinding() {
        mClose = binding.categoriesCloseButton;
        mNextMonth = binding.categoriesNextMonth;
        mPrevMonth = binding.categoriesPrevMonth;
        mToolbarMonth = binding.categoriesToolbarMonth;
        mToolbarYear = binding.categoriesToolbarYear;
        viewPager = binding.categoriesViewPager;
        tabLayout = binding.categoriesTabs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        // reference fragments
        expensesFragment = new CategoriesExpensesFragment();
        incomeFragment = new CategoriesIncomeFragment();

        // get current date
        today = MyDateUtils.getCurrentDate(this);
        selectedDate = today;

        // get info from intent extra or savedInstanceState
        int tab = 0;
        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                tab = getIntent().getIntExtra("tab", 0);
                int day = getIntent().getIntExtra("day", selectedDate.getDay());
                int month = getIntent().getIntExtra("month", selectedDate.getMonth());
                int year = getIntent().getIntExtra("year", selectedDate.getYear());
                selectedDate = new MyDate(day, month, year);
                selectedDate.setMonthName(MyDateUtils.getMonthName(this, month, year)[0]);
            }
        }
        else {
            Log.d(LOG, "savedInstance NOT null => get saved info and set toolbar date");
            int day = savedInstanceState.getInt(STATE_DAY, selectedDate.getDay());
            int month = savedInstanceState.getInt(STATE_MONTH, selectedDate.getMonth());
            int year = savedInstanceState.getInt(STATE_YEAR, selectedDate.getYear());
            int savedTab = savedInstanceState.getInt(STATE_TAB, 0);
            selectedDate = new MyDate(day,month,year);
            selectedDate.setMonthName(MyDateUtils.getMonthName(this, month, year)[0]);
            tab = savedTab;
        }

        initToolbar();

        // init tabs
        prepareTabs();
        setTabs();
        setInitialTab(tab);

        // get categories data
        Log.d("debug-omg", "Is fragment null? " + (expensesFragment == null));
        getCategoriesData(selectedDate.getMonth(), selectedDate.getYear());

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // only working if I init tabs onCreate and here... ???

        // init tabs
        prepareTabs();
        setTabs();
        setInitialTab(savedInstanceState.getInt(STATE_TAB, 0));

        // get categories data
        Log.d("debug-omg", "Is fragment null onRestore? " + (expensesFragment == null));
        getCategoriesData(selectedDate.getMonth(), selectedDate.getYear());


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(Tags.LOG_LIFECYCLE, "Main Activity onSaveInstanceState");
        outState.putInt(STATE_TAB, viewPager.getCurrentItem());
        outState.putInt(STATE_DAY, selectedDate.getDay());
        outState.putInt(STATE_MONTH, selectedDate.getMonth());
        outState.putInt(STATE_YEAR, selectedDate.getYear());
    }

    /* ===============================================================================
                                         TOOLBAR
     =============================================================================== */

    private void initToolbar() {
        Log.d(LOG, "=> Init toolbar");
        // set toolbar date
        mToolbarMonth.setText(selectedDate.getMonthName());
        mToolbarYear.setText(String.valueOf(selectedDate.getYear()));
        setToolbarMonthStyle();

        // init buttons
        mClose.setOnClickListener(v -> onBackPressed());
        mNextMonth.setOnClickListener(v -> setToolbarNextMonth());
        mPrevMonth.setOnClickListener(v -> setToolbarPrevMonth());
    }

    private void setToolbarNextMonth() {
        MyDate nextDate = MyDateUtils.getNextMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = nextDate;
        mToolbarMonth.setText(nextDate.getMonthName());
        mToolbarYear.setText(String.valueOf(nextDate.getYear()));
        setToolbarMonthStyle();
        getCategoriesData(selectedDate.getMonth(), selectedDate.getYear());

        Log.d(LOG, "Next month: " + nextDate.getMonthName());
    }

    private void setToolbarPrevMonth() {
        MyDate prevDate = MyDateUtils.getPrevMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = prevDate;
        mToolbarMonth.setText(prevDate.getMonthName());
        mToolbarYear.setText(String.valueOf(prevDate.getYear()));
        setToolbarMonthStyle();
        getCategoriesData(selectedDate.getMonth(), selectedDate.getYear());

        Log.d(LOG, "Previous month: " + prevDate.getMonthName());
    }

    private void setToolbarMonthStyle() {
        if (selectedDate.getMonth() != today.getMonth()
                || selectedDate.getYear() != today.getYear()) {
            mToolbarMonth.setTextAppearance(R.style.ToolbarNotCurrentMonth);
        }
        else mToolbarMonth.setTextAppearance(R.style.ToolbarMonth);
    }


    /* ===============================================================================
                                           TABS
     =============================================================================== */

    private void prepareTabs() {
        // Tabs
        List<Fragment> tabFragments = new ArrayList<>();
        tabFragments.add(incomeFragment);
        tabFragments.add(expensesFragment);
        tabsAdapter = new CategoriesPagerAdapter(this, tabFragments);
    }

    private void setTabs() {
        viewPager.setAdapter(tabsAdapter);
        viewPager.setOffscreenPageLimit(2);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText(R.string.title_income);
            else tab.setText(R.string.title_expense);
        }).attach();
    }

    private void setInitialTab(int tab) {
        Log.d("debug-categories", "TYPE => " + tab);
        viewPager.setCurrentItem(tab);
    }


    /* ===============================================================================
                                           DATA
     =============================================================================== */

    private void getCategoriesData(int month, int year) {
        TransactionDao transactionDao = AppDatabase.getDatabase(this).TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            List<CategoryResponse> expensesCategories = transactionDao.getCategoriesWithTotals(month, year, -1);
            List<CategoryResponse> incomeCategories = transactionDao.getCategoriesWithTotals(month, year, 1);

            handler.post(() -> {
                expensesFragment.setExpensesCategoriesData(expensesCategories);
                incomeFragment.setIncomeCategoriesData(incomeCategories);
            });

        });
    }
}