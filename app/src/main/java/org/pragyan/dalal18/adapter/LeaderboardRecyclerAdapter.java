package org.pragyan.dalal18.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.data.LeaderBoardDetails;
import org.pragyan.dalal18.R;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardRecyclerAdapter.MyViewHolder> {

    private List<LeaderBoardDetails> leaderBoardDetails;
    private Context context;

    public LeaderboardRecyclerAdapter(Context context, ArrayList<LeaderBoardDetails> leaderBoardDetails) {
        this.leaderBoardDetails = leaderBoardDetails;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false);
        return new LeaderboardRecyclerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        LeaderBoardDetails currentLeaderBoardDetails = leaderBoardDetails.get(position);

        holder.rankTextView.setText(String.valueOf(currentLeaderBoardDetails.getRank()));
        holder.nameTextView.setText(currentLeaderBoardDetails.getName());
        holder.wealthTextView.setText(String.valueOf(new DecimalFormat(Constants.PRICE_FORMAT).format(currentLeaderBoardDetails.getWealth())));

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
        if (leaderBoardDetails == null || leaderBoardDetails.size() == 0) return 0;
        return leaderBoardDetails.size();
    }

    public void swapData(List<LeaderBoardDetails> list) {
        leaderBoardDetails = list;
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