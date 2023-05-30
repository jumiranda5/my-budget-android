package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.ColorAdapter;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

import java.util.ArrayList;

public class ColorPickerDialog extends BottomSheetDialogFragment {

    public ColorPickerDialog() {}

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_color_picker, container, false);

        // Dismiss
        ImageButton dismissBtn = view.findViewById(R.id.dialog_color_dismiss);
        dismissBtn.setOnClickListener(v -> dismiss());

        // List
        RecyclerView recyclerView = view.findViewById(R.id.dialog_color_list);
        ArrayList<Color> colorsList = ColorUtils.getColorList();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 6);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        ColorAdapter adapter = new ColorAdapter(mContext, colorsList, ColorPickerDialog.this);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
