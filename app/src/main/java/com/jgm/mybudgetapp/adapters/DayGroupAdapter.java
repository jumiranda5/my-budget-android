package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.DayGroup;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.utils.MyDateUtils;

import java.util.List;

public class DayGroupAdapter extends RecyclerView.Adapter<DayGroupAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<DayGroup> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public DayGroupAdapter(Context context, List<DayGroup> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
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
        String weekDay = MyDateUtils.getDayOfWeek(mContext, day, month, year);
        String weekDayWithNumber = weekDay + ", " + day;
        holder.mDay.setText(weekDayWithNumber);

        // Set list
        List<TransactionResponse> transactions = dayGroup.getTransactions();
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        holder.mRecyclerView.setLayoutManager(listLayoutManager);
        TransactionAdapter adapter = new TransactionAdapter(mContext, transactions);
        holder.mRecyclerView.setAdapter(adapter);

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
