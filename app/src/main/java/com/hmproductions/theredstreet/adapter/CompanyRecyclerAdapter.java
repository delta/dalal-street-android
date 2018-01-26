package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.CompanyDetails;
import com.hmproductions.theredstreet.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompanyRecyclerAdapter extends RecyclerView.Adapter<CompanyRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<CompanyDetails> list;

    public CompanyRecyclerAdapter(Context context, ArrayList<CompanyDetails> portfolioValues) {
        this.list = portfolioValues;
        this.context = context;
    }

    @Override
    public CompanyRecyclerAdapter.PortfolioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.company_list_item, parent, false);
        return new PortfolioViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(CompanyRecyclerAdapter.PortfolioViewHolder holder, int position) {

        if (list.get(position).getCompany().length() >= 10) {
            holder.portfolioCompanyNameTextView.setText(list.get(position).getShortName());
        } else {
            holder.portfolioCompanyNameTextView.setText(list.get(position).getCompany());
        }

        String temporaryString = Constants.RUPEE_SYMBOL + String.valueOf(list.get(position).getValue()) + "/stock";
        holder.portfolioPriceTextView.setText(temporaryString);
    }

    @Override
    public int getItemCount() {
        if (list == null || list.size() == 0) return 0;
        return list.size();
    }

    public void swapData(List<CompanyDetails> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    class PortfolioViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.portfolio_company_textView)
        TextView portfolioCompanyNameTextView;

        @BindView(R.id.portfolio_price_textView)
        TextView portfolioPriceTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
