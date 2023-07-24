package com.jgm.mybudgetapp.fragmentsCategories;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.CategoryPercentAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesPagerBinding;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.utils.CategoryUtils;
import com.jgm.mybudgetapp.utils.Charts;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoriesIncomeFragment extends Fragment {

    public CategoriesIncomeFragment() {
        // Required empty public constructor
    }

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

        Log.w("omg", "onCreateView - income");

        return view;
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    public void setIncomeCategoriesData(List<CategoryResponse> categories) {

        Log.w("omg", "is imageView null - income? " + (mChartImage == null));

        if (mChartImage != null) {
            mChartImage.post(() -> {

                ArrayList<CategoryPercent> percents = CategoryUtils.getCategoriesPercents(categories);
                initCategoriesList(percents);

                float total = 0f;
                for(int i =0; i < categories.size(); i++){
                    total = NumberUtils.roundFloat(total + categories.get(i).getTotal());
                }

                String totalFormatted = NumberUtils.getCurrencyFormat(mContext, total)[2];
                mTotal.setText(totalFormatted);

                int size = mContext.getResources().getInteger(R.integer.categories_chart_size_int);
                int indicator = mContext.getResources().getInteger(R.integer.categories_chart_indicator_int);

                if (total > 0) {
                    Charts.setCategoriesChart(mContext, percents, mChartImage, size, indicator, false);
                }
                else {
                    ArrayList<CategoryPercent> empty = new ArrayList<>();
                    CategoryPercent categoryPercent = new CategoryPercent(0, "empty", 22, 0);
                    categoryPercent.setTotal(1.0f);
                    categoryPercent.setPercent(100);
                    empty.add(categoryPercent);
                    Charts.setCategoriesChart(mContext, empty, mChartImage, size, indicator, false);
                }
            });
        }
    }

    /* ===============================================================================
                                          LIST
     =============================================================================== */

    private void initCategoriesList(ArrayList<CategoryPercent> categories) {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        CategoryPercentAdapter adapter = new CategoryPercentAdapter(mContext, categories);
        mRecyclerView.setAdapter(adapter);
    }

}