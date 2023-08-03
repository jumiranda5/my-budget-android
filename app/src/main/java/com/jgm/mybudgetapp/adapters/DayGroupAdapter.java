package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.DayGroup;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.utils.MyDateUtils;

import java.util.List;

public class DayGroupAdapter extends RecyclerView.Adapter<DayGroupAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<DayGroup> mDataList;
    private final LayoutInflater layoutInflater;
    private final int type;

    public DayGroupAdapter(Context context, List<DayGroup> mDataList, int type) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.type = type;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_transaction_day, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

        DayGroup dayGroup = mDataList.get(position);

        // Set day title
        int day = dayGroup.getDay();
        int month = dayGroup.getMonth();
        int year = dayGroup.getYear();
        try {
            String weekDay = MyDateUtils.getDayOfWeek(mContext, day, month, year)[1];
            String weekDayWithNumber = weekDay + ", " + day;
            holder.mDay.setText(weekDayWithNumber);
        }
        catch (Exception e) {
            Log.e("debug-day-item", "wrong date: day = " + day + " month = " + month + " year = " + year);
        }

        // Set list
        List<TransactionResponse> transactions = dayGroup.getTransactions();
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        holder.mRecyclerView.setLayoutManager(listLayoutManager);
        if (type == 1) {
            TransactionAdapter adapter = new TransactionAdapter(mContext, transactions, position);
            holder.mRecyclerView.setAdapter(adapter);
        }
        else {
            TransactionAdapter2 adapter = new TransactionAdapter2(mContext, transactions);
            holder.mRecyclerView.setAdapter(adapter);
        }
    }

    public void updateCreditCardItemsPaidStatus(int cardId, int position) {

        List<TransactionResponse> list = mDataList.get(position).getTransactions();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCardId() == cardId) list.get(i).setPaid(true);
        }

        notifyItemChanged(position);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final TextView mDay;
        private final RecyclerView mRecyclerView;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mDay = itemView.findViewById(R.id.item_day_title);
            mRecyclerView = itemView.findViewById(R.id.item_day_list);

        }
    }
}
