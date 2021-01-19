package org.pragyan.dalal18.fragment.mortgage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.MortgagePagerAdapter
import org.pragyan.dalal18.databinding.FragmentMainMortgageBinding
import org.pragyan.dalal18.utils.viewLifecycle

class MainMortgageFragment : Fragment() {

    private var binding by viewLifecycle<FragmentMainMortgageBinding>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainMortgageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage_stocks)

        binding.apply {
            mortgageViewPager.adapter = MortgagePagerAdapter(childFragmentManager)
            mainMortgageTabLayout.setupWithViewPager(mortgageViewPager)

            mainMortgageTabLayout.setTabTextColors(ContextCompat.getColor(context!!, R.color.neutral_font_color), ContextCompat.getColor(context!!, R.color.neon_blue))
            mainMortgageTabLayout.setBackgroundColor(Color.parseColor("#20202C"))
        }
    }
}
