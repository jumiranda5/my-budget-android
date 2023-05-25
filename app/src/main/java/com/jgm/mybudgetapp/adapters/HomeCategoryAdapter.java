package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Category;

import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<Category> mDataList;
    private final LayoutInflater layoutInflater;

    public HomeCategoryAdapter(Context context, List<Category> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_home_category, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

        Category category = mDataList.get(position);
        int color = category.getColorId(); // todo: create color utils

        // Set color
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color));

        // Set category name
        holder.mName.setText(category.getName());

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_home_cat_circle);
            mName = itemView.findViewById(R.id.item_home_cat_text);

        }
    }
}
