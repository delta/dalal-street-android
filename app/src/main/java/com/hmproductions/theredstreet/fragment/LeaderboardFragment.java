package com.hmproductions.theredstreet.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.MiscellaneousUtils;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.LeaderboardRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.LeaderboardDetails;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetLeaderboardRequest;
import dalalstreet.api.actions.GetLeaderboardResponse;
import dalalstreet.api.models.LeaderboardRow;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/* Uses GetLeaderboard() to set leader board table and to set user's current rank */
public class LeaderboardFragment extends Fragment {

    private static final int LEADERBOARD_SIZE = 15;

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    Metadata metadata;

    @BindView(R.id.leaderboard_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.personal_name_textView)
    TextView personalNameTextView;

    @BindView(R.id.personal_rank_textView)
    TextView personalRankTextView;

    @BindView(R.id.personal_wealth_textView)
    TextView personalWealthTextView;

    private ArrayList<LeaderboardDetails> leaderBoardDetailsList = new ArrayList<>();
    AlertDialog loadingDialog;
    TextView totalWorthTextView;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null){
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(R.layout.progress_dialog)
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

        loadingDialog.show();
        setValues();
        loadingDialog.dismiss();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(new LeaderboardRecyclerAdapter(getContext(), leaderBoardDetailsList));

        return rootView;
    }

    public void setValues(){

        leaderBoardDetailsList.clear();

        MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata);

        GetLeaderboardResponse leaderboardResponse = actionServiceBlockingStub.getLeaderboard(
                GetLeaderboardRequest.newBuilder().setCount(LEADERBOARD_SIZE).setStartingId(1).build()
        );

        if (leaderboardResponse.getStatusCode().getNumber() == 0) {
            personalRankTextView.setText(String.valueOf(leaderboardResponse.getMyRank()));
            personalWealthTextView.setText(totalWorthTextView.getText().toString());
            personalNameTextView.setText(MiscellaneousUtils.username);

            for (int i = 0; i < leaderboardResponse.getRankListCount(); ++i) {
                LeaderboardRow currentRow = leaderboardResponse.getRankList(i);
                leaderBoardDetailsList.add(new LeaderboardDetails(currentRow.getRank(), currentRow.getUserName(), currentRow.getTotalWorth()));
            }
        } else {
            Toast.makeText(getContext(), "Internal server error", Toast.LENGTH_SHORT).show();
        }
    }
}
