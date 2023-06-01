package com.jgm.mybudgetapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CategoriesFormFragment extends Fragment {

    public CategoriesFormFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories_form, container, false);
    }

    public void setFormType(boolean isEdit) {
        Log.d("debug-type", "Is edit => " + isEdit);
    }
}