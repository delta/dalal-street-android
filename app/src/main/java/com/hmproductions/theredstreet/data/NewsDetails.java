package com.hmproductions.theredstreet.data;

/**
 * Created by ravi on 18/2/17.
 */

public class NewsDetails {

    private String headlines;
    private String content;


    public NewsDetails() {
    }

    public NewsDetails(String headlines, String content) {
        this.headlines = headlines;
        this.content = content;
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
}
