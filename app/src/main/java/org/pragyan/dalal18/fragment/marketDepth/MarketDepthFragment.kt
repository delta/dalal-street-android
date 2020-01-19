package org.pragyan.dalal18.fragment.marketDepth

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_company_description.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.DepthPagerAdapter
import org.pragyan.dalal18.fragment.CompanyDescriptionFragment
import org.pragyan.dalal18.utils.StockUtils

class MarketDepthFragment : Fragment() {

    private lateinit var companyName: String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val customView = inflater.inflate(R.layout.fragment_market_depth, container, false)

        // Binding view for Tabbed Fragments
        val viewPager = customView.findViewById<ViewPager>(R.id.content_viewPager)
        val tabLayout = customView.findViewById<TabLayout>(R.id.tab_headings)

        val depthPagerAdapter = DepthPagerAdapter(childFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)

        /*val companyName = arguments?.getString(COMPANY_NAME) ?: "No Company"

        if(companyName!="No Company") {
            DepthPagerAdapter.companyName = companyName
        }*/

        // Set adapter to viewpager and custom colors to tabLayout
        viewPager.adapter = depthPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        if (context != null) {
            tabLayout.setTabTextColors(
                    ContextCompat.getColor(context!!, R.color.neutral_font_color),
                    ContextCompat.getColor(context!!, R.color.neon_blue))
        }
        tabLayout.setBackgroundColor(Color.parseColor("#20202C"))

        return customView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.market_depth)
    }

    companion object {
        const val COMPANY_NAME=""
    }
}
