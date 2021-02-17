package org.pragyan.dalal18.adapter.pagerAdapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.pragyan.dalal18.fragment.adminPanel.AdminPanelDailyMarketFragment
import org.pragyan.dalal18.fragment.adminPanel.AdminPanelStocksFragment
import org.pragyan.dalal18.fragment.adminPanel.AdminPanelUserSpecificFragment
import org.pragyan.dalal18.fragment.help.FaqFragment
import org.pragyan.dalal18.fragment.help.GettingStartedFragment
import org.pragyan.dalal18.fragment.leaderboard.LeaderboardListFragment

class AdminPanelPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AdminPanelStocksFragment()
            1 -> AdminPanelDailyMarketFragment()
            else -> AdminPanelUserSpecificFragment()
        }
    }

    override fun getCount() = NUMBER_OF_FRAGMENTS

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Stocks"
            1 -> "Daily Market"
            else -> "User Specific"
        }
    }

    companion object {
        private const val NUMBER_OF_FRAGMENTS = 3
    }
}
