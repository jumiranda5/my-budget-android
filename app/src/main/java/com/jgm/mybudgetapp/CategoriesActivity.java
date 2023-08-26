package com.jgm.mybudgetapp;

import static com.jgm.mybudgetapp.utils.Tags.categoriesTag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jgm.mybudgetapp.adapters.CategoriesPagerAdapter;
import com.jgm.mybudgetapp.databinding.ActivityCategoriesBinding;
import com.jgm.mybudgetapp.dialogs.CategoryDialog;
import com.jgm.mybudgetapp.fragmentsCategories.CategoriesExpensesFragment;
import com.jgm.mybudgetapp.fragmentsCategories.CategoriesIncomeFragment;
import com.jgm.mybudgetapp.objects.CategoryItemResponse;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.ListSort;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity implements CategoryInterface, AdInterface {

    private static final String STATE_DAY = "day";
    private static final String STATE_MONTH = "month";
    private static final String STATE_YEAR = "year";
    private static final String STATE_TAB = "tab";
    private static final String LOG = "debug-categories";

    // Fragments
    private CategoriesExpensesFragment expensesFragment;
    private CategoriesIncomeFragment incomeFragment;
    private CategoriesPagerAdapter tabsAdapter;
    private AdLockFragment mAdLock;

    // Vars
    private MyDate selectedDate;
    private MyDate today;
    private CategoryPercent selectedCategory;
    private List<CategoryItemResponse> categoryItems;
    private int tab;
    private boolean isAdFragment;
    private boolean isPremium = false;

    // UI
    private ActivityCategoriesBinding binding;
    private ImageButton mClose, mNextMonth, mPrevMonth;
    private TextView mToolbarMonth, mToolbarYear;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Group mMainGroup;

    private void setBinding() {
        mMainGroup = binding.groupCategoriesMain;
        mClose = binding.categoriesCloseButton;
        mNextMonth = binding.categoriesNextMonth;
        mPrevMonth = binding.categoriesPrevMonth;
        mToolbarMonth = binding.categoriesToolbarMonth;
        mToolbarYear = binding.categoriesToolbarYear;
        viewPager = binding.categoriesViewPager;
        tabLayout = binding.categoriesTabs;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        Log.d(LOG, "======== onCreate");

        expensesFragment = new CategoriesExpensesFragment();
        incomeFragment = new CategoriesIncomeFragment();

        // init date
        today = MyDateUtils.getCurrentDate(this);
        selectedDate = today;
        int day = today.getDay();
        int month = today.getMonth();
        int year = today.getYear();

        // get intents
        if (savedInstanceState == null) {
            Log.d(LOG, "savedInstanceState = null");

            if (getIntent().getExtras() != null) {
                tab = getIntent().getIntExtra("tab", 0);
                day = getIntent().getIntExtra("day", selectedDate.getDay());
                month = getIntent().getIntExtra("month", selectedDate.getMonth());
                year = getIntent().getIntExtra("year", selectedDate.getYear());
                Log.d(LOG, "intent extras => tab: " + tab + " | day: " + day + " | month: " + month + " | year: " + year);
            }

            setDate(day, month, year);
            initToolbar();

            // If activity is locked by Ad, load ad fragment / else load tabs
            isPremium = SettingsPrefs.getSettingsPrefsBoolean(this, Tags.keyIsPremium);
            long lockTimer = MyDateUtils.getLockTimer(this, categoriesTag);
            Log.d(LOG, "lockTimer: " + lockTimer);
            if (!isPremium && lockTimer == 0) setAdLock(null);
            else loadTabs(tab);

        }
        else {
            // The tabs fragments only work if I load the tabs twice after screen rotation...
            // must be loaded here and onRestoreInstanceState todo: WHY??
            loadTabs(tab);
        }

    }

    protected void onResume() {
        super.onResume();
        Log.i(Tags.LOG_LIFECYCLE, "Main Activity onResume");
        isPremium = SettingsPrefs.getSettingsPrefsBoolean(this, Tags.keyIsPremium);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(LOG, "======== onRestoreInstanceState");
        tab = savedInstanceState.getInt(STATE_TAB, 0);
        int day = savedInstanceState.getInt(STATE_DAY, selectedDate.getDay());
        int month = savedInstanceState.getInt(STATE_MONTH, selectedDate.getMonth());
        int year = savedInstanceState.getInt(STATE_YEAR, selectedDate.getYear());
        Log.d(LOG, "savedInstanceState => tab: " + tab + " | day: " + day + " | month: " + month + " | year: " + year);

        setDate(day, month, year);
        initToolbar();

        // If activity is locked by Ad, load ad fragment / else load tabs
        isPremium = SettingsPrefs.getSettingsPrefsBoolean(this, Tags.keyIsPremium);
        long lockTimer = MyDateUtils.getLockTimer(this, categoriesTag);
        Log.d(LOG, "lockTimer: " + lockTimer);
        if (!isPremium && lockTimer == 0) {
            mMainGroup.setVisibility(View.GONE);
            setAdLock(savedInstanceState);
        }
        else loadTabs(tab);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(Tags.LOG_LIFECYCLE, "======== onSaveInstanceState");

        int currentTab;
        if (isAdFragment) currentTab = tab;
        else currentTab = viewPager.getCurrentItem();

        outState.putInt(STATE_TAB, currentTab);
        outState.putInt(STATE_DAY, selectedDate.getDay());
        outState.putInt(STATE_MONTH, selectedDate.getMonth());
        outState.putInt(STATE_YEAR, selectedDate.getYear());

    }

    /* =============================================================================================
                                              TOOLBAR
     ============================================================================================ */

    private void setDate(int day, int month, int year) {
        Log.d(LOG, "=> setDate");
        selectedDate = new MyDate(day, month, year);
        selectedDate.setMonthName(MyDateUtils.getMonthName(this, month, year)[0]);
    }

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
        setCategoriesDataOnMonthChange(selectedDate.getMonth(), selectedDate.getYear());

        Log.d(LOG, "Next month: " + nextDate.getMonthName());
    }

    private void setToolbarPrevMonth() {
        MyDate prevDate = MyDateUtils.getPrevMonth(this, selectedDate.getMonth(), selectedDate.getYear());
        selectedDate = prevDate;
        mToolbarMonth.setText(prevDate.getMonthName());
        mToolbarYear.setText(String.valueOf(prevDate.getYear()));
        setToolbarMonthStyle();
        setCategoriesDataOnMonthChange(selectedDate.getMonth(), selectedDate.getYear());

        Log.d(LOG, "Previous month: " + prevDate.getMonthName());
    }

    private void setToolbarMonthStyle() {
        if (selectedDate.getMonth() != today.getMonth()
                || selectedDate.getYear() != today.getYear()) {
            mToolbarMonth.setTextAppearance(R.style.ToolbarNotCurrentMonth);
        }
        else mToolbarMonth.setTextAppearance(R.style.ToolbarMonth);
    }

    /* =============================================================================================
                                               TABS
     ============================================================================================ */

    private void loadTabs(int tab) {
        Log.d(LOG, "==========> load tabs: " + tab);
        prepareTabs();
        setTabs();
        setInitialTab(tab);
    }

    private void prepareTabs() {
        Log.d(LOG, "=========> prepare tabs");
        List<Fragment> tabFragments = new ArrayList<>();
        tabFragments.add(incomeFragment);
        tabFragments.add(expensesFragment);
        tabsAdapter = new CategoriesPagerAdapter(this, tabFragments);
    }

    private void setTabs() {
        Log.d(LOG, "==========> setTabs");
        viewPager.setAdapter(tabsAdapter);
        viewPager.setOffscreenPageLimit(2);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText(R.string.title_income);
            else tab.setText(R.string.title_expense);
            setCategoriesDataOnMonthChange(selectedDate.getMonth(), selectedDate.getYear());
        }).attach();
    }

    private void setInitialTab(int tab) {
        Log.d(LOG, "=> setInitialTab => TYPE: " + tab);
        viewPager.setCurrentItem(tab, false);
    }

    /* =============================================================================================
                                               ADS
     ============================================================================================ */

    private void setAdLock(Bundle savedInstanceState) {
        isAdFragment = true;
        if (savedInstanceState == null) loadAdLockFragment();
        else mAdLock = (AdLockFragment) getSupportFragmentManager().findFragmentByTag(Tags.adLockTag);
    }

    private void loadAdLockFragment() {
        Log.d(LOG, "=> loadAdLockFragment");
        mMainGroup.setVisibility(View.GONE);
        mAdLock = AdLockFragment.newInstance(categoriesTag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.category_content_frame, mAdLock, Tags.adLockTag);
        transaction.commit();
    }

    private void destroyAdLockFragment() {
        Log.d(LOG, "=> destroyAdLockFragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(mAdLock);
        transaction.commit();
        mMainGroup.setVisibility(View.VISIBLE);
    }


    /* =============================================================================================
                                             INTERFACE
     ============================================================================================ */

    // Ads
    @Override
    public void onAdFragmentDismiss(boolean isRewardGranted) {
        Log.d(LOG, "dismiss ad fragment. isRewardGranted: " + isRewardGranted);
        isAdFragment = false;
        if (isRewardGranted) {
            destroyAdLockFragment();
            loadTabs(tab);
        }
        else onBackPressed();
    }

    // Fragments
    @Override
    public void showCategoryDetails(CategoryPercent category) {

        selectedCategory = category;

        Log.d(LOG, "=> showCategoryDetails: " + category.getName() + " = " + category.getTotal());

        TransactionDao transactionDao = AppDatabase.getDatabase(this).TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            int type;
            if (viewPager.getCurrentItem() == 0) type = 1;
            else type = -1;

            categoryItems = transactionDao.getCategoryDetails(
                    category.getId(), selectedDate.getMonth(), selectedDate.getYear(), type);

            handler.post(() -> {

                BottomSheetDialogFragment categoryDialog = new CategoryDialog();
                categoryDialog.show(getSupportFragmentManager(), "CATEGORY DIALOG");

//                for (int i = 0; i < items.size(); i++) {
//                    CategoryItemResponse item = items.get(i);
//                    Log.d("debug-category", item.getName() + "(" + item.getCount() + ") " + " => " + item.getTotal());
//                }

            });

        });
    }

    @Override
    public CategoryPercent getCategoryData() {
        return selectedCategory;
    }

    @Override
    public List<CategoryItemResponse> getCategoryItems() {
        return categoryItems;
    }

    @Override
    public MyDate getSelectedDate() {
        return selectedDate;
    }



    /* =============================================================================================
                                                DATA
     ============================================================================================ */

    private void setCategoriesDataOnMonthChange(int month, int year) {
        Log.d(LOG, "======== getCategoriesDataOnMonthChange");

        TransactionDao transactionDao = AppDatabase.getDatabase(this).TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            Log.d(LOG, "month: " + month + " | year: " + year);

            float accumulated = transactionDao.getAccumulated(month, year);
            List<CategoryResponse> expensesCategories = transactionDao.getCategoriesWithTotals(month, year, -1);
            List<CategoryResponse> incomeCategories = transactionDao.getCategoriesWithTotals(month, year, 1);

            handler.post(() -> {

                Log.d(LOG, "data successfully retrieved");

                // Set accumulated
                CategoryResponse accumulatedCategory = new CategoryResponse(0, accumulated, getString(R.string.label_accumulated), 16, 71);
                if (accumulated > 0) {
                    incomeCategories.add(0, accumulatedCategory);
                    incomeCategories.sort(ListSort.categoryResponseComparator);
                }
                else if (accumulated < 0) {
                    expensesCategories.add(0, accumulatedCategory);
                    expensesCategories.sort(ListSort.categoryResponseComparator);
                }

                expensesFragment.setExpensesCategoriesData(expensesCategories);
                incomeFragment.setIncomeCategoriesData(incomeCategories);

            });

        });
    }


//    private void getCategoriesData(int month, int year, int type) {
//        Log.d(LOG, "=> getCategoriesData");
//
//        TransactionDao transactionDao = AppDatabase.getDatabase(this).TransactionDao();
//        Handler handler = new Handler(Looper.getMainLooper());
//        AppDatabase.dbExecutor.execute(() -> {
//
//            Log.d(LOG, "month: " + month + " | year: " + year);
//
//            float accumulated = transactionDao.getAccumulated(month, year);
//            List<CategoryResponse> categories = transactionDao.getCategoriesWithTotals(month, year, type);
//
//            handler.post(() -> {
//
//                Log.d(LOG, "data successfully retrieved");
//
//                // Set accumulated
//                CategoryResponse accumulatedCategory = new CategoryResponse(0, accumulated, getString(R.string.label_accumulated), 16, 71);
//                if (accumulated > 0 && type == Tags.TYPE_IN) {
//                    categories.add(0, accumulatedCategory);
//                    categories.sort(ListSort.categoryResponseComparator);
//                }
//                else if (accumulated < 0 && type == Tags.TYPE_OUT) {
//                    categories.add(0, accumulatedCategory);
//                    categories.sort(ListSort.categoryResponseComparator);
//                }
//
//                if (type == Tags.TYPE_OUT) expensesFragment.setExpensesCategoriesData(categories);
//                else incomeFragment.setIncomeCategoriesData(categories);
//
//            });
//
//        });
//    }


}