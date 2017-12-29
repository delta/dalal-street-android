package com.hmproductions.theredstreet.data;

public class ExchangeValuesDetails {

    private int stockValue, dailyHigh, dailyLow, stocksInMarket, stocksInExchange;
    private String company;

    public ExchangeValuesDetails(String company, int stockValue, int dailyHigh, int dailyLow, int stocksInMarket, int stocksInExchange) {
        this.stockValue = stockValue;
        this.dailyHigh = dailyHigh;
        this.dailyLow = dailyLow;
        this.stocksInMarket = stocksInMarket;
        this.stocksInExchange = stocksInExchange;
        this.company = company;
    }

    public int getStockValue() {
        return stockValue;
    }

    public void setStockValue(int stockValue) {
        this.stockValue = stockValue;
    }

    public int getDailyHigh() {
        return dailyHigh;
    }

    public void setDailyHigh(int dailyHigh) {
        this.dailyHigh = dailyHigh;
    }

    public int getDailyLow() {
        return dailyLow;
    }

    public void setDailyLow(int dailyLow) {
        this.dailyLow = dailyLow;
    }

    public int getStocksInMarket() {
        return stocksInMarket;
    }

    public void setStocksInMarket(int stocksInMarket) {
        this.stocksInMarket = stocksInMarket;
    }

    public int getStocksInExchange() {
        return stocksInExchange;
    }

    public void setStocksInExchange(int stocksInExchange) {
        this.stocksInExchange = stocksInExchange;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
