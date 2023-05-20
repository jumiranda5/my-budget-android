package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.jgm.mybudgetapp.databinding.FragmentCreditCardsBinding;

public class CreditCardsFragment extends Fragment {

    public CreditCardsFragment() {
        // Required empty public constructor
    }

    // UI
    private FragmentCreditCardsBinding binding;
    private ImageButton buttonBack;
    private Button buttonDetails;

    private void setBinding() {
        buttonBack = binding.cardsBackButton;
        buttonDetails = binding.cardDetailsButton;
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
        binding = FragmentCreditCardsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonDetails.setOnClickListener(v -> mInterface.openCardDetails());
        buttonBack.setOnClickListener(v -> mInterface.navigateBack());

    }
}