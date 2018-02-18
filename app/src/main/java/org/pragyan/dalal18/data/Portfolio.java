package org.pragyan.dalal18.data;

public class Portfolio {

    private String companyName, shortname;
    private int quantityOwned, price, previousDayClose;

    public Portfolio(String shortname, String companyName, int quantityOwned, int price, int previousDayClose) {
        this.shortname = shortname;
        this.companyName = companyName;
        this.quantityOwned = quantityOwned;
        this.price = price;
        this.previousDayClose = previousDayClose;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getShortname() {
        return shortname;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getQuantityOwned() {
        return quantityOwned;
    }

    public void setQuantityOwned(int quantityOwned) {
        this.quantityOwned = quantityOwned;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPreviousDayClose() {
        return previousDayClose;
    }

    public void setPreviousDayClose(int previousDayClose) {
        this.previousDayClose = previousDayClose;
    }
}
