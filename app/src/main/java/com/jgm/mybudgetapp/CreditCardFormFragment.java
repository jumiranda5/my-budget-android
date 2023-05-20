package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.jgm.mybudgetapp.databinding.FragmentCreditCardFormBinding;

public class CreditCardFormFragment extends Fragment {

    private static final String ARG_TYPE = "ADD";

    private String mParamType;

    public CreditCardFormFragment() {
        // Required empty public constructor
    }

    public static CreditCardFormFragment newInstance(String param) {
        CreditCardFormFragment fragment = new CreditCardFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamType = getArguments().getString(ARG_TYPE);
        }
    }

    // UI
    private FragmentCreditCardFormBinding binding;
    private ImageButton buttonBack;
    private Button buttonSave;

    private void setBinding() {
        buttonBack = binding.cardFormBackButton;
        buttonSave = binding.cardFormSaveButton;
    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mInterface = (MainInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreditCardFormBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonBack.setOnClickListener(v -> mInterface.navigateBack());
        buttonSave.setOnClickListener(v -> mInterface.navigateBack());

    }

    public void setFormType(boolean isEdit) {
        if (isEdit) Log.d("debug-card", "Form type => EDIT");
        else Log.d("debug-card", "Form type => ADD");
    }
}