package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.Portfolio;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.List;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<Portfolio> portfolioList;
    private DecimalFormat decimalFormat = new DecimalFormat(Constants.PRICE_FORMAT);

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
        holder.reservedStocksTextView.setText(decimalFormat.format(currentPortfolioItem.getReservedStocks()));
        holder.quantityTextView.setText(decimalFormat.format(currentPortfolioItem.getQuantityOwned()));
        holder.worthTextView.setText(decimalFormat.format(currentPortfolioItem.getWorth()));

        String toastMessage = "";
        if (currentPortfolioItem.isBankrupt()) {
            holder.companyStatusIndicatorImageView.setVisibility(View.VISIBLE);
            holder.companyStatusIndicatorImageView.setImageResource(R.drawable.bankrupt_icon);
            toastMessage = "This company is bankrupt";
        } else if (currentPortfolioItem.getGivesDividend()) {
            holder.companyStatusIndicatorImageView.setVisibility(View.VISIBLE);
            holder.companyStatusIndicatorImageView.setImageResource(R.drawable.dividend_icon);
            toastMessage = "This company gives dividend";
        } else {
            holder.companyStatusIndicatorImageView.setVisibility(View.INVISIBLE);
        }

        if (!toastMessage.isEmpty()) {
            String finalToastMessage = toastMessage;
            holder.companyStatusIndicatorImageView.setOnClickListener(v -> Toast.makeText(context, finalToastMessage, Toast.LENGTH_SHORT).show());
        }
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
        ImageView companyStatusIndicatorImageView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            companyShortNameTextView = itemView.findViewById(R.id.companyName_textView);
            quantityTextView = itemView.findViewById(R.id.quantity_textView);
            reservedStocksTextView = itemView.findViewById(R.id.reservedStocksTextView);
            worthTextView = itemView.findViewById(R.id.worth_textView);
            companyStatusIndicatorImageView = itemView.findViewById(R.id.companyStatusIndicatorImageView);
        }
    }
}