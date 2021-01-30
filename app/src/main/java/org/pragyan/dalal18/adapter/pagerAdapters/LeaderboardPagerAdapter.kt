package org.pragyan.dalal18.adapter.pagerAdapters

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.pragyan.dalal18.fragment.leaderboard.LeaderboardListFragment

class LeaderboardPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

    override fun getPageTitle(position: Int) = when (position) {
        0 -> "Overall"
        else -> "Daily"
    }

    override fun getItem(position: Int) = when (position) {
        0 -> LeaderboardListFragment(LeaderboardListFragment.OVERALL_MODE)
        else -> LeaderboardListFragment(LeaderboardListFragment.DAILY_MODE)
    }

    override fun getCount() = NUMBER_OF_FRAGMENTS

    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
    }
}
