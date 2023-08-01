package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.DayGroupAdapter;
import com.jgm.mybudgetapp.databinding.FragmentTransactionsBinding;
import com.jgm.mybudgetapp.objects.DayGroup;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.TransactionDao;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;
import com.jgm.mybudgetapp.utils.TransactionsUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionsInFragment extends Fragment {

    public TransactionsInFragment() {
        // Required empty public constructor
    }

    private List<TransactionResponse> income;
    private float total = 0f;
    private float due = 0f;
    private float paid = 0f;

    // UI
    private FragmentTransactionsBinding binding;
    private TextView mTotal, mDue, mTotalOverline, mPaid;
    private RecyclerView mRecyclerView;

    private void setBinding() {
        mTotal = binding.transactionsTotal;
        mDue = binding.transactionsDue;
        mPaid = binding.transactionsPaid;
        mTotalOverline = binding.transactionsTitle;
        mRecyclerView = binding.transactionsList;
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
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
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
            getIncomeData(date.getMonth(), date.getYear());
        }

        mTotalOverline.setText(mContext.getString(R.string.label_income));
        mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));

    }

    /* ------------------------------------------------------------------------------
                                         LIST
    ------------------------------------------------------------------------------- */

    private void initRecyclerView(ArrayList<DayGroup> dayGroups) {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        DayGroupAdapter adapter = new DayGroupAdapter(mContext, dayGroups, 1);
        mRecyclerView.setAdapter(adapter);
    }

    /* ------------------------------------------------------------------------------
                                        INTERFACE
     ------------------------------------------------------------------------------- */

    public void updateOnTransactionDeleted(int id) {
        TransactionResponse transaction = income.stream()
                .filter(t -> id == t.getId())
                .findAny()
                .orElse(null);
        income.remove(transaction);

        if (transaction != null) setIncomeData(transaction.getMonth(), transaction.getYear());
    }

    public void getIncomeData(int month, int year) {

        TransactionDao transactionDao = AppDatabase.getDatabase(mContext).TransactionDao();
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            income = transactionDao.getTransactions(1, month, year);
            float prevTotal = transactionDao.getAccumulated(month, year);

            handler.post(() -> {
                if (prevTotal > 0) {
                    TransactionResponse accumulated = setAccumulated(prevTotal, month, year);
                    income.add(0, accumulated);
                }
                setIncomeData(month, year);
            });
        });
    }

    public void updateTotal(float value, boolean isPaid) {
        if (isPaid) {
            paid = paid + value;
            due = due - value;
        }
        else {
            paid = paid - value;
            due = due + value;
        }

        // set textViews
        TransactionsUtils.setTotalsTextViews(mContext, mTotal, mDue, mPaid, total, due, paid);
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
                0, 0, 0, true,
                1, 1, null,
                "",
                23,
                71);
    }

    private void setIncomeData(int month, int year) {

        ArrayList<DayGroup> dayGroups = new ArrayList<>();
        due = 0f;
        total = 0f;
        paid = 0f;

        for (int i = 0; i < income.size(); i++) {
            TransactionResponse transaction = income.get(i);
            int day = transaction.getDay();

            // set total
            total = total + transaction.getAmount();
            if (transaction.isPaid()) paid = paid + transaction.getAmount();
            else due = due + transaction.getAmount();

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
                int prevDay = income.get(i-1).getDay();
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
        TransactionsUtils.setTotalsTextViews(mContext, mTotal, mDue, mPaid, total, due, paid);

        // init list view
        initRecyclerView(dayGroups);

    }

    private DayGroup createDayGroup(TransactionResponse transaction, int month, int year) {
        int day = transaction.getDay();
        List<TransactionResponse> list = new ArrayList<>();
        list.add(transaction);
        return new DayGroup(day, month, year, list);
    }
}