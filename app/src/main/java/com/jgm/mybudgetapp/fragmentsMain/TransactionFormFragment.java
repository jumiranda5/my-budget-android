package com.jgm.mybudgetapp.fragmentsMain;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.databinding.FragmentTransactionFormBinding;
import com.jgm.mybudgetapp.objects.Autocomplete;
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
import com.jgm.mybudgetapp.utils.HideKeyboard;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class TransactionFormFragment extends Fragment implements Animation.AnimationListener {

    // todo: this fragment is a nightmare, specially after screen rotation... (note to self => improve that someday)

    public TransactionFormFragment() {
        // Required empty public constructor
    }


    private static final String LOG = "debug-add";
    private static final int METHOD_PICKER_MAIN = 0;
    private static final int METHOD_PICKER_OUT = 1;
    private static final int METHOD_PICKER_IN = 2;
    private static final String STATE_EDIT = "EDIT";
    private static final String STATE_TYPE = "TYPE";
    private static final String STATE_TRANSACTION = "TRANSACTION";
    private static final String STATE_METHOD = "METHOD";
    private static final String STATE_METHOD_IN = "METHOD_IN";
    private static final String STATE_METHOD_OUT = "METHOD_OUT";
    private static final String SPLIT = "%&div&%";

    // VARS
    private Transaction transaction;
    private TransactionResponse edit;
    private int currentMethodPicker = METHOD_PICKER_MAIN;
    private int type = Tags.TYPE_OUT;
    private String formType;
    private AppDatabase db;
    private boolean isEdit = false;
    private boolean isEditAll = false;
    private MyDate today;
    private MyDate selectedDate;
    private MyDate cardBillingDate;
    private PaymentMethod selectedMethod;
    private PaymentMethod transferAccountIn;
    private PaymentMethod transferAccountOut;
    private Category selectedCategory;
    private boolean isSavedInstanceState = false;

    // Autocomplete
    private ArrayAdapter<String> searchAdapter;
    private static final int AUTOCOMPLETE_SEARCH_THRESHOLD = 2;
    private final ArrayList<String> searchResultList = new ArrayList<>();
    private boolean dismissAutocomplete = false;

    // UI
    private FragmentTransactionFormBinding binding;
    private Group mTransferGroup, mIncomeExpenseGroup, mOptionsGroup;
    private ToggleButton mToggleExpense, mToggleIncome, mToggleTransfer;
    private ConstraintLayout mCreditCardMonthContainer, mFormContainer;
    private ProgressBar mProgressBar;
    private Button mSave, mDatePicker, mCategoryPicker, mMethodPicker, mAccountPickerOut, mAccountPickerIn;
    private MaterialSwitch mSwitchPaid, mSwitchEditAll;
    private ImageButton mClose, mIncreaseRepeat, mDecreaseRepeat;
    private TextView mToolbarTitle, mFormattedPrice, mRepeatLabel;
    private EditText mAmount, mRepeatInput;
    private AutoCompleteTextView mDescription;
    private ImageView mCategoryIcon, mMethodIcon;
    private RadioButton mMonth1, mMonth2;

    private void setBinding() {
        mFormContainer = binding.formContainer;
        mTransferGroup = binding.groupOnlyTransfer;
        mIncomeExpenseGroup = binding.groupSharedIncomeExpense;
        mOptionsGroup = binding.groupAdvancedOptions;
        mToggleExpense = binding.toggleExpense;
        mToggleIncome = binding.toggleIncome;
        mToggleTransfer = binding.toggleTransfer;
        mCreditCardMonthContainer = binding.creditCardMonthPicker;
        mProgressBar = binding.addProgressBar;
        mSave = binding.saveButton;
        mSwitchPaid = binding.addSwitchPaid;
        mSwitchEditAll = binding.addSwitchEditAll;
        mClose = binding.formButtonBack;
        mToolbarTitle = binding.formToolbarTitle;
        mFormattedPrice = binding.addFormattedPrice;
        mAmount = binding.addAmount;
        mDescription = binding.addDescription;
        mDatePicker = binding.addDatePicker;
        mCategoryPicker = binding.addCategoryPicker;
        mCategoryIcon = binding.addIconCategory;
        mMethodPicker = binding.addMethodPicker;
        mMethodIcon = binding.addIconMethod;
        mMonth1 = binding.billingMonth1;
        mMonth2 = binding.billingMonth2;
        mAccountPickerOut = binding.addAccountPickerOut;
        mAccountPickerIn = binding.addAccountPickerIn;
        mRepeatInput = binding.addQuantityInput;
        mIncreaseRepeat = binding.buttonRepeatIncrease;
        mDecreaseRepeat = binding.buttonRepeatDecrease;
        mRepeatLabel = binding.repeatLabel;
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
        mFormContainer.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(Tags.LOG_LIFECYCLE, "Transactions form onViewCreated");

        // init database
        db = AppDatabase.getDatabase(mContext);

        // Init back button
        mClose.setOnClickListener(v -> mInterface.navigateBack());

        if (savedInstanceState != null) {
            isSavedInstanceState = true;
            isEdit = savedInstanceState.getBoolean(STATE_EDIT, false);
            type = savedInstanceState.getInt(STATE_TYPE, 0);
            Log.d(LOG, "==================== TYPE SAVED: " + type);

            selectedMethod = savedInstanceState.getParcelable(STATE_METHOD);
            if (isEdit) {
                edit = savedInstanceState.getParcelable(STATE_TRANSACTION);
                setEditValues(edit);
                selectedCategory = new Category(edit.getCategoryName(), edit.getColorId(), edit.getIconId(), true);
                selectedCategory.setId(edit.getCategoryId());
            }
            else {
                transaction = savedInstanceState.getParcelable(STATE_TRANSACTION);
            }

            if (type == 2) {
                transferAccountIn = savedInstanceState.getParcelable(STATE_METHOD_IN);
                transferAccountOut = savedInstanceState.getParcelable(STATE_METHOD_OUT);
                if (transferAccountOut != null) updateTransferOut(transferAccountOut);
                if (transferAccountIn != null) updateTransferIn(transferAccountIn);
            }

            // Todo: mSwitchEditAll appears on screen rotation. Can't set visibility to gone. Why?
            mSwitchEditAll.setEnabled(false);
            if (isEdit && transaction.getRepeat() > 1) mSwitchEditAll.setEnabled(true);

            // Todo: same thing happens to repeat input... can't hide it after screen rotation
        }

        // init form fields
        initFormType();
        initAmount();
        initDescription();
        initDate();
        initCategory();
        initMethod();
        initTransferMethodPicker();
        initSwitchPayed();
        initSwitchEditAll();
        initRepeat();
        initSaveButton();
        mFormContainer.setVisibility(View.VISIBLE);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(LOG, "==================== TYPE TO SAVE: " + type);

        outState.putInt(STATE_TYPE, type);
        outState.putBoolean(STATE_EDIT, isEdit);
        outState.putParcelable(STATE_METHOD, selectedMethod);
        if (isEdit) outState.putParcelable(STATE_TRANSACTION, edit);
        else outState.putParcelable(STATE_TRANSACTION, transaction);

        if (type == 2) {
            outState.putParcelable(STATE_METHOD_IN, transferAccountIn);
            outState.putParcelable(STATE_METHOD_OUT, transferAccountOut);
        }
    }

    /* ---------------------------------------------------------------------------------------------
                                            INTERFACE
     -------------------------------------------------------------------------------------------- */

    // This happens before onCreateView
    public void setFormType(int type, boolean isEdit, TransactionResponse edit, PaymentMethod paymentMethod) {
        Log.d(LOG, "=> setFormType: isEdit = " + isEdit);
        this.type = type;
        this.isEdit = isEdit;
        if (isEdit) {
            this.edit = edit;
            setEditValues(edit);
            selectedMethod = paymentMethod;
            selectedCategory = new Category(edit.getCategoryName(), edit.getColorId(), edit.getIconId(), true);
            selectedCategory.setId(edit.getCategoryId());
        }
        else setEmptyValues();
    }

    public void setSelectedDate(long selection) {
        MyDate date = MyDateUtils.getDateFromMilliseconds(mContext, selection);
        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        String weekday = date.getWeekday();
        selectedDate = new MyDate(day, month, year);

        setTransactionDate(day, month, year);

        String dateFormatted = MyDateUtils.getFormattedFieldDate(mContext, year, month, day);
        String dateField = weekday + " - " + dateFormatted;

        if (isEdit && transaction.getRepeat() > 1) {
            if (isEditAll) {
                dateField = setDateTextForAllParcels();
                mDatePicker.setText(dateField);
            }
            else setDateTextForSingleParcel();
        }
        else mDatePicker.setText(dateField);

        mDatePicker.setTextColor(ContextCompat.getColor(mContext, R.color.high_emphasis_text));

        Log.d(LOG, "=> setSelectedDate: " + dateField);

        updateIsPaidAccordingToDate();

        if (transaction.getCardId() != 0 && selectedMethod != null) {
            setBillingMonthOptions(selectedMethod.getBillingDay());
        }
    }

    public void setSelectedCategory(Category category) {
        Log.d(LOG, "=> Selected category: " + category.getName());
        Icon icon = IconUtils.getIcon(category.getIconId());
        Color color = ColorUtils.getColor(category.getColorId());
        transaction.setCategoryId(category.getId());
        mCategoryPicker.setText(category.getName());
        mCategoryPicker.setTextColor(ContextCompat.getColor(mContext, R.color.high_emphasis_text));
        mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mCategoryIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
    }

    public void setSelectedPaymentMethod(PaymentMethod paymentMethod) {
        Log.d(LOG, "=> setSelectedPaymentMethod: " + paymentMethod.getName());

        selectedMethod = paymentMethod;

        if (paymentMethod.getType() == 3) {
            transaction.setCardId(paymentMethod.getId());
            transaction.setAccountId(0);
            mSwitchPaid.setEnabled(false);
        }
        else {
            if (!formType.equals(Tags.transfer)) mSwitchPaid.setVisibility(View.VISIBLE);
            transaction.setAccountId(paymentMethod.getId());
            transaction.setCardId(0);
            mSwitchPaid.setEnabled(true);
        }

        switch (currentMethodPicker) {
            case METHOD_PICKER_MAIN: updateMethod(paymentMethod); break;
            case METHOD_PICKER_OUT: updateTransferOut(paymentMethod); break;
            case METHOD_PICKER_IN: updateTransferIn(paymentMethod); break;
        }
    }


    /* ---------------------------------------------------------------------------------------------
                                            INITIAL VALUES
     -------------------------------------------------------------------------------------------- */

    private void setEditValues(TransactionResponse edit) {
        transaction = new Transaction(edit.getType(), edit.getDescription(), edit.getAmount(),
                edit.getYear(), edit.getMonth(), edit.getDay(),
                edit.getCategoryId(), edit.getAccountId(), edit.getCardId(), edit.isPaid(),
                edit.getRepeat(), edit.getRepeatCount(), edit.getRepeatId());
        transaction.setId(edit.getId());
    }

    private void setEmptyValues() {
        transaction = new Transaction(type, "", 0f, 0, 0, 0,
                0, 0, 0, false, 1, 1, null);
    }


    /* ---------------------------------------------------------------------------------------------
                                            FORM TYPE
     -------------------------------------------------------------------------------------------- */

    private void initFormType() {
        Log.d(LOG, "=> initFormType: " + type);

        String tag;

        mToggleExpense.setChecked(false);
        mToggleIncome.setChecked(false);
        mToggleTransfer.setChecked(false);
        mCreditCardMonthContainer.setVisibility(View.GONE);

        if (type == Tags.TYPE_IN) {
            showIncomeForm();
            mToggleIncome.setChecked(true);
            tag = Tags.income;
        }
        else if (type == Tags.TYPE_OUT) {
            showExpenseForm();
            mToggleExpense.setChecked(true);
            tag = Tags.expense;
        }
        else {
            showTransferForm();
            mToggleTransfer.setChecked(true);
            tag = Tags.transfer;
        }

        formType = tag;
        changeTitleAndColorOnTypeSwitch(tag);

        if (type != 2) {
            mOptionsGroup.setVisibility(View.VISIBLE);
            mSwitchEditAll.setVisibility(View.GONE);
            if (isEdit && transaction.getRepeat() > 1) mSwitchEditAll.setVisibility(View.VISIBLE);
        }

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
            button.setChecked(true);

            handleTypeChange(tag);
        });
    }

    private void handleTypeChange(String tag) {
        initFadeOutAnimation();
        initFadeInAnimation();
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    formType = tag;
                    changeTitleAndColorOnTypeSwitch(tag);
                    mCreditCardMonthContainer.setVisibility(View.GONE);
                    mSave.setEnabled(true);

                    mOptionsGroup.setVisibility(View.VISIBLE);
                    if (transaction.getRepeat() == 1) hideRepeatInput();
                    mSwitchEditAll.setVisibility(View.GONE);
                    if (isEdit && transaction.getRepeat() > 1) mSwitchEditAll.setVisibility(View.VISIBLE);

                    if (tag.equals(Tags.expense)) showExpenseForm();
                    else if (tag.equals(Tags.income)) showIncomeForm();
                    else showTransferForm();
                }, 150);
    }

    private void showExpenseForm() {
        Log.d(LOG, "=> showExpenseForm");
        transaction.setType(Tags.TYPE_OUT);
        type = Tags.TYPE_OUT;
        mIncomeExpenseGroup.setVisibility(View.VISIBLE);
        mTransferGroup.setVisibility(View.GONE);
        if (selectedMethod != null && selectedMethod.getType() == Tags.METHOD_CARD && selectedDate != null)
            setBillingMonthOptions(selectedMethod.getBillingDay());
    }

    private void showIncomeForm() {
        Log.d(LOG, "=> showIncomeForm");
        transaction.setType(Tags.TYPE_IN);
        type = Tags.TYPE_IN;
        mIncomeExpenseGroup.setVisibility(View.VISIBLE);
        mTransferGroup.setVisibility(View.GONE);

        // if switching from expense with credit card payment method:
        if (selectedMethod != null && selectedMethod.getType() == Tags.METHOD_CARD) {
            selectedMethod = null;
            transaction.setCardId(0);
            initMethod();
        }
    }

    private void showTransferForm() {
        Log.d(LOG, "=> showTransferForm");
        transaction.setType(2);
        type = 2;
        mTransferGroup.setVisibility(View.VISIBLE);
        mIncomeExpenseGroup.setVisibility(View.GONE);
        mOptionsGroup.setVisibility(View.GONE);
        mSave.setEnabled(false);
    }

    private void changeTitleAndColorOnTypeSwitch(String type) {
        switch (type) {
            case Tags.expense:
                mToolbarTitle.setText(mContext.getString(R.string.title_expense));
                mFormattedPrice.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
                mSave.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_save_expense));
                break;
            case Tags.income:
                mToolbarTitle.setText(mContext.getString(R.string.title_income));
                mFormattedPrice.setTextColor(ContextCompat.getColor(mContext, R.color.income));
                mSave.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_save_income));
                break;
            case Tags.transfer:
                mToolbarTitle.setText(mContext.getString(R.string.title_transfer));
                mFormattedPrice.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
                mSave.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_save_transfer));
                break;
        }
    }

    private void initFadeOutAnimation() {
        Animator animatorSetOut = AnimatorInflater.loadAnimator(mContext, R.animator.fade_out);
        animatorSetOut.setTarget(mToolbarTitle);
        animatorSetOut.start();
    }

    private void initFadeInAnimation() {
        Animator animatorSetIn = AnimatorInflater.loadAnimator(mContext, R.animator.fade_in);
        animatorSetIn.setTarget(mToolbarTitle);
        animatorSetIn.setStartDelay(170);
        animatorSetIn.start();
    }


    /* ---------------------------------------------------------------------------------------------
                                            AMOUNT
     -------------------------------------------------------------------------------------------- */

    private void initAmount() {
        Log.d(LOG, "=> initAmount: " + transaction.getAmount());
        mAmount.addTextChangedListener(priceWatcher);

        float amount = Math.abs(transaction.getAmount());
        if (amount > 0) mAmount.setText(String.valueOf(amount));
        setFormattedAmount(transaction.getAmount());
    }

    private void setFormattedAmount(float amount) {
        String formattedPrice = NumberUtils.getCurrencyFormat(mContext, Math.abs(amount))[2];
        mFormattedPrice.setText(formattedPrice);
    }

    private final TextWatcher priceWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String priceString = mAmount.getText().toString();
            if (priceString.length() == 0) priceString = "0";

            float typedPrice = Float.parseFloat(priceString);
            setFormattedAmount(typedPrice);
            transaction.setAmount(typedPrice);
        }
    };


    /* ---------------------------------------------------------------------------------------------
                                           DESCRIPTION
     -------------------------------------------------------------------------------------------- */

    private void initDescription() {
        Log.d(LOG, "=> initDescription: " + transaction.getDescription());
        String description = transaction.getDescription();
        mDescription.addTextChangedListener(descriptionWatcher);
        transaction.setDescription(description);
        mDescription.setText(description);
        initAutocomplete();
    }

    private void initAutocomplete() {
        searchAdapter = new ArrayAdapter<String>(mContext, R.layout.item_autocomplete,
                R.id.item_autocomplete_description, searchResultList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Log.d("debug-autocomplete", "result size: " + searchResultList.size());

                try {
                    TextView text1 = view.findViewById(R.id.item_autocomplete_description);
                    TextView text2 = view.findViewById(R.id.item_autocomplete_category);
                    ImageView iconImg = view.findViewById(R.id.item_autocomplete_icon);

                    // description + category + category icon + category color + category id
                    String[] item = searchResultList.get(position).split(SPLIT);
                    if (item.length == 5) {
                        String description = item[0];
                        String categoryName = item[1];
                        int iconId = Integer.parseInt(item[2]);
                        int colorId = Integer.parseInt(item[3]);
                        Icon icon = IconUtils.getIcon(iconId);
                        Color color = ColorUtils.getColor(colorId);
                        text1.setText(description);
                        text2.setText(categoryName);
                        iconImg.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
                        iconImg.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
                    }
                }
                catch (Exception e) {
                    Log.e("debug-autocomplete", e.getMessage());
                }

                return view;
            }
        };

        mDescription.setAdapter(searchAdapter);
        mDescription.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("debug-autocomplete", "view clicked!");
            // description + category + category icon + category color + category id
            String[] item = searchResultList.get(position).split(SPLIT);
            dismissAutocomplete = true;
            if (item.length == 5) {
                String description = item[0];
                String categoryName = item[1];
                int iconId = Integer.parseInt(item[2]);
                int colorId = Integer.parseInt(item[3]);
                int categoryId = Integer.parseInt(item[4]);
                Category autoCategory = new Category(categoryName, colorId, iconId, true);
                autoCategory.setId(categoryId);
                setSelectedCategory(autoCategory);
                mDescription.setText(description);
            }
            HideKeyboard.hide((Activity) mContext);
            searchResultList.clear();
            searchAdapter.getFilter().filter("");
            searchAdapter.clear();
        });
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
            if (editable.length() >= AUTOCOMPLETE_SEARCH_THRESHOLD && !dismissAutocomplete)
                searchTransaction(editable.toString());
            dismissAutocomplete = false;
        }
    };

    private void searchTransaction(String desc) {
        Log.d("debug-autocomplete", "!!!!!!!!!! searchTransaction");
        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            String pattern = desc + "%";
            List<Autocomplete> results = transactionDao.getAutocompleteResult(pattern);

            handler.post(() -> {
                searchResultList.clear();
                for (int i = 0; i < results.size(); i++) {
                    String description = results.get(i).getDescription();
                    String categoryName = results.get(i).getCategoryName();
                    int categoryId = results.get(i).getCategoryId();
                    int categoryIcon = results.get(i).getIconId();
                    int categoryColor = results.get(i).getColorId();
                    String item = description + SPLIT + categoryName + SPLIT + categoryIcon + SPLIT
                            + categoryColor + SPLIT + categoryId;
                    searchResultList.add(item);
                }
                searchAdapter.clear();
                searchAdapter.addAll(searchResultList);
                searchAdapter.getFilter().filter("");
            });
        });
    }


    /* ---------------------------------------------------------------------------------------------
                                              DATE
     -------------------------------------------------------------------------------------------- */

    private void initDate() {

        initSelectedDate();

        int day = selectedDate.getDay();
        int month = selectedDate.getMonth();
        int year = selectedDate.getYear();
        setTransactionDate(day, month, year);
        String formattedDate = MyDateUtils.getFormattedFieldDate(mContext, year, month, day);

        Log.d(LOG, "=> initDate: " + formattedDate);

        // Init date picker button
        String btnText;
        if (isEdit || isSavedInstanceState) {
            String weekday = MyDateUtils.getDayOfWeek(mContext, day, month, year)[0];
            btnText = weekday + " - " + formattedDate;
            if (transaction.getRepeat() > 1) btnText = setDateTextForAllParcels();
        }
        else btnText = getString(R.string.label_today) + " - " + formattedDate;

        mDatePicker.setTextColor(ContextCompat.getColor(mContext, R.color.high_emphasis_text));

        mDatePicker.setText(btnText);
        mDatePicker.setOnClickListener(view -> {
            long dateMilliseconds = MyDateUtils.getMillisecondsFromDate(selectedDate);
            Log.w(LOG, "millis: " + dateMilliseconds);
            mInterface.showDatePickerDialog(dateMilliseconds);
        });
    }

    private void initSelectedDate() {
        today = MyDateUtils.getCurrentDate(mContext);
        if (isEdit) {
            // Set date from first parcel, if parcel
            int parcel = transaction.getRepeatCount();
            int month;
            if (parcel > 1) month = (transaction.getMonth() - parcel) + 1;
            else month = transaction.getMonth();

            // Set date
            selectedDate = new MyDate(transaction.getDay(), month, transaction.getYear());
        }
        else if (isSavedInstanceState) {
            selectedDate = new MyDate(transaction.getDay(), transaction.getMonth(), transaction.getYear());
        }
        else selectedDate = today;
    }

    private void setTransactionDate(int day, int month, int year) {
        transaction.setDay(day);
        transaction.setMonth(month);
        transaction.setYear(year);
    }

    private void setDateTextForSingleParcel() {
        String monthName = MyDateUtils.getMonthName(mContext, edit.getMonth(), selectedDate.getYear())[0];
        String dateField = mContext.getString(R.string.label_date_picker_day) + " " + selectedDate.getDay() + " (" + monthName + ")";
        mDatePicker.setText(dateField);

        Log.d(LOG, "---- single parcel date: " + dateField);
    }

    private String setDateTextForAllParcels() {
        String formattedDate = MyDateUtils.getFormattedFieldDate(
                mContext, selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay());

        Log.d(LOG, "---- first parcel date: " + formattedDate);

        return mContext.getString(R.string.label_date_picker_first) + " " + formattedDate;
    }


    /* ---------------------------------------------------------------------------------------------
                                             CATEGORIES
     -------------------------------------------------------------------------------------------- */

    private void initCategory() {
        Log.d(LOG, "=> initCategory");

        Log.d(LOG, "-------- category id: " + transaction.getCategoryId());

        mCategoryPicker.setOnClickListener(view -> mInterface.openCategoriesList(false));

        if (!isEdit) {
            // get default account and category (first on db)
            Handler handler = new Handler();
            AppDatabase.dbExecutor.execute(() -> {

                // if onSavedInstanceState => get category by id
                Category category;
                if (transaction.getCategoryId() != 0 && isSavedInstanceState) {
                    category = db.CategoryDao().getCategoryById(transaction.getCategoryId());
                    setSelectedCategory(category);
                }
                else {
                    category = db.CategoryDao().getDefaultCategory();
                }

                handler.post(() -> {
                    Log.d(LOG, "default category: " + category.getName());
                    selectedCategory = category;
                    transaction.setCategoryId(category.getId());
                });
            });

        }
        else setSelectedCategory(selectedCategory);
    }


    /* ---------------------------------------------------------------------------------------------
                                            PAYMENT METHOD
     -------------------------------------------------------------------------------------------- */

    private void initMethod() {
        Log.d(LOG, "=> initMethod");

        mMethodPicker.setOnClickListener(view -> {
            boolean isExpense = formType.equals(Tags.expense);
            mInterface.showMethodPickerDialog(isExpense, null, 0);
        });

        if (!isEdit && selectedMethod == null) {
            // get default account and category (first on db)
            Handler handler = new Handler();
            AppDatabase.dbExecutor.execute(() -> {

                Account account = db.AccountDao().getDefaultPaymentMethod();

                handler.post(() -> {
                    Log.d(LOG, "---- default method: " + account.getName());

                    transaction.setAccountId(account.getId());
                    selectedMethod = new PaymentMethod(account.getId(), account.getType(),
                            account.getName(), account.getColorId(), account.getIconId(), 1);

                });
            });
        }
        else updateMethod(selectedMethod);
    }

    private void updateMethod(PaymentMethod paymentMethod) {
        Icon icon = IconUtils.getIcon(paymentMethod.getIconId());
        Color color = ColorUtils.getColor(paymentMethod.getColorId());
        String name = paymentMethod.getName();

        if (paymentMethod.getType() == 3) {
            setBillingMonthOptions(paymentMethod.getBillingDay());
            mSwitchPaid.setChecked(false);
            mSwitchPaid.setEnabled(false);
        }
        else {
            mCreditCardMonthContainer.setVisibility(View.GONE);
            mSwitchPaid.setEnabled(true);
        }

        mMethodPicker.setText(name);
        mMethodPicker.setTextColor(ContextCompat.getColor(mContext, R.color.high_emphasis_text));
        mMethodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mMethodIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
    }

    private void setBillingMonthOptions(int billingDay) {

        mCreditCardMonthContainer.setVisibility(View.VISIBLE);

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

        cardBillingDate = new MyDate(selectedMethod.getBillingDay(), month1, year1);

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
            cardBillingDate = new MyDate(selectedMethod.getBillingDay(), month1, year1);
        });
        mMonth2.setOnClickListener(v -> {
            mMonth1.setChecked(false);
            mMonth2.setChecked(true);
            cardBillingDate = new MyDate(selectedMethod.getBillingDay(), month2, year2);
        });
    }

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
        if (transferAccountIn != null) mSave.setEnabled(true);
    }

    private void updateTransferIn(PaymentMethod paymentMethod) {
        transferAccountIn = paymentMethod;
        mAccountPickerIn.setText(paymentMethod.getName());
        if (transferAccountOut != null) mSave.setEnabled(true);
    }


    /* ---------------------------------------------------------------------------------------------
                                           SWITCH EDIT ALL
     -------------------------------------------------------------------------------------------- */

    private void initSwitchEditAll() {
        if (isEdit) {
            if (transaction.getRepeat() == 1) {
                mSwitchEditAll.setVisibility(View.GONE);
                if (selectedMethod.getType() != Tags.METHOD_CARD) mSwitchPaid.setEnabled(true);
            }
            else {
                isEditAll = true;
                mSwitchPaid.setEnabled(false);
                mSwitchEditAll.setVisibility(View.VISIBLE);
                mSwitchEditAll.setChecked(true);
                mSwitchEditAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    isEditAll = isChecked;
                    if (isChecked) {
                        mDatePicker.setText(setDateTextForAllParcels());
                        setRepeatEnabled();
                        mSwitchPaid.setEnabled(false);
                    }
                    else {
                        setDateTextForSingleParcel();
                        // don't allow repeat count changes or paid status changes
                        setRepeatDisabled();
                        // allow paid status changes if method is not credit card
                        if (selectedMethod.getType() != Tags.METHOD_CARD) mSwitchPaid.setEnabled(true);
                        // initial repeat value
                        transaction.setRepeat(edit.getRepeat());
                    }
                });
            }
        }
        else {
            mSwitchEditAll.setVisibility(View.GONE);
        }
    }


    /* ---------------------------------------------------------------------------------------------
                                             SWITCH PAID
     -------------------------------------------------------------------------------------------- */

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

        boolean isCurrentMonthAndTodayOrBefore = selectedDate.getMonth() == today.getMonth()
                && selectedDate.getDay() <= today.getDay();
        boolean isLastMonth = selectedDate.getMonth() < today.getMonth();

        boolean isTodayOrBefore = (selectedDate.getYear() <= today.getYear()
                && (isCurrentMonthAndTodayOrBefore || isLastMonth));

        if (isTodayOrBefore) {
            mSwitchPaid.setChecked(true);
            transaction.setPaid(true);
        }
        else {
            mSwitchPaid.setChecked(false);
            transaction.setPaid(false);
        }
    }


    /* ---------------------------------------------------------------------------------------------
                                               REPEAT
     -------------------------------------------------------------------------------------------- */

    private void initRepeat() {
        int repeat = transaction.getRepeat();
        Log.d(LOG, "=> initRepeat: " + repeat);
        updateQuantityInputValue(repeat);
        if (repeat < 2) hideRepeatInput();
        mRepeatInput.addTextChangedListener(quantityWatcher);
        mIncreaseRepeat.setOnClickListener(view -> increaseRepeat());
        mDecreaseRepeat.setOnClickListener(view -> decreaseRepeat());
    }

    private void setRepeatDisabled() {
        hideRepeatInput();
        mRepeatInput.setEnabled(false);
        mDecreaseRepeat.setEnabled(false);
        mIncreaseRepeat.setEnabled(false);
        mIncreaseRepeat.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.disabled_text));
        mDecreaseRepeat.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.disabled_text));
        mRepeatInput.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
        mRepeatLabel.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
    }

    private void setRepeatEnabled() {
        if (transaction.getRepeat() > 1) showRepeatInput();
        mRepeatInput.setEnabled(true);
        mDecreaseRepeat.setEnabled(true);
        mIncreaseRepeat.setEnabled(true);
        mIncreaseRepeat.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.high_emphasis_text));
        mDecreaseRepeat.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.high_emphasis_text));
        mRepeatInput.setTextColor(ContextCompat.getColor(mContext, R.color.high_emphasis_text));
        mRepeatLabel.setTextColor(ContextCompat.getColor(mContext, R.color.high_emphasis_text));
    }

    private void updateQuantityInputValue(int repeat) {
        String sQt = String.valueOf(repeat);
        mRepeatInput.setText(sQt);
        transaction.setRepeat(repeat);
    }

    private void showRepeatInput() {
        mRepeatInput.setVisibility(View.VISIBLE);
        mDecreaseRepeat.setVisibility(View.VISIBLE);
    }

    private void hideRepeatInput() {
        mRepeatInput.setVisibility(View.GONE);
        mDecreaseRepeat.setVisibility(View.GONE);
    }

    private void increaseRepeat() {
        int repeat = transaction.getRepeat();
        if (repeat == 1) showRepeatInput();
        repeat++;
        updateQuantityInputValue(repeat);
    }

    private void decreaseRepeat() {
        int repeat = transaction.getRepeat();
        if (repeat > 1) {
            repeat--;
            updateQuantityInputValue(repeat);
        }
        if (repeat == 1) hideRepeatInput();
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
                updateQuantityInputValue(repeat);
            }
            if (qt < 1) {
                repeat = 1;
                updateQuantityInputValue(repeat);
            }
        }
    };

    /* ---------------------------------------------------------------------------------------------
                                                SAVE
     -------------------------------------------------------------------------------------------- */

    private void initSaveButtonProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mSave.setText("");
        mSave.setEnabled(false);
    }

    private void endSaveButtonProgress() {
        Log.d(Tags.LOG_DB, "Transaction saved on db");
        mProgressBar.setVisibility(View.GONE);
        mSave.setText(getText(R.string.action_save));
        mInterface.navigateBack();
    }

    private void initSaveButton() {

        if (isEdit) mSave.setText(getString(R.string.action_edit));

        mSave.setOnClickListener(view -> {
            initSaveButtonProgress();
            if (formType.equals(Tags.transfer)) setTransfer();
            else setTransaction();
        });
    }

    private void setTransaction() {
        // If method is credit card => day is billing day;
        if (selectedMethod.getType() == 3 && cardBillingDate != null) {
            transaction.setDay(selectedMethod.getBillingDay());
            transaction.setMonth(cardBillingDate.getMonth());
            transaction.setYear(cardBillingDate.getYear());
        }

        // Set default description if empty
        if (transaction.getDescription().isEmpty()) {
            if (transaction.getType() == -1) transaction.setDescription(getString(R.string.action_out));
            else transaction.setDescription(getString(R.string.action_in));
        }

        // save negative amount if expense
        float amount = transaction.getAmount();
        if (transaction.getType() == -1) amount = amount * -1;
        transaction.setAmount(amount);

        if (isEdit) {
            // get account id if new method is a different card or old method is not card
            boolean isNewCard = selectedMethod.getType() == 3 && !edit.getCardId().equals(transaction.getCardId());

            boolean isSingleTransaction = (edit.getRepeat() == 1 && transaction.getRepeat() == 1);
            boolean isSingleParcel = (edit.getRepeat() > 1 && !isEditAll);
            boolean isMultipleParcels = (edit.getRepeat() > 1 && isEditAll);
            boolean isAddParcelsToSingle = (edit.getRepeat() == 1 && transaction.getRepeat() > 1);

            if (isSingleTransaction) editSingleTransaction(isNewCard);
            else if (isSingleParcel) editSingleParcel(isNewCard);
            else if (isMultipleParcels) editAllParcels(isNewCard);
            else if (isAddParcelsToSingle) editSingleAddParcels(isNewCard);


        }
        else {
            // Add single transaction | add multiple
            if (transaction.getRepeat() == 1) saveSingleTransaction();
            else {
                long repeatId = System.currentTimeMillis();
                transaction.setRepeatId(repeatId);
                saveMultipleTransactions(false);
            }
        }
    }

    private void saveSingleTransaction() {
        Log.d(LOG, "=> Save single transaction");
        logTransaction(transaction);

        initSaveButtonProgress();

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {
            transactionDao.insert(transaction);
            handler.post(this::endSaveButtonProgress);
        });
    }

    private void saveMultipleTransactions(boolean isNewCard) {
        Log.d(LOG, "=> Save multiple transaction (parcels)");
        logTransaction(transaction);

        initSaveButtonProgress();

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            // First parcel
            transactionDao.insert(transaction);

            // from second parcel on...
            int i = 1;
            int repeat = transaction.getRepeat();
            while (i < repeat) {
                // update next transaction month and year
                int[] nextDate = MyDateUtils.getNextTransactionDate(transaction.getMonth(), transaction.getYear());
                transaction.setMonth(nextDate[0]);
                transaction.setYear(nextDate[1]);
                transaction.setRepeatCount(i+1);

                // set paid to false if future
                boolean isFuture = transaction.getMonth() > today.getMonth() ||
                        (transaction.getMonth() == today.getMonth() &&
                                transaction.getDay() > today.getDay());
                if (isFuture) transaction.setPaid(false);

                if (isNewCard) {
                    Log.d(LOG, "isNewCard... get transaction with same card and month...");
                    Transaction cardTransaction = transactionDao.getCardTransaction(
                            transaction.getCardId(), transaction.getMonth(), transaction.getYear());
                    if (cardTransaction != null) {
                        Log.d(LOG, "isNewCard... found transaction => update account id");
                        transaction.setAccountId(cardTransaction.getAccountId());
                        transaction.setPaid(cardTransaction.isPaid());
                    }
                }

                logTransaction(transaction);
                transactionDao.insert(transaction);

                i++;
            }

            handler.post(this::endSaveButtonProgress);
        });
    }

    private void editSingleTransaction(boolean isNewCard) {
        Log.d(LOG, "=> edit single transaction");
        logTransaction(transaction);

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            if (isNewCard) {
                Log.d(LOG, "isNewCard... get transaction with same card and month...");
                Transaction cardTransaction = transactionDao.getCardTransaction(
                        transaction.getCardId(), transaction.getMonth(), transaction.getYear());
                if (cardTransaction != null) {
                    Log.d(LOG, "isNewCard... found transaction => update account id");
                    transaction.setAccountId(cardTransaction.getAccountId());
                    transaction.setPaid(cardTransaction.isPaid());
                }
            }

            transactionDao.update(transaction);
            handler.post(this::endSaveButtonProgress);
        });

    }

    private void editSingleParcel(boolean isNewCard) {
        Log.d(LOG, "edit single parcel");
        logTransaction(transaction);
        // get parcel date
        transaction.setMonth(edit.getMonth());
        transaction.setYear(edit.getYear());
        editSingleTransaction(isNewCard);
    }

    private void editAllParcels(boolean isNewCard) {
        Log.d(LOG, "=> edit all parcels");

        // check if repeat count changed (for edit)
        boolean changedRepeat = transaction.getRepeat() != edit.getRepeat();

        // check if first parcel month changed
        boolean changedFirstParcelMonth = false;
        String editDate = edit.getMonth() + "/" + edit.getYear();
        String newDate = selectedDate.getMonth() + "/" + selectedDate.getYear();
        if (!editDate.equals(newDate)) changedFirstParcelMonth = true;

        boolean isSimpleEdit = (!changedRepeat && !changedFirstParcelMonth && !isNewCard);

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            if (isSimpleEdit) {
                Log.d(LOG, "simple edit"); // ok
                transactionDao.updateAllParcels(transaction.getRepeatId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getCardId(),
                        transaction.getAccountId(),
                        transaction.getCategoryId(),
                        selectedDate.getDay());

                handler.post(this::endSaveButtonProgress);
            }
            else {
                Log.d(LOG, "delete all and add again");
                db.TransactionDao().deleteByRepeatId(transaction.getRepeatId());

                if (isNewCard) {
                    Log.d(LOG, "isNewCard... get transaction with same card and month...");
                    Transaction cardTransaction = transactionDao.getCardTransaction(
                            transaction.getCardId(), transaction.getMonth(), transaction.getYear());
                    if (cardTransaction != null) {
                        Log.d(LOG, "isNewCard... found transaction => update account id");
                        transaction.setAccountId(cardTransaction.getAccountId());
                        transaction.setPaid(cardTransaction.isPaid());
                    }
                }

                // remove id from transaction obj (to avoid unique id error)
                transaction = new Transaction(
                        transaction.getType(), transaction.getDescription(), transaction.getAmount(),
                        transaction.getYear(), transaction.getMonth(), transaction.getDay(),
                        transaction.getCategoryId(), transaction.getAccountId(), transaction.getCardId(),
                        transaction.isPaid(), transaction.getRepeat(), 1, transaction.getRepeatId());

                saveMultipleTransactions(isNewCard);
            }
        });
    }

    private void editSingleAddParcels(boolean isNewCard) {
        Log.d(LOG, "=> edit single add parcels");

        // add repeat id
        long repeatId = System.currentTimeMillis();
        transaction.setRepeatId(repeatId);
        logTransaction(transaction);

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            if (isNewCard) {
                Log.d(LOG, "isNewCard... get transaction with same card and month...");
                Transaction cardTransaction = transactionDao.getCardTransaction(
                        transaction.getCardId(), transaction.getMonth(), transaction.getYear());
                if (cardTransaction != null) {
                    Log.d(LOG, "isNewCard... found transaction => update account id");
                    transaction.setAccountId(cardTransaction.getAccountId());
                    transaction.setPaid(cardTransaction.isPaid());
                }
            }

            // edit first parcel
            transactionDao.update(transaction);

            // remove id from transaction obj (to avoid unique id error)
            transaction = new Transaction(
                    transaction.getType(), transaction.getDescription(), transaction.getAmount(),
                    transaction.getYear(), transaction.getMonth(), transaction.getDay(),
                    transaction.getCategoryId(), transaction.getAccountId(), transaction.getCardId(),
                    transaction.isPaid(), transaction.getRepeat(), 1, transaction.getRepeatId());

            // add new parcels
            int i = 1;
            int repeat = transaction.getRepeat();
            while (i < repeat) {
                // update next transaction month and year
                int[] nextDate = MyDateUtils.getNextTransactionDate(transaction.getMonth(), transaction.getYear());
                transaction.setMonth(nextDate[0]);
                transaction.setYear(nextDate[1]);
                transaction.setRepeatCount(i+1);

                // if not first parcel => isPaid = false
                transaction.setPaid(false);

                if (isNewCard) {
                    Log.d(LOG, "isNewCard... get transaction with same card and month...");
                    Transaction cardTransaction = transactionDao.getCardTransaction(
                            transaction.getCardId(), transaction.getMonth(), transaction.getYear());
                    if (cardTransaction != null) {
                        Log.d(LOG, "isNewCard... found transaction => update account id");
                        transaction.setAccountId(cardTransaction.getAccountId());
                        transaction.setPaid(cardTransaction.isPaid());
                    }
                }

                logTransaction(transaction);
                transactionDao.insert(transaction);

                i++;
            }
            handler.post(this::endSaveButtonProgress);
        });
    }

    private void setTransfer() {
        // Transfer don't allow credit card => set accountId and selected date

        long repeatId = System.currentTimeMillis();

        String descTransferTo = getString(R.string.label_transfer_to) + " " + transferAccountIn.getName();
        String descTransferFrom = getString(R.string.label_transfer_from) + " " + transferAccountOut.getName();

        Transaction in = new Transaction(2, descTransferFrom, transaction.getAmount(),
                selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay(),
                transaction.getCategoryId(), transferAccountIn.getId(), 0,
                true, 1, null, repeatId);

        Transaction out = new Transaction(2, descTransferTo, transaction.getAmount()*-1,
                selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay(),
                transaction.getCategoryId(), transferAccountOut.getId(), 0,
                true, 1, null, repeatId);

        logTransaction(in);
        logTransaction(out);

        TransactionDao transactionDao = db.TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            transactionDao.insert(out);
            transactionDao.insert(in);

            handler.post(() -> {
                Log.d(Tags.LOG_DB, "Transfer saved on db");
                mProgressBar.setVisibility(View.GONE);
                mSave.setText(getText(R.string.action_save));
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

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
