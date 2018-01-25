package com.hmproductions.theredstreet.data;

public class Order {

    private boolean bid, closed;
    private int orderId, price, stockId, orderType;
    private int stockQuantity, stockQuantityFulfilled;

    public Order(int orderId, boolean bid, boolean closed, int price, int stockId, int orderType, int stockQuantity, int stockQuantityFulfilled) {
        this.orderId = orderId;
        this.bid = bid;
        this.closed = closed;
        this.price = price;
        this.stockId = stockId;
        this.orderType = orderType;
        this.stockQuantity = stockQuantity;
        this.stockQuantityFulfilled = stockQuantityFulfilled;
    }

    public boolean isBid() {
        return bid;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getPrice() {
        return price;
    }

    public int getStockId() {
        return stockId;
    }

    public int getOrderType() {
        return orderType;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getStockQuantityFulfilled() {
        return stockQuantityFulfilled;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setStockQuantityFulfilled(int stockQuantityFulfilled) {
        this.stockQuantityFulfilled = stockQuantityFulfilled;
    }
}