package org.pragyan.dalal18.adapter;

import org.pragyan.dalal18.fragment.MortgageFragment;
import org.pragyan.dalal18.fragment.RetreiveFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MortgageAdapter extends FragmentPagerAdapter {
    private static final int NUMBER_OF_FRAGMENTS = 2;
    public MortgageAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MortgageFragment();
            default:
                return new RetreiveFragment();
        }
    }

    @Override
    public int getCount() {
        return NUMBER_OF_FRAGMENTS;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Mortgage";
            default:
                return "Retrieve";
        }
    }
}
