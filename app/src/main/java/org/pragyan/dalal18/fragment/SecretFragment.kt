package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dalalstreet.api.DalalActionServiceGrpc
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.AdminPanelPagerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentSecretBinding
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

/**
 * Admin Panel: No sanity checks for UI have been implemented; Also code in this fragment is HACKY!
 */

class SecretFragment : Fragment() {

    private var binding by viewLifecycle<FragmentSecretBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel
    private var message = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSecretBinding.inflate(inflater, container, false)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            adminViewPager.adapter = AdminPanelPagerAdapter(childFragmentManager)
            mainAdminTabLayout.setupWithViewPager(adminViewPager)

            mainAdminTabLayout.setTabTextColors(ContextCompat.getColor(context!!, R.color.neutral_font_color), ContextCompat.getColor(context!!, R.color.neon_blue))
            mainAdminTabLayout.setBackgroundColor(Color.parseColor("#20202C"))
        }
    }
}

/**
 * Open Market
 * Close Market
 * Update EndOfDayValues
 *
 * AddStocksToExchange
 * AddMarketEvent
 * UpdateStockPrice
 *
 * SetBankruptcy
 * SetGivesDividend
 *
 * SendNotification
 */
