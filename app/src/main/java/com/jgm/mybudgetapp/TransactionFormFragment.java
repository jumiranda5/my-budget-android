package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.jgm.mybudgetapp.databinding.FragmentTransactionFormBinding;
import com.jgm.mybudgetapp.objects.Category;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

public class TransactionFormFragment extends Fragment {

    public TransactionFormFragment() {
        // Required empty public constructor
    }

    // Vars
    private boolean hasChosenCategory = false;
    private int selectedCategoryId;

    // UI
    private FragmentTransactionFormBinding binding;
    private Button mCategoryPicker;
    private ImageView mCategoryIcon;

    private void setBinding() {
        mCategoryPicker = binding.addCategoryPicker;
        mCategoryIcon = binding.addIconCategory;
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
        binding = FragmentTransactionFormBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initCategory();

    }

    /* ===============================================================================
                                         CATEGORY
     =============================================================================== */

    private void initCategory() {
        selectedCategoryId = 0;
        mCategoryPicker.setOnClickListener(view -> mInterface.openCategoriesList(false));
    }

    public void setSelectedCategory(Category category) {
        // todo: Icon icon = IconUtils.getIcon(category.getIconId());
        Color color = ColorUtils.getColor(category.getColorId());
        selectedCategoryId = category.getId();
        mCategoryPicker.setText(category.getName());
        // todo mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mCategoryIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
        hasChosenCategory = true;
    }

}