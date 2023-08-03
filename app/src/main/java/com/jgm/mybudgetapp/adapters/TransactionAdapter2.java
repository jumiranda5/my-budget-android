package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class TransactionAdapter2 extends RecyclerView.Adapter<TransactionAdapter2.ListViewHolder> {

    private final Context mContext;
    private final List<TransactionResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public TransactionAdapter2(Context context, List<TransactionResponse> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TransactionAdapter2.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_transaction2, parent, false);
        return new TransactionAdapter2.ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter2.ListViewHolder holder, int position) {

        // Data
        TransactionResponse item = mDataList.get(position);
        boolean isAccumulated = item.getId() == 0;
        boolean isCardItem = item.getCardId() > 0;
        boolean isNotPaid = !item.isPaid();

        // Set description
        String description = item.getDescription();
        if (item.getRepeat() > 1)
            description = "(" + item.getRepeatCount() + "/" + item.getRepeat() + ") " + item.getDescription();
        holder.mName.setText(description);

        // Set amount
        String[] currency = NumberUtils.getCurrencyFormat(mContext, item.getAmount());
        holder.mCurrencySymbol.setText(currency[0]);
        holder.mTotal.setText(currency[1]);

        // Set color for income/expense
        if (item.getType() == 2) {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
        }
        else if (item.getAmount() < 0) {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }

        // Show icon if not paid or accumulated
        if (isAccumulated || isNotPaid) {
            holder.mIcon.setVisibility(View.VISIBLE);
            holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.high_emphasis_text));
        }

        // Set accumulated
        if (isAccumulated) {
            holder.mContainer.setClickable(false);
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_40_autorenew_fill0_300));
        }

        // set not paid
        if (isNotPaid) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_40_cancel_fill0_300));
            holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.disabled_text));
            holder.mName.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.disabled_text));
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mTotal, mCurrencySymbol;
        private final ConstraintLayout mContainer;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_transaction2_icon);
            mName = itemView.findViewById(R.id.item_transaction2_name);
            mContainer = itemView.findViewById(R.id.item_transaction2);
            mTotal = itemView.findViewById(R.id.item_transaction2_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_transaction2_currency_symbol);

        }
    }
}
