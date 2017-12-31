package com.hmproductions.theredstreet.data;

public class Transaction {

    private String type, company, time;
    private int noOfStocks;
    private float stockPrice, totalMoney;

    public Transaction(String type, String company, int noOfStocks, float stockPrice, String time, float totalMoney) {
        this.type = type;
        this.company = company;
        this.noOfStocks = noOfStocks;
        this.stockPrice = stockPrice;
        this.time = time;
        this.totalMoney = totalMoney;
    }

    public float getTotalMoney() {
        return totalMoney;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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
