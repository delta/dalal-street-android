package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetDailyChallengeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.pagerAdapters.DailyChallengePagerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.databinding.FragmentDailyChallengesBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject


class DailyChallengesFragment : Fragment() {
    private var binding by viewLifecycle<FragmentDailyChallengesBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub


    private var isDailyChallengeOpen = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentDailyChallengesBinding.inflate(inflater,container,false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMarketDay()
    }

    private fun setUpViewPager(marketDay: Int) {
        binding.dailyChallengeViewPager.apply {
            adapter = DailyChallengePagerAdapter(
                    childFragmentManager,
                    FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                    marketDay,
                    isDailyChallengeOpen
            )
            setCurrentItem(marketDay-1)
        }
        binding.daysTabLayout.setupWithViewPager(binding.dailyChallengeViewPager)
        val tabStrip = binding.daysTabLayout.getChildAt(0) as LinearLayout
        val count = marketDay
        if(count==0){
            binding.dailyChallengeViewPager.visibility = View.GONE
            Toast.makeText(context,"Market is closed",Toast.LENGTH_SHORT).show()
        }else {
            binding.dailyChallengeViewPager.visibility = View.VISIBLE
        }
        for (i in (count)..7) {
            val newTab = binding.daysTabLayout.getTabAt(i)
            newTab?.text = "Day ${i + 1} \uD83D\uDD12"
            if (i != 7)
                tabStrip.getChildAt(i).isClickable = false
        }

    }

    private fun getMarketDay() = lifecycleScope.launch{
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context!!) }){
            val dailyChallengeConfigRequest = GetDailyChallengeConfig.GetDailyChallengeConfigRequest.newBuilder()
                    .build()
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                val dailyChallengeConfigResponse = withContext(Dispatchers.IO){actionServiceBlockingStub.getDailyChallengeConfig(dailyChallengeConfigRequest)}
                if(dailyChallengeConfigResponse.statusCode==GetDailyChallengeConfig.GetDailyChallengeConfigResponse.StatusCode.OK){
                    val marketDay = dailyChallengeConfigResponse.marketDay
                    isDailyChallengeOpen = dailyChallengeConfigResponse.isDailyChallengOpen
                    Toast.makeText(requireContext(),"market ${marketDay} isDailyCHallengeOpen ${isDailyChallengeOpen}",Toast.LENGTH_SHORT).show()
                    setUpViewPager(marketDay)
                }
            } else {
                showSnackBar("Server Unreachable")

            }
        }
    }

    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(activity!!.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { getMarketDay() }

        snackBar.setActionTextColor(ContextCompat.getColor(context!!, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }


}