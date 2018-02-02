package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.Transaction;
import com.hmproductions.theredstreet.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        String tempAssigningString;
        Transaction currentTransaction = transactionList.get(position);

        switch (currentTransaction.getType()){
            case "FROM_EXCHANGE_TRANSACTION":
                tempAssigningString = "Exchange";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "ORDER_FILL_TRANSACTION":
                tempAssigningString = "OrderFill";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "MORTGAGE_TRANSACTION":
                if (currentTransaction.getNoOfStocks() < 0)
                    tempAssigningString = "Retrieve Mortgage";
                else
                    tempAssigningString = "Mortgage";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "DIVIDEND_TRANSACTION":
                tempAssigningString = "Dividend";
                holder.typeTextView.setText(tempAssigningString);
                break;
        }

        tempAssigningString =  getCompanyNameFromStockId(currentTransaction.getStockId());
        holder.companyTextView.setText(tempAssigningString);

        tempAssigningString = parseDate(currentTransaction.getTime());
        holder.timeTextView.setText(tempAssigningString);

        tempAssigningString = String.valueOf(Math.abs(currentTransaction.getNoOfStocks()))
                    + " stocks @ " + Constants.RUPEE_SYMBOL + " " + String.valueOf(Math.abs(currentTransaction.getTotalMoney()));
        holder.noOfStocksTextView.setText(tempAssigningString);

        GradientDrawable buySellDrawable = (GradientDrawable)holder.buySellTextView.getBackground();
        if (currentTransaction.getTotalMoney() < 0) {
            holder.buySellTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
            buySellDrawable.setStroke(2, ContextCompat.getColor(context, R.color.neon_green));
            holder.buySellTextView.setText(context.getString(R.string.buy));
        } else {
            holder.buySellTextView.setTextColor(ContextCompat.getColor(context, R.color.redTint));
            buySellDrawable.setStroke(2, ContextCompat.getColor(context, R.color.redTint));
            holder.buySellTextView.setText(context.getString(R.string.sell));
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

    private String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String outputPattern = "hh:mm a    MMM dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView typeTextView, companyTextView, noOfStocksTextView, timeTextView, buySellTextView;

        MyViewHolder(View itemView) {

            super(itemView);

            typeTextView = itemView.findViewById(R.id.transactionType_textView);
            companyTextView = itemView.findViewById(R.id.transactionCompany_textView);
            noOfStocksTextView = itemView.findViewById(R.id.noOfStocks_textView);
            timeTextView = itemView.findViewById(R.id.time_textView);
            buySellTextView = itemView.findViewById(R.id.buySell_textView);
        }
    }
}