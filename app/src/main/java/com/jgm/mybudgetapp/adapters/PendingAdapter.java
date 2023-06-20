package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.jgm.mybudgetapp.utils.Tags;

import java.util.List;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<TransactionResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public PendingAdapter(Context context, List<TransactionResponse> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_pending, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TransactionResponse transaction = mDataList.get(position);
        Icon icon = IconUtils.getIcon(transaction.getIconId());
        Color color = ColorUtils.getColor(transaction.getColorId());

        // Set date
        int day = transaction.getDay();
        int month = transaction.getMonth();
        int year = transaction.getYear();
        String date = MyDateUtils.getFormattedFieldDate(mContext, year,month,day);
        holder.mDate.setText(date);

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

        if (transaction.getType() == Tags.TYPE_IN) {
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }
        else {
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }

        // Toggle paid
        holder.mPaid.setChecked(false);
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

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mTotal, mCurrencySymbol, mDate;
        private final ToggleButton mPaid;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_pending_icon);
            mName = itemView.findViewById(R.id.item_pending_name);
            mTotal = itemView.findViewById(R.id.item_pending_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_pending_currency_symbol);
            mDate = itemView.findViewById(R.id.item_pending_date);
            mPaid = itemView.findViewById(R.id.item_pending_toggle);

        }
    }
}
