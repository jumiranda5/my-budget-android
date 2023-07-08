package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.jgm.mybudgetapp.databinding.FragmentTransactionFormBinding;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.room.entity.Transaction;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

public class TransactionFormFragment extends Fragment {

    public TransactionFormFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-add";
    private static final int METHOD_PICKER_MAIN = 0;
    private static final int METHOD_PICKER_OUT = 1;
    private static final int METHOD_PICKER_IN = 2;

    // VARS
    private boolean isEdit = false;
    private TransactionResponse edit;
    private Transaction transaction;
    private String formType;
    private boolean hasChosenCategory = false;
    private boolean hasChosenMethod = false;
    private int currentMethodPicker = METHOD_PICKER_MAIN;
    private boolean isEditAll = false;
    private MyDate today;
    private MyDate selectedDate;
    private Category selectedCategory;
    private MyDate cardBillingDate;
    private PaymentMethod paymentMethod;
    private PaymentMethod transferAccountIn;
    private PaymentMethod transferAccountOut;
    private AppDatabase db;

    // UI
    private FragmentTransactionFormBinding binding;
    private Button mCategoryPicker, mDatePicker, mMethodPicker, mAccountPickerOut, mAccountPickerIn, mSave;
    private ImageButton mIncreaseRepeat, mDecreaseRepeat;
    private ImageView mCategoryIcon, mDescIcon, mCalendarIcon, mMethodIcon, mAmountIcon;
    private ToggleButton mToggleExpense, mToggleIncome, mToggleTransfer;
    private Group mTransferGroup, mMainGroup;
    private ConstraintLayout mCreditCardMonthContainer;
    private TextView mFormattedPrice, mRepeatLabel;
    private EditText mAmountInput, mRepeatInput;
    private MaterialSwitch mSwitchPaid, mSwitchEditAll;
    private RadioButton mMonth1, mMonth2;
    private AutoCompleteTextView mDescription;
    private ProgressBar mProgressBar;

    private void setBinding() {
        mMainGroup = binding.transactionsGroupMain;
        mTransferGroup = binding.transactionsGroupTransfer;
        mToggleExpense = binding.toggleExpense;
        mToggleIncome = binding.toggleIncome;
        mToggleTransfer = binding.toggleTransfer;
        mDatePicker = binding.addDatePicker;
        mCategoryPicker = binding.addCategoryPicker;
        mMethodPicker = binding.addMethodPicker;
        mAccountPickerOut = binding.addAccountPickerOut;
        mAccountPickerIn = binding.addAccountPickerIn;
        mSave = binding.saveButton;
        mCreditCardMonthContainer = binding.creditCardMonthPicker;
        mFormattedPrice = binding.addFormattedPrice;
        mAmountInput = binding.addAmount;
        mRepeatInput = binding.addQuantityInput;
        mSwitchPaid = binding.addSwitchPaid;
        mMonth1 = binding.billingMonth1;
        mMonth2 = binding.billingMonth2;
        mIncreaseRepeat = binding.buttonRepeatIncrease;
        mDecreaseRepeat = binding.buttonRepeatDecrease;
        mDescription = binding.addDescription;
        mProgressBar = binding.addProgressBar;
        mRepeatLabel = binding.repeatLabel;
        mSwitchEditAll = binding.addSwitchEditAll;
        // Icons
        mCategoryIcon = binding.addIconCategory;
        mDescIcon = binding.addIconDesc;
        mCalendarIcon = binding.addIconDate;
        mMethodIcon = binding.addIconMethod;
        mAmountIcon = binding.addIconAmount;
    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(Tags.LOG_LIFECYCLE, "Transactions form onAttach");
        mContext = context;
        mInterface = (MainInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(Tags.LOG_LIFECYCLE, "Transactions form onCreateView");
        binding = FragmentTransactionFormBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(Tags.LOG_LIFECYCLE, "Transactions form onViewCreated");

        db = AppDatabase.getDatabase(mContext);

        transaction = new Transaction(Tags.TYPE_OUT, "", 0f, 0, 0, 0,
                0, 0, 0, false, 1, 1, null);

        if (isEdit) setEditForm();
        else setDefaults();

    }

    private void initForm() {
        initType(transaction.getType());
        initAmount(Math.abs(transaction.getAmount()));
        initDescription(transaction.getDescription());
        initDate(selectedDate);
        initCategory(selectedCategory);
        initMethod();
        initTransferMethodPicker();
        initSwitchPayed();
        initRepeat(transaction.getRepeat());
        initSaveButton();
    }

    /* ===============================================================================
                                       EDIT FORM
     =============================================================================== */

    public void setFormType(boolean isEdit, TransactionResponse transaction, PaymentMethod paymentMethod) {
        Log.d(LOG, "=> setFormType: isEdit = " + isEdit);
        this.isEdit = isEdit;
        if (isEdit) {
            edit = transaction;
            this.paymentMethod = paymentMethod;
        }
    }

    private void setEditForm() {

        Log.d(LOG, "=> setEditForm");

        mToggleTransfer.setVisibility(View.GONE);

        selectedDate = new MyDate(edit.getDay(), edit.getMonth(), edit.getYear());
        selectedCategory = new Category(edit.getCategoryName(), edit.getColorId(), edit.getIconId(), true);
        selectedCategory.setId(edit.getCategoryId());

        transaction.setId(edit.getId());
        transaction.setType(edit.getType());
        transaction.setAmount(Math.abs(edit.getAmount()));
        transaction.setDescription(edit.getDescription());
        transaction.setPaid(edit.isPaid());
        transaction.setRepeat(edit.getRepeat());

        mAmountInput.setText(String.valueOf(Math.abs(edit.getAmount())));

        initSwitchPayed();

        if (transaction.getRepeat() == 1) initRepeat(transaction.getRepeat());
        else {
            mSwitchPaid.setVisibility(View.GONE);
            mSwitchEditAll.setVisibility(View.VISIBLE);
            mSwitchEditAll.setChecked(true);
            mSwitchEditAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isEditAll = isChecked;
                if (isChecked) {
                    mDatePicker.setText(setDateTextForAllParcels());
                    showRepeatInput();
                }
                else {
                    // don't allow repeat count changes
                    hideRepeatInput();
                    transaction.setRepeat(edit.getRepeat());
                    setDateTextForSingleParcel();
                }
            });
        }

        initForm();
    }

    private void setDateTextForSingleParcel() {
        String monthName = MyDateUtils.getMonthName(mContext, selectedDate.getMonth(), selectedDate.getYear())[0];
        String dateField = "Day: " + selectedDate.getDay() + " (" + monthName + ")";
        mDatePicker.setText(dateField);
    }

    private String setDateTextForAllParcels() {
        String formattedDate = MyDateUtils.getFormattedFieldDate(
                mContext, selectedDate.getDay(), selectedDate.getMonth(), selectedDate.getDay());

        return "First: " + formattedDate;
    }

    /* ===============================================================================
                                      DEFAULT VALUES
     =============================================================================== */

    private void setDefaults() {
        Log.d(LOG, "=> setDefaults");

        // get default account and category (first on db)
        Handler handler = new Handler();
        AppDatabase.dbExecutor.execute(() -> {

            Account account = db.AccountDao().getDefaultPaymentMethod();
            Category category = db.CategoryDao().getDefaultCategory();

            handler.post(() -> {
                Log.d(LOG, "default category: " + category.getName());
                Log.d(LOG, "default method: " + account.getName());

                transaction.setAccountId(account.getId());
                transaction.setCategoryId(category.getId());
                paymentMethod = new PaymentMethod(
                        account.getId(),
                        account.getType(),
                        account.getName(),
                        account.getColorId(),
                        account.getIconId(),
                        1);

                initForm();
            });
        });
    }


    /* ===============================================================================
                                          TYPE
     =============================================================================== */

    private void initType(int type) {
        Log.d(LOG, "=> initType: " + type);

        if (type == Tags.TYPE_IN) showIncomeForm();
        else showExpenseForm();

        mCreditCardMonthContainer.setVisibility(View.GONE);
        setTypeToggleButton(mToggleExpense, Tags.expense);
        setTypeToggleButton(mToggleIncome, Tags.income);
        setTypeToggleButton(mToggleTransfer, Tags.transfer);
    }

    private void setTypeToggleButton(ToggleButton button, String tag) {
        button.setOnClickListener(view -> {
            Log.d(LOG, "Toggle: " + tag);

            mToggleExpense.setChecked(false);
            mToggleIncome.setChecked(false);
            mToggleTransfer.setChecked(false);

            if (tag.equals(Tags.expense)) showExpenseForm();
            else if (tag.equals(Tags.income)) showIncomeForm();
            else showTransferForm();

            if (paymentMethod != null && paymentMethod.getType() != 3)
                mCreditCardMonthContainer.setVisibility(View.GONE);

        });
    }

    private void showExpenseForm() {
        Log.d(LOG, "=> showExpenseForm");
        mToggleExpense.setChecked(true);
        formType = Tags.expense;
        transaction.setType(Tags.TYPE_OUT);
        setMainGroup(R.drawable.button_save_expense);
        changeColorOnTypeSwitch(R.color.expense);
    }

    private void showIncomeForm() {
        Log.d(LOG, "=> showIncomeForm");
        mToggleIncome.setChecked(true);
        formType = Tags.income;
        transaction.setType(Tags.TYPE_IN);
        setMainGroup(R.drawable.button_save);
        changeColorOnTypeSwitch(R.color.income);
    }

    private void showTransferForm() {
        Log.d(LOG, "=> showTransferForm");
        mToggleTransfer.setChecked(true);
        formType = Tags.transfer;
        mTransferGroup.setVisibility(View.VISIBLE);
        mMainGroup.setVisibility(View.GONE);
        changeColorOnTypeSwitch(R.color.savings);
        mSave.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_save_transfer));
    }

    private void setMainGroup(int buttonDrawable) {
        mTransferGroup.setVisibility(View.GONE);
        mMainGroup.setVisibility(View.VISIBLE);
        mSave.setBackground(ContextCompat.getDrawable(mContext, buttonDrawable));
    }

    private void changeColorOnTypeSwitch(int colorId) {
        mDescIcon.setImageTintList(ContextCompat.getColorStateList(mContext, colorId));
        mAmountIcon.setImageTintList(ContextCompat.getColorStateList(mContext, colorId));
        mCalendarIcon.setImageTintList(ContextCompat.getColorStateList(mContext, colorId));
        mFormattedPrice.setTextColor(ContextCompat.getColor(mContext, colorId));
        if (!hasChosenCategory) mCategoryIcon.setImageTintList(ContextCompat.getColorStateList(mContext, colorId));
        if (!hasChosenMethod) mMethodIcon.setImageTintList(ContextCompat.getColorStateList(mContext, colorId));
    }


    /* ===============================================================================
                                          AMOUNT
     =============================================================================== */

    private void initAmount(float amount) {
        Log.d(LOG, "=> initAmount: " + amount);
        mAmountInput.addTextChangedListener(priceWatcher);
    }

    private final TextWatcher priceWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String priceString = mAmountInput.getText().toString();
            if (priceString.length() == 0) priceString = "0";

            float typedPrice = Float.parseFloat(priceString);
            String formattedPrice = NumberUtils.getCurrencyFormat(mContext, typedPrice)[2];

            mFormattedPrice.setText(formattedPrice);
            transaction.setAmount(typedPrice);
        }
    };


    /* ===============================================================================
                                        DESCRIPTION
     =============================================================================== */

    // Todo: autocomplete

    private void initDescription(String description) {
        mDescription.addTextChangedListener(descriptionWatcher);
        transaction.setDescription(description);
        mDescription.setText(description);
    }

    private final TextWatcher descriptionWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String description = mDescription.getText().toString();
            transaction.setDescription(description);
        }
    };


    /* ===============================================================================
                                        DATE PICKER
     =============================================================================== */

    private void initDate(MyDate date) {

        today = MyDateUtils.getCurrentDate(mContext);
        if (date == null) selectedDate = today;

        int day = selectedDate.getDay();
        int month = selectedDate.getMonth();
        int year = selectedDate.getYear();
        updateTransactionDate(day, month, year);

        String formattedDate = MyDateUtils.getFormattedFieldDate(mContext, year, month, day);

        Log.d(LOG, "=> initDate: " + formattedDate);

        String btnText;
        if (isEdit) {
            String weekday = MyDateUtils.getDayOfWeek(mContext, day, month, year)[0];
            btnText = weekday + " - " + formattedDate;
            if (transaction.getRepeat() > 1) btnText = setDateTextForAllParcels();
        }
        else btnText = getString(R.string.label_today) + " - " + formattedDate;

        mDatePicker.setText(btnText);
        mDatePicker.setOnClickListener(view -> mInterface.showDatePickerDialog());
    }

    public void setSelectedDate(long selection) {

        MyDate date = MyDateUtils.getDateFromMilliseconds(mContext, selection);
        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        String weekday = date.getWeekday();

        updateTransactionDate(day, month, year);

        Log.d(LOG, "setSelectedDate: " + day + "/" + month + "/" + year);

        String dateFormatted = MyDateUtils.getFormattedFieldDate(mContext, year, month, day);
        String dateField = weekday + " - " + dateFormatted;
        if (isEdit && transaction.getRepeat() > 1 && isEditAll) dateField = setDateTextForAllParcels();
        if (isEdit && transaction.getRepeat() > 1 && !isEditAll) setDateTextForSingleParcel();
        mDatePicker.setText(dateField);

        selectedDate = new MyDate(day, month, year);
        updateIsPaidAccordingToDate();

        if (transaction.getCardId() != 0 && paymentMethod != null) {
            setBillingMonthOptions(paymentMethod.getBillingDay());
        }
    }

    private void updateTransactionDate(int day, int month, int year) {
        transaction.setDay(day);
        transaction.setMonth(month);
        transaction.setYear(year);
    }


    /* ===============================================================================
                                         CATEGORY
     =============================================================================== */

    private void initCategory(Category category) {
        Log.d(LOG, "=> initCategory");
        if (category != null) setSelectedCategory(category);
        mCategoryPicker.setOnClickListener(view -> mInterface.openCategoriesList(false));
    }

    public void setSelectedCategory(Category category) {
        Log.d(LOG, "Category: " + category.getName());
        Icon icon = IconUtils.getIcon(category.getIconId());
        Color color = ColorUtils.getColor(category.getColorId());
        transaction.setCategoryId(category.getId());
        mCategoryPicker.setText(category.getName());
        mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mCategoryIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
        hasChosenCategory = true;
    }

    /* ===============================================================================
                                         METHOD
     =============================================================================== */

    private void initMethod() {
        Log.d(LOG, "=> initMethod: " + paymentMethod.getName());
        if (isEdit) setSelectedPaymentMethod(paymentMethod);
        mMethodPicker.setOnClickListener(view -> {
            boolean isExpense = formType.equals(Tags.expense);
            mInterface.showMethodPickerDialog(isExpense, null, 0);
        });
    }

    public void setSelectedPaymentMethod(PaymentMethod paymentMethod) {

        Log.d(LOG, "=> setSelectedPaymentMethod: " + paymentMethod.getName());

        this.paymentMethod = paymentMethod;

        if (paymentMethod.getType() == 3) {
            transaction.setCardId(paymentMethod.getId());
            transaction.setAccountId(0);
            mSwitchPaid.setVisibility(View.GONE);
            if (!isEdit) transaction.setPaid(false);
        }
        else {
            mSwitchPaid.setVisibility(View.VISIBLE);
            transaction.setAccountId(paymentMethod.getId());
            transaction.setCardId(0);
        }

        switch (currentMethodPicker) {
            case METHOD_PICKER_MAIN: updateMethod(paymentMethod); break;
            case METHOD_PICKER_OUT: updateTransferOut(paymentMethod); break;
            case METHOD_PICKER_IN: updateTransferIn(paymentMethod); break;
        }

    }

    private void updateMethod(PaymentMethod paymentMethod) {
        Icon icon = IconUtils.getIcon(paymentMethod.getIconId());
        Color color = ColorUtils.getColor(paymentMethod.getColorId());
        String name = paymentMethod.getName();

        if (paymentMethod.getType() == 3) {
            mCreditCardMonthContainer.setVisibility(View.VISIBLE);
            setBillingMonthOptions(paymentMethod.getBillingDay());
        }
        else {
            mCreditCardMonthContainer.setVisibility(View.GONE);
        }

        mMethodPicker.setText(name);
        mMethodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mMethodIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
        hasChosenMethod = true;
    }

    private void setBillingMonthOptions(int billingDay) {
        int month1;
        int month2;
        int year1;
        int year2;
        int[] nextDate = MyDateUtils.getNextTransactionDate(selectedDate.getMonth(), selectedDate.getYear());

        if (billingDay >= selectedDate.getDay()) {
            month1 = selectedDate.getMonth();
            year1 = selectedDate.getYear();
            month2 =  nextDate[0];
            year2 = nextDate[1];
        }
        else {
            month1 = nextDate[0];
            year1 = nextDate[1];
            int[] nextDate2 = MyDateUtils.getNextTransactionDate(month1, nextDate[1]);
            month2 =  nextDate2[0];
            year2 = nextDate2[1];
        }

        cardBillingDate = new MyDate(paymentMethod.getBillingDay(), month1, year1);

        mMonth1.setChecked(true);
        mMonth2.setChecked(false);
        setBillingMonthRadioGroup(month1, month2, year1, year2);

        mMonth1.setText(MyDateUtils.getMonthName(mContext, month1, selectedDate.getYear())[0]);
        mMonth2.setText(MyDateUtils.getMonthName(mContext, month2, selectedDate.getYear())[0]);
    }

    private void setBillingMonthRadioGroup(int month1, int month2, int year1, int year2) {
        mMonth1.setOnClickListener(v -> {
            mMonth1.setChecked(true);
            mMonth2.setChecked(false);
            cardBillingDate = new MyDate(paymentMethod.getBillingDay(), month1, year1);
        });
        mMonth2.setOnClickListener(v -> {
            mMonth1.setChecked(false);
            mMonth2.setChecked(true);
            cardBillingDate = new MyDate(paymentMethod.getBillingDay(), month2, year2);
        });
    }


    /* ===============================================================================
                                         TRANSFER
     =============================================================================== */

    private void initTransferMethodPicker() {
        mAccountPickerOut.setOnClickListener(view -> {
            currentMethodPicker = METHOD_PICKER_OUT;
            mInterface.showMethodPickerDialog(false, null, 0);
        });
        mAccountPickerIn.setOnClickListener(view -> {
            currentMethodPicker = METHOD_PICKER_IN;
            mInterface.showMethodPickerDialog(false, null, 0);
        });
    }

    private void updateTransferOut(PaymentMethod paymentMethod) {
        transferAccountOut = paymentMethod;
        mAccountPickerOut.setText(paymentMethod.getName());
    }

    private void updateTransferIn(PaymentMethod paymentMethod) {
        transferAccountIn = paymentMethod;
        mAccountPickerIn.setText(paymentMethod.getName());
    }


    /* ===============================================================================
                                        SWITCH PAYED
     =============================================================================== */

    private void initSwitchPayed() {
        if (isEdit) mSwitchPaid.setChecked(transaction.isPaid());
        else updateIsPaidAccordingToDate();

        Log.d(LOG, "=> initSwitchPaid: " + transaction.isPaid());

        mSwitchPaid.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> {
                    Log.w(LOG, "mSwitchPaid change listener");
                    transaction.setPaid(isChecked);
                });
    }

    private void updateIsPaidAccordingToDate() {
        if (selectedDate.getYear() <= today.getYear() &&
                selectedDate.getMonth() <= today.getMonth() &&
                selectedDate.getDay() <= today.getDay()) {
            mSwitchPaid.setChecked(true);
            transaction.setPaid(true);
        }
        else {
            mSwitchPaid.setChecked(false);
            transaction.setPaid(false);
        }
    }

    /* ===============================================================================
                                           REPEAT
     =============================================================================== */

    private void initRepeat(int repeat) {
        Log.d(LOG, "=> initRepeat: " + repeat);
        setQuantityInputValue(repeat);
        if (repeat < 2) hideRepeatInput();
        mRepeatInput.addTextChangedListener(quantityWatcher);
        mIncreaseRepeat.setOnClickListener(view -> increaseRepeat());
        mDecreaseRepeat.setOnClickListener(view -> decreaseRepeat());
    }

    private void increaseRepeat() {
        int repeat = transaction.getRepeat();
        if (repeat == 1) showRepeatInput();
        repeat++;
        setQuantityInputValue(repeat);
    }

    private void decreaseRepeat() {
        int repeat = transaction.getRepeat();
        if (repeat > 1) {
            repeat--;
            setQuantityInputValue(repeat);
        }
        if (repeat == 1) hideRepeatInput();
    }

    private void setQuantityInputValue(int repeat) {
        String sQt = String.valueOf(repeat);
        mRepeatInput.setText(sQt);
        transaction.setRepeat(repeat);
    }

    private final TextWatcher quantityWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String input = mRepeatInput.getText().toString();
            int qt;
            int repeat;

            if (input.isEmpty()) qt = 1;
            else qt = Integer.parseInt(input);

            if (qt > 120) {
                repeat = 120;
                setQuantityInputValue(repeat);
            }
            if (qt < 1) {
                repeat = 1;
                setQuantityInputValue(repeat);
            }
        }
    };

    private void showRepeatInput() {
        mRepeatInput.setVisibility(View.VISIBLE);
        mDecreaseRepeat.setVisibility(View.VISIBLE);
    }

    private void hideRepeatInput() {
        mRepeatInput.setVisibility(View.GONE);
        mDecreaseRepeat.setVisibility(View.GONE);
    }

    /* ===============================================================================
                                           SAVE
     =============================================================================== */

    private void initSaveButton() {

        if (isEdit) mSave.setText(getString(R.string.action_edit));

        mSave.setOnClickListener(view -> {

            // Disable button
            mSave.setEnabled(false);
            mSave.setText("");
            mProgressBar.setVisibility(View.VISIBLE);

            // transfer
            if (formType.equals(Tags.transfer)) setTransfer();
            else setTransaction();

        });
    }

    private void setTransaction() {

        // If method is credit card => day is billing day;
        if (paymentMethod != null && paymentMethod.getType() == 3) {
            transaction.setDay(paymentMethod.getBillingDay());
            transaction.setMonth(cardBillingDate.getMonth());
            transaction.setYear(cardBillingDate.getYear());
            // keep account id if editing
            if (isEdit) transaction.setAccountId(edit.getAccountId());
        }

        // Set default description if empty
        int transactionType = transaction.getType();
        if (transaction.getDescription().isEmpty()) {
            if (transactionType == -1) transaction.setDescription(getString(R.string.action_out));
            else transaction.setDescription(getString(R.string.action_in));
        }

        // save negative amount if expense
        float amount = transaction.getAmount();
        if (transactionType == Tags.TYPE_OUT) amount = amount * -1;
        transaction.setAmount(amount);

        // set repeat id
        if (transaction.getRepeat() > 1) {
           long repeatId = System.currentTimeMillis();
           transaction.setRepeatId(repeatId);
        }

        // call database
        if (isEdit) setEditTransaction();
        else insertTransactionOnDb(false);

    }

    private void setEditTransaction() {
        Log.d(LOG, "=> setEditTransaction");

        // keep repeatId if editing parcels
        if (edit.getRepeat() > 1) transaction.setRepeatId(edit.getRepeatId());

        // check if repeat count changed (for edit)
        boolean changedRepeat = transaction.getRepeat() != edit.getRepeat();

        // check if first parcel date changed
        boolean changedFirstParcelDate = false;
        String editDate = edit.getDay() + "/" + edit.getMonth() + "/" + edit.getYear();
        String newDate = selectedDate.getDay() + "/" + selectedDate.getMonth() + "/" + selectedDate.getYear();
        if (!editDate.equals(newDate)) changedFirstParcelDate = true;

        if (changedRepeat || changedFirstParcelDate) insertTransactionOnDb(true);
        else editTransactionOnDb();

    }

    private void insertTransactionOnDb(boolean isResetParcels) {

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            if (isResetParcels) {
                if (edit.getRepeat() == 1) {
                    Log.d(Tags.LOG_DB, "Transactions id to reset: " + transaction.getRepeatId());
                    db.TransactionDao().deleteById(transaction.getId());
                }
                else {
                    Log.d(Tags.LOG_DB, "Transactions repeatId to reset: " + transaction.getRepeatId());
                    db.TransactionDao().deleteByRepeatId(transaction.getRepeatId());
                }

                // remove id from transaction obj (to avoid unique id error)
                Transaction newTransaction = new Transaction(
                        transaction.getType(), transaction.getDescription(), transaction.getAmount(),
                        transaction.getYear(), transaction.getMonth(), transaction.getDay(),
                        transaction.getCategoryId(), transaction.getAccountId(), transaction.getCardId(),
                        transaction.isPaid(), transaction.getRepeat(), 1, transaction.getRepeatId()
                );
                transaction = newTransaction;
            }

            logTransaction(transaction);
            transactionDao.insert(transaction);

            // If repeat => save from second parcel on...
            int repeat = transaction.getRepeat();
            if (repeat > 1) {
                int i = 1;
                while (i < repeat) {
                    // update next transaction month and year
                    int[] nextDate = MyDateUtils.getNextTransactionDate(transaction.getMonth(), transaction.getYear());
                    transaction.setMonth(nextDate[0]);
                    transaction.setYear(nextDate[1]);
                    transaction.setRepeatCount(i+1);

                    // if repeat => update isPaid
                    if (transaction.getYear() >= today.getYear()
                            && transaction.getMonth() >= today.getMonth()
                            && transaction.getDay() >= today.getDay()) transaction.setPaid(false);

                    logTransaction(transaction);
                    transactionDao.insert(transaction);

                    i++;
                }
            }

            handler.post(() -> {
                Log.d(Tags.LOG_DB, "Transaction saved on db");
                mProgressBar.setVisibility(View.GONE);
                mSave.setText(getText(R.string.action_save));
                mInterface.navigateBack();
            });
        });
    }

    private void editTransactionOnDb() {
        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            int repeat = transaction.getRepeat();
            if (repeat == 1) transactionDao.update(transaction);
            else if (isEditAll) {
                transactionDao.updateAllParcels(transaction.getRepeatId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getCardId(),
                        transaction.getAccountId(),
                        transaction.getCategoryId(),
                        selectedDate.getDay());
            }
            else {
                transactionDao.updateParcel(transaction.getId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getCardId(),
                        transaction.getAccountId(),
                        transaction.getCategoryId(),
                        selectedDate.getDay());
            }

            handler.post(() -> {
                Log.d(LOG, "transaction updated");
                Log.d(Tags.LOG_DB, "Transaction updated on db");
                mProgressBar.setVisibility(View.GONE);
                mSave.setText(getText(R.string.action_edit));
                mInterface.navigateBack();
            });
        });
    }

    private void setTransfer() {

    }

    //    private void setTransfer() {
//
//        // Transfer don't allow credit card => set accountId and selected date
//
//        String descTransferTo = getString(R.string.label_transfer_to) + " " + transferAccountIn.getName();
//        String descTransferFrom = getString(R.string.label_transfer_from) + " " + transferAccountOut.getName();
//
//        Transaction in = new Transaction(1, descTransferFrom, amount,
//                selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay(),
//                0, transferAccountIn.getId(), null,
//                isPaid, repeat, null, null);
//
//        Transaction out = new Transaction(-1, descTransferTo, amount*-1,
//                selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay(),
//                0, transferAccountOut.getId(), null,
//                isPaid, repeat, null, null);
//
//        insertTransactionOnDb(out);
//        insertTransactionOnDb(in);
//    }
//

    private void logTransaction(Transaction t) {
        Log.d(LOG, "Transaction data to save => " + "\n" +
                "type: " + t.getType() + "\n" +
                "description: " + t.getDescription() + "\n" +
                "amount: " + t.getAmount() + "\n" +
                "date: " + t.getDay() + "/" + t.getMonth() + "/" + t.getYear() + "\n" +
                "category id: " + t.getCategoryId() + "\n" +
                "account id: " + t.getAccountId() + "\n" +
                "card id: " + t.getCardId() + "\n" +
                "isPaid: " + t.isPaid() + "\n" +
                "repeat: " + t.getRepeat() + " | " + "repeatCount: " + t.getRepeatCount() + " | " + "repeat id: " + t.getRepeatId());
    }

}