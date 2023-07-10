package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.databinding.FragmentAccountDetailsBinding;
import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.Transaction;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class AccountDetailsFragment extends Fragment {

    public AccountDetailsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-account";

    // Vars
    private int position;
    private AccountTotal accountTotal;
    private Account account;
    private MyDate date;
    private ArrayList<Transaction> accountTransactions = new ArrayList<>();

    // UI
    private FragmentAccountDetailsBinding binding;
    private ImageButton buttonBack, buttonEdit;
    private TextView mAccountName, mTotal;
    private ImageView mAccountIcon;

    private void setBinding() {
        buttonBack = binding.accountDetailsBackButton;
        buttonEdit = binding.accountDetailsEditButton;
        mAccountName = binding.accountDetailsTitle;
        mAccountIcon = binding.accountDetailsIcon;
        mTotal = binding.accountDetailsTotal;
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
        buttonEdit.setOnClickListener(v-> mInterface.openAccountForm(true, account, position));

    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setAccount(AccountTotal accountTotal, int position, MyDate date) {

        Log.d(LOG, "=> setAccount");

        this.position = position;
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

            List<Transaction> transactions = transactionDao.getAccountTransactions(
                    accountTotal.getId(), date.getYear(), date.getMonth());
            Log.d(LOG, "list size: " + transactions.size());

            handler.post(() -> {

                for (int i = 0; i < transactions.size(); i++) {
                    Transaction t = transactions.get(i);
                    Log.d(LOG, "Transaction data to save => " + "\n" +
                            "type: " + t.getType() + "\n" +
                            "description: " + t.getDescription() + "\n" +
                            "amount: " + t.getAmount() + "\n" +
                            "date: " + t.getDay() + "/" + t.getMonth() + "/" + t.getYear() + "\n" +
                            "category id: " + t.getCategoryId() + "\n" +
                            "account id: " + t.getAccountId() + "\n" +
                            "card id: " + t.getCardId() + "\n" +
                            "isPaid: " + t.isPaid() + "\n" +
                            "repeat: " + t.getRepeat() + " | " + "repeatCount: "
                            + t.getRepeatCount() + " | " + "repeat id: " + t.getRepeatId());
                }

                accountTransactions = (ArrayList<Transaction>) transactions;
                initRecyclerView();
            });

        });
    }

    private void initRecyclerView() {
        // todo...
    }
}