package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.MortgageDetails;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.List;

public class RetrieveRecyclerAdapter extends RecyclerView.Adapter<RetrieveRecyclerAdapter.RetrieveViewHolder> {

    private Context context;
    private List<MortgageDetails> mortgageDetailsList;
    private OnRetrieveButtonClickListener listener;

    public interface OnRetrieveButtonClickListener {
        void onRetrieveButtonClick(int position, String retrieveQuantity, String stocksQuantity);
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
        holder.companyNameTextView.setText(currentDetails.getShortName());
        holder.mortgagePriceTextView.setText(new DecimalFormat(Constants.PRICE_FORMAT).format(currentDetails.getMortgagePrice()));
        holder.stockQuantityTextView.setText(String.valueOf(currentDetails.getStockQuantity()));
        holder.retrieveQuantityEditText.setText("");
    }

    public void swapData(List<MortgageDetails> newList) {
        mortgageDetailsList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mortgageDetailsList == null || mortgageDetailsList.size() == 0) return 0;
        return mortgageDetailsList.size();
    }

    public class RetrieveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView stockQuantityTextView, companyNameTextView, mortgagePriceTextView;
        Button retrieveButton;
        EditText retrieveQuantityEditText;

        RetrieveViewHolder(@NonNull View itemView) {
            super(itemView);
            stockQuantityTextView = itemView.findViewById(R.id.stockQuantityTextView);
            companyNameTextView = itemView.findViewById(R.id.companyNameTextView);
            mortgagePriceTextView = itemView.findViewById(R.id.mortgagePriceTextView);
            retrieveButton = itemView.findViewById(R.id.retrieveButton);
            retrieveQuantityEditText = itemView.findViewById(R.id.retrieveQuantityEditText);

            retrieveButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRetrieveButtonClick(getAdapterPosition(), retrieveQuantityEditText.getText().toString(), stockQuantityTextView.getText().toString());
        }
    }
}
