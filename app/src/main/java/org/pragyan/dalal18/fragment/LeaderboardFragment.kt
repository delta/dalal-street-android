package org.pragyan.dalal18.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetLeaderboardRequest
import dalalstreet.api.actions.GetLeaderboardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.longToast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.LeaderboardRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.LeaderBoardDetails
import org.pragyan.dalal18.databinding.FragmentLeaderboardBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
import org.pragyan.dalal18.utils.viewLifecycle
import java.util.*
import javax.inject.Inject

/* Uses GetLeaderBoard() to set leader board table and to set user's current rank */
class LeaderboardFragment : Fragment() {

    private var binding by viewLifecycle<FragmentLeaderboardBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private val leaderBoardDetailsList = ArrayList<LeaderBoardDetails>()

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    private lateinit var loadingDialog: AlertDialog
    private lateinit var totalWorthTextView: TextView
    private lateinit var leaderBoardRecyclerAdapter: LeaderboardRecyclerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        totalWorthTextView = container!!.rootView.findViewById(R.id.totalWorthTextView)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.leaderboard)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.loading_leaderboard)
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        getRankListAsynchronously()
        leaderBoardRecyclerAdapter = LeaderboardRecyclerAdapter(context, leaderBoardDetailsList)

        with(binding.leaderboardRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = leaderBoardRecyclerAdapter
        }
    }

    private fun getRankListAsynchronously() = lifecycleScope.launch {
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
        loadingDialog.dismiss()
    }

    override fun onPause() {
        super.onPause()
        leaderBoardDetailsList.clear()
    }

    companion object {
        private const val LEADER_BOARD_SIZE = 100
    }
}
