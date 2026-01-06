package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

/**
 * Body for confirming OTP and setting a new password.
 */
public class PasswordResetConfirm {

    @SerializedName("phone")
    private String phone;

    @SerializedName("otp")
    private String otp;

    @SerializedName("new_password")
    private String newPassword;

    @SerializedName("role")
    private String role;

    public PasswordResetConfirm(String phone, String otp, String newPassword, String role) {
        this.phone = phone;
        this.otp = otp;
        this.newPassword = newPassword;
        this.role = role;
    }
}





