package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.jgm.mybudgetapp.adapters.AccountAdapter;
import com.jgm.mybudgetapp.databinding.FragmentAccountsBinding;
import com.jgm.mybudgetapp.objects.Account;

import java.util.ArrayList;

public class AccountsFragment extends Fragment {

    public AccountsFragment() {
        // Required empty public constructor
    }

    // List
    private ArrayList<Account> accountsList = new ArrayList<>();
    private AccountAdapter adapter;

    // UI
    private FragmentAccountsBinding binding;
    private ImageButton mBack;
    private RecyclerView mRecyclerView;
    private Button mAddAccount;

    private void setBinding() {
        mBack = binding.accountsBackButton;
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

        mAddAccount.setOnClickListener(v -> mInterface.openAccountForm(false));
        mBack.setOnClickListener(v -> mInterface.navigateBack());

        initDummyList();
        initAccountsList();

    }

    private void initAccountsList() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        adapter = new AccountAdapter(mContext, accountsList);
        mRecyclerView.setAdapter(adapter);
    }

    private void initDummyList() {
        accountsList.add(new Account(0, "Cash", 0, 0, 0, true));
        accountsList.add(new Account(1, "Bank 1", 0, 0, 1, true));
        accountsList.add(new Account(2, "Bank 2", 0, 0, 1, true));
        accountsList.add(new Account(3, "Savings", 0, 0, 2, true));
    }
}