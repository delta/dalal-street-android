package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.Portfolio;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<Portfolio> portfolioList = new ArrayList<>();

    public PortfolioRecyclerAdapter(Context context, List<Portfolio> portfolioList) {
        this.context = context;
        this.portfolioList = portfolioList;
    }

    @Override
    public PortfolioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.portfolio_list_item, parent, false);
        return new PortfolioViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(PortfolioViewHolder holder, int position) {

        if (portfolioList.get(position).getCompanyName().length() > 10 && portfolioList.get(position).getShortname() != null)
            holder.companyNameTextView.setText(portfolioList.get(position).getShortname());
        else
            holder.companyNameTextView.setText(portfolioList.get(position).getCompanyName());

        holder.priceTextView.setText(String.valueOf(portfolioList.get(position).getPrice()));
        holder.quantityTextView.setText(String.valueOf(portfolioList.get(position).getQuantityOwned()));
    }

    @Override
    public int getItemCount() {
        if (portfolioList == null || portfolioList.size() == 0) return 0;
        return portfolioList.size();
    }

    public void swapData(List<Portfolio> newList) {
        this.portfolioList = newList;
        notifyDataSetChanged();
    }

    class PortfolioViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.companyName_textView)
        TextView companyNameTextView;

        @BindView(R.id.quantity_textView)
        TextView quantityTextView;

        @BindView(R.id.price_textView)
        TextView priceTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}