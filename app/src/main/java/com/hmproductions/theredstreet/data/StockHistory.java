package com.hmproductions.theredstreet.data;

import java.util.Date;

public class StockHistory {

    private Date stockDate;
    private int stockClose;

    public StockHistory(Date stockDate, int stockClose) {
        this.stockDate = stockDate;
        this.stockClose = stockClose;
    }

    public Date getStockDate() {
        return stockDate;
    }

    public void setStockDate(Date stockDate) {
        this.stockDate = stockDate;
    }

    public int getStockClose() {
        return stockClose;
    }

    public void setStockClose(int stockClose) {
        this.stockClose = stockClose;
    }
}
