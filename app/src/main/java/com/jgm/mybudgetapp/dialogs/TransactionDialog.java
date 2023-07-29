package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

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
        Icon icon = IconUtils.getIcon(transaction.getIconId());
        Color color = ColorUtils.getColor(transaction.getColorId());

        // Title and icons
        TextView title = view.findViewById(R.id.dialog_transaction_title);
        ImageView descIcon = view.findViewById(R.id.dialog_transaction_desc_icon);
        ImageView amountIcon = view.findViewById(R.id.dialog_transaction_amount_icon);
        ImageView categoryIcon = view.findViewById(R.id.dialog_transaction_category_icon);
        ImageView methodIcon = view.findViewById(R.id.dialog_transaction_method_icon);
        ImageView paidIcon = view.findViewById(R.id.dialog_transaction_paid_icon);

        categoryIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        categoryIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        String type;
        if (transaction.getType() == -1) {
            type = "Expense";
            descIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.expense));
            amountIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.expense));
        }
        else {
            type = "Income";
            descIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.income));
            amountIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.income));
        }
        title.setText(type); // todo: add to strings file

        // Description
        TextView description = view.findViewById(R.id.dialog_transaction_desc);
        String descString = transaction.getDescription();
        if (transaction.getRepeat() > 1)
            descString = transaction.getDescription() + " (" + transaction.getRepeatCount() + "/" + transaction.getRepeat() + ")";
        description.setText(descString);

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

        if (transaction.getAccountId() != null && transaction.getAccountId() > 0 && transaction.getCardId() == 0) {
            AppDatabase.dbExecutor.execute(() -> {
                Account account = AppDatabase.getDatabase(mContext).AccountDao()
                        .getAccountById(transaction.getAccountId());
                method.setText(account.getName());
                paymentMethod.setId(account.getId());
                paymentMethod.setName(account.getName());
                paymentMethod.setType(account.getType());
                paymentMethod.setColorId(account.getColorId());
                paymentMethod.setIconId(account.getIconId());

                if (paymentMethod.getType() == 0)
                    methodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_40_cash_fill0_300));
                else if (paymentMethod.getType() == 1)
                    methodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_40_account_balance_fill0_300));
                else
                    methodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_40_savings_fill0_300));

                Color methodColor = ColorUtils.getColor(paymentMethod.getColorId());
                methodIcon.setImageTintList(ContextCompat.getColorStateList(mContext, methodColor.getColor()));
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
                methodIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_48_credit_card_300));
                Color methodColor = ColorUtils.getColor(paymentMethod.getColorId());
                methodIcon.setImageTintList(ContextCompat.getColorStateList(mContext, methodColor.getColor()));
            });
        }

        // is paid
        TextView paid = view.findViewById(R.id.dialog_transaction_paid);
        if (transaction.isPaid()) {
            paid.setText("Paid");
            paidIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.check_circle_color));
        }
        else paid.setText("Not paid");

        // if credit card => hide paid/not paid
        if (transaction.getCardId() > 0) {
            paid.setVisibility(View.GONE);
            paidIcon.setVisibility(View.GONE);
        }

        // Dismiss
        ImageButton dismissBtn = view.findViewById(R.id.dialog_transaction_dismiss);
        dismissBtn.setOnClickListener(v -> dismiss());

        // Open edit fragment
        ImageButton editBtn = view.findViewById(R.id.dialog_transaction_edit);
        editBtn.setOnClickListener(v -> {
            mInterface.openTransactionForm(transaction.getType(), true, transaction, paymentMethod);
            dismiss();
        });

        // Delete transaction
        Group textGroup = view.findViewById(R.id.group_transaction);
        Group deleteItemGroup = view.findViewById(R.id.group_delete_item);
        TextView deleteMessage = view.findViewById(R.id.progress_delete_text);
        ImageButton deleteBtn = view.findViewById(R.id.dialog_transaction_delete);

        Log.d("debug-repeat", "Transaction repeat id: " + transaction.getRepeatId());

        if (transaction.getRepeatId() != null) {
            deleteMessage.setText("Deleting all parcels...");
        }

        deleteBtn.setOnClickListener(v -> {

            // Show progressBar to indicate that the item is being deleted
            textGroup.setVisibility(View.INVISIBLE);
            deleteItemGroup.setVisibility(View.VISIBLE);

            Handler handler = new Handler(Looper.getMainLooper());
            AppDatabase.dbExecutor.execute(() -> {

                AppDatabase db = AppDatabase.getDatabase(mContext);

                if (transaction.getRepeatId() == null) {
                    Log.d(Tags.LOG_DB, "Transaction id to delete: " + transaction.getId());
                    db.TransactionDao().deleteById(transaction.getId());
                }
                else {
                    Log.d(Tags.LOG_DB, "Transactions repeatId to delete: " + transaction.getRepeatId());
                    db.TransactionDao().deleteByRepeatId(transaction.getRepeatId());
                }

                handler.postDelayed(() -> {
                    mInterface.handleTransactionDeleted(transaction.getId());
                    dismiss();
                }, 600);
            });
        });

        return view;
    }
}
