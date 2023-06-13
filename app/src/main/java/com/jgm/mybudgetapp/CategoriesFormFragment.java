package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.databinding.FragmentCategoriesFormBinding;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;

public class CategoriesFormFragment extends Fragment {

    public CategoriesFormFragment() {
        // Required empty public constructor
    }

    private boolean isEdit;
    private int position;
    private Icon selectedIcon;
    private Color selectedColor;
    private Category categoryToEdit;

    // UI
    private FragmentCategoriesFormBinding binding;
    private ImageView icColor, icIcon;
    private Button mColorPicker, mIconPicker, mSave;
    private ImageButton mClose, mArchive;
    private EditText mCategoryNameInput;
    private TextView mTitle;

    private void setBinding() {
        mTitle = binding.catFormTitle;
        mCategoryNameInput = binding.categoryNameInput;
        mColorPicker = binding.categoryColorButton;
        mIconPicker = binding.categoryIconButton;
        icIcon = binding.categoryIcon;
        icColor = binding.categoryColor;
        mSave = binding.buttonSaveCategory;
        mClose = binding.catFormToolbarClose;
        mArchive = binding.catFormToolbarArchive;
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

        // Default
        mSave.setEnabled(false);
        mCategoryNameInput.addTextChangedListener(categoryWatcher);
        if (isEdit) {
            mTitle.setText(getString(R.string.title_edit_category));
            setEditOptions();
        }
        else {
            mTitle.setText(getString(R.string.title_add_category));
            setDefaultOptions();
        }

        // Init buttons
        mClose.setOnClickListener(v -> mInterface.navigateBack());
        mArchive.setOnClickListener(v -> mInterface.showConfirmationDialog(getString(R.string.msg_archive_category)));
        mColorPicker.setOnClickListener(v -> mInterface.showColorPickerDialog());
        mIconPicker.setOnClickListener(v -> mInterface.showIconPickerDialog());
        mSave.setOnClickListener(v -> {
            if (isEdit) editCategory(position, true);
            else createCategory();
        });
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setFormType(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public void setCategoryToEdit(Category category, int position) {
        this.position = position;
        categoryToEdit = category;
    }

    public void setSelectedIcon(Icon icon) {
        selectedIcon = icon;
        icIcon.setImageDrawable(ContextCompat.getDrawable(mContext, selectedIcon.getIcon()));
        icIcon.setContentDescription(selectedIcon.getIconName());
    }

    public void setSelectedColor(Color color) {
        selectedColor = color;
        icColor.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        icColor.setContentDescription(selectedColor.getColorName());
    }

    public void handleArchiveConfirmation() {
        editCategory(position, false);
    }

    /* ===============================================================================
                                    CATEGORY OPTIONS
     =============================================================================== */

    private void setDefaultOptions() {
        selectedIcon = IconUtils.getIcon(0);
        selectedColor = ColorUtils.getColor(9);
        icIcon.setImageDrawable(ContextCompat.getDrawable(mContext, selectedIcon.getIcon()));
        icIcon.setContentDescription(selectedIcon.getIconName());
        icColor.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        icColor.setContentDescription(selectedColor.getColorName());
    }

    private void setEditOptions() {
        mCategoryNameInput.setText(categoryToEdit.getName());
        Color color = ColorUtils.getColor(categoryToEdit.getColorId());
        Icon icon = IconUtils.getIcon(categoryToEdit.getIconId());
        setSelectedColor(color);
        setSelectedIcon(icon);
    }

    /* ===============================================================================
                                   CREATE/EDIT CATEGORY
     =============================================================================== */

    private void createCategory() {
        String name = mCategoryNameInput.getText().toString().trim();
        Category category = new Category(name, selectedColor.getId(), selectedIcon.getId(), true);
        mInterface.insertCategoryData(category);
        mInterface.navigateBack();
    }

    private void editCategory(int pos, boolean active) {
        String name = mCategoryNameInput.getText().toString().trim();
        Category category = new Category(name, selectedColor.getId(), selectedIcon.getId(), active);
        category.setId(categoryToEdit.getId());
        mInterface.editCategoryData(pos, category);
        mInterface.navigateBack();
    }

    private final TextWatcher categoryWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String text = mCategoryNameInput.getText().toString();
            mSave.setEnabled(text.length() > 0);
        }
    };

}