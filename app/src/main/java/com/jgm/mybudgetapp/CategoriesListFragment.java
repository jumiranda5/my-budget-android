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
import android.widget.ImageButton;

import com.jgm.mybudgetapp.adapters.CategoryAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesListBinding;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.util.ArrayList;
import java.util.List;

public class CategoriesListFragment extends Fragment {

    public CategoriesListFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-cat-list";

    // Vars
    private boolean isEdit;

    // List
    private ArrayList<Category> categoryList = new ArrayList<>();
    private CategoryAdapter adapter;

    // UI
    private FragmentCategoriesListBinding binding;
    private RecyclerView mRecyclerView;
    private Button mOpenForm;
    private ImageButton mClose;

    private void setBinding() {
        mRecyclerView = binding.categoriesList;
        mOpenForm = binding.buttonAddCategory;
        mClose = binding.catListToolbarClose;
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

        Log.d(LOG, "Categories list onViewCreated");

        initCategoriesList();
        mInterface.getCategoriesData();
        mOpenForm.setOnClickListener(v -> mInterface.openCategoryForm(false, null, 0));
        mClose.setOnClickListener(v -> mInterface.navigateBack());
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setListType(boolean isEdit) {
        this.isEdit = isEdit;
        Log.w(LOG, "is edit list => " + isEdit);
    }

    public void updateListAfterDbRead(List<Category> dbCategories) {
        Log.d(LOG, "Update ui list: " + dbCategories.size());
        if (dbCategories.size() > 0) {
            categoryList = (ArrayList<Category>) dbCategories;
            initCategoriesList();
        }
    }

    public void updateListAfterDbInsertion(Category category) {
        Log.d(LOG, "Update ui list: " + category.getName());
        adapter.addItem(category);
    }

    public void updateListAfterDelete(int pos) {
        Log.d(LOG, "Update ui list after item delete");
        adapter.deleteItem(pos);
    }

    public void updateListAfterEdit(int pos, Category editedCategory) {
        Log.d(LOG, "Update ui list after item edit");
        adapter.updateItem(pos, editedCategory);
    }

    /* ===============================================================================
                                          LIST
    =============================================================================== */

    private void initCategoriesList() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        adapter = new CategoryAdapter(mContext, categoryList, isEdit);
        mRecyclerView.setAdapter(adapter);
    }

}