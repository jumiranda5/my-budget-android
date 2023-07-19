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
import com.jgm.mybudgetapp.objects.Color;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.GridViewHolder> {

    private final Context mContext;
    private final List<Color> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;
    private final BottomSheetDialogFragment dialog;

    public ColorAdapter(Context context, List<Color> mDataList, BottomSheetDialogFragment dialog) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.dialog = dialog;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
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
        String colorName = color.getColorName();

        holder.mColor.setImageTintList(ContextCompat.getColorStateList(mContext, colorRes));
        holder.mColor.setContentDescription(colorName);

        holder.mColor.setOnClickListener(view -> {
            mInterface.handleColorSelection(color);
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
