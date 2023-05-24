package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jgm.mybudgetapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private static final String LOG_LIFECYCLE = "debug-lifecycle-home";

    // UI
    private FragmentHomeBinding binding;
    private ConstraintLayout cardIncome, cardExpenses, cardSavings,
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
        Log.d(LOG_LIFECYCLE, "Home onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_LIFECYCLE, "Home onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_LIFECYCLE, "Home onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_LIFECYCLE, "Home onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_LIFECYCLE, "Home onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_LIFECYCLE, "Home onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_LIFECYCLE, "Home onDestroy");
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