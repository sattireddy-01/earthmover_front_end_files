package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

public class Earnings {
    @SerializedName("total_earnings")
    private double totalEarnings;

    @SerializedName("this_month")
    private double thisMonth;

    @SerializedName("last_month")
    private double lastMonth;

    @SerializedName("total_transactions")
    private int totalTransactions;

    // Getters and setters
    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public double getThisMonth() {
        return thisMonth;
    }

    public void setThisMonth(double thisMonth) {
        this.thisMonth = thisMonth;
    }

    public double getLastMonth() {
        return lastMonth;
    }

    public void setLastMonth(double lastMonth) {
        this.lastMonth = lastMonth;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}





























