package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.CompanyDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        CompanyDetails currentCompanyDetails = list.get(position);

        if (currentCompanyDetails.getCompany().length() > 14) {
            holder.companyNameTextView.setText(currentCompanyDetails.getShortName());
        } else {
            holder.companyNameTextView.setText(currentCompanyDetails.getCompany());
        }

        String temporaryString = String.valueOf(currentCompanyDetails.getValue()) + "/stock";
        holder.priceTextView.setText(temporaryString);

        double diff = (double)(currentCompanyDetails.getValue() - currentCompanyDetails.getPreviousDayClose()) / (double)currentCompanyDetails.getPreviousDayClose() * 100.0;
        holder.differenceTextView.setText(String.format(Locale.getDefault(),"%.1f", diff));
        if (diff < 0) {
            holder.differenceTextView.setTextColor(ContextCompat.getColor(context, R.color.redTint));
        } else if (diff == 0) {
            holder.differenceTextView.setTextColor(ContextCompat.getColor(context, R.color.neutral_font_color));
        } else {
            holder.differenceTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
        }
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

        @BindView(R.id.companyName_textView)
        TextView companyNameTextView;

        @BindView(R.id.price_textView)
        TextView priceTextView;

        @BindView(R.id.difference_textView)
        TextView differenceTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
