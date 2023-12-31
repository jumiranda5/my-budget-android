package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.SettingsInterface;
import com.jgm.mybudgetapp.adapters.CategoryAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesListBinding;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.entity.Category;

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
    private List<Category> categoryList = new ArrayList<>();
    private CategoryAdapter adapter;

    // UI
    private FragmentCategoriesListBinding binding;
    private RecyclerView mRecyclerView;
    private ImageButton mOpenForm;
    private ImageButton mClose;

    private void setBinding() {
        mRecyclerView = binding.categoriesList;
        mOpenForm = binding.catListAdd;
        mClose = binding.catListToolbarClose;
    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;
    private SettingsInterface mSettingsInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;

        try { mInterface = (MainInterface) context; }
        catch (Exception e) { Log.e(LOG, "Can't cast to main activity"); }

        try { mSettingsInterface = (SettingsInterface) context; }
        catch (Exception e) { Log.e(LOG, "Can't cast to settings activity"); }
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
        getCategoriesData();
        mOpenForm.setOnClickListener(v -> openCategoryForm());
        mClose.setOnClickListener(v -> navigateBack());
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    private void openCategoryForm() {
        if (mInterface != null) mInterface.openCategoryForm(false, null, 0);
        else if (mSettingsInterface != null) mSettingsInterface.openCategoryForm(false, null, 0);
    }

    private void navigateBack() {
        if (mInterface != null) mInterface.navigateBack();
        else if (mSettingsInterface != null) mSettingsInterface.navigateBack();
    }

    public void setListType(boolean isEdit) {
        this.isEdit = isEdit;
        Log.w(LOG, "is edit list => " + isEdit);
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

    private void getCategoriesData() {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            categoryList = AppDatabase.getDatabase(mContext).CategoryDao().getCategories();
            handler.post(() -> initCategoriesList());

        });
    }

    private void initCategoriesList() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        adapter = new CategoryAdapter(mContext, categoryList, isEdit);
        mRecyclerView.setAdapter(adapter);
    }

}