package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

public class ReportsData {
    @SerializedName("active_users")
    private int activeUsers;

    @SerializedName("new_users")
    private int newUsers;

    @SerializedName("active_users_change")
    private String activeUsersChange;

    @SerializedName("new_users_change")
    private String newUsersChange;

    @SerializedName("total_revenue")
    private double totalRevenue;

    @SerializedName("avg_booking_value")
    private double avgBookingValue;

    @SerializedName("revenue_change")
    private String revenueChange;

    @SerializedName("avg_booking_change")
    private String avgBookingChange;

    @SerializedName("total_bookings")
    private int totalBookings;

    @SerializedName("bookings_change")
    private String bookingsChange;

    @SerializedName("most_booked_machine")
    private String mostBookedMachine;

    @SerializedName("machine_bookings_count")
    private int machineBookingsCount;

    @SerializedName("active_operators")
    private int activeOperators;

    @SerializedName("operators_change")
    private String operatorsChange;

    @SerializedName("top_operator")
    private String topOperator;

    @SerializedName("operator_bookings_count")
    private int operatorBookingsCount;

    // Getters and setters
    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(int newUsers) {
        this.newUsers = newUsers;
    }

    public String getActiveUsersChange() {
        return activeUsersChange;
    }

    public void setActiveUsersChange(String activeUsersChange) {
        this.activeUsersChange = activeUsersChange;
    }

    public String getNewUsersChange() {
        return newUsersChange;
    }

    public void setNewUsersChange(String newUsersChange) {
        this.newUsersChange = newUsersChange;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getAvgBookingValue() {
        return avgBookingValue;
    }

    public void setAvgBookingValue(double avgBookingValue) {
        this.avgBookingValue = avgBookingValue;
    }

    public String getRevenueChange() {
        return revenueChange;
    }

    public void setRevenueChange(String revenueChange) {
        this.revenueChange = revenueChange;
    }

    public String getAvgBookingChange() {
        return avgBookingChange;
    }

    public void setAvgBookingChange(String avgBookingChange) {
        this.avgBookingChange = avgBookingChange;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public String getBookingsChange() {
        return bookingsChange;
    }

    public void setBookingsChange(String bookingsChange) {
        this.bookingsChange = bookingsChange;
    }

    public String getMostBookedMachine() {
        return mostBookedMachine;
    }

    public void setMostBookedMachine(String mostBookedMachine) {
        this.mostBookedMachine = mostBookedMachine;
    }

    public int getMachineBookingsCount() {
        return machineBookingsCount;
    }

    public void setMachineBookingsCount(int machineBookingsCount) {
        this.machineBookingsCount = machineBookingsCount;
    }

    public int getActiveOperators() {
        return activeOperators;
    }

    public void setActiveOperators(int activeOperators) {
        this.activeOperators = activeOperators;
    }

    public String getOperatorsChange() {
        return operatorsChange;
    }

    public void setOperatorsChange(String operatorsChange) {
        this.operatorsChange = operatorsChange;
    }

    public String getTopOperator() {
        return topOperator;
    }

    public void setTopOperator(String topOperator) {
        this.topOperator = topOperator;
    }

    public int getOperatorBookingsCount() {
        return operatorBookingsCount;
    }

    public void setOperatorBookingsCount(int operatorBookingsCount) {
        this.operatorBookingsCount = operatorBookingsCount;
    }
}

