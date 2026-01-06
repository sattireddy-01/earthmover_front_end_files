package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    // Constructor for phone login
    public LoginRequest(String phone, String password, String role) {
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.email = null;
    }

    // Constructor for email or phone login
    public LoginRequest(String phone, String password, String role, String email) {
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}

