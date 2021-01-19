package org.pragyan.dalal18.fragment.help

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.HelpPagerAdapter
import org.pragyan.dalal18.databinding.FragmentMainHelpBinding
import org.pragyan.dalal18.utils.viewLifecycle

class HelpMainFragment : Fragment() {

    private var binding by viewLifecycle<FragmentMainHelpBinding>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.help_title)

        binding.apply {
            helpViewPager.adapter = HelpPagerAdapter(childFragmentManager)
            mainHelpTabLayout.setupWithViewPager(helpViewPager)

            mainHelpTabLayout.setTabTextColors(ContextCompat.getColor(context!!, R.color.neutral_font_color), ContextCompat.getColor(context!!, R.color.neon_blue))
            mainHelpTabLayout.setBackgroundColor(Color.parseColor("#20202C"))
        }
    }
}
