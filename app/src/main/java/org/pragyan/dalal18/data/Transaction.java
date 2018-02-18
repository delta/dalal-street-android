package org.pragyan.dalal18.data;

public class Transaction {

    private String time,type;
    private int noOfStocks, stockId ;
    private float stockPrice, totalMoney;

    public Transaction(String type, int stockId, int noOfStocks, float stockPrice, String time, float totalMoney) {
        this.type = type;
        this.stockId = stockId;
        this.noOfStocks = noOfStocks;
        this.stockPrice = stockPrice;
        this.time = time;
        this.totalMoney = totalMoney;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getTotalMoney() {
        return totalMoney;
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
