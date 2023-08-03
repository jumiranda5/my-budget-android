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

    // todo:
    //      | set credit card items

    public AccountDetailsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-account";

    // Vars
    private AccountTotal accountTotal;
    private Account account;
    private MyDate date;

    // UI
    private FragmentAccountDetailsBinding binding;
    private ImageButton buttonBack;
    private TextView mAccountName, mTotal;
    private ImageView mAccountIcon;
    private RecyclerView mRecyclerView;

    private void setBinding() {
        buttonBack = binding.accountBackButton;
        //buttonEdit = binding.accountEditButton;
        mAccountName = binding.accountDetailsTitle;
        mAccountIcon = binding.accountIcon;
        mTotal = binding.accountTotal;
        mRecyclerView = binding.accountDetailsList;
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

        initAccountInfo();
        getAccountTransactions(date);
        buttonBack.setOnClickListener(v-> mInterface.navigateBack());
        //buttonEdit.setOnClickListener(v-> mInterface.openAccountForm(true, account, position));

    }

    /* ------------------------------------------------------------------------------
                                         LIST
    ------------------------------------------------------------------------------- */

    private void initRecyclerView(ArrayList<DayGroup> dayGroups) {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        DayGroupAdapter adapter = new DayGroupAdapter(mContext, dayGroups, 2);
        mRecyclerView.setAdapter(adapter);
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setAccount(AccountTotal accountTotal, MyDate date) {

        Log.d(LOG, "=> setAccount");

        this.accountTotal = accountTotal;
        this.date = date;

        // Set account db object to send to edit from
        account = new Account(
                accountTotal.getName(),
                accountTotal.getColorId(),
                accountTotal.getIconId(),
                accountTotal.getType(),
                accountTotal.isActive()
        );

        account.setId(accountTotal.getId());
    }

    public void updateAccountOnMonthChange(MyDate date) {
        this.date = date;
        getAccountTransactions(date);
    }

    public void updateAccountAfterEdit(Account editedAccount) {
        Log.d(LOG, "=> updateAccountAfterEdit");
        account = editedAccount;
        initAccountInfo();
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    private void initAccountInfo() {
        Log.d(LOG, "=> initAccountInfo");

        // Account name
        mAccountName.setText(accountTotal.getName());

        // Account icon
        Color color = ColorUtils.getColor(accountTotal.getColorId());
        Icon icon = IconUtils.getIcon(accountTotal.getIconId());
        mAccountIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mAccountIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Account total
        mTotal.setText(NumberUtils.getCurrencyFormat(mContext, accountTotal.getTotal())[2]);
        if (accountTotal.getType() == 2)
            mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
        else if (accountTotal.getTotal() < 0)
            mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        else
            mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
    }

    private void getAccountTransactions(MyDate date) {

        Log.d(LOG, "=> getAccountTransactions");

        AppDatabase db = AppDatabase.getDatabase(mContext);
        TransactionDao transactionDao = db.TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            Log.d(LOG, "account id: " + account.getId());

            List<TransactionResponse> transactions = transactionDao.getAccountTransactions(
                    account.getId(), date.getMonth(), date.getYear());

            float prevTotal = transactionDao.getAccountAccumulated(
                    account.getId(), date.getMonth(), date.getYear());

            Log.d(LOG, "list size: " + transactions.size());

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

        ArrayList<DayGroup> dayGroups = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            TransactionResponse transaction = transactions.get(i);
            int day = transaction.getDay();

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

        // set account total
        initAccountInfo();

        // init list view
        initRecyclerView(dayGroups);

    }

    private DayGroup createDayGroup(TransactionResponse transaction, int month, int year) {
        int day = transaction.getDay();
        List<TransactionResponse> list = new ArrayList<>();
        list.add(transaction);
        return new DayGroup(day, month, year, list);
    }
}