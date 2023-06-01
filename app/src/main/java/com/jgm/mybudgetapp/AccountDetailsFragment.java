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

import com.jgm.mybudgetapp.databinding.FragmentAccountDetailsBinding;
import com.jgm.mybudgetapp.databinding.FragmentCreditCardDetailsBinding;

public class AccountDetailsFragment extends Fragment {

    public AccountDetailsFragment() {
        // Required empty public constructor
    }

    // UI
    private FragmentAccountDetailsBinding binding;
    private ImageButton buttonBack, buttonArchive, buttonEdit;
    //private Button buttonTransactionDialog;

    private void setBinding() {
        buttonBack = binding.accountDetailsBackButton;
        buttonArchive = binding.accountDetailsArchiveButton;
        buttonEdit = binding.accountDetailsEditButton;
        //buttonTransactionDialog = binding.accountDetailsTransaction;
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
        binding = FragmentAccountDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String archiveMsg = "This will permanently archive this account. Would you like to continue?";

        buttonBack.setOnClickListener(v-> mInterface.navigateBack());
        buttonEdit.setOnClickListener(v-> mInterface.openAccountForm(true));
        buttonArchive.setOnClickListener(v-> mInterface.showConfirmationDialog(archiveMsg, 0));
        //buttonTransactionDialog.setOnClickListener(v-> mInterface.showTransactionDialog());
    }
}