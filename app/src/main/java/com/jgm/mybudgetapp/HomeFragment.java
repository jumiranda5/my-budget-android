package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jgm.mybudgetapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    // UI
    private FragmentHomeBinding binding;
    private CardView cardIncome, cardExpenses, cardSavings,
            cardCash, cardChecking, cardIncomeCategories,
            cardExpensesCategories, cardYear;

    private void bindViews() {
        cardIncome = binding.homeCardIncome;
        cardExpenses = binding.homeCardExpenses;
        cardSavings = binding.homeCardSavings;
        cardCash = binding.homeCardCash;
        cardChecking = binding.homeCardChecking;
        cardIncomeCategories = binding.homeCardIncomeCategories;
        cardExpensesCategories = binding.homeCardExpensesCategories;
        cardYear = binding.homeCardYear;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        bindViews();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initNavigation();

    }

    /* ===============================================================================
                                           NAVIGATION
     =============================================================================== */

    private void initNavigation() {
        cardIncome.setOnClickListener(v -> mInterface.openIncome());
        cardExpenses.setOnClickListener(v -> mInterface.openExpenses());
        cardSavings.setOnClickListener(v -> mInterface.openAccounts());
        cardCash.setOnClickListener(v -> mInterface.openAccounts());
        cardChecking.setOnClickListener(v -> mInterface.openAccounts());
        cardExpensesCategories.setOnClickListener(v -> mInterface.openExpensesCategories());
        cardIncomeCategories.setOnClickListener(v -> mInterface.openIncomeCategories());
        cardYear.setOnClickListener(v -> mInterface.openYear());
    }


}