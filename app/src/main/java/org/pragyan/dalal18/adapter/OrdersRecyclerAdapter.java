package org.pragyan.dalal18.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nineoldandroids.view.ViewPropertyAnimator;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.CustomOrderUpdate;
import org.pragyan.dalal18.data.Order;
import org.pragyan.dalal18.utils.Constants;
import org.pragyan.dalal18.utils.OrderTypeUtils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import dalalstreet.api.models.OrderType;

import static org.pragyan.dalal18.utils.Constants.CANCEL_ORDER_TOUR_KEY;

public class OrdersRecyclerAdapter extends RecyclerView.Adapter<OrdersRecyclerAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> openOrdersList;
    private SwipeToCancelListener listener;
    private SharedPreferences preferences;

    private int screenWidth;
    private OrderViewHolder firstPositionViewHolder = null;

    public interface SwipeToCancelListener {
        void showTapTargetForNewOrder(View view);
    }

    public OrdersRecyclerAdapter(Context context, List<Order> orderList, SwipeToCancelListener listener, SharedPreferences preferences) {
        this.context = context;
        this.openOrdersList = orderList;
        this.listener = listener;
        this.preferences = preferences;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.order_list_item, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Order order = openOrdersList.get(position);
        String tempString;

        tempString = (order.isBid() ? "BID - " : "ASK - ") + OrderTypeUtils.getOrderTypeFromType(order.getOrderType());
        holder.typeTextView.setText(tempString);

        if (holder.typeTextView.getText().toString().substring(0, 3).equals("BID"))
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
        else
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.neon_blue));

        tempString = order.getCompanyName();
        holder.companyNameTextView.setText(tempString);

        tempString = order.getStockQuantityFulfilled() + " / " + order.getStockQuantity();
        holder.quantityTextView.setText(tempString);

        if (order.getOrderType() == OrderType.MARKET) {
            holder.priceTextView.setVisibility(View.GONE);
        } else {
            holder.priceTextView.setVisibility(View.VISIBLE);
            tempString = (order.getStockQuantityFulfilled() == 0 ? "Placed " : order.getStockQuantity() == order.getStockQuantityFulfilled() ? "Filled" : "Partially Filled") + " at " +
                    Constants.RUPEE_SYMBOL + " " + new DecimalFormat(Constants.PRICE_FORMAT).format(order.getPrice()) + "/stock";
            holder.priceTextView.setText(tempString);
        }

        if (position == 0 && !preferences.getBoolean(CANCEL_ORDER_TOUR_KEY, false)) {
            firstPositionViewHolder = holder;
            ViewPropertyAnimator.animate(holder.orderViewForeground).translationXBy(-screenWidth / 2).setDuration(450);
            listener.showTapTargetForNewOrder(holder.deleteOrderText);
            preferences.edit().putBoolean(CANCEL_ORDER_TOUR_KEY, true).apply();
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

    public void swipeBackFirstOrder() {
        if (firstPositionViewHolder != null) {
            ViewPropertyAnimator.animate(firstPositionViewHolder.orderViewForeground).translationXBy(screenWidth / 2).setDuration(450);
            firstPositionViewHolder = null;
        }
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
    public boolean updateOrder(CustomOrderUpdate orderUpdate, String companyName) {

        if (orderUpdate.isNewOrder()) {
            openOrdersList.add(new Order(orderUpdate.getOrderId(), !orderUpdate.isAsk(), orderUpdate.isClosed(), orderUpdate.getOrderPrice(), orderUpdate.getStockId(),
                    companyName, orderUpdate.getOrderType(), Math.abs(orderUpdate.getStockQuantity()), 0));
            notifyItemInserted(openOrdersList.size() - 1);
        } else {
            int position = -1;

            for (int i = 0; i < openOrdersList.size(); ++i) {
                if (openOrdersList.get(i).getOrderId() == orderUpdate.getOrderId()) {
                    position = i;
                    break;
                }
            }

            if (position != -1 && orderUpdate.isClosed()) {
                openOrdersList.remove(position);
                notifyItemRemoved(position);
            } else if (position != -1) {
                openOrdersList.get(position).incrementQuantityFulfilled(Math.abs(orderUpdate.getStockQuantity()));
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

    public int getOrderIdFromPosition(int position) {
        return openOrdersList.get(position).getOrderId();
    }

    public boolean getIsBidFromPosition(int position) {
        return openOrdersList.get(position).isBid();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView typeTextView, priceTextView, quantityTextView, companyNameTextView, deleteOrderText;
        Button cancelButton;
        SeekBar quantitySeekbar;
        public ConstraintLayout orderViewForeground;

        OrderViewHolder(View view) {
            super(view);

            typeTextView = view.findViewById(R.id.orderType_textView);
            priceTextView = view.findViewById(R.id.orderPrice_textView);
            quantityTextView = view.findViewById(R.id.quantity_textView);
            companyNameTextView = view.findViewById(R.id.company_textView);
            deleteOrderText = view.findViewById(R.id.deleteOrderText);
            cancelButton = view.findViewById(R.id.cancel_button);
            quantitySeekbar = view.findViewById(R.id.stockDisplay_seekBar);
            orderViewForeground = view.findViewById(R.id.orderViewForeground);
        }
    }
}