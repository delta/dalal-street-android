package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.CompanyDetails;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompanyRecyclerAdapter extends RecyclerView.Adapter<CompanyRecyclerAdapter.PortfolioViewHolder> {

    private Context context;
    private List<CompanyDetails> list;
    private OnCompanyClickListener listener;

    public CompanyRecyclerAdapter(Context context, ArrayList<CompanyDetails> portfolioValues, OnCompanyClickListener listener) {
        this.list = portfolioValues;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CompanyRecyclerAdapter.PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.company_list_item, parent, false);
        return new PortfolioViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyRecyclerAdapter.PortfolioViewHolder holder, int position) {

        CompanyDetails currentCompany = list.get(position);

        String temp = currentCompany.getCompanyName();
        if (currentCompany.getGivesDividend()) {
            temp += context.getString(R.string.dividendSuffix);
        } else if (currentCompany.isBankrupt()) {
            temp += context.getString(R.string.bankruptSuffix);
        }

        holder.companyNameTextView.setText(temp);
        new Handler().postDelayed(() -> holder.companyNameTextView.setSelected(true), 1000);

        String temporaryString = new DecimalFormat(Constants.PRICE_FORMAT).format(currentCompany.getValue());
        holder.priceTextView.setText(temporaryString);

        double diff = (double) (currentCompany.getValue() - currentCompany.getPreviousDayClose()) / (double) currentCompany.getPreviousDayClose() * 100.0;
        holder.differenceTextView.setText(String.format(Locale.getDefault(), "%.1f", diff));
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

    public interface OnCompanyClickListener {
        void onCompanyClick(int stockId);
    }

    class PortfolioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView companyNameTextView, priceTextView, differenceTextView;

        PortfolioViewHolder(View itemView) {
            super(itemView);
            priceTextView = itemView.findViewById(R.id.price_textView);
            differenceTextView = itemView.findViewById(R.id.difference_textView);
            companyNameTextView = itemView.findViewById(R.id.companyName_textView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onCompanyClick(list.get(getAdapterPosition()).getStockId());
        }
    }
}
