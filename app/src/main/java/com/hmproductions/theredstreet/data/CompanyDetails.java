package com.hmproductions.theredstreet.data;

public class CompanyDetails {

    private String company, shortName;
    private int value, volume, previousDayClose;

    public CompanyDetails(String company, String shortName, int value, int volume, int previousDayClose) {
        this.shortName = shortName;
        this.company = company;
        this.value = value;
        this.volume = volume;
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

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getPreviousDayClose() {
        return previousDayClose;
    }

    public void setPreviousDayClose(int previousDayClose) {
        this.previousDayClose = previousDayClose;
    }
}
