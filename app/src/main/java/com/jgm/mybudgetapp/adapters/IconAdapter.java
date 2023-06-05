package com.jgm.mybudgetapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Icon;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Icon> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;
    private final BottomSheetDialogFragment dialog;

    public IconAdapter(Context context, List<Icon> mDataList, BottomSheetDialogFragment dialog) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.dialog = dialog;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_icon, parent, false);
        return new GridViewHolder(itemView);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        Icon icon = mDataList.get(position);
        int iconRes = icon.getIcon();
        String iconName = icon.getIconName();

        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconRes));
        holder.mIcon.setContentDescription(iconName);

        holder.mIcon.setOnClickListener(view -> {
            mInterface.handleIconSelection(icon);
            dialog.dismiss();
        });

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton mIcon;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.icon_item);

        }
    }
}
