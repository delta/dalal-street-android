package org.pragyan.dalal18.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.LeaderboardRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.LeaderBoardDetails
import org.pragyan.dalal18.loaders.LeaderBoardLoader
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.MiscellaneousUtils

import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetLeaderboardRequest
import dalalstreet.api.actions.GetLeaderboardResponse
import dalalstreet.api.models.LeaderboardRow
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.utils.Constants

import org.pragyan.dalal18.utils.Constants.LEADER_BOARD_LOADER_ID

/* Uses GetLeaderboard() to set leader board table and to set user's current rank */
class LeaderboardFragment : Fragment(){

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    private val LEADERBOARD_SIZE = 15

    private val leaderBoardDetailsList = ArrayList<LeaderBoardDetails>()

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    lateinit var loadingDialog: AlertDialog
    lateinit var totalWorthTextView: TextView
    lateinit var leaderboardRecyclerAdapter: LeaderboardRecyclerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement network down handler.")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (context != null) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.loading_leaderboard)
            loadingDialog = AlertDialog.Builder(context!!)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        totalWorthTextView = container!!.rootView.findViewById(R.id.totalWorthTextView)

        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        if (activity != null) activity!!.title = "Leaderboard"


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.leaderboard)
        getRankListAsynchronously()

        leaderboard_recyclerView.layoutManager = LinearLayoutManager(context)
        leaderboard_recyclerView.setHasFixedSize(false)
        leaderboardRecyclerAdapter = LeaderboardRecyclerAdapter(context, leaderBoardDetailsList)
        leaderboard_recyclerView.adapter = leaderboardRecyclerAdapter
    }

    private fun getRankListAsynchronously()
    {
        loadingDialog.show()
        leaderboard_recyclerView.visibility = View.GONE
        doAsync {
            if(ConnectionUtils.getConnectionInfo(getContext()) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT))
            {
                val rankListResponse = actionServiceBlockingStub?.getLeaderboard(GetLeaderboardRequest
                        .newBuilder()
                        .setCount(LEADERBOARD_SIZE)
                        .setStartingId(1)
                        .build())
                if(rankListResponse?.statusCode == GetLeaderboardResponse.StatusCode.OK)
                {
                    uiThread { personal_rank_textView.text = rankListResponse?.myRank.toString()
                        personal_wealth_textView.text = totalWorthTextView.text.toString()
                        personal_name_textView.text = MiscellaneousUtils.username }
                    if (rankListResponse != null) {
                        for (i in 0 until rankListResponse?.rankListCount) {
                            val currentRow = rankListResponse.getRankList(i)
                            leaderBoardDetailsList.add(LeaderBoardDetails(currentRow.rank, currentRow.userName, currentRow.totalWorth))
                        }
                    }
                    uiThread { leaderboardRecyclerAdapter.swapData(leaderBoardDetailsList) }
                    loadingDialog.dismiss()
                    leaderboard_recyclerView.visibility = View.VISIBLE
                }
                else
                {
                    uiThread { Toast.makeText(activity,rankListResponse?.statusMessage + "Internal server error",Toast.LENGTH_LONG).show() }
                }
            }
            else
            {
                networkDownHandler.onNetworkDownError()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        leaderBoardDetailsList.clear()
    }
}