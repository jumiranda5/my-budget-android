package com.jgm.mybudgetapp.utils;

import androidx.fragment.app.Fragment;

public class FragmentTag {
    private Fragment fragment;
    private String tag;

    public FragmentTag(Fragment fragment, String tag) {
        this.fragment = fragment;
        this.tag = tag;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTag() {
        return tag;
    }

}
