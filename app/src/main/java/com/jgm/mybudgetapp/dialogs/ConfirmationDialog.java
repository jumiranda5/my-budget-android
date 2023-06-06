package com.jgm.mybudgetapp.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;

public class ConfirmationDialog extends DialogFragment {
    public ConfirmationDialog() {}

    public static ConfirmationDialog newInstance(String message) {
        ConfirmationDialog frag = new ConfirmationDialog();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInterface = (MainInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_confirmation, container, false);

        // Message
        assert getArguments() != null;
        String message = getArguments().getString("message", "");
        TextView textView = view.findViewById(R.id.dialog_confirmation_message);
        textView.setText(message);

        // Dismiss
        Button dismissBtn = view.findViewById(R.id.dialog_confirmation_dismiss);
        dismissBtn.setOnClickListener(v -> dismiss());

        // Confirm
        Button confirmBtn = view.findViewById(R.id.dialog_confirmation_button);
        confirmBtn.setOnClickListener(v -> {
            mInterface.handleConfirmation();
            dismiss();
        });

        return view;
    }
}
