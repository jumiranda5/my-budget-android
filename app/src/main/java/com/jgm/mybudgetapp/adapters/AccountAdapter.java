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
import com.jgm.mybudgetapp.objects.Account;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Account> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public AccountAdapter(Context context, List<Account> mDataList) {
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

        Account account = mDataList.get(position);
        int iconId;
        switch (account.getType()) {
            case 0: iconId = R.drawable.ic_app_cash; break;
            case 2: iconId = R.drawable.ic_app_savings; break;
            default: iconId = R.drawable.ic_app_account_balance;
        }

        // Set Icon and color
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconId));
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.savings));

        // Set account name
        holder.mName.setText(account.getName());

        // Open account details
        holder.mContainer.setOnClickListener(v -> mInterface.openAccountDetails());

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName;
        private final ConstraintLayout mContainer;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_account_icon);
            mName = itemView.findViewById(R.id.item_account_name);
            mContainer = itemView.findViewById(R.id.account_item_container);

        }
    }

}