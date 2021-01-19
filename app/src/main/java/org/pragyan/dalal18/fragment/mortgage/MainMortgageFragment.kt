package org.pragyan.dalal18.fragment.mortgage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main_mortgage.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.MortgagePagerAdapter

class MainMortgageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_mortgage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.mortgage_stocks)

        mortgageViewPager.adapter = MortgagePagerAdapter(childFragmentManager)
        mainMortgageTabLayout.setupWithViewPager(mortgageViewPager)

        mainMortgageTabLayout.setTabTextColors(ContextCompat.getColor(context!!, R.color.neutral_font_color), ContextCompat.getColor(context!!, R.color.neon_blue))
        mainMortgageTabLayout.setBackgroundColor(Color.parseColor("#20202C"))
    }
}