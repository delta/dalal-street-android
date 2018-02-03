package com.hmproductions.theredstreet.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.LeaderboardRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.LeaderboardDetails;
import com.hmproductions.theredstreet.loaders.LeaderBoardLoader;
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.MiscellaneousUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetLeaderboardResponse;
import dalalstreet.api.models.LeaderboardRow;

import static com.hmproductions.theredstreet.utils.Constants.LEADER_BOARD_LOADER_ID;

/* Uses GetLeaderboard() to set leader board table and to set user's current rank */
public class LeaderboardFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetLeaderboardResponse>{

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @BindView(R.id.leaderboard_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.personal_name_textView)
    TextView personalNameTextView;

    @BindView(R.id.personal_rank_textView)
    TextView personalRankTextView;

    @BindView(R.id.personal_wealth_textView)
    TextView personalWealthTextView;

    private ArrayList<LeaderboardDetails> leaderBoardDetailsList = new ArrayList<>();

    ConnectionUtils.OnNetworkDownHandler networkDownHandler;

    AlertDialog loadingDialog;
    TextView totalWorthTextView;
    LeaderboardRecyclerAdapter leaderboardRecyclerAdapter ;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down hnadler.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null){
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.loading_leaderboard);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_leaderboard, container, false);

        totalWorthTextView = container.getRootView().findViewById(R.id.totalWorth_textView);
        ButterKnife.bind(this, rootView);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        if (getActivity() != null) getActivity().setTitle("Leaderboard");

        setValues();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        leaderboardRecyclerAdapter = new LeaderboardRecyclerAdapter(getContext(), leaderBoardDetailsList);
        recyclerView.setAdapter(leaderboardRecyclerAdapter);

        return rootView;
    }

    public void setValues(){

        leaderBoardDetailsList.clear();
        loadingDialog.show();

        if (getActivity() != null)
            getActivity().getSupportLoaderManager().restartLoader(LEADER_BOARD_LOADER_ID, null, this);
    }

    @Override
    public Loader<GetLeaderboardResponse> onCreateLoader(int id, Bundle args) {
        if (getContext() != null)
            return new LeaderBoardLoader(getContext(),actionServiceBlockingStub);
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<GetLeaderboardResponse> loader, GetLeaderboardResponse data) {
        loadingDialog.dismiss();

        if (data == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        if (data.getStatusCode().getNumber() == 0) {
            personalRankTextView.setText(String.valueOf(data.getMyRank()));
            personalWealthTextView.setText(totalWorthTextView.getText().toString());
            personalNameTextView.setText(MiscellaneousUtils.username);

            for (int i = 0; i < data.getRankListCount(); ++i) {
                LeaderboardRow currentRow = data.getRankList(i);
                leaderBoardDetailsList.add(new LeaderboardDetails(currentRow.getRank(), currentRow.getUserName(), currentRow.getTotalWorth()));
            }

            leaderboardRecyclerAdapter.swapData(leaderBoardDetailsList);

        } else {
            Toast.makeText(getContext(), "Internal server error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<GetLeaderboardResponse> loader) {
        // Do nothing
    }

    @Override
    public void onPause() {
        super.onPause();
        leaderBoardDetailsList.clear();
    }
}
