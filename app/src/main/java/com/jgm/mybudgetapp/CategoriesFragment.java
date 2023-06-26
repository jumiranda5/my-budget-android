package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.jgm.mybudgetapp.adapters.TextTabsAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesBinding;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private static final String OUT = "OUT";
    private static final String IN = "IN";

    public CategoriesFragment() {
        // Required empty public constructor
    }

    // Fragments
    private final CategoriesExpensesFragment expensesFragment = new CategoriesExpensesFragment();
    private final CategoriesIncomeFragment incomeFragment = new CategoriesIncomeFragment();

    // Tabs
    private final List<Fragment> tabFragments = new ArrayList<>();
    private final List<String> tabTitles = new ArrayList<>();
    private TextTabsAdapter tabsAdapter;

    // UI
    private FragmentCategoriesBinding binding;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private void setBinding() {
        viewPager = binding.categoriesViewPager;
        tabLayout = binding.categoriesTabs;
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
        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            prepareTabs();
            setTabs();
            setInitialTab(OUT);
            MyDate date = mInterface.getDate();
            getCategoriesData(date.getMonth(), date.getYear());
        }

    }

    /* -------------------------------------------------------------------------------------------
                                                DATA
    --------------------------------------------------------------------------------------------- */

    public void getCategoriesData(int month, int year) {
        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();
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

    /* -------------------------------------------------------------------------------------------
                                                TABS
    --------------------------------------------------------------------------------------------- */

    private void prepareTabs() {
        tabsAdapter = new TextTabsAdapter(getChildFragmentManager(), tabFragments, tabTitles);
        addTab(incomeFragment, getString(R.string.label_income));
        addTab(expensesFragment, getString(R.string.label_expenses));
    }

    private void addTab(Fragment fragment, String title) {
        tabFragments.add(fragment);
        tabTitles.add(title);
    }

    private void setTabs() {
        viewPager.setAdapter(tabsAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void setInitialTab(String param) {
        Log.d("debug-categories", "PARAM => " + param);
        if (param.equals("OUT")) viewPager.setCurrentItem(1);
        else viewPager.setCurrentItem(0);
    }
}