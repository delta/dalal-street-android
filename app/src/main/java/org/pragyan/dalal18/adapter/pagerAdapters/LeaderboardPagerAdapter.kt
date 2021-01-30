package org.pragyan.dalal18.adapter.pagerAdapters

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.pragyan.dalal18.fragment.leaderboard.LeaderboardListFragment

class LeaderboardPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

    private val fragments = arrayOf(
            LeaderboardListFragment(LeaderboardListFragment.OVERALL_MODE),
            LeaderboardListFragment(LeaderboardListFragment.DAILY_MODE)
    )

    override fun getPageTitle(position: Int) = when (position) {
        0 -> "Overall"
        else -> "Daily"
    }

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = NUMBER_OF_FRAGMENTS

    fun refresh() {
        fragments[0].refresh()
        fragments[1].refresh()
    }

    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
    }
}
