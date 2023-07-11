package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class TransactionAdapter2 extends RecyclerView.Adapter<TransactionAdapter2.ListViewHolder> {

    private final Context mContext;
    private final List<TransactionResponse> mDataList;
    private final LayoutInflater layoutInflater;

    public TransactionAdapter2(Context context, List<TransactionResponse> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
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
        TransactionResponse item = mDataList.get(position);
        Icon icon = IconUtils.getIcon(item.getIconId());
        Color color = ColorUtils.getColor(item.getColorId());

        // Set icon
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        holder.mIcon.setContentDescription(icon.getIconName());
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

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
        if (item.getAmount() < 0) {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }

        // Set color for not paid
        if (!item.isPaid()) {
            holder.mName.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mTotal, mCurrencySymbol;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_transaction2_icon);
            mName = itemView.findViewById(R.id.item_transaction2_name);
            mTotal = itemView.findViewById(R.id.item_transaction2_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_transaction2_currency_symbol);

        }
    }
}
