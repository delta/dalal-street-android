package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.Order;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.StockUtils;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import dalalstreet.api.datastreams.MyOrderUpdate;
import dalalstreet.api.models.OrderType;

public class OrdersRecyclerAdapter extends RecyclerView.Adapter<OrdersRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Order> openOrdersList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onCancelOrderClick(int orderId, boolean bid);
    }

    public OrdersRecyclerAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.openOrdersList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.order_list_item, parent, false);
        return new OrdersRecyclerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Order order = openOrdersList.get(position);
        String tempString;

        tempString = (order.isBid() ? "BID - " : "ASK - ") + StockUtils.getOrderTypeFromType(order.getOrderType());
        holder.typeTextView.setText(tempString);

        if (holder.typeTextView.getText().toString().substring(0, 3).equals("BID"))
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
        else
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_blue));

        tempString = StockUtils.getCompanyNameFromStockId(order.getStockId());
        holder.companyNameTextView.setText(tempString);

        tempString = String.valueOf(order.getStockQuantityFulfilled()) + " / " + String.valueOf(order.getStockQuantity());
        holder.quantityTextView.setText(tempString);

        if (order.getOrderType() == OrderType.MARKET) {
            holder.priceTextView.setVisibility(View.GONE);
        } else {
            holder.priceTextView.setVisibility(View.VISIBLE);
            tempString = (order.getStockQuantityFulfilled() == 0 ? "Placed " : order.getStockQuantity() == order.getStockQuantityFulfilled() ? "Filled" : "Partially Filled") + " at " +
                    Constants.RUPEE_SYMBOL + " " + String.valueOf(order.getPrice() + "/stock");
            holder.priceTextView.setText(tempString);
        }

        holder.quantitySeekbar.setMax((int) order.getStockQuantity());
        holder.quantitySeekbar.setProgress((int) order.getStockQuantityFulfilled());

        holder.quantitySeekbar.setOnTouchListener((view, motionEvent) -> true);
    }

    @Override
    public int getItemCount() {
        if (openOrdersList == null || openOrdersList.size() == 0) return 0;
        return openOrdersList.size();
    }

    // Returns true if openOrdersList is empty
    public boolean swapData(List<Order> list) {

        openOrdersList = list;

        Collections.sort(openOrdersList, (o1, o2) -> {
            if (o1.isBid() && !o2.isBid())
                return 1;
            else if (o2.isBid() && !o1.isBid())
                return -1;
            else
                return 0;
        });

        notifyDataSetChanged();

        return openOrdersList.size() <= 0;
    }

    // Returns true if openOrdersList is empty
    public boolean updateOrder(MyOrderUpdate order) {

        if (order.getIsNewOrder()) {
            openOrdersList.add(new Order(order.getId(), !order.getIsAsk(), order.getIsClosed(), order.getOrderPrice(), order.getStockId(),
                    order.getOrderType(), Math.abs(order.getStockQuantity()), 0));
            notifyItemInserted(openOrdersList.size() - 1);
        } else {
            int position = -1;

            for (int i = 0; i < openOrdersList.size(); ++i) {
                Order currentOrder = openOrdersList.get(i);
                if (currentOrder.getOrderId() == order.getId()) {
                    position = i;
                    break;
                }
            }

            if (position != -1 && order.getIsClosed()) {
                openOrdersList.remove(position);
                notifyItemRemoved(position);
            } else if (position != -1) {
                openOrdersList.get(position).incrementQuantityFulfilled(Math.abs(order.getStockQuantity()));
                notifyItemChanged(position);
            }
        }

        return openOrdersList.size() <= 0;
    }

    // Returns true if openOrdersList is empty
    public boolean cancelOrder(int orderId) {
        int position = -1;

        for (int i = 0; i < openOrdersList.size(); ++i) {
            Order currentOrder = openOrdersList.get(i);
            if (currentOrder.getOrderId() == orderId) {
                position = i;
                break;
            }
        }

        openOrdersList.remove(position);
        notifyItemRemoved(position);

        return openOrdersList.size() <= 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView typeTextView, priceTextView, quantityTextView, companyNameTextView;
        Button cancelButton;
        SeekBar quantitySeekbar;

        MyViewHolder(View view) {
            super(view);

            typeTextView = view.findViewById(R.id.orderType_textView);
            priceTextView = view.findViewById(R.id.orderPrice_textView);
            quantityTextView = view.findViewById(R.id.quantity_textView);
            companyNameTextView = view.findViewById(R.id.company_textView);
            cancelButton = view.findViewById(R.id.cancel_button);
            quantitySeekbar = view.findViewById(R.id.stockDisplay_seekBar);

            cancelButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onCancelOrderClick(openOrdersList.get(getAdapterPosition()).getOrderId(), openOrdersList.get(getAdapterPosition()).isBid());
        }
    }
}