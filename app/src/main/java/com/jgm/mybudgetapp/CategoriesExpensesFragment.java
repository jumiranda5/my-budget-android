package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.adapters.CategoryPercentAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesPagerBinding;
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.ArrayList;

public class CategoriesExpensesFragment extends Fragment {

    public CategoriesExpensesFragment() {
        // Required empty public constructor
    }

    // List
    private final ArrayList<Category> categories = new ArrayList<>();
    private final float total = 3508.75f;

    // UI
    private FragmentCategoriesPagerBinding binding;
    private ImageView mChartImage;
    private TextView mTotal;
    private RecyclerView mRecyclerView;

    private void bindViews() {
        mChartImage = binding.categoriesDonutChart;
        mTotal = binding.categoriesTotal;
        mRecyclerView = binding.categoriesPercentList;
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
        binding = FragmentCategoriesPagerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        bindViews();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currencySymbol = NumberUtils.getCurrencyFormat(mContext, total)[0];
        String totalFormatted = NumberUtils.getCurrencyFormat(mContext, total)[1];
        String totalCurrency = currencySymbol + totalFormatted;
        mTotal.setText(totalCurrency);
        setDummyList();
        initCategoriesList();
        Charts.setCategoriesChart(mContext, categories, mChartImage, 220, 16);

    }

    private void setDummyList() {
        Category c1 = new Category(0, "Home", R.color.savings, 0, true);
        Category c2 = new Category(0, "Restaurant", R.color.colorAccent, 0, true);
        Category c3 = new Category(0, "Groceries", R.color.expense, 0, true);
        Category c4 = new Category(0, "Clothes", R.color.colorSecondary, 0, true);
        Category c5 = new Category(0, "Car", R.color.main_text, 0, true);
        Category c6 = new Category(0, "Gym", R.color.income, 0, true);

        c1.setTotal(2034.80f);
        c2.setTotal(323.95f);
        c3.setTotal(200f);
        c4.setTotal(350f);
        c5.setTotal(500f);
        c6.setTotal(100f);

        categories.add(c1);
        categories.add(c2);
        categories.add(c3);
        categories.add(c4);
        categories.add(c5);
        categories.add(c6);

        for(int i =0; i < categories.size(); i++){
            float percent = NumberUtils.roundFloat((categories.get(i).getTotal() * 100) / total);
            categories.get(i).setPercent(percent);
        }
    }

    /* ===============================================================================
                                          LIST
     =============================================================================== */

    private void initCategoriesList() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        CategoryPercentAdapter adapter = new CategoryPercentAdapter(mContext, categories);
        mRecyclerView.setAdapter(adapter);
    }
}