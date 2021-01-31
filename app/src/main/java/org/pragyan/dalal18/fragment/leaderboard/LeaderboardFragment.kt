package org.pragyan.dalal18.fragment.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.LeaderboardPagerAdapter
import org.pragyan.dalal18.databinding.FragmentLeaderboardBinding
import org.pragyan.dalal18.utils.viewLifecycle

class LeaderboardFragment : Fragment() {

    private var binding by viewLifecycle<FragmentLeaderboardBinding>()

    private var overallRefresh = false
    private var dailyRefresh = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        val pagerAdapter = LeaderboardPagerAdapter(childFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)

        binding.apply {
            contentViewPager.adapter = pagerAdapter
            tabLayout.setupWithViewPager(contentViewPager)

            contentViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {}

                override fun onPageScrollStateChanged(state: Int) {
                    enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE)
                }

            })

            refreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.colorPrimary))
            refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        }

        binding.refreshLayout.setOnRefreshListener {
            pagerAdapter.refresh()
            overallRefresh = true
            dailyRefresh = true
        }

        return binding.root
    }

    fun stopRefresh(from: Int) {
        when (from) {
            LeaderboardListFragment.OVERALL_MODE -> overallRefresh = false
            LeaderboardListFragment.DAILY_MODE -> dailyRefresh = false
        }
        // Stop Refreshing only when both Fragments are done
        if (!overallRefresh && !dailyRefresh)
            binding.refreshLayout.isRefreshing = false
    }

    // To fix issue with ViewPager inside SwipeRefreshLayout
    // Link : https://stackoverflow.com/questions/25978462/swiperefreshlayout-viewpager-limit-horizontal-scroll-only
    private fun enableDisableSwipeRefresh(enable: Boolean) {
        binding.refreshLayout.isEnabled = enable
    }
}
