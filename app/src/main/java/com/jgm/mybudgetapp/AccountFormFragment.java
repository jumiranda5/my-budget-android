package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import android.widget.RadioButton;
import android.widget.TextView;

import com.jgm.mybudgetapp.databinding.FragmentAccountFormBinding;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

public class AccountFormFragment extends Fragment {

    public AccountFormFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-account-form";

    // Vars
    private Color selectedColor;
    private int chosenType;

    // UI
    private FragmentAccountFormBinding binding;
    private TextView mToolbarTitle;
    private ImageButton mBack;
    private EditText mNicknameInput;
    private Button mColorButton, mSave;
    private ImageView mColorIcon;
    private RadioButton mCashType, mCheckingType, mSavingsType;

    private void setBinding() {
        mBack = binding.accountFormBackButton;
        mToolbarTitle = binding.accountFormTitle;
        mNicknameInput = binding.accountFormNickname;
        mColorButton = binding.accountFormColor;
        mColorIcon = binding.accountFormColorIcon;
        mCashType = binding.accountOptionCash;
        mCheckingType = binding.accountOptionChecking;
        mSavingsType = binding.accountOptionSavings;
        mSave = binding.accountFormSaveButton;
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
        binding = FragmentAccountFormBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBack.setOnClickListener(v -> mInterface.navigateBack());
        mSave.setOnClickListener(v -> mInterface.navigateBack());
        initCreditCardForm();

    }

    public void setFormType(boolean isEdit) {
        if (isEdit) {
            mToolbarTitle.setText(getString(R.string.title_edit_account));
            Log.d("debug-account", "Form type => EDIT");
        }
        else {
            mToolbarTitle.setText(getString(R.string.title_add_account));
            Log.d("debug-account", "Form type => ADD");
        }
    }

    private void initCreditCardForm() {
        setDefaultOptions();
        initTypeRadioGroup();
        mSave.setEnabled(false);
        mNicknameInput.addTextChangedListener(accountNameWatcher);
        mSave.setOnClickListener(v -> createAccount());
        mColorButton.setOnClickListener(v -> mInterface.showColorPickerDialog());
    }

    private void setDefaultOptions() {
        selectedColor = ColorUtils.getColor(4);
        mCashType.setChecked(true);
        chosenType = 0;
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    private void initTypeRadioGroup() {
        mCashType.setOnClickListener(v -> chosenType = 0);
        mCheckingType.setOnClickListener(v -> chosenType = 1);
        mSavingsType.setOnClickListener(v -> chosenType = 2);
    }

    public void updateColor(Color color) {
        selectedColor = color;
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    private void clearForm() {
        mNicknameInput.setText("");
        setDefaultOptions();
    }

    private final TextWatcher accountNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String text = mNicknameInput.getText().toString();
            mSave.setEnabled(text.length() > 0);
        }
    };

    private void createAccount() {
        String nickname = mNicknameInput.getText().toString().trim();

        Log.d(LOG, "Account nickname => " + nickname);
        Log.d(LOG, "Account color => " + selectedColor.getColorName());
        Log.d(LOG, "Type => " + chosenType);

        clearForm();
        mInterface.navigateBack();
    }
}