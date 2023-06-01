package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jgm.mybudgetapp.adapters.CategoryAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesListBinding;
import com.jgm.mybudgetapp.objects.Category;

import java.util.ArrayList;

public class CategoriesListFragment extends Fragment {

    public CategoriesListFragment() {
        // Required empty public constructor
    }

    // Vars
    private boolean isEdit;

    // List
    private ArrayList<Category> categoryList = new ArrayList<>();
    private CategoryAdapter adapter;

    // UI
    private FragmentCategoriesListBinding binding;
    private RecyclerView mRecyclerView;
    private Button mOpenForm;

    private void setBinding() {
        mRecyclerView = binding.categoriesList;
        mOpenForm = binding.buttonAddCategory;
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
        binding = FragmentCategoriesListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initCategoriesList();
        mOpenForm.setOnClickListener(v -> mInterface.openCategoryForm(isEdit));

    }

    public void setListType(boolean isEdit) {
        this.isEdit = isEdit;
        Log.w("debug-type", "is edit list => " + isEdit);
    }

    private void initCategoriesList() {
        initDummyList();
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        adapter = new CategoryAdapter(mContext, categoryList, isEdit);
        mRecyclerView.setAdapter(adapter);
    }

    private void initDummyList() {
        Category c1 = new Category(0, "Home", 0, 0, true);
        Category c2 = new Category(0, "Groceries", 1, 0, true);
        Category c3 = new Category(0, "Leisure", 2, 0, true);
        Category c4 = new Category(0, "Pet", 3, 0, true);
        Category c5 = new Category(0, "Car", 4, 0, true);
        Category c6 = new Category(0, "Restaurant", 5, 0, true);

        categoryList.add(c1);
        categoryList.add(c2);
        categoryList.add(c3);
        categoryList.add(c4);
        categoryList.add(c5);
        categoryList.add(c6);
    }
}