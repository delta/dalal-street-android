package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

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
                tempAssigningString = "Type : Exchange";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "ORDER_FILL_TRANSACTION":
                tempAssigningString = "Type : OrderFill";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "MORTGAGE_TRANSACTION":
                if (currentTransaction.getNoOfStocks() < 0)
                    tempAssigningString = "Type : RetrieveMortgage";
                else
                    tempAssigningString = "Type : Mortgage";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "DIVIDEND_TRANSACTION":
                tempAssigningString = "Type : Dividend";
                holder.typeTextView.setText(tempAssigningString);
                break;
        }

        tempAssigningString =  getCompanyNameFromStockId(currentTransaction.getStockId());
        holder.companyTextView.setText(tempAssigningString);

        tempAssigningString = "Quantity : " + String.valueOf(Math.abs(currentTransaction.getNoOfStocks()));
        holder.noOfStocksTextView.setText(tempAssigningString);

        tempAssigningString = "Stock price : " + String.valueOf(currentTransaction.getStockPrice());
        holder.priceTextView.setText(tempAssigningString);

        tempAssigningString = "Time : " + parseDate(currentTransaction.getTime());
        holder.timeTextView.setText(tempAssigningString);

        tempAssigningString = "Total : " + String.valueOf(currentTransaction.getTotalMoney());
        holder.totalPriceTextView.setText(tempAssigningString);

        if (currentTransaction.getTotalMoney() >= 0) {
            holder.totalPriceTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
        } else {
            holder.totalPriceTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_red));
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

        @BindView(R.id.transactions_item_layout)
        LinearLayout transacLayout;

        @BindView(R.id.transactionType_textView)
        TextView typeTextView;

        @BindView(R.id.transactionCompany_textView)
        TextView companyTextView;

        @BindView(R.id.noOfStocks_textView)
        TextView noOfStocksTextView;

        @BindView(R.id.stockPrice_textView)
        TextView priceTextView;

        @BindView(R.id.time_textView)
        TextView timeTextView;

        @BindView(R.id.total_price_text_view)
        TextView totalPriceTextView;

        MyViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    private String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String outputPattern = "hh:mm a  MMM dd, yyyy";
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
}