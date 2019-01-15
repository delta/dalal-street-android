package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.MortgageDetails;
import org.pragyan.dalal18.utils.StockUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RetrieveRecyclerAdapter extends RecyclerView.Adapter<RetrieveRecyclerAdapter.RetrieveViewHolder> {

    private Context context;
    private List<MortgageDetails> mortgageDetailsList;
    private OnRetrieveButtonClickListener listener;

    public interface OnRetrieveButtonClickListener {
        void onRetrieveButtonClick(int position, int quantity);
    }

    public RetrieveRecyclerAdapter(Context context, List<MortgageDetails> mortgageDetailsList, OnRetrieveButtonClickListener listener) {
        this.context = context;
        this.mortgageDetailsList = mortgageDetailsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RetrieveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RetrieveViewHolder(LayoutInflater.from(context).inflate(R.layout.retrieve_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RetrieveViewHolder holder, int position) {

        MortgageDetails currentDetails = mortgageDetailsList.get(position);

        holder.companyNameTextView.setText(StockUtils.getCompanyNameFromStockId(currentDetails.getStockId()));
        holder.mortgagePriceTextView.setText(String.valueOf(currentDetails.getMortgagePrice()));
        holder.stockQuantityTextView.setText(String.valueOf(currentDetails.getStockQuantity()));
    }

    public void swapData(List<MortgageDetails> newList) {
        mortgageDetailsList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(mortgageDetailsList == null || mortgageDetailsList.size() == 0) return 0;
        return mortgageDetailsList.size();
    }

    public class RetrieveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView stockQuantityTextView, companyNameTextView, mortgagePriceTextView;
        Button retrieveButton;
        EditText retrieveQuantityTextView;

        RetrieveViewHolder(@NonNull View itemView) {
            super(itemView);
            stockQuantityTextView = itemView.findViewById(R.id.stockQuantityTextView);
            companyNameTextView = itemView.findViewById(R.id.companyNameTextView);
            mortgagePriceTextView = itemView.findViewById(R.id.mortgagePriceTextView);
            retrieveButton = itemView.findViewById(R.id.retrieveButton);
            retrieveQuantityTextView = itemView.findViewById(R.id.retrieveQuantityTextView);

            retrieveButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRetrieveButtonClick(getAdapterPosition(), Integer.parseInt(retrieveQuantityTextView.getText().toString()));
        }
    }
}
