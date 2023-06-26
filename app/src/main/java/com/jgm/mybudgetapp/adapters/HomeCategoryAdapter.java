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
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.utils.ColorUtils;

import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ListViewHolder> {

    private final Context mContext;
    private final List<CategoryResponse> mDataList;
    private final LayoutInflater layoutInflater;

    public HomeCategoryAdapter(Context context, List<CategoryResponse> mDataList) {
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

        CategoryResponse category = mDataList.get(position);
        int color = ColorUtils.getColor(category.getColorId()).getColor();

        // Set color
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color));

        // Set category name
        holder.mName.setText(category.getCategory());

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
