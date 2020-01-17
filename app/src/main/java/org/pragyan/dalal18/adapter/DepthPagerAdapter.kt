package org.pragyan.dalal18.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import org.pragyan.dalal18.fragment.marketDepth.DepthGraphFragment
import org.pragyan.dalal18.fragment.marketDepth.DepthTableFragment

class DepthPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var companyName = "Not Known"

    fun setCompanyName(name: String) {
        companyName=name
    }

    override fun getItem(position: Int): Fragment {

        if(companyName=="Not Known")
        return when (position) {
            0 -> DepthTableFragment()
            else -> DepthGraphFragment()
        }
        else {
            val a = DepthTableFragment()
            a.setCompany(companyName)
            return when (position) {
                0 -> a
                else -> DepthGraphFragment()
            }
        }
    }

    override fun getCount() = NUMBER_OF_FRAGMENTS

    override fun getPageTitle(position: Int): CharSequence? {

        return when (position) {
            0 -> "Table"
            else -> "Chart"
        }
    }

    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
    }
}
