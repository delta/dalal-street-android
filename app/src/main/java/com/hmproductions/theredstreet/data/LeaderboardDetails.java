package com.hmproductions.theredstreet.data;

public class LeaderboardDetails {

    private int rank;
    private String name;
    private int wealth;

    public LeaderboardDetails(int rank, String name, int wealth) {
        this.rank = rank;
        this.name = name;
        this.wealth = wealth;
    }

    public int getWealth() {
        return wealth;
    }

    public void setWealth(int wealth) {
        this.wealth = wealth;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
