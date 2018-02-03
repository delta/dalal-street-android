package com.hmproductions.theredstreet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hmproductions.theredstreet.fragment.marketDepth.DepthGraphFragment;
import com.hmproductions.theredstreet.fragment.marketDepth.DepthTableFragment;

public class ContentAdapter extends FragmentPagerAdapter {

    private static final int NUMBER_OF_FRAGMENTS = 2;

    public ContentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new DepthTableFragment();
            default:
                return new DepthGraphFragment();
        }
    }

    @Override
    public int getCount() {
        return NUMBER_OF_FRAGMENTS;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "Table";
            default:
                return "Timeline";
        }
    }
}
