package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.CategoryInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.CategoryItemResponse;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.List;

public class CategoryDetailsItemAdapter extends RecyclerView.Adapter<CategoryDetailsItemAdapter.ListViewHolder> {


    private final Context mContext;
    private final List<CategoryItemResponse> mDataList;
    private final LayoutInflater layoutInflater;
    private final CategoryInterface mInterface;

    public CategoryDetailsItemAdapter(Context context, List<CategoryItemResponse> items) {
        mContext = context;
        mDataList = items;
        mInterface = (CategoryInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_category_details, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

        CategoryItemResponse item = mDataList.get(position);

        // Name
        String name = item.getName();
        holder.mName.setText(name);

        // Count
        int count = item.getCount();
        if (count <= 1) holder.mCount.setVisibility(View.GONE);
        else holder.mCount.setText(String.valueOf(count));

        // Total
        float total = item.getTotal();
        String[] currency = NumberUtils.getCurrencyFormat(mContext, total);
        holder.mCurrencySymbol.setText(currency[0]);
        holder.mTotal.setText(currency[1]);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final TextView mName, mCount, mTotal, mCurrencySymbol;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.item_category_details_name);
            mCount = itemView.findViewById(R.id.item_category_details_count);
            mTotal = itemView.findViewById(R.id.item_category_total);
            mCurrencySymbol = itemView.findViewById(R.id.item_category_currency_symbol);

        }
    }

}
