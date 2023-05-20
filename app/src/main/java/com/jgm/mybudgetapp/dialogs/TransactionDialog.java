package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;

public class TransactionDialog extends DialogFragment {

    public TransactionDialog() {}

    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInterface = (MainInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_transaction, container, false);

        // Dismiss
        ImageButton dismissBtn = view.findViewById(R.id.dialog_transaction_dismiss);
        dismissBtn.setOnClickListener(v -> dismiss());

        // Open edit fragment
        Button editBtn = view.findViewById(R.id.dialog_transaction_edit);
        editBtn.setOnClickListener(v -> {
            mInterface.openTransactionForm();
            dismiss();
        });

        // Delete transaction
        Button deleteBtn = view.findViewById(R.id.dialog_transaction_delete);
        deleteBtn.setOnClickListener(v -> {
            // todo: delete on database
            dismiss();
        });

        return view;
    }
}
