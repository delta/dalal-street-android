package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.Transaction;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.List;

import static org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate;
import static org.pragyan.dalal18.utils.StockUtils.getCompanyNameFromStockId;

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionRecyclerAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.transactions_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String tempAssigningString;
        Transaction currentTransaction = transactionList.get(position);

        if(currentTransaction.getType() == null) return;

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
                if (currentTransaction.getNoOfStocks() > 0)
                    tempAssigningString = "Retrieve Mortgage";
                else
                    tempAssigningString = "Mortgage";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "DIVIDEND_TRANSACTION":
                tempAssigningString = "Dividend";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "TAX_TRANSACTION":
                tempAssigningString = "Tax";
                holder.typeTextView.setText(tempAssigningString);
                break;

            case "ORDER_FEE_TRANSACTION":
                tempAssigningString = "Order Fee";
                holder.typeTextView.setText(tempAssigningString);
                break;

                default:
                    holder.typeTextView.setText(currentTransaction.getType());
                    break;
        }

        tempAssigningString =  getCompanyNameFromStockId(currentTransaction.getStockId());
        holder.companyTextView.setText(tempAssigningString);

        tempAssigningString = parseDate(currentTransaction.getTime());
        if (tempAssigningString != null)
            holder.timeTextView.setText(tempAssigningString);
        if(currentTransaction.getType().equals("TAX_TRANSACTION") || currentTransaction.getType().equals("ORDER_FEE_TRANSACTION")) {
            tempAssigningString = Constants.RUPEE_SYMBOL + " " + String.valueOf(new DecimalFormat(Constants.PRICE_FORMAT).format(-currentTransaction.getTotalMoney()));
            holder.noOfStocksTextView.setText(tempAssigningString);
        } else {
            tempAssigningString = String.valueOf(Math.abs(currentTransaction.getNoOfStocks()))
                    + " stocks @ " + Constants.RUPEE_SYMBOL + " " + String.valueOf(new DecimalFormat(Constants.PRICE_FORMAT).format(Math.abs(currentTransaction.getStockPrice())));

            holder.noOfStocksTextView.setText(tempAssigningString);
        }

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