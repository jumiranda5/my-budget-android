package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jgm.mybudgetapp.adapters.AccountAdapter;
import com.jgm.mybudgetapp.databinding.FragmentAccountsBinding;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

import java.util.ArrayList;
import java.util.List;

public class AccountsFragment extends Fragment {

    public AccountsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-accounts";

    // List
    private ArrayList<Account> accountsList = new ArrayList<>();
    private AccountAdapter adapter;

    // UI
    private FragmentAccountsBinding binding;
    private RecyclerView mRecyclerView;
    private Button mAddAccount;

    private void setBinding() {
        mRecyclerView = binding.accountsList;
        mAddAccount = binding.buttonAddAccount;
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

        mInterface.getAccountsData();
        mAddAccount.setOnClickListener(v -> mInterface.openAccountForm(false, null, 0));
        initRecyclerView();

    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void updateListAfterDbRead(List<Account> dbAccounts) {
        Log.d(LOG, "Update ui list: " + dbAccounts.size());
        if (dbAccounts.size() > 0) {
            accountsList = (ArrayList<Account>) dbAccounts;
            initRecyclerView();
        }
    }

    public void updateUiAfterInsertion(Account account) {
        adapter.addItem(account);
    }

    public void updateListAfterDelete(int pos) {
        Log.d(LOG, "Update ui list after item delete");
        adapter.deleteItem(pos);
    }

    public void updateListAfterEdit(int pos, Account editedAccount) {
        Log.d(LOG, "Update ui list after item edit");
        adapter.updateItem(pos, editedAccount);
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

    private void setDefaultList() {
        boolean hasInitialAccounts = SettingsPrefs.getSettingsPrefsBoolean(mContext, "hasInitialAccounts");

        if (!hasInitialAccounts) {

            Log.d(LOG, "== INIT ACCOUNT DEFAULT LIST");

            ArrayList<Account> list = new ArrayList<>();

            Account cash = new Account(getString(R.string.account_cash), 21, 67, 0, true);
            Account checking = new Account(getString(R.string.account_checking), 14, 68, 1, true);
            Account savings = new Account(getString(R.string.account_savings), 20, 69, 2, true);

            list.add(cash);
            list.add(checking);
            list.add(savings);

            for (int i = 0; i < list.size(); i++) {
                mInterface.insertAccountData(list.get(i));
            }

            SettingsPrefs.setSettingsPrefsBoolean(mContext, "hasInitialAccounts", true);

        }
    }

}