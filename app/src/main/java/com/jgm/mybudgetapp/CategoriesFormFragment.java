package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.jgm.mybudgetapp.databinding.FragmentCategoriesFormBinding;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesListBinding;
import com.jgm.mybudgetapp.objects.Icon;

public class CategoriesFormFragment extends Fragment {

    public CategoriesFormFragment() {
        // Required empty public constructor
    }

    private Icon selectedIcon;

    // UI
    private FragmentCategoriesFormBinding binding;
    private ImageView icColor, icIcon;
    private Button mColorPicker, mIconPicker;

    private void setBinding() {
        mColorPicker = binding.categoryColorButton;
        mIconPicker = binding.categoryIconButton;
        icIcon = binding.categoryIcon;
        icColor = binding.categoryColor;
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
        binding = FragmentCategoriesFormBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init button
        mColorPicker.setOnClickListener(v -> mInterface.showColorPickerDialog());
        mIconPicker.setOnClickListener(v -> mInterface.showIconPickerDialog());
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setFormType(boolean isEdit) {
        Log.d("debug-type", "Is edit => " + isEdit);
    }

    public void setSelectedIcon(Icon icon) {
        selectedIcon = icon;
        icIcon.setImageDrawable(ContextCompat.getDrawable(mContext, selectedIcon.getIcon()));
        icIcon.setContentDescription(selectedIcon.getIconName());
    }
}