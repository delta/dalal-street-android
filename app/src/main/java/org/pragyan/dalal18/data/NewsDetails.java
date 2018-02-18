package org.pragyan.dalal18.data;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsDetails implements Parcelable{

    private String headlines, content, createdAt,imagePath;

    public NewsDetails() {
    }

    public NewsDetails( String createdAt, String headlines, String content, String imagePath) {
        this.headlines = headlines;
        this.content = content;
        this.createdAt = createdAt;
        this.imagePath = imagePath;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(headlines);
        parcel.writeString(content);
        parcel.writeString(createdAt);
        parcel.writeString(imagePath);
    }

    public static final Creator<NewsDetails> CREATOR = new Creator<NewsDetails>() {
        @Override
        public NewsDetails createFromParcel(Parcel parcel) {
            return new NewsDetails(parcel);
        }

        @Override
        public NewsDetails[] newArray(int i) {
            return new NewsDetails[0];
        }
    };

    private NewsDetails(Parcel in) {
        this.headlines = in.readString();
        this.content = in.readString();
        this.createdAt = in.readString();
        this.imagePath = in.readString();
    }
}
