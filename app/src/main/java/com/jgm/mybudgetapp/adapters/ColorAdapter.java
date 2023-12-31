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
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Color> mDataList;
    private final LayoutInflater layoutInflater;
    private MainInterface mInterface;
    private SettingsInterface mSettingsInterface;
    private final BottomSheetDialogFragment dialog;

    public ColorAdapter(Context context, List<Color> mDataList, BottomSheetDialogFragment dialog) {
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
        View itemView = layoutInflater.inflate(R.layout.item_color, parent, false);
        return new GridViewHolder(itemView);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {

        Color color = mDataList.get(position);
        int colorRes = color.getColor();
        int colorNameRes = ColorUtils.getColorNameResource(color.getId());
        String colorName = mContext.getString(colorNameRes);

        holder.mColor.setImageTintList(ContextCompat.getColorStateList(mContext, colorRes));
        holder.mColor.setContentDescription(colorName);

        holder.mColor.setOnClickListener(view -> {
            if (mInterface != null) mInterface.handleColorSelection(color);
            else if (mSettingsInterface != null) mSettingsInterface.handleColorSelection(color);
            dialog.dismiss();
        });

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ImageButton mColor;

        private GridViewHolder(@NonNull View itemView) {
            super(itemView);

            mColor = itemView.findViewById(R.id.item_color);

        }
    }
}
