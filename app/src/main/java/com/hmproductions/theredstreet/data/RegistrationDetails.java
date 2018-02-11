package com.hmproductions.theredstreet.data;

public class RegistrationDetails {

    private String fullName, password, username, country, email;

    public RegistrationDetails(String fullName, String password, String username, String country, String email) {
        this.fullName = fullName;
        this.password = password;
        this.username = username;
        this.country = country;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }
}
