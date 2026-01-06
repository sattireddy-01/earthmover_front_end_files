package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

/**
 * Body for requesting an OTP to reset password.
 * For admin/users: 'phone' field contains email
 * For operators: 'phone' field contains phone number or email
 */
public class PasswordResetRequest {

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    // Constructor for phone-based reset (operators)
    public PasswordResetRequest(String phone, String role) {
        this.phone = phone;
        this.role = role;
        this.email = null;
    }

    // Constructor for email-based reset (admin/users)
    public PasswordResetRequest(String email, String role, boolean isEmail) {
        if (isEmail) {
            this.email = email;
            this.phone = null;
        } else {
            this.phone = email;
            this.email = null;
        }
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}








