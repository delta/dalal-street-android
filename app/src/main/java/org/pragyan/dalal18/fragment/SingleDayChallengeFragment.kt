package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetDailyChallenges
import dalalstreet.api.actions.GetMyUserState
import dalalstreet.api.models.DailyChallengeOuterClass
import dalalstreet.api.models.UserStateOuterClass
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.DailyChallengesRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentSingleDayChallengeBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject


class SingleDayChallengeFragment(private val day:Int,private val currMarketDay: Int,private val isDailyChallengeOpen: Boolean) : Fragment() {

    private var binding by viewLifecycle<FragmentSingleDayChallengeBinding>()
    private lateinit var dailyChallengesRecyclerAdapter: DailyChallengesRecyclerAdapter
    private  val challengesState = mutableListOf<Pair<Boolean,Boolean>>()
    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private lateinit var model: DalalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSingleDayChallengeBinding.inflate(inflater,container,false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(currMarketDay!=0)
        getDailyChallenge(day)

    }

    private fun initRecyclerView(dailyChallengesList: MutableList<DailyChallengeOuterClass.DailyChallenge>,userStates: MutableList<Pair<Boolean,Boolean>>) {
        dailyChallengesRecyclerAdapter = DailyChallengesRecyclerAdapter(dailyChallengesList,userStates,
                object : DailyChallengesRecyclerAdapter.CheckUserStateListener{


                    override fun getCompanyNameFromStockId(stockId: Int): String {
                       return model.getCompanyNameFromStockId(stockId)
                    }

                    override fun isDailyChallengeOpen(): Boolean {
                        return (isDailyChallengeOpen && (day==currMarketDay))
                    }

                })
        binding.dailyChallengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dailyChallengeRecyclerView.adapter = dailyChallengesRecyclerAdapter
    }






    private fun getDailyChallenge(day: Int) = lifecycleScope.launch{
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context!!) }){
            val dailyChallengesRequest = GetDailyChallenges.GetDailyChallengesRequest.newBuilder()
                    .setMarketDay(day)
                    .build()
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                val dailyChallengesResponse = withContext(Dispatchers.IO) { actionServiceBlockingStub.getDailyChallenges(dailyChallengesRequest) }


                if (dailyChallengesResponse.statusCode == GetDailyChallenges.GetDailyChallengesResponse.StatusCode.OK) {
                   // Toast.makeText(context!!,dailyChallengesResponse.dailyChallengesList.toString(), Toast.LENGTH_SHORT).show()
                           //   Log.i("daily",dailyChallengesResponse.dailyChallengesList[day].toString())
                    val dailyChallenges = dailyChallengesResponse.dailyChallengesList
                    getChallengeUserStatesAsynchronously(dailyChallenges)


                } else {
                    Toast.makeText(context!!, dailyChallengesResponse.statusMessage.toString(), Toast.LENGTH_LONG).show()
                }

            } else {
                showSnackBar("Server Unreachable", day)

            }
        }
    }

    private fun getChallengeUserStatesAsynchronously(dailyChallenges: MutableList<DailyChallengeOuterClass.DailyChallenge>) = lifecycleScope.launch{
        val size = dailyChallenges.size
        for(i in 0..size-1) {
            if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context!!) }) {
                val myUserStateRequest = GetMyUserState.GetMyUserStateRequest.newBuilder()
                        .setMarketDay(day)
                        .setChallengeId(dailyChallenges[i].challengeId)
                        .build()
                if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                    val userStateResponse = withContext(Dispatchers.IO) { actionServiceBlockingStub.getMyUserState(myUserStateRequest) }


                    if (userStateResponse.statusCode == GetMyUserState.GetMyUserStateResponse.StatusCode.OK) {
                        val myUserState = userStateResponse.userState
                        challengesState.add(Pair(myUserState.isCompleted,myUserState.isRewardClamied))


                    } else {
                        Toast.makeText(context!!, userStateResponse.statusMessage.toString(), Toast.LENGTH_LONG).show()
                    }

                } else {
                    showSnackBar("Server Unreachable", day)

                }
            }
        }
        initRecyclerView(dailyChallenges,challengesState)
    }
    private fun showSnackBar(message: String, marketDay: Int) {
        val snackBar = Snackbar.make(activity!!.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { getDailyChallenge(marketDay) }

        snackBar.setActionTextColor(ContextCompat.getColor(context!!, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }


}
