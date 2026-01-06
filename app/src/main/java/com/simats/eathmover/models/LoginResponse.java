package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private LoginData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public LoginData getData() {
        return data;
    }

    public static class LoginData {
        @SerializedName("user_id")
        private int userId;

        @SerializedName("name")
        private String name;

        @SerializedName("phone")
        private String phone;

        public int getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }
    }
}




































