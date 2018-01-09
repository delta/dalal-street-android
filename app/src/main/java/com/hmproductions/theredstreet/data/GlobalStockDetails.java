package com.hmproductions.theredstreet.data;

import android.os.Parcel;
import android.os.Parcelable;

/* Modify definition according to needs; Refer Stock.proto for more attributes */
public class GlobalStockDetails implements Parcelable{

    private int stockId, price, quantityInMarket, quantityInExchange, previousDayClose;
    private int up;

    public GlobalStockDetails(int stockId, int price, int quantityInMarket, int quantityInExchange, int previousDayClose, int up) {
        this.stockId = stockId;
        this.price = price;
        this.quantityInMarket = quantityInMarket;
        this.quantityInExchange = quantityInExchange;
        this.previousDayClose = previousDayClose;
        this.up = up;
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

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    private GlobalStockDetails(Parcel in) {
        this.stockId = in.readInt();
        this.price = in.readInt();
        this.quantityInMarket = in.readInt();
        this.quantityInExchange = in.readInt();
        this.previousDayClose = in.readInt();
        this.up = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(stockId);
        dest.writeInt(price);
        dest.writeInt(quantityInMarket);
        dest.writeInt(quantityInExchange);
        dest.writeInt(previousDayClose);
        dest.writeInt(up);
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
