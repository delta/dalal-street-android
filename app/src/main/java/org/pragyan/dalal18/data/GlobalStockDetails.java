package org.pragyan.dalal18.data;

import android.os.Parcel;
import android.os.Parcelable;

/* Modify definition according to needs; Refer Stock.proto for more attributes */
public class GlobalStockDetails implements Parcelable {

    private String fullName, shortName, imagePath;
    private int stockId;
    private int price;
    private int quantityInMarket;
    private int quantityInExchange;
    private int previousDayClose;
    private int up; // up isn't boolean because Parcelable cannot readBoolean()

    public GlobalStockDetails(String fullName, String shortName, int stockId, int price, int quantityInMarket, int quantityInExchange, int previousDayClose, int up, String imagePath) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.stockId = stockId;
        this.price = price;
        this.quantityInMarket = quantityInMarket;
        this.quantityInExchange = quantityInExchange;
        this.previousDayClose = previousDayClose;
        this.up = up;
        this.imagePath = imagePath;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantityInMarket() {
        return quantityInMarket;
    }

    public void setQuantityInMarket(int quantityInMarket) {
        this.quantityInMarket = quantityInMarket;
    }

    public int getQuantityInExchange() {
        return quantityInExchange;
    }

    public void setQuantityInExchange(int quantityInExchange) {
        this.quantityInExchange = quantityInExchange;
    }

    public int getPreviousDayClose() {
        return previousDayClose;
    }

    public void setPreviousDayClose(int previousDayClose) {
        this.previousDayClose = previousDayClose;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    private GlobalStockDetails(Parcel in) {
        this.fullName = in.readString();
        this.shortName = in.readString();
        this.stockId = in.readInt();
        this.price = in.readInt();
        this.quantityInMarket = in.readInt();
        this.quantityInExchange = in.readInt();
        this.previousDayClose = in.readInt();
        this.up = in.readInt();
        this.imagePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(shortName);
        dest.writeInt(stockId);
        dest.writeInt(price);
        dest.writeInt(quantityInMarket);
        dest.writeInt(quantityInExchange);
        dest.writeInt(previousDayClose);
        dest.writeInt(up);
        dest.writeString(imagePath);
    }

    public static final Creator<GlobalStockDetails> CREATOR = new Creator<GlobalStockDetails>() {
        @Override
        public GlobalStockDetails createFromParcel(Parcel in) {
            return new GlobalStockDetails(in);
        }

        @Override
        public GlobalStockDetails[] newArray(int size) {
            return new GlobalStockDetails[size];
        }
    };
}
