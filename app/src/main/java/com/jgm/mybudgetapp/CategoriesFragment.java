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
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

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
    private int type = Tags.TYPE_OUT;

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
            setInitialTab(type);
            MyDate date = mInterface.getDate();
            getCategoriesData(date.getMonth(), date.getYear());
        }

    }

    public void setType(int type) {
        this.type = type;
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
        addTab(incomeFragment, getString(R.string.title_income));
        addTab(expensesFragment, getString(R.string.title_expenses));
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

    private void setInitialTab(int tab) {
        Log.d("debug-categories", "TYPE => " + tab);
        if (tab == Tags.TYPE_OUT) viewPager.setCurrentItem(1);
        else viewPager.setCurrentItem(0);
    }

}