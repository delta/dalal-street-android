package org.pragyan.dalal18.adapter.pagerAdapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import org.pragyan.dalal18.fragment.SingleDayChallengeFragment

class DailyChallengePagerAdapter(fm: FragmentManager, behavior: Int,private val marketDay: Int,private val isDailyChallengeOpen: Boolean,private val totalDays:Int) : FragmentStatePagerAdapter(fm,behavior){
    override fun getItem(position: Int): Fragment {
       return SingleDayChallengeFragment(position+1,marketDay,isDailyChallengeOpen)
    }

    override fun getCount(): Int = totalDays

    override fun getPageTitle(position: Int): CharSequence? {
        return "Day ${position+1}"
    }




}