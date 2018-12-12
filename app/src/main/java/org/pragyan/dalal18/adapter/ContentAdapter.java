package org.pragyan.dalal18.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.pragyan.dalal18.fragment.marketDepth.DepthGraphFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthTableFragment;

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
                return "Chart";
        }
    }
}
