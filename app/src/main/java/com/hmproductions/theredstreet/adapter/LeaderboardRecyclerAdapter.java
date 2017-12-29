package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.data.LeaderboardDetails;
import com.hmproductions.theredstreet.R;

import java.util.ArrayList;

public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardRecyclerAdapter.MyViewHolder>{

    private ArrayList<LeaderboardDetails> leaderboardDetails = new ArrayList<>();
    private Context context;

    public LeaderboardRecyclerAdapter(Context context, ArrayList<LeaderboardDetails> leaderboardDetails) {
        this.leaderboardDetails = leaderboardDetails;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false);
        return new LeaderboardRecyclerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        LeaderboardDetails currentLeaderboardDetails = leaderboardDetails.get(position);
        holder.rankTextView.setText(String.valueOf(currentLeaderboardDetails.getRank()));
        holder.nameTextView.setText(currentLeaderboardDetails.getName());
        holder.wealthTextView.setText(String.valueOf(currentLeaderboardDetails.getWealth()));
    }

    @Override
    public int getItemCount() {
        if (leaderboardDetails == null || leaderboardDetails.size() == 0) return 0;
        return leaderboardDetails.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        
        TextView rankTextView, nameTextView, wealthTextView;

        MyViewHolder(View view) {
            super(view);
            
            rankTextView = view.findViewById(R.id.rank_textView);
            nameTextView = view.findViewById(R.id.name_textView);
            wealthTextView = view.findViewById(R.id.wealth_textView);
        }
    }
}