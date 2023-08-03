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
import com.jgm.mybudgetapp.objects.AccountTotal;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<AccountTotal> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public AccountAdapter(Context context, List<AccountTotal> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_account, parent, false);
        return new GridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        AccountTotal account = mDataList.get(position);
        int colorId = account.getColorId();
        Color color = ColorUtils.getColor(colorId);
        int iconId;
        switch (account.getType()) {
            case 0: iconId = R.drawable.ic_40_cash_fill0_300; break;
            case 2: iconId = R.drawable.ic_40_savings_fill0_300; break;
            default: iconId = R.drawable.ic_40_account_balance_fill0_300;
        }

        // Set Icon and color
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconId));
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Set account name
        holder.mName.setText(account.getName());

        // Set total
        String[] currency = NumberUtils.getCurrencyFormat(mContext, account.getTotal());
        holder.mCurrencySymbol.setText(currency[0]);
        holder.mTotal.setText(currency[1]);

        // Set currency color
        if (account.getType() == 2) {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
        }
        else if (account.getTotal() < 0) {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }
        else {
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }

        // Open account details
        holder.mContainer.setOnClickListener(v -> mInterface.openAccountDetails(account));

    }

    public void addItem(AccountTotal account) {
        if (account != null) {
            mDataList.add(account);
            int pos = mDataList.size();
            notifyItemInserted(pos);
            notifyItemRangeInserted(pos, mDataList.size());
        }
    }

    public void updateItem(int pos, Account newData) {
        mDataList.get(pos).setName(newData.getName());
        mDataList.get(pos).setColorId(newData.getColorId());
        mDataList.get(pos).setIconId(newData.getIconId());
        mDataList.get(pos).setType(newData.getType());
        notifyItemChanged(pos);
    }

    public void deleteItem(int pos) {
        mDataList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, mDataList.size());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mTotal, mCurrencySymbol;
        private final ConstraintLayout mContainer;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_account_icon);
            mName = itemView.findViewById(R.id.item_account_name);
            mContainer = itemView.findViewById(R.id.account_item_container);
            mTotal = itemView.findViewById(R.id.item_account_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_account_currency_symbol);

        }
    }

}
