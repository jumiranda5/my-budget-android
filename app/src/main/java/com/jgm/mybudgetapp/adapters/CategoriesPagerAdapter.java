package com.jgm.mybudgetapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class CategoriesPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList;

    public CategoriesPagerAdapter(FragmentActivity fa, List<Fragment> fragments) {
        super(fa);
        this.fragmentList = fragments;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

}
