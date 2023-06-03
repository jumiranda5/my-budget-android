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
import com.jgm.mybudgetapp.objects.Category;

import java.util.List;

public class CategoryPercentAdapter extends RecyclerView.Adapter<CategoryPercentAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Category> mDataList;
    private final LayoutInflater layoutInflater;

    public CategoryPercentAdapter(Context context, List<Category> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_category_percent, parent, false);
        return new GridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        Category category = mDataList.get(position);

        // Set Icon and color
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, category.getColorId()));
        holder.mPercent.setTextColor(ContextCompat.getColorStateList(mContext, category.getColorId()));

        // Set category name
        String percent = category.getPercent() + "%";
        holder.mName.setText(category.getName());
        holder.mPercent.setText(percent);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mPercent;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.category_icon);
            mName = itemView.findViewById(R.id.category_name);
            mPercent = itemView.findViewById(R.id.category_percent);

        }
    }
}
