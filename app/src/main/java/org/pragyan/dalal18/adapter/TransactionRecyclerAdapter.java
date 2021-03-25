package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.GlobalStockDetails;

import java.util.HashMap;
import java.util.List;

import dalalstreet.api.models.Transaction;

import static org.pragyan.dalal18.utils.LongEvaluatorKt.formatTransactionType;
import static org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate;

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private HashMap<Integer, GlobalStockDetails> globalStockDetailsHashMap;

    private static final String COLON_SEPARATOR = " : ";

    public TransactionRecyclerAdapter(Context context, List<Transaction> transactionList, HashMap<Integer, GlobalStockDetails> map) {
        this.context = context;
        this.transactionList = transactionList;
        globalStockDetailsHashMap = map;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.transactions_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Transaction currentTransaction = transactionList.get(position);
        GlobalStockDetails currentStock = globalStockDetailsHashMap.get(currentTransaction.getStockId());

        if (currentTransaction.getType() == null || currentStock == null) return;

        holder.typeTextView.setText(formatTransactionType(currentTransaction.getType().name()));
        holder.companyTextView.setText(currentStock.getFullName());
        holder.timeTextView.setText(parseDate(currentTransaction.getCreatedAt()));

        String tempString = COLON_SEPARATOR + currentTransaction.getStockQuantity();
        if (currentTransaction.getStockQuantity() > 0)
            holder.quantityTextView.setTextColor(Color.GREEN);
        else if (currentTransaction.getStockQuantity() < 0)
            holder.quantityTextView.setTextColor(Color.RED);
        holder.quantityTextView.setText(tempString);

        tempString = COLON_SEPARATOR + currentTransaction.getPrice();
        holder.tradePriceTextView.setText(tempString);

        tempString = COLON_SEPARATOR + currentTransaction.getTotal();
        if (currentTransaction.getTotal() > 0)
            holder.cashTextView.setTextColor(Color.GREEN);
        else if (currentTransaction.getTotal() < 0)
            holder.cashTextView.setTextColor(Color.RED);
        holder.cashTextView.setText(tempString);

        if (currentTransaction.getReservedStockQuantity() != 0) {
            tempString = COLON_SEPARATOR + currentTransaction.getReservedStockQuantity();
            holder.reservedAssetsText.setText(context.getString(R.string.stocks_reserved));
            holder.reservedAssetsTextView.setText(tempString);
        } else {
            tempString = COLON_SEPARATOR +
                    (currentTransaction.getReservedCashTotal() != 0 ? currentTransaction.getReservedCashTotal() : "-");
            holder.reservedAssetsText.setText(context.getString(R.string.cash_reserved));
            holder.reservedAssetsTextView.setText(tempString);
        }
    }

    @Override
    public int getItemCount() {
        if (transactionList == null || transactionList.size() == 0) return 0;
        return transactionList.size();
    }

    public void setList(List<Transaction> newList) {
        transactionList = newList;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView typeTextView, companyTextView, quantityTextView, timeTextView, reservedAssetsText,
                reservedAssetsTextView, cashTextView, tradePriceTextView;

        MyViewHolder(View itemView) {

            super(itemView);

            typeTextView = itemView.findViewById(R.id.transactionType_textView);
            companyTextView = itemView.findViewById(R.id.transactionCompanyTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            timeTextView = itemView.findViewById(R.id.time_textView);
            reservedAssetsText = itemView.findViewById(R.id.reservedAssetsText);
            reservedAssetsTextView = itemView.findViewById(R.id.reservedAssetsTextView);
            cashTextView = itemView.findViewById(R.id.cashTextView);
            tradePriceTextView = itemView.findViewById(R.id.tradePriceTextView);
        }
    }
}
