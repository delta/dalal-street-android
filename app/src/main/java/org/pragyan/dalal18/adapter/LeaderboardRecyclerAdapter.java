package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.data.LeaderboardDetails;
import org.pragyan.dalal18.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardRecyclerAdapter.MyViewHolder>{

    private List<LeaderboardDetails> leaderboardDetails = new ArrayList<>();
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

        if (position == 0) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(context, R.color.gold_medal));
            holder.rankTextView.setTextColor(ContextCompat.getColor(context, R.color.gold_medal));
            holder.wealthTextView.setTextColor(ContextCompat.getColor(context, R.color.gold_medal));
        } else if (position == 1) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(context, R.color.silver_medal));
            holder.rankTextView.setTextColor(ContextCompat.getColor(context, R.color.silver_medal));
            holder.wealthTextView.setTextColor(ContextCompat.getColor(context, R.color.silver_medal));
        } else if (position == 2) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(context, R.color.bronze_medal));
            holder.rankTextView.setTextColor(ContextCompat.getColor(context, R.color.bronze_medal));
            holder.wealthTextView.setTextColor(ContextCompat.getColor(context, R.color.bronze_medal));
        } else {
            holder.nameTextView.setTextColor(ContextCompat.getColor(context, R.color.neutral_font_color));
            holder.rankTextView.setTextColor(ContextCompat.getColor(context, R.color.neutral_font_color));
            holder.wealthTextView.setTextColor(ContextCompat.getColor(context, R.color.neutral_font_color));
        }
    }

    @Override
    public int getItemCount() {
        if (leaderboardDetails == null || leaderboardDetails.size() == 0) return 0;
        return leaderboardDetails.size();
    }

    public void swapData(List<LeaderboardDetails> list) {
        leaderboardDetails = list;
        notifyDataSetChanged();
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