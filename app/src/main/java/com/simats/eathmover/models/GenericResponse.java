package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

/**
 * Generic API Response model for simple success/failure responses
 * Used for operations that don't return complex data, just success status and message
 */
public class GenericResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("ok")
    private boolean ok;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    /**
     * Check if the response indicates success
     * Returns true if either success or ok is true
     */
    public boolean isSuccess() {
        return success || ok;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        return error != null ? error : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
