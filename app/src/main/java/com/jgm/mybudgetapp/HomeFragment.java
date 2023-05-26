package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jgm.mybudgetapp.adapters.HomeCategoryAdapter;
import com.jgm.mybudgetapp.databinding.FragmentHomeBinding;
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.objects.MonthTotal;
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
        setDummyExpensesList();
        setDummyIncomeList();
        setDummyYear();

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
                                    CATEGORIES CONTAINERS
     =============================================================================== */

    // Lists
    private final ArrayList<Category> categoriesExpenses = new ArrayList<>();
    private final ArrayList<Category> categoriesIncome = new ArrayList<>();
    private void setDummyExpensesList() {
        Category c1 = new Category(0, "Home", R.color.savings, 0, true);
        Category c2 = new Category(0, "Restaurant", R.color.colorAccent, 0, true);
        Category c3 = new Category(0, "Groceries", R.color.expense, 0, true);
        Category c4 = new Category(0, "Clothes", R.color.colorSecondary, 0, true);
        Category c5 = new Category(0, "Car", R.color.main_text, 0, true);
        Category c6 = new Category(0, "Gym", R.color.income, 0, true);

        float totalExpenses = 3508.75f;
        c1.setTotal(2034.80f);
        c2.setTotal(323.95f);
        c3.setTotal(200f);
        c4.setTotal(350f);
        c5.setTotal(500f);
        c6.setTotal(100f);

        categoriesExpenses.add(c1);
        categoriesExpenses.add(c2);
        categoriesExpenses.add(c3);
        categoriesExpenses.add(c4);
        categoriesExpenses.add(c5);
        categoriesExpenses.add(c6);

        // Set percentage for each category
        for(int i =0; i < categoriesExpenses.size(); i++){
            float percent = NumberUtils.roundFloat((categoriesExpenses.get(i).getTotal() * 100) / totalExpenses);
            categoriesExpenses.get(i).setPercent(percent);
        }

        // Set expenses chart
        expensesChart.setImageTintList(null);
        Charts.setCategoriesChart(mContext, categoriesExpenses, expensesChart, 100, 10);

        initCategoriesExpensesList();
    }

    private void setDummyIncomeList() {
        Category c1 = new Category(0, "Salary", R.color.savings, 0, true);
        Category c2 = new Category(0, "Rent", R.color.colorAccent, 0, true);
        Category c3 = new Category(0, "Extras", R.color.expense, 0, true);

        float totalIncome = 5000.0f;
        c1.setTotal(3000f);
        c2.setTotal(1000f);
        c3.setTotal(1000f);

        categoriesIncome.add(c1);
        categoriesIncome.add(c2);
        categoriesIncome.add(c3);

        // Set percentage for each category
        for(int i =0; i < categoriesIncome.size(); i++){
            float percent = NumberUtils.roundFloat((categoriesIncome.get(i).getTotal() * 100) / totalIncome);
            categoriesIncome.get(i).setPercent(percent);
        }

        categoriesIncome.sort(Category.CategoryTotalComparator);
        initCategoriesIncomeList();

        // Set expenses chart
        incomeChart.setImageTintList(null);
        Charts.setCategoriesChart(mContext, categoriesIncome, incomeChart, 100, 10);
    }

    private void initCategoriesIncomeList() {

        List<Category> cat = categoriesIncome.subList(0, 3);

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        incomeCategoryListView.setLayoutManager(listLayoutManager);
        incomeCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        incomeCategoryListView.setAdapter(adapter);
    }

    private void initCategoriesExpensesList() {

        List<Category> cat = categoriesExpenses.subList(0, 3);

        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        expensesCategoryListView.setLayoutManager(listLayoutManager);
        expensesCategoryListView.setHasFixedSize(true);
        HomeCategoryAdapter adapter = new HomeCategoryAdapter(mContext, cat);
        expensesCategoryListView.setAdapter(adapter);
    }


    /* ===============================================================================
                                          YEAR
     =============================================================================== */

    private void setDummyYear() {

        yearChart.post(() -> {

            ArrayList<MonthTotal> year = new ArrayList<>();
            float[] expenses = {4000f, 6200f, 4900f, 5000f, 6500f, 5100f, 4600f, 4800f, 5500f, 5600f, 4500f, 5000f};
            float[] income = {5000f, 5200f, 5100f, 6000f, 7000f, 4900f, 5000f, 5300f, 5200f, 5000f, 5100f, 5200f};

            int month = 0;
            float higherBar = 0f;
            while (month < 12) {
                String[] monthName = MyDateUtils.getMonthName(mContext, month, 2023);
                float monthExpenses = NumberUtils.roundFloat(expenses[month]);
                float monthIncome = NumberUtils.roundFloat(income[month]);

                MonthTotal monthTotal = new MonthTotal(
                        2023, month,
                        monthName[0],
                        monthName[1],
                        monthExpenses,
                        monthIncome);

                year.add(monthTotal);

                if (monthExpenses > higherBar) higherBar = monthExpenses;
                if (monthIncome > higherBar) higherBar = monthIncome;

                month++;
            }

            int width = yearChart.getWidth();
            int height = yearChart.getHeight();
            yearChart.setImageTintList(null);
            Charts.setYearTotalChart(mContext, yearChart, year, higherBar, width, height);
        });
    }

}