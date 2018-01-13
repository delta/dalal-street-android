package com.hmproductions.theredstreet.data;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nullable;

public class StockDetails implements Parcelable{

    private int stockId, quantity;

    public StockDetails(int stockId, int quantity) {
        this.stockId = stockId;
        this.quantity = quantity;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    private StockDetails(Parcel in) {
        this.stockId = in.readInt();
        this.quantity = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(stockId);
        dest.writeInt(quantity);
    }

    public static final Creator<StockDetails> CREATOR = new Creator<StockDetails>() {
        @Override
        public StockDetails createFromParcel(Parcel in) {
            return new StockDetails(in);
        }

        @Override
        public StockDetails[] newArray(int size) {
            return new StockDetails[size];
        }
    };
}
