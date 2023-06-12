package com.jgm.mybudgetapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.adapters.MethodPickerAdapter;
import com.jgm.mybudgetapp.objects.PaymentMethod;

import java.util.ArrayList;

public class MethodPickerDialog extends DialogFragment {

    public MethodPickerDialog() {}

    private MainInterface mInterface;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInterface = (MainInterface) context;
        mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_method_picker, null);
        builder.setView(mView);

        ImageButton btnClose = mView.findViewById(R.id.method_picker_close);
        btnClose.setOnClickListener(v -> dismiss());

        ArrayList<PaymentMethod> paymentMethods = mInterface.getMethodsList();

        final RecyclerView recyclerView = mView.findViewById(R.id.payment_method_list);
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(listLayoutManager);
        recyclerView.setHasFixedSize(true);
        MethodPickerAdapter adapter = new MethodPickerAdapter(mContext, paymentMethods, MethodPickerDialog.this);
        recyclerView.setAdapter(adapter);

        return builder.create();
    }
}
