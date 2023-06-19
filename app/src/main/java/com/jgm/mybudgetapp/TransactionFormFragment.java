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
    private static final int IN = 1;
    private static final int OUT = -1;
    private static final int MAIN_METHOD_PICKER = 0; // transfer out = 1 / transfer in = 2;

    // VARS
    private boolean isEdit = false;
    private TransactionResponse selectedTransaction;
    private int transactionType;
    private String formType;
    private String description;
    private boolean hasChosenCategory = false;
    private boolean hasChosenMethod = false;
    private int currentMethodPicker = 0; // 0 => expense/income | 1 => transfer out | 2 => transfer in
    private int selectedAccountId = 0;
    private int selectedCardId = 0;
    private int selectedCategoryId;
    private float amount;
    private int repeat = 1;
    private boolean isPaid;
    private MyDate today;
    private MyDate selectedDate;
    private MyDate cardBillingDate;
    private PaymentMethod paymentMethod;
    private PaymentMethod transferAccountIn;
    private PaymentMethod transferAccountOut;

    // UI
    private FragmentTransactionFormBinding binding;
    private Button mCategoryPicker, mDatePicker, mMethodPicker, mAccountPickerOut, mAccountPickerIn, mSave;
    private ImageButton mIncreaseRepeat, mDecreaseRepeat;
    private ImageView mCategoryIcon, mDescIcon, mCalendarIcon, mMethodIcon, mAmountIcon;
    private ToggleButton mToggleExpense, mToggleIncome, mToggleTransfer;
    private Group mTransferGroup, mMainGroup;
    private ConstraintLayout mCreditCardMonthContainer;
    private TextView mFormattedPrice;
    private EditText mAmountInput, mRepeatInput;
    private MaterialSwitch mSwitchPaid;
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

        Log.d(LOG, "onViewCreated");

        if (isEdit) setEditForm();
        else {
            initType(Tags.TYPE_OUT);
            initAmount(0.0f);
            initDescription("");
            initDate(null);
            initCategory(null);
            initMethod();
            initTransferMethodPicker();
            initSwitchPayed();
            initRepeat();
            initSaveButton();
        }

    }

    /* ===============================================================================
                                      EDIT FORM
     =============================================================================== */

    public void setFormType(boolean isEdit, TransactionResponse transaction, PaymentMethod paymentMethod) {
        this.isEdit = isEdit;
        if (isEdit) {
            selectedTransaction = transaction;
            this.paymentMethod = paymentMethod;
        }
        Log.d(LOG, "setFormType");
    }

    private void setEditForm() {

        mToggleTransfer.setVisibility(View.GONE);

        isPaid = selectedTransaction.isPaid();
        repeat = selectedTransaction.getRepeat();
        currentMethodPicker = 0;

        MyDate date = new MyDate(
                selectedTransaction.getDay(),
                selectedTransaction.getMonth(),
                selectedTransaction.getYear());

        Category category = new Category(
                selectedTransaction.getCategoryName(),
                selectedTransaction.getColorId(),
                selectedTransaction.getIconId(),
                true);
        category.setId(selectedTransaction.getCategoryId());

        initType(selectedTransaction.getType());
        initAmount(Math.abs(selectedTransaction.getAmount()));
        mAmountInput.setText(String.valueOf(Math.abs(selectedTransaction.getAmount())));
        initDescription(selectedTransaction.getDescription());
        initDate(date);
        initCategory(category);
        initMethod();
        setSelectedPaymentMethod(paymentMethod);
        initSwitchPayed();
        initRepeat();
        initSaveButton();

    }

    /* ===============================================================================
                                      TOGGLE GROUP
     =============================================================================== */

    private void initType(int type) {
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
        mToggleExpense.setChecked(true);
        transactionType =  OUT;
        formType = Tags.expense;
        setMainGroup(R.drawable.button_save_expense);
        changeColorOnTypeSwitch(R.color.expense);
        if (!isEdit) setInitialPaymentMethod();
    }

    private void showIncomeForm() {
        mToggleIncome.setChecked(true);
        transactionType = IN;
        formType = Tags.income;
        setMainGroup(R.drawable.button_save);
        changeColorOnTypeSwitch(R.color.income);
        if (!isEdit) setInitialPaymentMethod();
    }

    private void showTransferForm() {
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
        mAmountInput.addTextChangedListener(priceWatcher);
        this.amount = amount;
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
            amount = typedPrice;
        }
    };

    /* ===============================================================================
                                        DESCRIPTION
     =============================================================================== */

    // Todo: autocomplete

    private void initDescription(String description) {
        mDescription.addTextChangedListener(descriptionWatcher);
        this.description = description;
        mDescription.setText(description);
    }

    private final TextWatcher descriptionWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            description = mDescription.getText().toString();
        }
    };

    /* ===============================================================================
                                        DATE PICKER
     =============================================================================== */

    private void initDate(MyDate date) {

        today = MyDateUtils.getCurrentDate(mContext);

        if (date == null) selectedDate = today;
        else selectedDate = date;

        int day = selectedDate.getDay();
        int month = selectedDate.getMonth();
        int year = selectedDate.getYear();

        String formattedDate = MyDateUtils.getFormattedFieldDate(mContext, year, month, day);

        String btnText;
        if (isEdit) {
            String weekday = MyDateUtils.getDayOfWeek(mContext, day, month, year)[0];
            btnText = weekday + " - " + formattedDate;
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

        Log.d(LOG, "setSelectedDate: " + day + "/" + month + "/" + year);

        String dateFormatted = MyDateUtils.getFormattedFieldDate(mContext, year, month, day);
        String dateField = weekday + " - " + dateFormatted;
        mDatePicker.setText(dateField);

        selectedDate = new MyDate(day, month, year);
        updateIsPaidAccordingToDate();

        if (selectedCardId != 0 && paymentMethod != null) {
            setBillingMonthOptions(paymentMethod.getBillingDay());
        }

    }

    /* ===============================================================================
                                         CATEGORY
     =============================================================================== */

    private void initCategory(Category category) {
        if (category == null) selectedCategoryId = 0;
        else setSelectedCategory(category);
        mCategoryPicker.setOnClickListener(view -> mInterface.openCategoriesList(false));
    }

    public void setSelectedCategory(Category category) {
        Icon icon = IconUtils.getIcon(category.getIconId());
        Color color = ColorUtils.getColor(category.getColorId());
        selectedCategoryId = category.getId();
        mCategoryPicker.setText(category.getName());
        mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mCategoryIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
        hasChosenCategory = true;
    }

    /* ===============================================================================
                                         METHOD
     =============================================================================== */

    private void initMethod() {
        mMethodPicker.setOnClickListener(view -> {
            currentMethodPicker = MAIN_METHOD_PICKER;
            boolean isExpense = formType.equals(Tags.expense);
            mInterface.showMethodPickerDialog(isExpense);
        });
    }

    private void setInitialPaymentMethod() {
        String methodName = getString(R.string.account_cash);
        paymentMethod = new PaymentMethod(1, 0, methodName, 19, 67, 1);
        mMethodIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.income));
        mMethodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_app_cash));
        mMethodPicker.setText(getString(R.string.account_cash));
        selectedAccountId = 1;
    }

    public void setSelectedPaymentMethod(PaymentMethod paymentMethod) {

        this.paymentMethod = paymentMethod;

        if (paymentMethod.getType() == 3) {
            selectedCardId = paymentMethod.getId();
            selectedAccountId = 0;
            mSwitchPaid.setChecked(false);
            isPaid = false;
        }
        else {
            selectedAccountId = paymentMethod.getId();
            selectedCardId = 0;
        }

        switch (currentMethodPicker) {
            case 0: updateMethod(paymentMethod); break;
            case 1: updateTransferOut(paymentMethod); break;
            case 2: updateTransferIn(paymentMethod); break;
        }

    }

    private void updateMethod(PaymentMethod paymentMethod) {
        Icon icon = IconUtils.getIcon(paymentMethod.getIconId());
        Color color = ColorUtils.getColor(paymentMethod.getColorId());
        String name = paymentMethod.getName();

        Log.d(LOG, "method card id=> " + icon.getId() + " / " + icon.getIconName());

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
            currentMethodPicker = 1;
            mInterface.showMethodPickerDialog(false);
        });
        mAccountPickerIn.setOnClickListener(view -> {
            currentMethodPicker = 2;
            mInterface.showMethodPickerDialog(false);
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
        updateIsPaidAccordingToDate();
        mSwitchPaid.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> isPaid = isChecked);
    }

    private void updateIsPaidAccordingToDate() {
        if (selectedDate.getYear() <= today.getYear() &&
                selectedDate.getMonth() <= today.getMonth() &&
                selectedDate.getDay() <= today.getDay()) {
            mSwitchPaid.setChecked(true);
            isPaid = true;
        }
        else {
            mSwitchPaid.setChecked(false);
            isPaid = false;
        }
    }

    /* ===============================================================================
                                           REPEAT
     =============================================================================== */

    private void initRepeat() {
        setQuantityInputValue();
        hideRepeatInput();
        mRepeatInput.addTextChangedListener(quantityWatcher);
        mIncreaseRepeat.setOnClickListener(view -> increaseRepeat());
        mDecreaseRepeat.setOnClickListener(view -> decreaseRepeat());
    }

    private void increaseRepeat() {
        if (repeat == 1) {
            showRepeatInput();
        }

        repeat++;
        setQuantityInputValue();

    }

    private void decreaseRepeat() {
        if (repeat > 1) {
            repeat--;
            setQuantityInputValue();
        }
        if (repeat == 1) {
            hideRepeatInput();
        }
    }

    private void setQuantityInputValue() {
        String sQt = String.valueOf(repeat);
        mRepeatInput.setText(sQt);
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

            if (input.isEmpty()) qt = 1;
            else qt = Integer.parseInt(input);

            if (qt > 120) {
                repeat = 120;
                setQuantityInputValue();
            }
            if (qt < 1) {
                repeat = 1;
                setQuantityInputValue();
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

            // Todo => handle db error

        });
    }

    private void setTransfer() {

        // Transfer don't allow credit card => set accountId and selected date

        String descTransferTo = getString(R.string.label_transfer_to) + " " + transferAccountIn.getName();
        String descTransferFrom = getString(R.string.label_transfer_from) + " " + transferAccountOut.getName();

        Transaction in = new Transaction(1, descTransferFrom, amount,
                selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay(),
                0, transferAccountIn.getId(), null,
                isPaid, repeat, null, null);

        Transaction out = new Transaction(-1, descTransferTo, amount*-1,
                selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay(),
                0, transferAccountOut.getId(), null,
                isPaid, repeat, null, null);

        saveTransaction(out);
        saveTransaction(in);
    }

    private void setTransaction() {

        int day = selectedDate.getDay();
        int month = selectedDate.getMonth();
        int year = selectedDate.getYear();

        // If method is credit card => day is billing day;
        if (paymentMethod != null && paymentMethod.getType() == 3) {
            day = paymentMethod.getBillingDay();
            month = cardBillingDate.getMonth();
            year = cardBillingDate.getYear();
        }

        // Set default description if empty
        if (description.isEmpty()) {
            if (transactionType == -1) description = getString(R.string.action_out);
            else description = getString(R.string.action_in);
        }

        if (transactionType == OUT) amount = amount * -1;

        Transaction transaction = new Transaction(
                transactionType, description, amount,
                year, month, day,
                selectedCategoryId,
                0, 0, isPaid, repeat, 0, null);

        if (repeat > 1) {
           long repeatId = System.currentTimeMillis();
           transaction.setRepeatId(repeatId);
        }

        if (paymentMethod != null) {
            if (paymentMethod.getType() == 3) transaction.setCardId(selectedCardId);
            else transaction.setAccountId(selectedAccountId);
        }

        if (isEdit) {
            transaction.setId(selectedTransaction.getId());
            editTransaction(transaction);
        }
        else saveTransaction(transaction);

    }

    private void saveTransaction(Transaction transaction) {

        logTransaction(transaction);
        insertTransactionOnDb(transaction);

        if (repeat > 1) {
            int i = 1;
            while (i < repeat) {
                // if repeat => update next transaction month and year
                int[] nextDate = MyDateUtils.getNextTransactionDate(transaction.getMonth(), transaction.getYear());
                transaction.setMonth(nextDate[0]);
                transaction.setYear(nextDate[1]);
                transaction.setRepeatCount(i);

                // if repeat => update isPaid
                if (transaction.getYear() >= today.getYear()
                        && transaction.getMonth() >= today.getMonth()
                        && transaction.getDay() >= today.getDay()) isPaid = false;

                logTransaction(transaction);
                insertTransactionOnDb(transaction);

                i++;
            }
        }
    }

    private void insertTransactionOnDb(Transaction transaction) {

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            transactionDao.insert(transaction);

            handler.post(() -> {
                Log.d(LOG, "Transaction saved on db... update ui");
                mProgressBar.setVisibility(View.GONE);
                mSave.setText(getText(R.string.action_save));
                mInterface.navigateBack();
            });
        });
    }

    private void editTransaction(Transaction transaction) {
        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            transactionDao.update(transaction);
            // todo: edit repeated transactions...

            handler.post(() -> {
                Log.d(LOG, "Transaction updated on db... update ui");
                mProgressBar.setVisibility(View.GONE);
                mSave.setText(getText(R.string.action_edit));
                mInterface.navigateBack();
            });
        });
    }

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