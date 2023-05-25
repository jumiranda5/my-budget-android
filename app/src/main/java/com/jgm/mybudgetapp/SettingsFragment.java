package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jgm.mybudgetapp.databinding.FragmentSettingsBinding;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    // UI
    private FragmentSettingsBinding binding;
    private SwitchCompat switchDarkMode;

    private void setBinding() {
        switchDarkMode = binding.switchDarkMode;
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
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("debug-settings", "onViewCreated");

        boolean isDark = SettingsPrefs.getSettingsPrefsBoolean(mContext, "isDark");
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mInterface.switchDarkMode(isChecked);
            SettingsPrefs.setSettingsPrefsBoolean(mContext, "isDark", isChecked);
        });
    }
}