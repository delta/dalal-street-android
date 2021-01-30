package org.pragyan.dalal18.fragment.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.LeaderboardPagerAdapter
import org.pragyan.dalal18.databinding.FragmentLeaderboardBinding
import org.pragyan.dalal18.utils.viewLifecycle

class LeaderboardFragment : Fragment() {

    private var binding by viewLifecycle<FragmentLeaderboardBinding>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        binding.apply {
            contentViewPager.adapter = LeaderboardPagerAdapter(childFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            tabLayout.setupWithViewPager(contentViewPager)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.leaderboard)
    }
}
