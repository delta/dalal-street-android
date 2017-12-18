package com.hmproductions.theredstreet.data;

public class LeaderboardDetails {

    private int rank;
    private String name;
    private int total_wealth;

    public LeaderboardDetails(int rank, String name, int total_wealth) {
        this.rank = rank;
        this.name = name;
        this.total_wealth = total_wealth;
    }

    public LeaderboardDetails() {
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

    public int getTotal_wealth() {
        return total_wealth;
    }

    public void setTotal_wealth(int total_wealth) {
        this.total_wealth = total_wealth;
    }
}
