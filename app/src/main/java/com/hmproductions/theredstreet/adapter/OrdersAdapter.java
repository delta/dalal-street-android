package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.Orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder> {

    private Context context;
    private List<Orders> orderList;

    public OrdersAdapter(Context context, List<Orders> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders, parent, false);
        return new OrdersAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Orders orders=orderList.get(orderList.size()-position-1);

        holder.type.setText("Order Type : "+orders.getOrderType());
        if(orders.isBid())
            holder.bidOrAsk.setText(context.getString(R.string.bid));
        else
            holder.bidOrAsk.setText(context.getString(R.string.ask));

        holder.company.setText("Company : "+orders.getCompany());
        holder.status.setText("Status : "+orders.getStatus());
        holder.price.setText("Order price : "+ String.valueOf(orders.getOrderPrice()));

        if(orders.getOrderType().equals("Market Order"))
        {
            holder.price.setVisibility(View.INVISIBLE);
        }

        if(orders.getStatus().equals("Completed"))
        {
            holder.cancel.setEnabled(false);
        }

        holder.pieChart.setUsePercentValues(false);

        holder.pieChart.setDrawHoleEnabled(false);
        holder.pieChart.setTransparentCircleRadius(5);
        holder.pieChart.setDrawLegend(false);
        holder.pieChart.setDescription("Number of stocks filled vs the price");
        holder.pieChart.setDescriptionTextSize(5);

        ArrayList<Entry> pieY=new ArrayList<>();
        for(int i=0;i<orders.getNoOfStocks().length;i++){
            pieY.add(new Entry(orders.getNoOfStocks()[i],i));
        }

        ArrayList<String> pieX = new ArrayList<>();
        Collections.addAll(pieX, orders.getPrice());

        PieDataSet dataSet = new PieDataSet(pieY,"Market share");
        dataSet.setSliceSpace(0);
        dataSet.setSelectionShift(5);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        colors.add(ContextCompat.getColor(context,android.R.color.darker_gray));
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        dataSet.setColors(colors);

        PieData data = new PieData(pieX, dataSet);

        holder.pieChart.setDescription("");
        holder.pieChart.animateX(1000);
        holder.pieChart.setData(data);
        holder.pieChart.invalidate();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView type,bidOrAsk,price,status,company;
        Button cancel;
        PieChart pieChart;

        MyViewHolder(View view) {
            super(view);
            type= view.findViewById(R.id.order_type);
            bidOrAsk= view.findViewById(R.id.bidOrAsk);
            price= view.findViewById(R.id.order_price);
            status= view.findViewById(R.id.order_status);
            company= view.findViewById(R.id.order_company);
            cancel= view.findViewById(R.id.cancel);
            pieChart= view.findViewById(R.id.piechart);

        }
    }
}