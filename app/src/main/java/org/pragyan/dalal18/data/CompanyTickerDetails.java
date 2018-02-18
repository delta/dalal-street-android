package org.pragyan.dalal18.data;

import javax.annotation.Nullable;

public class CompanyTickerDetails {

    private String fullName, imageUrl;
    private int previousDayClose;
    private boolean up;

    public CompanyTickerDetails(String fullName, @Nullable String imageUrl, int previousDayClose, boolean up) {
        this.fullName = fullName;
        this.imageUrl = imageUrl;
        this.previousDayClose = previousDayClose;
        this.up = up;
    }

    public String getFullName() {
        return fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPreviousDayClose() {
        return previousDayClose;
    }

    public boolean isUp() {
        return up;
    }
}
