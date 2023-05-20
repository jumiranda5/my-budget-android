package com.jgm.mybudgetapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CategoriesFormFragment extends Fragment {

    private static final String ARG_TYPE = "ADD";

    private String mParamType;

    public CategoriesFormFragment() {
        // Required empty public constructor
    }

    public static CategoriesFormFragment newInstance(String param) {
        CategoriesFormFragment fragment = new CategoriesFormFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories_form, container, false);
    }
}