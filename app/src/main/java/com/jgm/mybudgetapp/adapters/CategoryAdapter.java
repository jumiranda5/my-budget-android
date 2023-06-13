package com.jgm.mybudgetapp.adapters;

import android.content.Context;
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

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.room.entity.Category;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Category> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;
    private final boolean isEdit;

    public CategoryAdapter(Context context, List<Category> mDataList, boolean isEdit) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        this.isEdit = isEdit;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_category, parent, false);
        return new GridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        Category category = mDataList.get(position);
        Icon icon = IconUtils.getIcon(category.getIconId());
        Color color = ColorUtils.getColor(category.getColorId());

        // Set Icon and color
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        holder.mIcon.setContentDescription(icon.getIconName());
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Set category name
        holder.mName.setText(category.getName());

        // Select category | Set edit button
        if (isEdit) {
            holder.mEdit.setVisibility(View.VISIBLE);
            holder.mEdit.setOnClickListener(v -> mInterface.openCategoryForm(true, category, position));
            holder.mContainer.setOnClickListener(v -> mInterface.openCategoryForm(true, category, position));
        }
        else {
            holder.mEdit.setVisibility(View.GONE);
            holder.mContainer.setOnClickListener(v -> mInterface.setSelectedCategory(category));
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addItem(Category category) {
        if (category != null) {
            mDataList.add(category);
            int pos = mDataList.size();
            notifyItemInserted(pos);
            notifyItemRangeInserted(pos, mDataList.size());
        }
    }

    public void updateItem(int pos, Category newData) {
        mDataList.get(pos).setName(newData.getName());
        mDataList.get(pos).setColorId(newData.getColorId());
        mDataList.get(pos).setIconId(newData.getIconId());
        notifyItemChanged(pos);
    }

    public void deleteItem(int pos) {
        mDataList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, mDataList.size());
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName;
        private final ImageButton mEdit;
        private final ConstraintLayout mContainer;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.category_icon);
            mName = itemView.findViewById(R.id.category_name);
            mEdit = itemView.findViewById(R.id.category_item_edit);
            mContainer = itemView.findViewById(R.id.category_item);

        }
    }
}
