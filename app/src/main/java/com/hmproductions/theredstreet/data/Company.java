package com.hmproductions.theredstreet.data;

public class Company {

    private String name;
    private String value;
    private int image;
    private int status;

    public Company() {
    }

    public Company(String name, String value, int image, int status) {
        this.name = name;
        this.value = value;
        this.image = image;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
