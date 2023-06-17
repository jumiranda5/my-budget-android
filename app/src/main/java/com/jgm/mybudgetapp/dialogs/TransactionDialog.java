package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionDialog extends BottomSheetDialogFragment {

    public TransactionDialog() {}

    private MainInterface mInterface;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInterface = (MainInterface) context;
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_transaction, container, false);

        // get data from main activity
        TransactionResponse transaction = mInterface.getSelectedTransactionData();

        // Title
        TextView title = view.findViewById(R.id.dialog_transaction_title);
        String type;
        if (transaction.getType() == 1) type = "Expense";
        else type = "Income";
        title.setText(type); // todo: add to strings file

        // Description
        TextView description = view.findViewById(R.id.dialog_transaction_desc);
        description.setText(transaction.getDescription());

        // Amount
        TextView amount = view.findViewById(R.id.dialog_transaction_amount);
        String[] currency = NumberUtils.getCurrencyFormat(mContext, transaction.getAmount());
        amount.setText(currency[2]);

        // Category
        TextView categoryName = view.findViewById(R.id.dialog_transaction_category);
        categoryName.setText(transaction.getCategoryName());

        // Payment method
        TextView method = view.findViewById(R.id.dialog_transaction_method);
        // todo: save as null when not used, instead of 0...
        if (transaction.getAccountId() != null && transaction.getAccountId() > 0) {
            AppDatabase.dbReadExecutor.execute(() -> {
                Account account = AppDatabase.getDatabase(mContext).AccountDao()
                        .getAccountById(transaction.getAccountId());
                method.setText(account.getName());
            });
        }
        else if (transaction.getCardId() != null && transaction.getCardId() > 0) {
            AppDatabase.dbReadExecutor.execute(() -> {
                CreditCard creditCard = AppDatabase.getDatabase(mContext).CardDao()
                        .getCreditCardById(transaction.getCardId());
                method.setText(creditCard.getName());
            });
        }

        // is paid
        TextView paid = view.findViewById(R.id.dialog_transaction_paid);
        if (transaction.isPaid()) paid.setText("Paid");
        else paid.setText("Pending");

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
