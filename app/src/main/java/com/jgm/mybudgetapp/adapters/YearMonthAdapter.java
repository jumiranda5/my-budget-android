package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.MonthResponse;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class YearMonthAdapter extends RecyclerView.Adapter<YearMonthAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<MonthResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final int year;
    private final int type;


    public YearMonthAdapter(Context context, List<MonthResponse> mDataList, int year, int type) {
        this.mContext = context;
        this.mDataList = mDataList;
        layoutInflater = LayoutInflater.from(context);
        this.year = year;
        this.type = type;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_year_month, parent, false);
        return new YearMonthAdapter.ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

        MonthResponse data = mDataList.get(position);
        int month = data.getMonth();
        float income = data.getIncome();
        float expense = data.getExpenses();
        float balance = NumberUtils.roundFloat(income - expense);
        expense = expense  * -1;

        // Set month
        String monthName = MyDateUtils.getMonthName(mContext, month, year)[1];
        holder.mMonth.setText(monthName);

        // Set amount
        String[] total;
        switch (type) {
            case 0:
                total = NumberUtils.getCurrencyFormat(mContext, balance);
                setTotalColor(balance, holder.mTotal, holder.mCurrencySymbol);
                break;
            case 1:
                total = NumberUtils.getCurrencyFormat(mContext, income);
                setTotalColor(income, holder.mTotal, holder.mCurrencySymbol);
                break;
            default:
                total = NumberUtils.getCurrencyFormat(mContext, expense);
                setTotalColor(expense, holder.mTotal, holder.mCurrencySymbol);
        }
        holder.mCurrencySymbol.setText(total[0]);
        holder.mTotal.setText(total[1]);

    }

    private void setTotalColor(float value, TextView total, TextView currency) {
        if (value < 0) {
            total.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            currency.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else if (value == 0) {
            total.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
            currency.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
        }
        else {
            total.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            currency.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final TextView mMonth, mTotal, mCurrencySymbol;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mMonth = itemView.findViewById(R.id.item_year_month_name);
            mTotal = itemView.findViewById(R.id.item_year_month_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_year_month_currency_symbol);

        }
    }
}
