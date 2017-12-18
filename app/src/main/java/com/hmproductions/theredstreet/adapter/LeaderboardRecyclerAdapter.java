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

/**
 * Created by ravi on 11/2/17.
 */

public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardRecyclerAdapter.MyViewHolder>{

    ArrayList<LeaderboardDetails> leaderboardDetails;
    Context context;

    public LeaderboardRecyclerAdapter(ArrayList<LeaderboardDetails> leaderboardDetails, Context context) {
        this.leaderboardDetails = leaderboardDetails;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_list, parent, false);

        return new LeaderboardRecyclerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        LeaderboardDetails details=leaderboardDetails.get(position);
        holder.rank.setText(String.valueOf(details.getRank()));
        holder.name.setText(details.getName());
        holder.wealth.setText(String.valueOf(details.getTotal_wealth()));
    }

    @Override
    public int getItemCount() {
        return leaderboardDetails.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView rank,name,wealth;

        public MyViewHolder(View view) {
            super(view);
            rank=(TextView)view.findViewById(R.id.rank_list);
            name=(TextView)view.findViewById(R.id.name_list);
            wealth=(TextView)view.findViewById(R.id.wealth_list);
        }
    }

}
