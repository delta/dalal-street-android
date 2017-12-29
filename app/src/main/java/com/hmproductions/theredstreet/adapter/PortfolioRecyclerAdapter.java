package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.PortfolioDetails;

import java.util.ArrayList;
import java.util.List;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<PortfolioDetails> list;

    public PortfolioRecyclerAdapter(Context context, ArrayList<PortfolioDetails> portfolioValues) {
        this.list = portfolioValues;
        this.context = context;
    }

    @Override
    public PortfolioRecyclerAdapter.PortfolioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.portfolio_list_item, parent, false);
        return new PortfolioViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(PortfolioRecyclerAdapter.PortfolioViewHolder holder, int position) {
        holder.portfolioDetailsTextView.setText(list.get(position).getPortfolioDetails());
    }

    @Override
    public int getItemCount() {
        if (list == null || list.size() == 0) return 0;
        return list.size();
    }

    class PortfolioViewHolder extends RecyclerView.ViewHolder {

        private TextView portfolioDetailsTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            portfolioDetailsTextView = itemView.findViewById(R.id.portfolioDetails_textView);
        }
    }
}
