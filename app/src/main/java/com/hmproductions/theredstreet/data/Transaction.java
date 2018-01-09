package com.hmproductions.theredstreet.data;

public class Transaction {

    private String time;
    private int noOfStocks, stockId, type;
    private float stockPrice, totalMoney;

    public Transaction(int type, int stockId, int noOfStocks, float stockPrice, String time, float totalMoney) {
        this.type = type;
        this.stockId = stockId;
        this.noOfStocks = noOfStocks;
        this.stockPrice = stockPrice;
        this.time = time;
        this.totalMoney = totalMoney;
    }

    public float getTotalMoney() {
        return totalMoney;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public int getNoOfStocks() {
        return noOfStocks;
    }

    public float getStockPrice() {
        return stockPrice;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
