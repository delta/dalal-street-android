package org.pragyan.dalal18.data;

public class NewsDetails {

    private String headlines, content, createdAt;


    public NewsDetails() {
    }

    public NewsDetails( String createdAt, String headlines, String content) {
        this.headlines = headlines;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHeadlines() {
        return headlines;
    }

    public void setHeadlines(String headlines) {
        this.headlines = headlines;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
