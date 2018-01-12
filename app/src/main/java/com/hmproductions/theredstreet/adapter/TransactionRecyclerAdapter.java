package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.Transaction;

import java.util.List;

import static com.hmproductions.theredstreet.utils.StockUtils.getCompanyNameFromStockId;

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionRecyclerAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.transactions_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Transaction currentTransaction = transactionList.get(position);

        holder.typeTextView.setText("Transaction Type : " + String.valueOf(currentTransaction.getType()));
        holder.companyTextView.setText("Company : " + getCompanyNameFromStockId(currentTransaction.getStockId()));
        holder.noOfStocksTextView.setText("Number of stocks : " + String.valueOf(currentTransaction.getNoOfStocks()));
        holder.priceTextView.setText("Stock price : " + String.valueOf(currentTransaction.getStockPrice()));
        holder.timeTextView.setText("Time : " + currentTransaction.getTime());

        if (currentTransaction.getTotalMoney() >= 0) {
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.greenTint));
        } else {
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.redTint));
        }
    }

    @Override
    public int getItemCount() {
        if (transactionList == null || transactionList.size() == 0) return 0;
        return transactionList.size();
    }

    public void swapData(List<Transaction> newList) {
        transactionList = newList;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView typeTextView, companyTextView, noOfStocksTextView, priceTextView, timeTextView;
        RelativeLayout relativeLayout;

        MyViewHolder(View itemView) {

            super(itemView);

            typeTextView = itemView.findViewById(R.id.transactionType_textView);
            companyTextView = itemView.findViewById(R.id.transactionCompany_textView);
            noOfStocksTextView = itemView.findViewById(R.id.noOfStocks_textView);
            priceTextView = itemView.findViewById(R.id.stockPrice_textView);
            timeTextView = itemView.findViewById(R.id.time_textView);
            relativeLayout = itemView.findViewById(R.id.list_item_relativeLayout);
        }
    }
}