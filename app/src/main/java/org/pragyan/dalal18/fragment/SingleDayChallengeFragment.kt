package org.pragyan.dalal18.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetDailyChallenges
import dalalstreet.api.models.DailyChallengeOuterClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.DailyChallengesRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.databinding.FragmentSingleDayChallengeBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject


class SingleDayChallengeFragment(private val day:Int) : Fragment() {

    private var binding by viewLifecycle<FragmentSingleDayChallengeBinding>()
    private lateinit var dailyChallengesRecyclerAdapter: DailyChallengesRecyclerAdapter

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSingleDayChallengeBinding.inflate(inflater,container,false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDailyChallenge(day)

    }

    private fun initRecyclerView(dailyChallengesList: MutableList<DailyChallengeOuterClass.DailyChallenge>) {
        dailyChallengesRecyclerAdapter = DailyChallengesRecyclerAdapter(dailyChallengesList,
                object : DailyChallengesRecyclerAdapter.CheckUserStateListener{
                    override fun checkChallengeState(challengeId: Int) {

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
                    //Toast.makeText(context!!,dailyChallengesResponse.dailyChallengesList.toString(), Toast.LENGTH_SHORT).show()
          //          Log.i("daily",dailyChallengesResponse.dailyChallengesList[day].toString())
                    val dailyChallenges = dailyChallengesResponse.dailyChallengesList
                    initRecyclerView(dailyChallenges)

                } else {
                    Toast.makeText(context!!, dailyChallengesResponse.statusMessage.toString(), Toast.LENGTH_LONG).show()
                }

            } else {
                showSnackBar("Server Unreachable", day)

            }
        }
    }

    private fun showSnackBar(message: String, marketDay: Int) {
        val snackBar = Snackbar.make(activity!!.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY") { getDailyChallenge(marketDay) }

        snackBar.setActionTextColor(ContextCompat.getColor(context!!, R.color.neon_green))
        snackBar.view.setBackgroundColor(Color.parseColor("#20202C"))
        snackBar.show()
    }


}