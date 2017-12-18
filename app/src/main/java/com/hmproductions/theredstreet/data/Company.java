package com.hmproductions.theredstreet.data;

/**
 * Created by ravi on 28/1/17.
 */

public class Company {

    private String company_name;
    private String company_value;
    private int company_image;
    private int company_status;

    public Company() {
    }

    public Company(String company_name, String company_value, int company_image, int company_status) {
        this.company_name = company_name;
        this.company_value = company_value;
        this.company_image = company_image;
        this.company_status = company_status;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getCompany_value() {
        return company_value;
    }

    public void setCompany_value(String company_value) {
        this.company_value = company_value;
    }

    public int getCompany_image() {
        return company_image;
    }

    public void setCompany_image(int company_image) {
        this.company_image = company_image;
    }

    public int getCompany_status() {
        return company_status;
    }

    public void setCompany_status(int company_status) {
        this.company_status = company_status;
    }
}
