package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jgm.mybudgetapp.adapters.DayGroupAdapter;
import com.jgm.mybudgetapp.databinding.FragmentTransactionsOutBinding;
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.objects.DayGroup;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class TransactionsOutFragment extends Fragment {

    public TransactionsOutFragment() {
        // Required empty public constructor
    }

    private List<TransactionResponse> expenses;

    // UI
    private FragmentTransactionsOutBinding binding;
    //private FloatingActionButton mFab;
    private TextView mTotal;
    private RecyclerView mRecyclerView;
    DayGroupAdapter adapter;

    private void setBinding() {
        //mFab = binding.transactionOutAdd;
        mTotal = binding.outTotal;
        mRecyclerView = binding.outList;
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
        binding = FragmentTransactionsOutBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            Log.d(Tags.LOG_LIFECYCLE, "saved instance is null => init transactions data");
            MyDate date = mInterface.getDate();
            getExpensesData(date.getMonth(), date.getYear());
        }

    }

    /* ------------------------------------------------------------------------------
                                         LIST
    ------------------------------------------------------------------------------- */
    private void initRecyclerView(ArrayList<DayGroup> dayGroups) {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        adapter = new DayGroupAdapter(mContext, dayGroups, 1);
        mRecyclerView.setAdapter(adapter);
    }

    /* ------------------------------------------------------------------------------
                                        INTERFACE
     ------------------------------------------------------------------------------- */

    public void updateOnTransactionDeleted(int id) {
        TransactionResponse transaction = expenses.stream()
                .filter(t -> id == t.getId())
                .findAny()
                .orElse(null);
        expenses.remove(transaction);

        if (transaction != null) setExpensesData(transaction.getMonth(), transaction.getYear());
    }

    public void updateOnCreditCardPaid(TransactionResponse item, PaymentMethod paymentMethod, int position) {
        AppDatabase db = AppDatabase.getDatabase(mContext);
        Handler handler = new Handler();
        AppDatabase.dbExecutor.execute(() -> {
            db.TransactionDao().updatePaidCard(
                    item.getCardId(),
                    true,
                    item.getMonth(),
                    item.getYear(),
                    paymentMethod.getId());
            handler.post(() -> {
                expenses.get(position).setPaid(true);
                setExpensesData(item.getMonth(), item.getYear());
            });
        });
    }

    public void getExpensesData(int month, int year) {

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            expenses = transactionDao.getTransactions(-1, month, year);
            float prevTotal = transactionDao.getAccumulated(month, year);

            handler.post(() -> {
                if (prevTotal < 0) {
                    TransactionResponse accumulated = setAccumulated(prevTotal, month, year);
                    expenses.add(0, accumulated);
                }
                setExpensesData(month, year);
            });

        });

    }

    /* ------------------------------------------------------------------------------
                                         SET DATA
    ------------------------------------------------------------------------------- */

    private TransactionResponse setAccumulated(float value, int month, int year) {
        return new TransactionResponse(0,
                Tags.TYPE_OUT,
                getString(R.string.label_accumulated),
                value,
                year, month, 1,
                0, 0, 0, false,
                1, 1, null,
                "",
                23,
                71);
    }

    private void setExpensesData(int month, int year) {

        float total = 0f;
        ArrayList<DayGroup> dayGroups = new ArrayList<>();
        boolean hasCreditCard = false;

        for (int i = 0; i < expenses.size(); i++) {
            TransactionResponse transaction = expenses.get(i);
            int day = transaction.getDay();

            // check if payment method credit card is present
            if (transaction.getCardId() > 0) hasCreditCard = true;

            // set total
            total = total + expenses.get(i).getAmount();
            Log.d(Tags.LOG_DB, transaction.getDescription() + " = " + transaction.getId() + "/" +
                    transaction.getAmount() + "/day: " + day);

            // set transactions grouped by day
            // if first item => create new dayGroup obj and add to dayGroups list
            if (i == 0) {
                DayGroup dayGroup = createDayGroup(transaction, month, year);
                dayGroups.add(dayGroup);
            }
            // else => check if same day and update list
            else {
                int prevDay = expenses.get(i-1).getDay();
                if (day == prevDay) {
                    int dayGroupIndex = dayGroups.size() - 1;
                    dayGroups.get(dayGroupIndex).getTransactions().add(transaction);
                }
                else {
                    DayGroup newDayGroup = createDayGroup(transaction, month, year);
                    dayGroups.add(newDayGroup);
                }
            }
        }

        // Set total in currency format
        String totalCurrency = NumberUtils.getCurrencyFormat(mContext, total)[2];
        mTotal.setText(totalCurrency);

        // handle credit card items and init list view
        if (hasCreditCard) setCreditCardItems(dayGroups);
        else initRecyclerView(dayGroups);

    }

    private DayGroup createDayGroup(TransactionResponse transaction, int month, int year) {
        int day = transaction.getDay();
        List<TransactionResponse> list = new ArrayList<>();
        list.add(transaction);
        return new DayGroup(day, month, year, list);
    }

    /* ------------------------------------------------------------------------------
                                    SET CREDIT CARD ITEMS
    ------------------------------------------------------------------------------- */

    private void setCreditCardItems(ArrayList<DayGroup> dayGroups) {

        // loop through days lists to add credit card item
        for (int i = 0; i < dayGroups.size(); i++) {
            List<TransactionResponse> list = dayGroups.get(i).getTransactions();
            int day = dayGroups.get(i).getDay();
            int month = dayGroups.get(i).getMonth();
            int year = dayGroups.get(i).getYear();

            // Get card id(s)
            ArrayList<Integer> cardIds = new ArrayList<>();
            ArrayList<Boolean> isPaid = new ArrayList<>();
            int listPosition = 0;
            boolean hasFirstItemPos = false;
            for (int y = 0; y < list.size(); y++) {
                int cardId = list.get(y).getCardId();
                if (cardId != 0 && !cardIds.contains(cardId)) {
                    cardIds.add(cardId);
                    isPaid.add(list.get(y).isPaid());
                    if (!hasFirstItemPos) {
                        listPosition = y;
                        hasFirstItemPos = true;
                    }
                }
            }

            // Get card data and update day list
            if (cardIds.size() == 1) addSingleCard(dayGroups, i, listPosition,
                    cardIds.get(0), day, month, year, isPaid.get(0));
            else {
                // probably rare usage... but terrible implementation... find a better solution...
                for (int x = 0; x < cardIds.size(); x++) {
                    addSingleCard(dayGroups, i, listPosition, cardIds.get(x), day, month, year, isPaid.get(x));
                }
            }

        }

    }

    private void addSingleCard(ArrayList<DayGroup> dayGroups, int dayPos, int listPos,
                               int id, int day, int month, int year, boolean isPaid) {
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();
            Card card = transactionDao.getCreditCardWithTotal(id, day, month, year);
            handler.post(() -> {
                TransactionResponse transaction = new TransactionResponse(
                        -1,
                        Tags.TYPE_OUT,
                        card.getName(),
                        card.getTotal(),
                        year, month, day,
                        0, 0,
                        card.getId(),
                        isPaid,
                        1, null, null,
                        null,
                        card.getColorId(),
                        Tags.cardIconId);

                dayGroups.get(dayPos).getTransactions().add(listPos, transaction);

                // init list
                initRecyclerView(dayGroups);
            });

        });
    }


}