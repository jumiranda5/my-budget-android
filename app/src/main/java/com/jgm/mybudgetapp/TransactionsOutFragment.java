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
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jgm.mybudgetapp.adapters.DayGroupAdapter;
import com.jgm.mybudgetapp.databinding.FragmentTransactionsOutBinding;
import com.jgm.mybudgetapp.objects.DayGroup;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.room.entity.Transaction;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class TransactionsOutFragment extends Fragment {

    public TransactionsOutFragment() {
        // Required empty public constructor
    }

    private static final String LOG_LIFECYCLE = "debug-lifecycle";
    private List<TransactionResponse> expenses;

    private float accumulated = 0.0f;

    // UI
    private FragmentTransactionsOutBinding binding;
    private FloatingActionButton mFab;
    private TextView mTotal, mAccumulated;
    private RecyclerView mRecyclerView;
    private ToggleButton mToggleAccumulated;

    private void setBinding() {
        mFab = binding.transactionOutAdd;
        mTotal = binding.outTotal;
        mRecyclerView = binding.outList;
        mAccumulated = binding.outAccumulatedTotal;
        mToggleAccumulated = binding.outAccumulatedToggle;
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
            Log.d(LOG_LIFECYCLE, "saved instance is null => init transactions data");
            mToggleAccumulated.setChecked(true);
            MyDate date = mInterface.getDate();
            getExpensesData(date.getMonth(), date.getYear());
        }

    }

    public void updateOnTransactionDeleted(int id) {
        TransactionResponse transaction = expenses.stream()
                .filter(t -> id == t.getId())
                .findAny()
                .orElse(null);
        expenses.remove(transaction);

        if (transaction != null) setExpensesData(transaction.getMonth(), transaction.getYear());
    }

    public void getExpensesData(int month, int year) {

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            expenses = transactionDao.getTransactions(-1, month, year);
            float prevTotal = transactionDao.getAccumulated(month, year);

            handler.post(() -> {
                accumulated = prevTotal;
                setExpensesData(month, year);
            });

        });

    }

    private void setExpensesData(int month, int year) {

        float total = 0f;
        ArrayList<DayGroup> dayGroups = new ArrayList<>();

        for (int i = 0; i < expenses.size(); i++) {
            TransactionResponse transaction = expenses.get(i);
            int day = transaction.getDay();

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

        // Add accumulated amount from previous months
        setAccumulated(total);
        total = total + accumulated;

        // Set total in currency format
        String totalCurrency = NumberUtils.getCurrencyFormat(mContext, total)[2];
        mTotal.setText(totalCurrency);

        // init list view
        initRecyclerView(dayGroups);
    }

    private void setAccumulated(float total) {

        float totalWithPrev = total + accumulated;
        String totalCurrency = NumberUtils.getCurrencyFormat(mContext, total)[2];
        String prevTotalCurrency = NumberUtils.getCurrencyFormat(mContext, accumulated)[2];
        String totalWithPrevCurrency = NumberUtils.getCurrencyFormat(mContext, totalWithPrev)[2];

        mAccumulated.setText(prevTotalCurrency);

        if (mToggleAccumulated.isChecked()) mTotal.setText(totalWithPrevCurrency);
        else mTotal.setText(totalCurrency);

        mToggleAccumulated.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) mTotal.setText(totalWithPrevCurrency);
            else mTotal.setText(totalCurrency);
        });

    }

    private DayGroup createDayGroup(TransactionResponse transaction, int month, int year) {
        int day = transaction.getDay();
        List<TransactionResponse> list = new ArrayList<>();
        list.add(transaction);
        return new DayGroup(day, month, year, list);
    }

    private void initRecyclerView(ArrayList<DayGroup> dayGroups) {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        DayGroupAdapter adapter = new DayGroupAdapter(mContext, dayGroups);
        mRecyclerView.setAdapter(adapter);
    }

}