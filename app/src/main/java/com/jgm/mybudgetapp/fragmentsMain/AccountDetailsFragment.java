package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.DayGroupAdapter;
import com.jgm.mybudgetapp.databinding.FragmentAccountDetailsBinding;
import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.DayGroup;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;
import com.jgm.mybudgetapp.utils.TransactionsUtils;

import java.util.ArrayList;
import java.util.List;

public class AccountDetailsFragment extends Fragment {

    public AccountDetailsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-account";
    private static final String STATE_ID = "id";

    // Vars
    private int id;
    private AccountTotal accountTotal;
    private MyDate date;
    private TransactionDao transactionDao;
    private DayGroupAdapter adapter;
    private final ArrayList<DayGroup> dayGroups = new ArrayList<>();

    // UI
    private FragmentAccountDetailsBinding binding;
    private ImageButton buttonBack;
    private TextView mAccountName, mTotal;
    private ImageView mAccountIcon;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    private void setBinding() {
        buttonBack = binding.accountBackButton;
        mAccountName = binding.accountDetailsTitle;
        mAccountIcon = binding.accountIcon;
        mTotal = binding.accountTotal;
        mRecyclerView = binding.accountDetailsList;
        mFab = binding.acountEditButton;
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
        Log.d(Tags.LOG_LIFECYCLE, "Accounts details onCreateView");
        binding = FragmentAccountDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(Tags.LOG_LIFECYCLE, "Accounts details onViewCreated");

        // init listview
        initRecyclerView();

        // init db
        AppDatabase db = AppDatabase.getDatabase(mContext);
        transactionDao = db.TransactionDao();

        if (savedInstanceState != null) {
            id = savedInstanceState.getInt(STATE_ID);
            Log.d(LOG, "savedInstanceState id = " + id);
            date = mInterface.getDate();
            getAccountById();
        }
        else {
            initAccountInfo();
            getAccountTransactions();
        }

        buttonBack.setOnClickListener(v-> mInterface.navigateBack());

        mFab.setOnClickListener(v -> {
            // Set account db object to send to edit from
            Account accountToEdit = new Account(
                    accountTotal.getName(),
                    accountTotal.getColorId(),
                    accountTotal.getIconId(),
                    accountTotal.getType(),
                    accountTotal.isActive()
            );

            accountToEdit.setId(accountTotal.getId());

            mInterface.openAccountForm(true, accountToEdit);
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(Tags.LOG_LIFECYCLE, "Account Details fragment onSaveInstanceState");
        outState.putInt(STATE_ID, id);
    }

    /* ===============================================================================
                                         LIST
    =============================================================================== */

    private void initRecyclerView() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        adapter = new DayGroupAdapter(mContext, dayGroups, 2);
        mRecyclerView.setAdapter(adapter);
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setAccount(AccountTotal accountTotal, MyDate date) {
        Log.d(LOG, "=> setAccount");
        this.accountTotal = accountTotal;
        this.date = date;
    }

    public void updateAccountOnMonthChange(MyDate date) {
        Log.d(LOG, "=> updateAccountOnMonthChange");
        // don't update (here) on screen rotation
        if (accountTotal != null) {
            this.date = date;
            getAccountTotal();
            getAccountTransactions();
        }
    }

    public void updateAccountAfterEdit(Account editedAccount) {
        Log.d(LOG, "=> updateAccountAfterEdit");
        float total = accountTotal.getTotal();
        accountTotal = new AccountTotal(editedAccount.getId(), editedAccount.getName(),
                editedAccount.getColorId(), editedAccount.getIconId(), editedAccount.getType(),
                editedAccount.isActive(), total);
        initAccountInfo();
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    private void setTotalColor(float value) {
        if (accountTotal.getType() == 2) mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
        else if (value < 0) mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        else mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
    }

    private void initAccountInfo() {
        Log.d(LOG, "=> initAccountInfo");

        // set id
        id = accountTotal.getId();

        // Account name
        mAccountName.setText(accountTotal.getName());

        // Account icon
        Color color = ColorUtils.getColor(accountTotal.getColorId());
        Icon icon = IconUtils.getIcon(accountTotal.getIconId());
        mAccountIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mAccountIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Account total
        mTotal.setText(NumberUtils.getCurrencyFormat(mContext, accountTotal.getTotal())[2]);
        setTotalColor(accountTotal.getTotal());
    }

    private void getAccountById() {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(mContext);
            AccountDao accountDao = db.AccountDao();
            Account account = accountDao.getAccountById(id);

            handler.post(() -> {
                accountTotal = new AccountTotal(
                        id,
                        account.getName(),
                        account.getColorId(),
                        account.getIconId(),
                        account.getType(),
                        account.isActive(),
                        0f);

                initAccountInfo();
                getAccountTotal();
                getAccountTransactions();
            });

        });
    }

    private void getAccountTotal() {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            float total = transactionDao.getAccountTotal(accountTotal.getId(), date.getMonth(), date.getYear());
            Log.d(LOG, "total = " + total);

            handler.post(() -> {
                accountTotal.setTotal(total);
                String totalFormatted = NumberUtils.getCurrencyFormat(mContext, total)[2];
                Log.d(LOG, "total formatted = " + totalFormatted);
                mTotal.setText(totalFormatted);
                setTotalColor(total);
            });
        });
    }

    private void getAccountTransactions() {

        Log.d(LOG, "=> getAccountTransactions");

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            Log.d(LOG, "account id: " + accountTotal.getId());

            List<TransactionResponse> transactions = transactionDao.getAccountTransactions2(
                    accountTotal.getId(), date.getMonth(), date.getYear());

            float prevTotal = transactionDao.getAccountAccumulated(
                    accountTotal.getId(), date.getMonth(), date.getYear());

            handler.post(() -> {
                // set accumulated value
                TransactionResponse accumulated = TransactionsUtils.setAccumulated(
                        mContext, prevTotal, date.getMonth(), date.getYear());
                transactions.add(0, accumulated);
                // set list data with day groups
                setListData(transactions, date.getMonth(), date.getYear());
            });

        });
    }

    private void setListData(List<TransactionResponse> transactions, int month, int year) {

        dayGroups.clear();

        for (int i = 0; i < transactions.size(); i++) {
            TransactionResponse transaction = transactions.get(i);
            int day = transaction.getDay();
            Log.d(LOG, "transaction day: " + day + " | " + transaction.getDescription());

            // set transactions grouped by day
            // if first item => create new dayGroup obj and add to dayGroups list
            if (i == 0) {
                DayGroup dayGroup = createDayGroup(transaction, month, year);
                dayGroups.add(dayGroup);
            }
            // else => check if same day and update list
            else {
                int prevDay = transactions.get(i-1).getDay();
                if (day == prevDay) {
                    int dayGroupIndex = dayGroups.size() - 1;
                    dayGroups.get(dayGroupIndex).getTransactions().add(transaction);
                }
                else {
                    DayGroup newDayGroup = createDayGroup(transaction, month, year);
                    dayGroups.add(newDayGroup);
                }
            }
        }

        // set listview data
        adapter.notifyDataSetChanged();

    }

    private DayGroup createDayGroup(TransactionResponse transaction, int month, int year) {
        int day = transaction.getDay();
        List<TransactionResponse> list = new ArrayList<>();
        list.add(transaction);
        return new DayGroup(day, month, year, list);
    }
}