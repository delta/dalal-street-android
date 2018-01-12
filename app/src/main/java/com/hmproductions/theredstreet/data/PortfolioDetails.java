package com.hmproductions.theredstreet.data;

public class PortfolioDetails {

    private String company, shortName;
    private int noOfStock,value;

    public PortfolioDetails(String company, String shortName, int noOfStock, int value) {
        this.shortName = shortName;
        this.company = company;
        this.noOfStock = noOfStock;
        this.value = value;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getNoOfStock() {
        return noOfStock;
    }

    public void setNoOfStock(int noOfStock) {
        this.noOfStock = noOfStock;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
