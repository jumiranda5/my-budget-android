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
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

public class CreditCardFormFragment extends Fragment {

    public CreditCardFormFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-card-form";

    // Vars
    private boolean isEdit = false;
    private Color selectedColor;
    private int billingDay;
    private int position;
    private Card creditCard;

    // UI
    private FragmentCreditCardFormBinding binding;
    private TextView mToolbarTitle;
    private EditText mNicknameInput, mBillingDayInput;
    private Button mColorButton, mSave;
    private ImageButton mBack, mArchive;
    private ImageView mColorIcon;

    private void setBinding() {
        mToolbarTitle = binding.cardFormTitle;
        mBack = binding.cardFormBackButton;
        mArchive = binding.cardFormArchiveButton;
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
        mArchive.setOnClickListener(v-> mInterface.showConfirmationDialog(getString(R.string.msg_archive_credit_card)));
        mColorButton.setOnClickListener(v -> mInterface.showColorPickerDialog());
        mSave.setOnClickListener(v -> {
            if (isEdit) editCreditCard(true);
            else createCard();
        });

        initCreditCardForm();

    }

    private void initCreditCardForm() {
        mSave.setEnabled(false);
        mNicknameInput.addTextChangedListener(cardNameWatcher);
        if (isEdit) {
            mToolbarTitle.setText(getString(R.string.title_edit_credit_card));
            setEditOptions();
        }
        else {
            mToolbarTitle.setText(getString(R.string.title_add_credit_card));
            setDefaultOptions();
        }
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    // Init Edit / Add form
    public void setFormType(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public void setCreditCard(Card card, int position) {
        this.position = position;
        creditCard = card;
    }

    public void setSelectedColor(Color color) {
        selectedColor = color;
        Log.e(LOG, "selected color => " + selectedColor.getColorName());
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    public void handleArchiveConfirmation() {
        editCreditCard(false);
    }

    /* ===============================================================================
                                        OPTIONS
     =============================================================================== */

    private void setDefaultOptions() {
        billingDay = 1;
        selectedColor = ColorUtils.getColor(4);
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    private void setEditOptions() {
        mNicknameInput.setText(creditCard.getName());
        mBillingDayInput.setText(String.valueOf(creditCard.getBillingDay()));
        selectedColor = ColorUtils.getColor(creditCard.getColorId());
        mColorIcon.setImageTintList(ContextCompat.getColorStateList(mContext, selectedColor.getColor()));
        mColorIcon.setContentDescription(selectedColor.getColorName());
    }

    /* ===============================================================================
                                        FORM
     =============================================================================== */

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

        Card newCard = new Card(0, nickname, selectedColor.getId(), billingDay, true);
        mInterface.insertCreditCardData(newCard);
        mInterface.navigateBack();
    }

    private void editCreditCard(boolean isActive) {
        String nickname = mNicknameInput.getText().toString().trim();
        String billingDayText = mBillingDayInput.getText().toString();
        if (billingDayText.equals("")) billingDayText = "1";
        billingDay = Integer.parseInt(billingDayText);

        Card editedCard = new Card(
                creditCard.getId(),
                nickname,
                selectedColor.getId(),
                billingDay,
                isActive);

        mInterface.editCreditCardData(position, editedCard);
        mInterface.navigateBack();
    }
}