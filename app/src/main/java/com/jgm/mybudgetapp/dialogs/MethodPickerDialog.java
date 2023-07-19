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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.MethodPickerAdapter;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.AccountDao;
import com.jgm.mybudgetapp.room.dao.CardDao;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class MethodPickerDialog extends BottomSheetDialogFragment {

    public MethodPickerDialog() {}

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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_method_picker, container, false);

        // Dismiss
        ImageButton btnClose = view.findViewById(R.id.method_picker_close);
        btnClose.setOnClickListener(v -> dismiss());

        // List
        final RecyclerView recyclerView = view.findViewById(R.id.payment_method_list);
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(listLayoutManager);
        recyclerView.setHasFixedSize(true);

        boolean isExpenseMethodDialog = mInterface.getMethodDialogType();

        // Create a payment methods list
        ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            AccountDao accountDao = AppDatabase.getDatabase(mContext).AccountDao();
            CardDao cardDao = AppDatabase.getDatabase(mContext).CardDao();

            // get accounts and credit cards from db
            // Using the main thread to be able to use the return statement
            List<Account> accountsList = accountDao.getAccounts();
            List<CreditCard> cardsList = cardDao.getCreditCards();

            handler.post(() -> {
                for (int i = 0; i < accountsList.size(); i++) {
                    Account account = accountsList.get(i);
                    PaymentMethod paymentMethod = new PaymentMethod(
                            account.getId(),
                            account.getType(),
                            account.getName(),
                            account.getColorId(),
                            account.getIconId(),
                            0);
                    paymentMethods.add(paymentMethod);
                }

                if (isExpenseMethodDialog) {
                    for (int i = 0; i < cardsList.size(); i++) {
                        CreditCard card = cardsList.get(i);
                        PaymentMethod paymentMethod = new PaymentMethod(
                                card.getId(),
                                Tags.METHOD_CARD,
                                card.getName(),
                                card.getColorId(),
                                Tags.CARD_ICON_ID,
                                card.getBillingDay());
                        paymentMethods.add(paymentMethod);
                    }
                }

                MethodPickerAdapter adapter = new MethodPickerAdapter(mContext, paymentMethods, MethodPickerDialog.this);
                recyclerView.setAdapter(adapter);

                Log.d(Tags.LOG_DB, "Methods list size: " + paymentMethods.size());
            });
        });

        return view;
    }
}
