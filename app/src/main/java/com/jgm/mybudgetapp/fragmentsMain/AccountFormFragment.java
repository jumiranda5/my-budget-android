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
import android.widget.RadioButton;
import android.widget.TextView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
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
    private static final String STATE_EDIT = "EDIT";
    private static final String STATE_TYPE = "TYPE";
    private static final String STATE_COLOR = "COLOR";
    private static final String STATE_ICON = "ICON";
    private static final String STATE_NAME = "NAME";
    private static final String STATE_ID = "ID";

    // Vars
    private boolean isEdit = false;
    private int selectedType;
    private Color selectedColor;
    private int selectedIconId;
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

        mArchive.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            isEdit = savedInstanceState.getBoolean(STATE_EDIT, false);
            selectedType = savedInstanceState.getInt(STATE_TYPE, 0);
            selectedIconId = savedInstanceState.getInt(STATE_ICON, 67);
            String name = savedInstanceState.getString(STATE_NAME, "");
            int colorId = savedInstanceState.getInt(STATE_COLOR, 4);
            account = new Account(name, colorId, selectedIconId, selectedType, true);
            if (isEdit) {
                int id = savedInstanceState.getInt(STATE_ID, 0);
                account.setId(id);
            }
        }

        initTypeRadioGroup();
        initButtons();

        if (isEdit) {
            mArchive.setVisibility(View.VISIBLE);
            mToolbarTitle.setText(getString(R.string.title_edit_account));
            setEditOptions();
        }
        else {
            mToolbarTitle.setText(getString(R.string.title_add_account));
            if (savedInstanceState == null) setDefaultOptions();
            else setOptionsOnRestore();
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_EDIT, isEdit);
        outState.putInt(STATE_COLOR, selectedColor.getId());
        outState.putInt(STATE_ICON, selectedIconId);
        outState.putInt(STATE_TYPE, selectedType);
        outState.putString(STATE_NAME, mNicknameInput.getText().toString());
        if (isEdit) outState.putInt(STATE_ID, account.getId());
    }

    private void initButtons() {
        mNicknameInput.addTextChangedListener(accountNameWatcher);
        mBack.setOnClickListener(v -> mInterface.navigateBack());
        mArchive.setOnClickListener(v -> {
            mInterface.showConfirmationDialog(
                    getString(R.string.msg_archive_account),
                    getString(R.string.action_archive),
                    R.drawable.ic_40_archive_fill0_300);
        });
        mColorButton.setOnClickListener(v -> mInterface.showColorPickerDialog());

        mSave.setEnabled(false);
        mSave.setOnClickListener(v -> {
            if (isEdit) editAccount(true);
            else createAccount();
        });
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setFormType(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public void setAccount(Account account) {
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
        selectedIconId = 67;
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    private void setOptionsOnRestore() {
        mNicknameInput.setText(account.getName());
        selectedColor = ColorUtils.getColor(account.getColorId());

        switch (selectedType) {
            case 0: mCashType.setChecked(true); break;
            case 1: mCheckingType.setChecked(true); break;
            case 2: mSavingsType.setChecked(true); break;
        }

        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());

        Log.d(LOG, "Restore: " + "id: " + account.getId() + "\n" +
                "name: " + account.getName() + "\n" +
                "color: " + selectedColor.getId() + " | " + selectedColor.getColorName() + "\n" +
                "type: " + selectedType + "\n" +
                "icon: " + selectedIconId);
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

        Log.d(LOG, "Edit: " + "id: " + account.getId() + "\n" +
                "name: " + account.getName() + "\n" +
                "color: " + selectedColor.getId() + " | " + selectedColor.getColorName() + "\n" +
                "type: " + selectedType + "\n" +
                "icon: " + selectedIconId);
    }

    /* ===============================================================================
                                      FORM
    =============================================================================== */

    private void initTypeRadioGroup() {
        mCashType.setOnClickListener(v -> {
            selectedType = 0;
            selectedIconId = 67;
        });
        mCheckingType.setOnClickListener(v -> {
            selectedType = 1;
            selectedIconId = 68;
        });
        mSavingsType.setOnClickListener(v -> {
            selectedType = 2;
            selectedIconId = 69;
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
                mInterface.updateAccountInserted(editedAccount, true, 0);
                mInterface.navigateBack();
            });

        });
    }
}