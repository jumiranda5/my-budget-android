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
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.objects.PendingListResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.List;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<PendingListResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;

    public PendingAdapter(Context context, List<PendingListResponse> mDataList) {
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

        PendingListResponse item = mDataList.get(position);

        // Set date
        int day = item.getDay();
        int month = item.getMonth();
        int year = item.getYear();
        String date = MyDateUtils.getFormattedFieldDate(mContext, year,month,day);
        holder.mDate.setText(date);

        // Set description
        holder.mName.setText(item.getDescription());

        // Set amount
        String[] currency = NumberUtils.getCurrencyFormat(mContext, item.getTotal());
        holder.mCurrencySymbol.setText(currency[0]);
        holder.mTotal.setText(currency[1]);

        // Set icon and color
        if (item.getType() == Tags.TYPE_IN) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.arrow_south_west_600));
            holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.income));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.income));
        }
        else {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.arrow_north_east_600));
            holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.expense));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
        }

        // Toggle paid
        holder.mPaid.setChecked(item.isPaid());
        AppDatabase db = AppDatabase.getDatabase(mContext);
        holder.mPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getAdapterPosition();
            if (item.getCardId() > 0) {
                holder.mPaid.setChecked(false); // set to true after method picker
                mInterface.showMethodPickerDialog(false, null, pos);
            }
            else {
                Handler handler = new Handler(Looper.getMainLooper());
                AppDatabase.dbExecutor.execute(() -> {
                    db.TransactionDao().updatePaid(item.getId(), isChecked);
                    handler.post(() -> {
                        item.setPaid(true);
                        notifyItemChanged(pos);
                        remove(pos);
                    });
                });
            }
        });

        // Container click listener
        holder.mContainer.setOnClickListener(v -> {
            mInterface.setSelectedToolbarDate(day, month, year);
            if (item.getType() == Tags.TYPE_IN) mInterface.openBottomNavFragment(Tags.transactionsInTag);
            else mInterface.openBottomNavFragment(Tags.transactionsOutTag);
        });
    }

    public void updateOnCardPaid(int position, PaymentMethod method) {
        AppDatabase db = AppDatabase.getDatabase(mContext);
        Handler handler = new Handler();
        AppDatabase.dbExecutor.execute(() -> {

            PendingListResponse item = mDataList.get(position);
            db.TransactionDao().updatePaidCard(
                    item.getCardId(),
                    true,
                    item.getMonth(),
                    item.getYear(),
                    method.getId());

            handler.post(() -> {
                item.setPaid(true);
                notifyItemChanged(position);
                // time to show the checked button
                Handler handler2 = new Handler();
                handler2.postDelayed(() -> remove(position), 150);
            });
        });
    }

    private void remove(int position) {
        Log.d("debug-list", "removing position: " + position);
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mTotal, mCurrencySymbol, mDate;
        private final ToggleButton mPaid;
        private final ConstraintLayout mContainer;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mContainer = itemView.findViewById(R.id.item_pending_container);
            mIcon = itemView.findViewById(R.id.item_pending_icon);
            mName = itemView.findViewById(R.id.item_pending_name);
            mTotal = itemView.findViewById(R.id.item_pending_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_pending_currency_symbol);
            mDate = itemView.findViewById(R.id.item_pending_date);
            mPaid = itemView.findViewById(R.id.item_pending_toggle);

        }
    }
}
