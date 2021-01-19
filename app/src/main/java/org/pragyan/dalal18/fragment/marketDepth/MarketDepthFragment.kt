package org.pragyan.dalal18.fragment.marketDepth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.DepthPagerAdapter
import org.pragyan.dalal18.databinding.FragmentMarketDepthBinding
import org.pragyan.dalal18.utils.viewLifecycle

class MarketDepthFragment : Fragment() {

    private var binding by viewLifecycle<FragmentMarketDepthBinding>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMarketDepthBinding.inflate(inflater, container, false)

        binding.apply {
            // Set Adapter to Viewpager
            contentViewPager.adapter = DepthPagerAdapter(childFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            tabHeadings.setupWithViewPager(contentViewPager)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.market_depth)
    }
}
