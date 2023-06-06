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
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.util.ArrayList;

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

    public void updateListAfterDbRead(ArrayList<Category> dbCategories) {
        Log.d(LOG, "Update ui list: " + dbCategories.size());
        if (dbCategories.size() > 0) {
            categoryList = dbCategories;
            initCategoriesList();
        }
        else setDefaultList();
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

    private void setDefaultList() {

        boolean hasInitialCategories = SettingsPrefs.getSettingsPrefsBoolean(mContext, "hasInitialCategories");

        if (!hasInitialCategories) {

            Log.d(LOG, "== INIT CATEGORIES DEFAULT LIST");

            ArrayList<Category> list = new ArrayList<>();

            Category c1 = new Category(0, mContext.getString(R.string.category_home), 3, 6, true);
            Category c2 = new Category(0, mContext.getString(R.string.category_health), 5, 34, true);
            Category c3 = new Category(0, mContext.getString(R.string.category_groceries), 14, 9, true);
            Category c4 = new Category(0, mContext.getString(R.string.category_transport), 11, 29, true);
            Category c5 = new Category(0, mContext.getString(R.string.category_leisure), 1, 46, true);
            Category c6 = new Category(0, mContext.getString(R.string.category_education), 7, 15, true);
            Category c7 = new Category(0, mContext.getString(R.string.category_work), 4, 11, true);

            list.add(c1);
            list.add(c2);
            list.add(c3);
            list.add(c4);
            list.add(c5);
            list.add(c6);
            list.add(c7);

            for (int i = 0; i < list.size(); i++) {
                mInterface.insertCategoryData(list.get(i));
            }

            SettingsPrefs.setSettingsPrefsBoolean(mContext, "hasInitialCategories", true);
        }

    }
}