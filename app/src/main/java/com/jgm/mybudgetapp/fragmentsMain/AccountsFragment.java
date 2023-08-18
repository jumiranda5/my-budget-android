package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.adapters.AccountAdapter;
import com.jgm.mybudgetapp.databinding.FragmentAccountsBinding;
import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.entity.Account;

import java.util.ArrayList;
import java.util.Map;

public class AccountsFragment extends Fragment {

    public AccountsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-accounts";

    // List
    private ArrayList<AccountTotal> accountsList = new ArrayList<>();
    private AccountAdapter adapter;

    // UI
    private FragmentAccountsBinding binding;
    private RecyclerView mRecyclerView;
    //private ImageButton mAddAccount;

    private void setBinding() {
        mRecyclerView = binding.accountsList;
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
       binding = FragmentAccountsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initRecyclerView();
        MyDate date = mInterface.getDate();
        getAccountsData(date);
        //mAddAccount.setOnClickListener(v -> mInterface.openAccountForm(false, null, 0));

    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void updateUiAfterInsertion(Account account) {
        Log.d(LOG, "Update ui list after item inserted");
        AccountTotal newAccount = new AccountTotal(
                account.getId(),
                account.getName(),
                account.getColorId(),
                account.getIconId(),
                account.getType(),
                account.isActive(),
                0.0f
        );
        adapter.addItem(newAccount);
    }

    public void updateListAfterDelete(int pos) {
        Log.d(LOG, "Update ui list after item delete");
        adapter.deleteItem(pos);
    }

    public void updateListAfterEdit(int pos, Account editedAccount) {
        Log.d(LOG, "Update ui list after item edit");
        adapter.updateItem(pos, editedAccount);
    }

    public void updateListOnDateChange(MyDate selectedDate) {
        getAccountsData(selectedDate);
    }


    /* ===============================================================================
                                        DATABASE
     =============================================================================== */

    private void getAccountsData(MyDate date) {
        AppDatabase db = AppDatabase.getDatabase(mContext);
        AccountDao mAccountDao = db.AccountDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            Log.d(LOG, "get data");

            Map<Account, String> accountsTotals = mAccountDao.getAccountsWithTotals2(date.getMonth(), date.getYear());

            Log.d(LOG, "size: " + accountsTotals.size());

            handler.post(() -> {

                accountsList.clear();

                for (Account account : accountsTotals.keySet()) {

                    String stringTotal = accountsTotals.get(account);

                    Log.d(LOG, "key: " + account.getName() + " value: " + stringTotal);

                    float total = 0.0f;
                    if (stringTotal != null)
                        total = Float.parseFloat(stringTotal);

                    AccountTotal accountTotal = new AccountTotal(
                            account.getId(), account.getName(),
                            account.getColorId(), account.getIconId(),
                            account.getType(), account.isActive(), total
                    );

                    accountsList.add(accountTotal);
                }

                //adapter.notifyDataSetChanged();
                initRecyclerView();

                Log.d(LOG, "Done reading all accounts from db: " + accountsTotals.size());
            });
        });
    }


    /* ===============================================================================
                                         LIST
     =============================================================================== */

    private void initRecyclerView() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        adapter = new AccountAdapter(mContext, accountsList);
        mRecyclerView.setAdapter(adapter);
    }

}