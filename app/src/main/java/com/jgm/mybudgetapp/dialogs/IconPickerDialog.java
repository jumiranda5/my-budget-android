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
import com.jgm.mybudgetapp.adapters.IconAdapter;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.utils.IconUtils;

import java.util.ArrayList;

public class IconPickerDialog extends BottomSheetDialogFragment {

    public IconPickerDialog() {
    }

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
        View view = inflater.inflate(R.layout.dialog_icon_picker, container, false);

        // Dismiss
        ImageButton dismissBtn = view.findViewById(R.id.dialog_icon_dismiss);
        dismissBtn.setOnClickListener(v -> dismiss());

        // List
        RecyclerView recyclerView = view.findViewById(R.id.dialog_icon_list);
        ArrayList<Icon> iconsList = IconUtils.getIconList();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 5);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        IconAdapter adapter = new IconAdapter(mContext, iconsList, IconPickerDialog.this);
        recyclerView.setAdapter(adapter);

        return view;
    }
}

