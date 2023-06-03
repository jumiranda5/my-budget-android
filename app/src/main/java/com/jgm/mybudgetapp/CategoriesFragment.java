package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.jgm.mybudgetapp.adapters.TextTabsAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesBinding;

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
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

        prepareTabs();
        setTabs();
        setInitialTab(OUT);

    }

    /* -------------------------------------------------------------------------------------------
                                                TABS
    --------------------------------------------------------------------------------------------- */

    private void prepareTabs() {
        tabsAdapter = new TextTabsAdapter(getChildFragmentManager(), tabFragments, tabTitles);
        addTab(incomeFragment, "Income");
        addTab(expensesFragment, "Expenses");
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