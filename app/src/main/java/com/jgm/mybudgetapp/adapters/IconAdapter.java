package com.jgm.mybudgetapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
import com.jgm.mybudgetapp.SettingsInterface;
import com.jgm.mybudgetapp.objects.Icon;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Icon> mDataList;
    private final LayoutInflater layoutInflater;
    private MainInterface mInterface;
    private SettingsInterface mSettingsInterface;
    private final BottomSheetDialogFragment dialog;

    public IconAdapter(Context context, List<Icon> mDataList, BottomSheetDialogFragment dialog) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.dialog = dialog;
        layoutInflater = LayoutInflater.from(context);

        try { this.mInterface = (MainInterface) context; }
        catch (Exception e) { Log.e("debug-category-adapter", "Can't cast to main activity"); }

        try { mSettingsInterface = (SettingsInterface) context; }
        catch (Exception e) { Log.e("debug-category-adapter", "Can't cast to settings activity"); }
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
            if (mInterface != null) mInterface.handleIconSelection(icon);
            else if (mSettingsInterface != null) mSettingsInterface.handleIconSelection(icon);
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
