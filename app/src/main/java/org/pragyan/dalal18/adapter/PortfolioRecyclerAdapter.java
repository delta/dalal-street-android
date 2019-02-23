package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.Portfolio;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<Portfolio> portfolioList;
    private DecimalFormat decimalFormat=new DecimalFormat(Constants.PRICE_FORMAT);

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

        holder.companyShortNameTextView.setText(currentPortfolioItem.getShortName());
        holder.reservedStocksTextView.setText(String.valueOf(decimalFormat.format(currentPortfolioItem.getReservedStocks())));
        holder.quantityTextView.setText(String.valueOf(decimalFormat.format(currentPortfolioItem.getQuantityOwned())));
        holder.worthTextView.setText(String.valueOf(decimalFormat.format(currentPortfolioItem.getWorth())));
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

        TextView companyShortNameTextView, quantityTextView, reservedStocksTextView, worthTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            companyShortNameTextView = itemView.findViewById(R.id.companyName_textView);
            quantityTextView = itemView.findViewById(R.id.quantity_textView);
            reservedStocksTextView = itemView.findViewById(R.id.reservedStocksTextView);
            worthTextView = itemView.findViewById(R.id.worth_textView);
        }
    }
}