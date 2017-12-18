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

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.MyViewHolder>{


    private Context context;
    private List<Transaction> transactionList;

    public TransactionRecyclerAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.transactions, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        int size=transactionList.size();

        Transaction transaction =transactionList.get(size-position-1);

        holder.type.setText("Transaction Type : "+ transaction.getType());
        holder.company.setText("Company : "+ transaction.getCompany());
        holder.noOfStocks.setText("Numberof stocks : "+ String.valueOf(transaction.getNoOfStocks()));
        holder.price.setText("Stock price : "+ String.valueOf(transaction.getStockPrice()));
        holder.time.setText("Time : "+ transaction.getTime());

        if(transaction.getTotalMoney()>=0){
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.greenTint));
        }
        else{
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.redTint));

        }

    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView type,company,noOfStocks,price,time;
        RelativeLayout relativeLayout;

        public MyViewHolder(View view) {
            super(view);
            type=(TextView)view.findViewById(R.id.transaction_type);
            company=(TextView)view.findViewById(R.id.transaction_company);
            noOfStocks=(TextView)view.findViewById(R.id.trans_noOfStocks);
            price=(TextView)view.findViewById(R.id.trans_stockPrice);
            time=(TextView)view.findViewById(R.id.trans_time);
            relativeLayout=(RelativeLayout)view.findViewById(R.id.trans);
        }
    }

}
