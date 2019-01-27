package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.MortgagePagerAdapter

class MainMortgageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val customView = inflater.inflate(R.layout.fragment_main_mortgage, container, false)

        val viewPager = customView.findViewById<ViewPager>(R.id.content_viewPager_mortgage)
        val tabLayout = customView.findViewById<TabLayout>(R.id.tab_main_mortgage)

        viewPager.adapter = MortgagePagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.setTabTextColors(
                ContextCompat.getColor(context!!, R.color.neutral_font_color),
                ContextCompat.getColor(context!!, R.color.neon_blue))
        tabLayout.setBackgroundColor(Color.parseColor("#20202C"))

        return customView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.main_mortgage)
    }
}