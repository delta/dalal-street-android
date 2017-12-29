package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.LeaderboardRecyclerAdapter;
import com.hmproductions.theredstreet.data.LeaderboardDetails;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LeaderboardFragment extends Fragment {

    @BindView(R.id.leaderboard_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.personal_name_textView)
    TextView personalNameTextView;

    @BindView(R.id.personal_rank_textView)
    TextView personalRankTextView;

    @BindView(R.id.personal_wealth_textView)
    TextView personalWealthTextView;

    private ArrayList<LeaderboardDetails> leaderBoardDetailsList = new ArrayList<>();
    private LeaderboardRecyclerAdapter leaderboardRecyclerAdapter;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_leaderboard, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle("Leaderboard");

        setValues();

        leaderboardRecyclerAdapter = new LeaderboardRecyclerAdapter(getContext(), leaderBoardDetailsList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(leaderboardRecyclerAdapter);

        personalNameTextView.setText("Username");  //todo : add our details
        personalRankTextView.setText("9");
        personalWealthTextView.setText("3500");

        return rootView;
    }

    public void setValues(){
        leaderBoardDetailsList.clear(); //TODO : Get from service

        leaderBoardDetailsList.add(new LeaderboardDetails(1,"Delta Force",5000));
        leaderBoardDetailsList.add(new LeaderboardDetails(2,"Pai",4500));
        leaderBoardDetailsList.add(new LeaderboardDetails(3,"Sibi",4400));
        leaderBoardDetailsList.add(new LeaderboardDetails(4,"Santhosh",4300));
        leaderBoardDetailsList.add(new LeaderboardDetails(5,"Thakkar",4200));
        leaderBoardDetailsList.add(new LeaderboardDetails(6,"Rb",4200));
        leaderBoardDetailsList.add(new LeaderboardDetails(7,"Jumbo",4200));
        leaderBoardDetailsList.add(new LeaderboardDetails(8,"Santa",3900));
        leaderBoardDetailsList.add(new LeaderboardDetails(9,"Username",3500));
        leaderBoardDetailsList.add(new LeaderboardDetails(10,"Deep",2100));
    }
}
