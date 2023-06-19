package com.jgm.mybudgetapp;

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
import android.widget.RadioButton;
import android.widget.TextView;

import com.jgm.mybudgetapp.databinding.FragmentAccountFormBinding;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.utils.ColorUtils;

public class AccountFormFragment extends Fragment {

    public AccountFormFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-account-form";

    // Vars
    private boolean isEdit = false;
    private int selectedType;
    private Color selectedColor;
    private int selectedIconId;
    private int position;
    private Account account;

    // UI
    private FragmentAccountFormBinding binding;
    private TextView mToolbarTitle;
    private ImageButton mBack, mArchive;
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
        mArchive = binding.accountFormArchiveButton;
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

        initTypeRadioGroup();
        setDefaultOptions();

        mNicknameInput.addTextChangedListener(accountNameWatcher);
        mBack.setOnClickListener(v -> mInterface.navigateBack());
        mArchive.setOnClickListener(v -> mInterface.showConfirmationDialog(getString(R.string.msg_archive_account)));
        mColorButton.setOnClickListener(v -> mInterface.showColorPickerDialog());

        mSave.setEnabled(false);
        mSave.setOnClickListener(v -> mInterface.navigateBack());
        mSave.setOnClickListener(v -> {
            if (isEdit) editAccount(true);
            else createAccount();
        });

        if (isEdit) {
            mToolbarTitle.setText(getString(R.string.title_edit_account));
            setEditOptions();
        }
        else {
            mToolbarTitle.setText(getString(R.string.title_add_account));
            setDefaultOptions();
        }

    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setFormType(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public void setAccount(Account account, int position) {
        this.position = position;
        this.account = account;
        Log.d(LOG, "account id: " + account.getId());
    }

    public void setSelectedColor(Color color) {
        selectedColor = color;
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    public void handleArchiveConfirmation() {
        editAccount(false);
    }


    /* ===============================================================================
                                      OPTIONS
    =============================================================================== */

    private void setDefaultOptions() {
        selectedColor = ColorUtils.getColor(4);
        mCashType.setChecked(true);
        selectedType = 0;
        selectedIconId = 1;
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    private void setEditOptions() {
        mNicknameInput.setText(account.getName());

        selectedColor = ColorUtils.getColor(account.getColorId());
        selectedIconId = account.getIconId();
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());

        selectedType = account.getType();
        switch (selectedType) {
            case 0: mCashType.setChecked(true); break;
            case 1: mCheckingType.setChecked(true); break;
            case 2: mSavingsType.setChecked(true); break;
        }
    }

    /* ===============================================================================
                                      FORM
    =============================================================================== */

    private void initTypeRadioGroup() {
        mCashType.setOnClickListener(v -> {
            selectedType = 0;
            selectedIconId = 1;
        });
        mCheckingType.setOnClickListener(v -> {
            selectedType = 1;
            selectedIconId = 13;
        });
        mSavingsType.setOnClickListener(v -> {
            selectedType = 2;
            selectedIconId = 4;
        });
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

        Account newAccount = new Account(
                nickname,
                selectedColor.getId(),
                selectedIconId,
                selectedType,
                true);

        saveAccountOnDb(newAccount);
    }

    private void editAccount(boolean isActive) {
        String nickname = mNicknameInput.getText().toString().trim();

        Account editedAccount = new Account(
                nickname,
                selectedColor.getId(),
                selectedIconId,
                selectedType,
                isActive);
        editedAccount.setId(account.getId());

        editAccountOnDb(editedAccount);
    }

    /* ===============================================================================
                                      DATABASE
    =============================================================================== */

    private void saveAccountOnDb(Account newAccount) {

        AccountDao accountDao = AppDatabase.getDatabase(mContext).AccountDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            int id = (int) accountDao.insert(newAccount);

            handler.post(() -> {
                Log.d(LOG, "account saved on db... update ui");
                newAccount.setId(id);
                mInterface.updateAccountInserted(newAccount, false, 0);
                mInterface.navigateBack();
            });

        });

    }

    private void editAccountOnDb(Account editedAccount) {

        AccountDao accountDao = AppDatabase.getDatabase(mContext).AccountDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            accountDao.update(editedAccount);

            handler.post(() -> {
                Log.d(LOG, "account updated on db... update ui");
                mInterface.updateAccountInserted(editedAccount, true, position);
                mInterface.navigateBack();
            });

        });
    }
}