package org.pragyan.dalal18.fragment.leaderboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetDailyLeaderboardRequest
import dalalstreet.api.actions.GetDailyLeaderboardResponse
import dalalstreet.api.actions.GetLeaderboardRequest
import dalalstreet.api.actions.GetLeaderboardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.LeaderboardRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.LeaderBoardDetails
import org.pragyan.dalal18.databinding.FragmentLeaderboardListBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
import org.pragyan.dalal18.utils.viewLifecycle
import java.util.*
import javax.inject.Inject

class LeaderboardListFragment(private val mode: Int) : Fragment() {

    private var binding by viewLifecycle<FragmentLeaderboardListBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private lateinit var loadingDialog: AlertDialog

    private lateinit var leaderBoardRecyclerAdapter: LeaderboardRecyclerAdapter
    private val leaderBoardDetailsList = ArrayList<LeaderBoardDetails>()

    private lateinit var totalWorthTextView: TextView

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
        binding = FragmentLeaderboardListBinding.inflate(inflater, container, false)

        totalWorthTextView = container!!.rootView.findViewById(R.id.totalWorthTextView)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.loading_leaderboard)
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        initRecyclerView()

        when (mode) {
            OVERALL_MODE -> {
                getOverallLeaderboardAsynchronously()
            }
            DAILY_MODE -> {
                getDailyLeaderBoardAsynchronously()
            }
        }
    }

    private fun initRecyclerView() {
        leaderBoardRecyclerAdapter = LeaderboardRecyclerAdapter(context, leaderBoardDetailsList)

        with(binding.leaderboardRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = leaderBoardRecyclerAdapter
        }
    }

    private fun getOverallLeaderboardAsynchronously() = lifecycleScope.launch {
        leaderBoardDetailsList.clear()
        loadingDialog.show()
        binding.leaderboardRecyclerView.visibility = View.GONE
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                val rankListResponses = mutableListOf<GetLeaderboardResponse>()

                for (i in 1..LEADER_BOARD_SIZE step 10) {
                    rankListResponses.add(withContext(Dispatchers.IO) {
                        actionServiceBlockingStub.getLeaderboard(GetLeaderboardRequest.newBuilder().setStartingId(i).build())
                    })
                }

                for (rankListResponse in rankListResponses) {
                    if (rankListResponse.statusCode == GetLeaderboardResponse.StatusCode.OK) {
                        binding.apply {
                            personalRankTextView.text = rankListResponse.myRank.toString()
                            personalWealthTextView.text = totalWorthTextView.text.toString()
                            personalNameTextView.text = MiscellaneousUtils.username
                            for (currentRow in rankListResponse.rankListList)
                                leaderBoardDetailsList.add(LeaderBoardDetails(currentRow.rank, currentRow.userName, currentRow.stockWorth, currentRow.totalWorth, currentRow.isBlocked))
                            leaderboardRecyclerView.visibility = View.VISIBLE
                        }
                    } else {
                        context?.longToast("Server internal error")
                    }
                }

                leaderBoardRecyclerAdapter.swapData(leaderBoardDetailsList)
            } else {
                networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.leaderboard_dest)
            }
        } else {
            networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.leaderboard_dest)
        }
        (parentFragment as LeaderboardFragment).stopRefresh(mode)
        loadingDialog.dismiss()
    }

    private fun getDailyLeaderBoardAsynchronously() = lifecycleScope.launch {
        leaderBoardDetailsList.clear()
        loadingDialog.show()
        binding.leaderboardRecyclerView.visibility = View.GONE
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(context) }) {
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT) }) {
                val rankListResponses = mutableListOf<GetDailyLeaderboardResponse>()

                for (i in 1..LEADER_BOARD_SIZE step 10) {
                    rankListResponses.add(withContext(Dispatchers.IO) {
                        actionServiceBlockingStub.getDailyLeaderboard(GetDailyLeaderboardRequest.newBuilder().setStartingId(i).build())
                    })
                }

                for (rankListResponse in rankListResponses) {
                    if (rankListResponse.statusCode == GetDailyLeaderboardResponse.StatusCode.OK) {
                        binding.apply {
                            personalRankTextView.text = rankListResponse.myRank.toString()
                            personalWealthTextView.text = rankListResponse.myTotalWorth.toString()
                            personalNameTextView.text = MiscellaneousUtils.username
                            for (currentRow in rankListResponse.rankListList)
                                leaderBoardDetailsList.add(LeaderBoardDetails(currentRow.rank, currentRow.userName, currentRow.stockWorth, currentRow.totalWorth, currentRow.isBlocked))
                            leaderboardRecyclerView.visibility = View.VISIBLE
                        }
                    } else {
                        context?.toast("Server internal error")
                    }
                }

                leaderBoardRecyclerAdapter.swapData(leaderBoardDetailsList)
            } else {
                networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.leaderboard_dest)
            }
        } else {
            networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.leaderboard_dest)
        }
        (parentFragment as LeaderboardFragment).stopRefresh(mode)
        loadingDialog.dismiss()
    }

    fun refresh() {
        when (mode) {
            OVERALL_MODE -> {
                getOverallLeaderboardAsynchronously()
            }
            DAILY_MODE -> {
                getDailyLeaderBoardAsynchronously()
            }
        }
    }

    companion object {
        const val OVERALL_MODE = -1
        const val DAILY_MODE = 1

        private const val LEADER_BOARD_SIZE = 100
    }
}
