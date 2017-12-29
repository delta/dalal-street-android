package com.hmproductions.theredstreet.data;

public class PortfolioDetails {

    private String company;
    private int noOfStock,value;

    public PortfolioDetails(String company, int noOfStock, int value) {
        this.company = company;
        this.noOfStock = noOfStock;
        this.value = value;
    }

    public String getPortfolioDetails(){
        return company + " : "+ String.valueOf(noOfStock)+" ( ₹"+ String.valueOf(value)+" per stock)";
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
