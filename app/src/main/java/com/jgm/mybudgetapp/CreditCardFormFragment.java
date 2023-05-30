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
import android.widget.TextView;

import com.jgm.mybudgetapp.databinding.FragmentCreditCardFormBinding;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

public class CreditCardFormFragment extends Fragment {

    public CreditCardFormFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-card-form";

    // Vars
    private Color selectedColor;
    private int billingDay;

    // UI
    private FragmentCreditCardFormBinding binding;
    private TextView mToolbarTitle;
    private EditText mNicknameInput, mBillingDayInput;
    private Button mColorButton, mSave;
    private ImageButton mBack;
    private ImageView mColorIcon;

    private void setBinding() {
        mToolbarTitle = binding.cardFormTitle;
        mBack = binding.cardFormBackButton;
        mSave = binding.cardFormSaveButton;
        mNicknameInput = binding.cardFormNickname;
        mBillingDayInput = binding.cardFormDay;
        mColorButton = binding.cardFormColor;
        mColorIcon = binding.cardFormColorIcon;
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
        binding = FragmentCreditCardFormBinding.inflate(inflater, container, false);
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

    // Init Edit / Add form
    public void setFormType(boolean isEdit) {
        if (isEdit) {
            mToolbarTitle.setText(getString(R.string.title_edit_credit_card));
            Log.d(LOG, "Form type => EDIT");
        }
        else {
            mToolbarTitle.setText(getString(R.string.title_add_credit_card));
            Log.d(LOG, "Form type => ADD");
        }
    }

    private void initCreditCardForm() {
        setDefaultOptions();
        mSave.setEnabled(false);
        mNicknameInput.addTextChangedListener(cardNameWatcher);
        mSave.setOnClickListener(v -> createCard());
        mColorButton.setOnClickListener(v -> mInterface.showColorPickerDialog());
    }

    private void setDefaultOptions() {
        billingDay = 1;
        selectedColor = ColorUtils.getColor(4);
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    public void updateColor(Color color) {
        Log.d(LOG, "Should change color: " + color.getColorName());
        selectedColor = color;
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    private void clearForm() {
        mNicknameInput.setText("");
        mBillingDayInput.setText("");
        setDefaultOptions();
    }

    private final TextWatcher cardNameWatcher = new TextWatcher() {
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

    private void createCard() {
        String nickname = mNicknameInput.getText().toString().trim();
        String billingDayText = mBillingDayInput.getText().toString();
        if (billingDayText.equals("")) billingDayText = "1";
        billingDay = Integer.parseInt(billingDayText);

        Log.d(LOG, "Card nickname => " + nickname);
        Log.d(LOG, "Card color => " + selectedColor.getColorName());
        Log.d(LOG, "Card billing day => " + billingDay);

        clearForm();
        mInterface.navigateBack();
    }
}