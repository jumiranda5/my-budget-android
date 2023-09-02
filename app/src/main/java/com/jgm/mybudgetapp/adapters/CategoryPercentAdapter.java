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

import com.jgm.mybudgetapp.CategoryInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;

import java.util.List;

public class CategoryPercentAdapter extends RecyclerView.Adapter<CategoryPercentAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<CategoryPercent> mDataList;
    private final LayoutInflater layoutInflater;
    private final CategoryInterface mInterface;

    public CategoryPercentAdapter(Context context, List<CategoryPercent> mDataList) {
        this.mContext = context;
        this.mDataList = mDataList;
        layoutInflater = LayoutInflater.from(context);
        mInterface = (CategoryInterface) context;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_category_percent, parent, false);
        return new GridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        CategoryPercent category = mDataList.get(position);
        Color color = ColorUtils.getColor(category.getColorId());
        Icon icon = IconUtils.getIcon(category.getIconId());

        // Set Icon and color
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
        holder.mPercent.setTextColor(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Set category name
        String percent = category.getPercent() + "%";
        holder.mName.setText(category.getName());
        holder.mPercent.setText(percent);

        // Open dialog with details
        holder.mContainer.setOnClickListener(v -> mInterface.showCategoryDetails(category));

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mPercent;
        private final ConstraintLayout mContainer;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mContainer = itemView.findViewById(R.id.category_item_percent);
            mIcon = itemView.findViewById(R.id.category_icon);
            mName = itemView.findViewById(R.id.category_name);
            mPercent = itemView.findViewById(R.id.category_percent);

        }
    }
}
