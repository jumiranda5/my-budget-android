package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<TransactionResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public TransactionAdapter(Context context, List<TransactionResponse> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_transaction, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TransactionResponse transaction = mDataList.get(position);
        Icon icon = IconUtils.getIcon(transaction.getIconId());
        Color color = ColorUtils.getColor(transaction.getColorId());

        Log.d("debug-transaction", "Item: " + transaction.getId() + " | " + transaction.getDescription() +
                " | " + transaction.getCardId());

        // Set icon
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        holder.mIcon.setContentDescription(icon.getIconName());
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Set description
        holder.mName.setText(transaction.getDescription());

        // Set amount
        String[] currency = NumberUtils.getCurrencyFormat(mContext, transaction.getAmount());
        holder.mCurrencySymbol.setText(currency[0]);
        holder.mTotal.setText(currency[1]);

        // Toggle paid
        if (transaction.isPaid()) holder.mPaid.setChecked(true);
        else {
            holder.mPaid.setChecked(true);
            MyDate today = MyDateUtils.getCurrentDate(mContext);
            if (transaction.getDay() < today.getDay() &&
                    transaction.getMonth() <= today.getMonth() &&
                    transaction.getYear() <= today.getYear()) {
                holder.mPaid.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_toggle_paid_pending));
            }
        }

        AppDatabase db = AppDatabase.getDatabase(mContext);
        holder.mPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppDatabase.dbExecutor.execute(() -> {
                    db.TransactionDao().updatePaid(transaction.getId(), true);
                });
            }
            else {
                AppDatabase.dbExecutor.execute(() -> {
                    db.TransactionDao().updatePaid(transaction.getId(), false);
                });
            }
        });

        // Set accumulated
        boolean isAccumulated = transaction.getId() == 0;
        if (isAccumulated) {
            holder.mPaid.setVisibility(View.GONE);
            holder.mName.setTextColor(ContextCompat.getColor(mContext, color.getColor()));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, color.getColor()));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, color.getColor()));
        }

        // Set credit card item
        if (transaction.getId() != -1 && transaction.getCardId() > 0) {
            holder.mPaid.setVisibility(View.GONE);
            holder.mCardIcon.setVisibility(View.VISIBLE);
            holder.mCardSpace.setVisibility(View.VISIBLE);
            holder.mName.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
        }

        // Open transaction details dialog
        if (!isAccumulated)
            holder.mContainer.setOnClickListener(v -> mInterface.showTransactionDialog(transaction));

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon, mCardIcon, mCardSpace;
        private final TextView mName, mTotal, mCurrencySymbol;
        private final CardView mContainer;
        private final ToggleButton mPaid;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_transaction_icon);
            mName = itemView.findViewById(R.id.item_transaction_name);
            mContainer = itemView.findViewById(R.id.item_transaction);
            mTotal = itemView.findViewById(R.id.item_transaction_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_transaction_currency_symbol);
            mPaid = itemView.findViewById(R.id.item_transaction_toggle);
            mCardIcon = itemView.findViewById(R.id.item_transaction_card_icon);
            mCardSpace = itemView.findViewById(R.id.empty_view);

        }
    }
}
