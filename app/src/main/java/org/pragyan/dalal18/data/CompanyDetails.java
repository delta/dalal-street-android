package org.pragyan.dalal18.data;

public class CompanyDetails {

    private String company, shortName;
    private int value, previousDayClose;

    public CompanyDetails(String company, String shortName, int value, int previousDayClose) {
        this.shortName = shortName;
        this.company = company;
        this.value = value;
        this.previousDayClose = previousDayClose;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getPreviousDayClose() {
        return previousDayClose;
    }

    public void setPreviousDayClose(int previousDayClose) {
        this.previousDayClose = previousDayClose;
    }
}
