package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.SettingsInterface;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.utils.ColorUtils;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<CreditCard> mDataList;
    private final LayoutInflater layoutInflater;
    private final SettingsInterface mInterface;

    public CardAdapter(Context context, List<CreditCard> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (SettingsInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_card, parent, false);
        return new GridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        CreditCard card = mDataList.get(position);
        String billingDay = "Billing day: " + card.getBillingDay();

        // Set color
        Color color = ColorUtils.getColor(card.getColorId());
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Set account name and type
        holder.mName.setText(card.getName());
        holder.mDay.setText(billingDay);

        // Edit
        holder.mEdit.setOnClickListener(v -> mInterface.openCardForm(true, card, position));

    }

    public void addItem(CreditCard card) {
        if (card != null) {
            mDataList.add(card);
            int pos = mDataList.size();
            notifyItemInserted(pos);
            notifyItemRangeInserted(pos, mDataList.size());
        }
    }

    public void updateItem(int pos, CreditCard newData) {
        mDataList.get(pos).setName(newData.getName());
        mDataList.get(pos).setColorId(newData.getColorId());
        mDataList.get(pos).setBillingDay(newData.getBillingDay());
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
        private final TextView mName, mDay;
        private final ImageButton mEdit;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_card_icon);
            mName = itemView.findViewById(R.id.item_card_name);
            mDay = itemView.findViewById(R.id.item_card_billing_day);
            mEdit = itemView.findViewById(R.id.item_card_edit);

        }
    }
}
