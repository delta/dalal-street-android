package org.pragyan.dalal18.adapter

import org.pragyan.dalal18.fragment.MortgageFragment
import org.pragyan.dalal18.fragment.RetreiveFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MortgagePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 ->  MortgageFragment()
            else -> RetreiveFragment()
        }
    }

    override fun getCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Mortgage"
            else -> "Retrieve"
        }
    }

    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
    }
}
