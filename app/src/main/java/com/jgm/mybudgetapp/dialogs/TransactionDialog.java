package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.jgm.mybudgetapp.utils.Tags;

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
        PaymentMethod paymentMethod = new PaymentMethod(0,0,"", 0, 0, 0);
        TextView method = view.findViewById(R.id.dialog_transaction_method);
        // todo: save as null when not used, instead of 0...
        if (transaction.getAccountId() != null && transaction.getAccountId() > 0) {
            AppDatabase.dbExecutor.execute(() -> {
                Account account = AppDatabase.getDatabase(mContext).AccountDao()
                        .getAccountById(transaction.getAccountId());
                method.setText(account.getName());
                paymentMethod.setId(account.getId());
                paymentMethod.setName(account.getName());
                paymentMethod.setType(account.getType());
                paymentMethod.setColorId(account.getColorId());
                paymentMethod.setIconId(account.getIconId());
            });
        }
        else if (transaction.getCardId() != null && transaction.getCardId() > 0) {
            AppDatabase.dbExecutor.execute(() -> {
                CreditCard creditCard = AppDatabase.getDatabase(mContext).CardDao()
                        .getCreditCardById(transaction.getCardId());
                method.setText(creditCard.getName());
                paymentMethod.setId(creditCard.getId());
                paymentMethod.setType(Tags.METHOD_CARD);
                paymentMethod.setName(creditCard.getName());
                paymentMethod.setColorId(creditCard.getColorId());
                paymentMethod.setIconId(Tags.CARD_ICON_ID);
                paymentMethod.setBillingDay(creditCard.getBillingDay());
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
            mInterface.openTransactionForm(true, transaction, paymentMethod);
            dismiss();
        });

        // Delete transaction
        Button deleteBtn = view.findViewById(R.id.dialog_transaction_delete);
        deleteBtn.setOnClickListener(v -> {
            Handler handler = new Handler(Looper.getMainLooper());
            AppDatabase.dbExecutor.execute(() -> {
                int res = AppDatabase.getDatabase(mContext).TransactionDao().deleteById(transaction.getId());
                handler.post(() -> {
                    Log.d("debug-dialog", "delete response: " + res);
                    mInterface.handleTransactionDeleted(transaction.getId());
                    dismiss();
                });
            });
        });

        return view;
    }
}
