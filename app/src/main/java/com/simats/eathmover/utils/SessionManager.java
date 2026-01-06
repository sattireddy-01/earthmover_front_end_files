package com.simats.eathmover.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "EathmoverSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole"; // "user", "operator", "admin"
    private static final String KEY_OPERATOR_ID = "operatorId";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String name, String phone, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        if ("operator".equals(role)) {
            editor.putString(KEY_OPERATOR_ID, userId);
        }
        editor.commit();
    }

    public void createOperatorSession(String operatorId, String name, String phone, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, operatorId);
        editor.putString(KEY_OPERATOR_ID, operatorId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, "operator");
        editor.commit();
    }

    public void createAdminSession(String adminId, String name, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, adminId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_PHONE, ""); // Admin may not have phone
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getOperatorId() {
        return pref.getString(KEY_OPERATOR_ID, null);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, null);
    }

    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, null);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, null);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public boolean isOperator() {
        return "operator".equals(getUserRole());
    }

    public boolean isAdmin() {
        return "admin".equals(getUserRole());
    }

    public boolean isUser() {
        return "user".equals(getUserRole());
    }
}






