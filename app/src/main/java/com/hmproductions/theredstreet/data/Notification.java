package com.hmproductions.theredstreet.data;

public class Notification {

    private String text, createdAt;

    public Notification(String text, String createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
