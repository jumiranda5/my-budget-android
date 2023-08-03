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
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.MyDateUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<TransactionResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;
    private final int dayPosition;

    public TransactionAdapter(Context context, List<TransactionResponse> mDataList, int dayPosition) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        this.dayPosition = dayPosition;
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

        TransactionResponse item = mDataList.get(position);
        Color color = ColorUtils.getColor(item.getColorId());
        MyDate today = MyDateUtils.getCurrentDate(mContext);

        boolean isCardTotal = item.getId() == -1;
        boolean isAccumulated = item.getId() == 0;
        boolean isPendingMonth = item.getYear() <= today.getYear() && item.getMonth() <= today.getMonth();
        boolean isPendingDay = (item.getMonth() == today.getMonth() && item.getDay() <= today.getDay())
                || item.getMonth() < today.getMonth() && item.getDay() <= 31;
        boolean isPending = isPendingMonth && isPendingDay && !item.isPaid();

        Log.d("debug-item", "category: " + item.getCategoryId()
                + " " + item.getCategoryName()
                + "/ " + item.getDescription()
                + "/ account id: " + item.getAccountId()
                + "/ card id: " + item.getCardId()
                + "/ pending: " + isPending);

        // Set description
        String description = item.getDescription();
        if (item.getRepeat() > 1)
            description = "(" + item.getRepeatCount() + "/" + item.getRepeat() + ") " + item.getDescription();
        holder.mName.setText(description);

        // Set amount
        String[] currency = NumberUtils.getCurrencyFormat(mContext, item.getAmount());
        holder.mCurrencySymbol.setText(currency[0]);
        holder.mTotal.setText(currency[1]);

        // Set initial paid value
        if (item.isPaid()) holder.mPaid.setChecked(true);
        else {
            holder.mPaid.setChecked(false);
            if (isPending) {
                holder.mPaid.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_toggle_paid_pending));
            }
        }

        // Toggle paid
        AppDatabase db = AppDatabase.getDatabase(mContext);
        holder.mPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isPending) {
                holder.mPaid.setBackground(
                        ContextCompat.getDrawable(mContext, R.drawable.button_toggle_paid_pending));
            }

            if (isCardTotal) {
                Log.d("debug-item", "card item on toggle");
                if (isChecked) {
                    Log.d("debug-item", "card item on toggle On");
                    holder.mPaid.setChecked(false); // set to true after method picker
                    mInterface.showMethodPickerDialog(false, item, dayPosition);
                }
                else {
                    Log.d("debug-item", "card item on toggle Off => cardId = " + item.getCardId());
                    Handler handler = new Handler(Looper.getMainLooper());
                    AppDatabase.dbExecutor.execute(() -> {
                        db.TransactionDao().updatePaidCard(item.getCardId(), false, item.getMonth(), item.getYear(), 0);
                        handler.post(() -> {
                            item.setPaid(false);
                            updateCreditCardItemsNotPaidStatus(item.getCardId());
                            mInterface.updateTotal(item.getAmount(), false);
                        });
                    });
                }
            }
            else {
                Handler handler = new Handler(Looper.getMainLooper());
                AppDatabase.dbExecutor.execute(() -> {
                    db.TransactionDao().updatePaid(item.getId(), isChecked);
                    handler.post(() -> {
                        item.setPaid(isChecked);
                        mInterface.updateTotal(item.getAmount(), isChecked);
                    });
                });
            }
        });

        // Set accumulated
        if (isAccumulated) {
            holder.mContainer.setClickable(false);
            holder.mPaid.setVisibility(View.GONE);
            holder.mName.setTextColor(ContextCompat.getColor(mContext, color.getColor()));
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, color.getColor()));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, color.getColor()));
            holder.mCardIcon.setVisibility(View.VISIBLE);
            holder.mCardIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_40_autorenew_fill0_300));
            holder.mCardIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
        }

        // Set credit card item
        if (item.getId() != -1 && item.getCardId() > 0) {
            holder.mPaid.setVisibility(View.GONE);
            holder.mCardIcon.setVisibility(View.VISIBLE);
            holder.mIcon.setVisibility(View.VISIBLE);
            holder.mTotal.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
            holder.mCurrencySymbol.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
            holder.mName.setTextColor(ContextCompat.getColor(mContext, R.color.medium_emphasis_text));
        }

        if (isCardTotal) {
            item.setCategoryName(mContext.getString(R.string.credit_card));
            holder.mContainer.setOnClickListener(v -> {
                item.setCardId(-1); // to use on transactions dialog
                mInterface.showTransactionDialog(item);
            });
        }

        // Open transaction details dialog
        if (item.getId() > 0) {
            holder.mContainer.setOnClickListener(v -> mInterface.showTransactionDialog(item));
        }
    }

    private void updateCreditCardItemsNotPaidStatus(int cardId) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getCardId() == cardId) mDataList.get(i).setPaid(false);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon, mCardIcon;
        private final TextView mName, mTotal, mCurrencySymbol;
        private final ConstraintLayout mContainer;
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

        }
    }
}
