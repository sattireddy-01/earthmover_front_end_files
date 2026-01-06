package com.simats.eathmover.utils;

import android.util.Log;

import com.simats.eathmover.config.ApiConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP API Client for direct JSON communication with PHP backend
 * Similar to the Kotlin ApiClient reference, but written in Java
 * 
 * This can be used as an alternative to Retrofit for direct HTTP calls
 * or when you need more control over the request/response handling
 */
public class ApiClient {
    
    private static final String TAG = "ApiClient";
    
    /**
     * Result class for API responses
     */
    public static class ApiResult {
        public final boolean ok;
        public final int httpCode;
        public final JSONObject json;
        public final String errorMessage;
        public final String url;
        
        public ApiResult(boolean ok, int httpCode, JSONObject json, String errorMessage, String url) {
            this.ok = ok;
            this.httpCode = httpCode;
            this.json = json;
            this.errorMessage = errorMessage;
            this.url = url;
        }
    }
    
    /**
     * POST JSON to API endpoint
     * @param path API endpoint path (e.g., "auth/user_login.php")
     * @param body JSON body to send
     * @return ApiResult with response data
     */
    public static ApiResult postJson(String path, JSONObject body) {
        return postJson(path, body, ApiConfig.DEFAULT_TIMEOUT_MS, null);
    }
    
    /**
     * POST JSON to API endpoint with custom timeout
     * @param path API endpoint path
     * @param body JSON body to send
     * @param timeoutMs Timeout in milliseconds
     * @return ApiResult with response data
     */
    public static ApiResult postJson(String path, JSONObject body, int timeoutMs) {
        return postJson(path, body, timeoutMs, null);
    }
    
    /**
     * POST JSON to API endpoint with custom timeout and headers
     * @param path API endpoint path
     * @param body JSON body to send
     * @param timeoutMs Timeout in milliseconds
     * @param headers Additional headers (can be null)
     * @return ApiResult with response data
     */
    public static ApiResult postJson(String path, JSONObject body, int timeoutMs, Map<String, String> headers) {
        return postJsonInternal(path, body, timeoutMs, true, headers);
    }
    
    /**
     * POST JSON with Bearer token authentication
     * @param path API endpoint path
     * @param body JSON body to send
     * @param token Bearer token
     * @param timeoutMs Timeout in milliseconds
     * @return ApiResult with response data
     */
    public static ApiResult postJsonAuth(String path, JSONObject body, String token, int timeoutMs) {
        Map<String, String> headers = null;
        if (token != null && !token.trim().isEmpty()) {
            headers = new java.util.HashMap<>();
            headers.put("Authorization", "Bearer " + token);
        }
        return postJsonInternal(path, body, timeoutMs, true, headers);
    }
    
    /**
     * POST JSON with Bearer token authentication (default timeout)
     * @param path API endpoint path
     * @param body JSON body to send
     * @param token Bearer token
     * @return ApiResult with response data
     */
    public static ApiResult postJsonAuth(String path, JSONObject body, String token) {
        return postJsonAuth(path, body, token, ApiConfig.DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * POST JSON with authentication (alias for postJsonAuth)
     * @param path API endpoint path
     * @param body JSON body to send
     * @param token Bearer token
     * @param timeoutMs Timeout in milliseconds
     * @return ApiResult with response data
     */
    public static ApiResult postJsonWithAuth(String path, JSONObject body, String token, int timeoutMs) {
        if (token == null || token.trim().isEmpty()) {
            return postJson(path, body, timeoutMs);
        } else {
            return postJsonAuth(path, body, token, timeoutMs);
        }
    }
    
    /**
     * POST JSON with authentication (default timeout)
     * @param path API endpoint path
     * @param body JSON body to send
     * @param token Bearer token
     * @return ApiResult with response data
     */
    public static ApiResult postJsonWithAuth(String path, JSONObject body, String token) {
        return postJsonWithAuth(path, body, token, ApiConfig.DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Internal method to handle POST JSON requests
     * Fixes XAMPP intermittent EOF issues
     */
    private static ApiResult postJsonInternal(
            String path,
            JSONObject body,
            int timeoutMs,
            boolean retryOnEof,
            Map<String, String> headers) {
        
        String fullUrl = ApiConfig.getFullUrl(path);
        
        // Fix XAMPP intermittent EOF
        System.setProperty("http.keepAlive", "false");
        
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(fullUrl);
            conn = (HttpURLConnection) url.openConnection();
            
            // Configure connection
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            
            // Set default headers
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("Connection", "close");
            
            // Add custom headers if provided
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            // Write request body
            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(bytes.length);
            
            OutputStream os = conn.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
            
            // Get response
            int code = conn.getResponseCode();
            String text;
            
            if (code >= 200 && code < 300) {
                text = readAll(conn.getInputStream());
            } else {
                text = readAll(conn.getErrorStream());
            }
            
            // Parse JSON response
            JSONObject json = null;
            try {
                if (text != null && !text.trim().isEmpty()) {
                    json = new JSONObject(text);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse JSON response: " + e.getMessage());
            }
            
            // Determine if request was successful
            boolean ok = (code >= 200 && code < 300);
            if (json != null) {
                // Check for "ok" or "success" field in response
                if (json.has("ok")) {
                    ok = ok && json.optBoolean("ok", false);
                } else if (json.has("success")) {
                    ok = ok && json.optBoolean("success", false);
                }
            }
            
            // Extract error message
            String errorMessage = null;
            if (!ok && json != null) {
                if (json.has("error")) {
                    errorMessage = json.optString("error", null);
                } else if (json.has("message")) {
                    errorMessage = json.optString("message", null);
                }
            }
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                if (!ok && text != null && !text.trim().isEmpty()) {
                    errorMessage = text;
                }
            }
            
            return new ApiResult(ok, code, json, errorMessage, fullUrl);
            
        } catch (ProtocolException e) {
            // Retry on EOF error (XAMPP issue)
            if (retryOnEof) {
                Log.d(TAG, "ProtocolException, retrying: " + e.getMessage());
                return postJsonInternal(path, body, timeoutMs, false, headers);
            }
            return new ApiResult(false, 0, null, "ProtocolException: " + e.getMessage(), fullUrl);
            
        } catch (Exception e) {
            Log.e(TAG, "API request failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return new ApiResult(false, 0, null, e.getClass().getSimpleName() + ": " + e.getMessage(), fullUrl);
            
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    /**
     * Read all data from InputStream
     */
    private static String readAll(InputStream stream) {
        if (stream == null) {
            return "";
        }
        
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString().trim();
        } catch (Exception e) {
            Log.e(TAG, "Error reading stream: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * GET request to API endpoint
     * @param path API endpoint path
     * @param queryParams Query parameters (can be null)
     * @return ApiResult with response data
     */
    public static ApiResult getJson(String path, Map<String, String> queryParams) {
        return getJson(path, queryParams, ApiConfig.DEFAULT_TIMEOUT_MS, null);
    }
    
    /**
     * GET request with authentication
     * @param path API endpoint path
     * @param queryParams Query parameters (can be null)
     * @param token Bearer token
     * @return ApiResult with response data
     */
    public static ApiResult getJsonWithAuth(String path, Map<String, String> queryParams, String token) {
        Map<String, String> headers = null;
        if (token != null && !token.trim().isEmpty()) {
            headers = new java.util.HashMap<>();
            headers.put("Authorization", "Bearer " + token);
        }
        return getJson(path, queryParams, ApiConfig.DEFAULT_TIMEOUT_MS, headers);
    }
    
    /**
     * Internal GET method
     */
    private static ApiResult getJson(String path, Map<String, String> queryParams, int timeoutMs, Map<String, String> headers) {
        String fullUrl = ApiConfig.getFullUrl(path);
        
        // Build query string
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder queryString = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (first) {
                    queryString.append("?");
                    first = false;
                } else {
                    queryString.append("&");
                }
                try {
                    queryString.append(entry.getKey())
                            .append("=")
                            .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (Exception e) {
                    Log.e(TAG, "Error encoding query param: " + e.getMessage());
                }
            }
            fullUrl += queryString.toString();
        }
        
        System.setProperty("http.keepAlive", "false");
        
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(fullUrl);
            conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("Connection", "close");
            
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            int code = conn.getResponseCode();
            String text;
            
            if (code >= 200 && code < 300) {
                text = readAll(conn.getInputStream());
            } else {
                text = readAll(conn.getErrorStream());
            }
            
            JSONObject json = null;
            try {
                if (text != null && !text.trim().isEmpty()) {
                    json = new JSONObject(text);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse JSON response: " + e.getMessage());
            }
            
            boolean ok = (code >= 200 && code < 300);
            if (json != null) {
                if (json.has("ok")) {
                    ok = ok && json.optBoolean("ok", false);
                } else if (json.has("success")) {
                    ok = ok && json.optBoolean("success", false);
                }
            }
            
            String errorMessage = null;
            if (!ok && json != null) {
                if (json.has("error")) {
                    errorMessage = json.optString("error", null);
                } else if (json.has("message")) {
                    errorMessage = json.optString("message", null);
                }
            }
            
            return new ApiResult(ok, code, json, errorMessage, fullUrl);
            
        } catch (Exception e) {
            Log.e(TAG, "GET request failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return new ApiResult(false, 0, null, e.getClass().getSimpleName() + ": " + e.getMessage(), fullUrl);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}




























