package org.pragyan.dalal18.fragment.marketDepth;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.ContentAdapter;

public class MarketDepthFragment extends Fragment {

    public MarketDepthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View customView =  inflater.inflate(R.layout.fragment_market_depth, container, false);

        // Binding view for Tabbed Fragments
        ViewPager viewPager = customView.findViewById(R.id.content_viewPager);
        TabLayout tabLayout = customView.findViewById(R.id.tab_headings);

        // Set adapter to viewpager and custom colors to tabLayout
        viewPager.setAdapter(new ContentAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        if (getContext() != null) {
            tabLayout.setTabTextColors(
                    ContextCompat.getColor(getContext(), R.color.neutral_font_color),
                    ContextCompat.getColor(getContext(), R.color.neon_blue));
        }
        tabLayout.setBackgroundColor(Color.parseColor("#20202C"));

        return customView;
    }
}