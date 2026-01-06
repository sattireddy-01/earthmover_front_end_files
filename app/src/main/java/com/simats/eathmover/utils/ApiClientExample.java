package com.simats.eathmover.utils;

import android.util.Log;

import com.simats.eathmover.utils.ApiClient.ApiResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Example usage of ApiClient
 * This demonstrates how to use the direct HTTP API client
 * as an alternative to Retrofit
 * 
 * NOTE: This is an example file - you can delete it or use it as reference
 */
public class ApiClientExample {
    
    private static final String TAG = "ApiClientExample";
    
    /**
     * Example: Login using ApiClient
     */
    public static void exampleLogin(String phone, String password) {
        try {
            // Create request body
            JSONObject body = new JSONObject();
            body.put("phone", phone);
            body.put("password", password);
            
            // Make API call
            ApiResult result = ApiClient.postJson("auth/user_login.php", body);
            
            if (result.ok && result.json != null) {
                // Success
                String userId = result.json.optString("user_id", "");
                String userName = result.json.optString("name", "");
                Log.d(TAG, "Login successful: " + userName);
                
                // Handle success...
            } else {
                // Error
                String errorMsg = result.errorMessage != null ? result.errorMessage : "Login failed";
                Log.e(TAG, "Login failed: " + errorMsg);
                
                // Handle error...
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
        }
    }
    
    /**
     * Example: Login with authentication token
     */
    public static void exampleLoginWithAuth(String phone, String password, String token) {
        try {
            JSONObject body = new JSONObject();
            body.put("phone", phone);
            body.put("password", password);
            
            // Use authentication
            ApiResult result = ApiClient.postJsonWithAuth("auth/user_login.php", body, token);
            
            if (result.ok) {
                // Handle success...
            } else {
                // Handle error...
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Example: GET request with query parameters
     */
    public static void exampleGetRequest(String operatorId) {
        // Build query parameters
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("operator_id", operatorId);
        
        // Make GET request
        ApiResult result = ApiClient.getJson("operator/get_operator_profile.php", params);
        
        if (result.ok && result.json != null) {
            // Parse response
            String name = result.json.optString("name", "");
            String phone = result.json.optString("phone", "");
            
            Log.d(TAG, "Operator: " + name + " - " + phone);
        } else {
            Log.e(TAG, "Error: " + result.errorMessage);
        }
    }
    
    /**
     * Example: GET request with authentication
     */
    public static void exampleGetWithAuth(String operatorId, String token) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("operator_id", operatorId);
        
        ApiResult result = ApiClient.getJsonWithAuth("operator/get_operator_profile.php", params, token);
        
        if (result.ok) {
            // Handle success...
        } else {
            // Handle error...
        }
    }
    
    /**
     * Example: Custom timeout
     */
    public static void exampleWithTimeout(String phone, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("phone", phone);
            body.put("password", password);
            
            // Use extended timeout (30 seconds)
            ApiResult result = ApiClient.postJson(
                "auth/user_login.php", 
                body, 
                com.simats.eathmover.config.ApiConfig.EXTENDED_TIMEOUT_MS
            );
            
            if (result.ok) {
                // Handle success...
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Example: Handle API response structure
     * 
     * PHP Backend typically returns:
     * {
     *   "success": true/false,
     *   "message": "Success message",
     *   "data": { ... }
     * }
     * 
     * OR
     * 
     * {
     *   "ok": true/false,
     *   "error": "Error message",
     *   "data": { ... }
     * }
     */
    public static void exampleHandleResponse(ApiResult result) {
        if (result.ok && result.json != null) {
            try {
                // Check for data field
                if (result.json.has("data")) {
                    JSONObject data = result.json.getJSONObject("data");
                    // Process data...
                }
                
                // Get message if available
                if (result.json.has("message")) {
                    String message = result.json.getString("message");
                    Log.d(TAG, "Message: " + message);
                }
                
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing response: " + e.getMessage());
            }
        } else {
            // Handle error
            String error = result.errorMessage != null ? result.errorMessage : "Unknown error";
            Log.e(TAG, "API Error: " + error);
            Log.e(TAG, "HTTP Code: " + result.httpCode);
        }
    }
}




























