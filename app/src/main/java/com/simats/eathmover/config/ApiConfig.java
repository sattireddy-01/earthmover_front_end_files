package com.simats.eathmover.config;

/**
 * Centralized API configuration for the Earth Mover application.
 * 
 * This class provides:
 * - Base URL configuration for API endpoints
 * - Timeout configurations for network requests
 * - Helper methods for URL construction
 */
public class ApiConfig {
    
    // Base URLs
    // For physical device: Use your computer's IP address on the same network
    // For emulator: Use 10.0.2.2 to access host's localhost
    private static final String PHYSICAL_DEVICE_URL = "http://10.118.154.247/Earth_mover/api/";
    private static final String EMULATOR_URL = "http://10.0.2.2/Earth_mover/api/";
    
    // Use PHYSICAL_DEVICE_URL for physical device testing
    // Change to EMULATOR_URL if testing on emulator
    public static final String BASE_URL = EMULATOR_URL;
    
    // Timeout configurations (in milliseconds)
    public static final int DEFAULT_TIMEOUT_MS = 15000;      // 15 seconds
    public static final int EXTENDED_TIMEOUT_MS = 30000;     // 30 seconds
    public static final int FAST_TIMEOUT_MS = 10000;         // 10 seconds
    
    /**
     * Get the base URL for API requests.
     * Returns the base URL without trailing slash for consistency.
     * 
     * @return Base URL string
     */
    public static String getBaseUrl() {
        // Remove trailing slash if present for consistency
        String url = BASE_URL;
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
    
    /**
     * Get the full URL by appending the API path to the base URL.
     * 
     * @param path API endpoint path (e.g., "auth/user_login.php")
     * @return Full URL string
     */
    public static String getFullUrl(String path) {
        String baseUrl = BASE_URL;
        
        // Ensure base URL ends with /
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        // Remove leading / from path if present
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        return baseUrl + path;
    }
    
    /**
     * Get the root URL (without /api/) for file uploads and image loading.
     * 
     * @return Root URL string (e.g., "http://10.118.154.247/Earth_mover/")
     */
    public static String getRootUrl() {
        String url = BASE_URL;
        
        // Remove /api/ from the end if present
        if (url.endsWith("/api/")) {
            url = url.substring(0, url.length() - 5);
        } else if (url.endsWith("/api")) {
            url = url.substring(0, url.length() - 4);
        }
        
        // Ensure it ends with /
        if (!url.endsWith("/")) {
            url += "/";
        }
        
        return url;
    }
}
