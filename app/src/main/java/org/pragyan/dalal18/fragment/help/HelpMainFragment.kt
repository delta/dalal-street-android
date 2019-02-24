package org.pragyan.dalal18.fragment.help

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main_help.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.HelpPagerAdapter

class HelpMainFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val customView = inflater.inflate(R.layout.fragment_main_help, container, false)
        return customView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.help_title)

        helpViewPager.adapter = HelpPagerAdapter(childFragmentManager)
        mainHelpTabLayout.setupWithViewPager(helpViewPager)

        mainHelpTabLayout.setTabTextColors(ContextCompat.getColor(context!!, R.color.neutral_font_color), ContextCompat.getColor(context!!, R.color.neon_blue))
        mainHelpTabLayout.setBackgroundColor(Color.parseColor("#20202C"))
    }
}