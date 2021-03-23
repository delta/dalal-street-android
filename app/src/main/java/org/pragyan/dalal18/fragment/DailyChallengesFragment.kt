package org.pragyan.dalal18.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentDailyChallengesBinding.inflate(inflater,container,false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMarketDayAsynchronously()
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.colorPrimary))
        binding.refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        binding.refreshLayout.setOnRefreshListener {
            getMarketDayAsynchronously()
        }
    }

    private fun setUpViewPager(marketDay: Int,isDailyChallengeOpen:Boolean,days:Int) {
        binding.dailyChallengeViewPager.apply {
            adapter = DailyChallengePagerAdapter(
                    childFragmentManager,
                    FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                    marketDay,
                    isDailyChallengeOpen,
                    days
            )
            setCurrentItem(marketDay-1)
        }
        binding.daysTabLayout.setupWithViewPager(binding.dailyChallengeViewPager)
        val tabStrip = binding.daysTabLayout.getChildAt(0) as LinearLayout
        val count = marketDay
        if(count==0){
            binding.dailyChallengeViewPager.visibility = View.GONE
            binding.comingsoonTextView.visibility = View.VISIBLE
        }else {
            binding.dailyChallengeViewPager.visibility = View.VISIBLE
        }
        for (i in (count)..days) {
            val newTab = binding.daysTabLayout.getTabAt(i)
            newTab?.text = "Day ${i + 1} \uD83D\uDD12"
            if (i != days)
                tabStrip.getChildAt(i).isClickable = false
        }
        binding.refreshLayout.isRefreshing=false

    }

    private fun getMarketDayAsynchronously() = lifecycleScope.launch{
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context!!) }){
            val dailyChallengeConfigRequest = GetDailyChallengeConfig.GetDailyChallengeConfigRequest.newBuilder()
                    .build()
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                val dailyChallengeConfigResponse = withContext(Dispatchers.IO){actionServiceBlockingStub.getDailyChallengeConfig(dailyChallengeConfigRequest)}
                if(dailyChallengeConfigResponse.statusCode==GetDailyChallengeConfig.GetDailyChallengeConfigResponse.StatusCode.OK){
                    val marketDay = dailyChallengeConfigResponse.marketDay
                    val isDailyChallengeOpen = dailyChallengeConfigResponse.isDailyChallengOpen
                    val days = dailyChallengeConfigResponse.totalMarketDays
                    setUpViewPager(marketDay,isDailyChallengeOpen,days)
                }else{
                    showSnackBar("Server Internal Error")
                }
            } else {
                showSnackBar("Server Unreachable")

            }
        }else {
            networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.dailyChallenge_dest)
        }
    }

    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(activity!!.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { getMarketDayAsynchronously() }

        snackBar.setActionTextColor(ContextCompat.getColor(context!!, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }
}
