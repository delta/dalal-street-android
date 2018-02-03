package com.hmproductions.theredstreet.data;

public class MarketDepth {

    private int price,volume;

    public MarketDepth(int price, int volume) {
        this.price = price;
        this.volume = volume;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
