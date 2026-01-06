package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

public class UserData {

    @SerializedName("user_id")
    private int userId;

    @SerializedName("operator_id")
    private int operatorId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * Get the ID (either user_id or operator_id, whichever is available)
     */
    public int getId() {
        return operatorId > 0 ? operatorId : userId;
    }
}
