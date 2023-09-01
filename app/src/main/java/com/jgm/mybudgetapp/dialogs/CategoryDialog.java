package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jgm.mybudgetapp.CategoryInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.CategoryDetailsItemAdapter;
import com.jgm.mybudgetapp.adapters.ColorAdapter;
import com.jgm.mybudgetapp.objects.CategoryItemResponse;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoryDialog extends BottomSheetDialogFragment {

    public CategoryDialog() {}

    private Context mContext;
    private CategoryInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mInterface = (CategoryInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_category, container, false);

        try {
            // Data
            CategoryPercent category = mInterface.getCategoryData();
            Icon icon = IconUtils.getIcon(category.getIconId());
            Color color = ColorUtils.getColor(category.getColorId());
            List<CategoryItemResponse> items = mInterface.getCategoryItems();

            // Icon
            ImageView mIcon = view.findViewById(R.id.dialog_category_icon);
            mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
            mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

            // Title
            TextView mTitle = view.findViewById(R.id.dialog_category_title);
            mTitle.setText(category.getName());

            // Total
            TextView mTotal = view.findViewById(R.id.dialog_category_total);
            String total = NumberUtils.getCurrencyFormat(mContext, category.getTotal())[3];
            mTotal.setText(total);
            mTotal.setTextColor(ContextCompat.getColor(mContext, color.getColor()));

            // List
            RecyclerView mRecyclerView = view.findViewById(R.id.dialog_category_list);
            LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
            mRecyclerView.setLayoutManager(listLayoutManager);
            mRecyclerView.setHasFixedSize(true);
            CategoryDetailsItemAdapter adapter = new CategoryDetailsItemAdapter(mContext, items);
            mRecyclerView.setAdapter(adapter);
        }
        catch (Exception e) {
            // dismissing because of nullPointerException on screen rotation => transaction returns null
            Log.e("debug-dialog", e.getMessage());
            dismiss();
        }

        return view;
    }

}
