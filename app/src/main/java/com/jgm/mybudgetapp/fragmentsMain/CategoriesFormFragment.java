package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.SettingsInterface;
import com.jgm.mybudgetapp.databinding.FragmentCategoriesFormBinding;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.CategoryDao;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.Tags;

public class CategoriesFormFragment extends Fragment {

    public CategoriesFormFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-cat-form";
    private static final String STATE_EDIT = "EDIT";
    private static final String STATE_NAME = "NAME";
    private static final String STATE_COLOR = "COLOR";
    private static final String STATE_ICON = "ICON";
    private static final String STATE_POSITION = "POSITION";
    private static final String STATE_ID = "ID";
    private boolean isEdit;
    private int position;
    private Icon selectedIcon;
    private Color selectedColor;
    private Category categoryToEdit;

    // UI
    private FragmentCategoriesFormBinding binding;
    private ImageView icColor, icIcon;
    private Button mColorPicker, mIconPicker, mSave;
    private ImageButton mBack, mArchive;
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
        mBack = binding.catFormBackButton;
        mArchive = binding.catFormToolbarArchive;
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

        if (savedInstanceState != null) {
            isEdit = savedInstanceState.getBoolean(STATE_EDIT, false);
            String name = savedInstanceState.getString(STATE_NAME, "");
            int colorId = savedInstanceState.getInt(STATE_COLOR, 9);
            int iconId = savedInstanceState.getInt(STATE_ICON, 0);
            categoryToEdit = new Category(name, colorId, iconId, true);
            if (isEdit) {
                int id = savedInstanceState.getInt(STATE_ID, 0);
                categoryToEdit.setId(id);
                position = savedInstanceState.getInt(STATE_POSITION, 0);
            }
        }

        if (isEdit) {
            mTitle.setText(getString(R.string.title_edit_category));
            setEditOptions();
        }
        else {
            mTitle.setText(getString(R.string.title_add_category));
            mArchive.setVisibility(View.GONE);
            if (savedInstanceState == null) setDefaultOptions();
            else setOptionsOnRestore();
        }

        initButtons();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_EDIT, isEdit);
        outState.putInt(STATE_POSITION, position);
        outState.putInt(STATE_COLOR, selectedColor.getId());
        outState.putInt(STATE_ICON, selectedIcon.getId());
        outState.putString(STATE_NAME, mCategoryNameInput.getText().toString());

        if (isEdit) outState.putInt(STATE_ID, categoryToEdit.getId());
    }

    private void initButtons() {
        // Init buttons
        mBack.setOnClickListener(v -> navigateBack());
        mColorPicker.setOnClickListener(v -> showColorPicker());
        mIconPicker.setOnClickListener(v -> showIconPicker());
        mSave.setOnClickListener(v -> {
            if (isEdit) editCategory(position, true);
            else createCategory();
        });

        // form is only editable on settings activity
        mArchive.setOnClickListener(v -> mSettingsInterface.showConfirmationDialog(
                getString(R.string.msg_archive_category),
                getString(R.string.action_archive),
                R.drawable.ic_40_archive_fill0_300));
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    private void handleCategoryInserted(Category category) {
        if (mSettingsInterface != null) {
            mSettingsInterface.handleCategoryInserted(category);
            mSettingsInterface.navigateBack();
        }
        else {
            mInterface.handleCategoryInserted(category);
            mInterface.navigateBack();
        }
    }

    private void showIconPicker() {
        if (mSettingsInterface != null) mSettingsInterface.showIconPickerDialog();
        else mInterface.showIconPickerDialog();
    }

    private void showColorPicker() {
        if (mSettingsInterface != null) mSettingsInterface.showColorPickerDialog();
        else mInterface.showColorPickerDialog();
    }

    private void navigateBack() {
        if (mSettingsInterface != null) mSettingsInterface.navigateBack();
        else mInterface.navigateBack();
    }

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

    private void setOptionsOnRestore() {
        selectedIcon = IconUtils.getIcon(categoryToEdit.getIconId());
        selectedColor = ColorUtils.getColor(categoryToEdit.getColorId());
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

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            CategoryDao categoryDao = AppDatabase.getDatabase(mContext).CategoryDao();
            int id = (int) categoryDao.insert(category);
            category.setId(id);

            handler.post(() -> {
                Log.d(Tags.LOG_DB, "Category saved in db... update ui");
                handleCategoryInserted(category);
            });

        });

    }

    private void editCategory(int pos, boolean active) {
        String name = mCategoryNameInput.getText().toString().trim();
        Category category = new Category(name, selectedColor.getId(), selectedIcon.getId(), active);
        category.setId(categoryToEdit.getId());

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            CategoryDao categoryDao = AppDatabase.getDatabase(mContext).CategoryDao();
            categoryDao.update(category);

            handler.post(() -> {
                // only editable on settings activity
                mSettingsInterface.handleCategoryEdited(pos, category);
                mSettingsInterface.navigateBack();
                Log.d(Tags.LOG_DB, "category updated on db... update ui");
            });

        });
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