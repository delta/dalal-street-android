package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.Portfolio;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<Portfolio> portfolioList;

    public PortfolioRecyclerAdapter(Context context, List<Portfolio> portfolioList) {
        this.context = context;
        this.portfolioList = portfolioList;
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.portfolio_list_item, parent, false);
        return new PortfolioViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {

        Portfolio currentPortfolioItem = portfolioList.get(position);

        if (currentPortfolioItem.getCompanyName() != null && currentPortfolioItem.getCompanyName().length() > 10)
            holder.companyNameTextView.setText(currentPortfolioItem.getShortName());
        else
            holder.companyNameTextView.setText(currentPortfolioItem.getCompanyName());

        holder.priceTextView.setText(String.valueOf(currentPortfolioItem.getPrice()));
        if (currentPortfolioItem.getPreviousDayClose() > currentPortfolioItem.getPrice())
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_orange));
        else if (currentPortfolioItem.getPreviousDayClose() < currentPortfolioItem.getPrice())
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
        else
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.neutral_font_color));

        holder.quantityTextView.setText(String.valueOf(currentPortfolioItem.getQuantityOwned()));

        holder.worthTextView.setText(String.valueOf(currentPortfolioItem.getPrice() * currentPortfolioItem.getQuantityOwned()));
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

        TextView companyNameTextView, quantityTextView, priceTextView, worthTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            companyNameTextView = itemView.findViewById(R.id.companyName_textView);
            quantityTextView = itemView.findViewById(R.id.quantity_textView);
            priceTextView = itemView.findViewById(R.id.price_textView);
            worthTextView = itemView.findViewById(R.id.worth_textView);
        }
    }
}