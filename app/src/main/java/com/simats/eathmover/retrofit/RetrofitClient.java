package com.simats.eathmover.retrofit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simats.eathmover.config.ApiConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    
    // Uses ApiConfig for consistent IP address configuration
    // Base URL already includes /api/ - endpoints in ApiService include their folder paths (auth/, admin/, operator/)
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create OkHttpClient with proper timeout configuration
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            
            // Set timeouts
            httpClient.connectTimeout(ApiConfig.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            httpClient.readTimeout(ApiConfig.EXTENDED_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            httpClient.writeTimeout(ApiConfig.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            // Disable keep-alive to fix XAMPP EOF issues
            httpClient.connectionPool(new okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS));
            
            // Add logging interceptor for debugging (only in debug builds)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d(TAG, message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
            
            // Add interceptor to log request body for debugging
            httpClient.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                
                // Log request details
                Log.d(TAG, "=== RETROFIT REQUEST ===");
                Log.d(TAG, "URL: " + original.url());
                Log.d(TAG, "Method: " + original.method());
                
                // Log request body if it exists
                if (original.body() != null) {
                    try {
                        RequestBody requestBody = original.body();
                        Buffer buffer = new Buffer();
                        requestBody.writeTo(buffer);
                        String bodyString = buffer.readUtf8();
                        Log.d(TAG, "Request body length: " + bodyString.length());
                        // Log first 1000 chars to see structure
                        if (bodyString.length() > 0) {
                            Log.d(TAG, "Request body preview: " + bodyString.substring(0, Math.min(1000, bodyString.length())));
                            // Check if profile_picture is in request
                            boolean hasProfilePicture = bodyString.contains("profile_picture");
                            Log.d(TAG, "Request body contains 'profile_picture': " + hasProfilePicture);
                            if (hasProfilePicture) {
                                // Try to find profile_picture value
                                int idx = bodyString.indexOf("\"profile_picture\"");
                                if (idx >= 0) {
                                    String snippet = bodyString.substring(Math.max(0, idx - 50), Math.min(bodyString.length(), idx + 200));
                                    Log.d(TAG, "profile_picture field context: " + snippet);
                                }
                            }
                        }
                        
                        // Recreate request with logged body (buffer is consumed, so recreate from string)
                        MediaType contentType = requestBody.contentType();
                        RequestBody newRequestBody = RequestBody.create(
                            contentType != null ? contentType : MediaType.parse("application/json; charset=utf-8"),
                            bodyString
                        );
                        original = original.newBuilder().method(original.method(), newRequestBody).build();
                    } catch (Exception e) {
                        Log.e(TAG, "Error logging request body: " + e.getMessage());
                    }
                }
                
                try {
                    okhttp3.Response response = chain.proceed(original);
                    Log.d(TAG, "Response code: " + response.code());
                    return response;
                } catch (Exception e) {
                    Log.e(TAG, "Network error: " + e.getMessage(), e);
                    throw e;
                }
            });
            
            OkHttpClient client = httpClient.build();
            
            // Create Gson with lenient mode to handle malformed JSON responses
            // Also serialize nulls to ensure all fields are included in JSON
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .serializeNulls() // Include null fields in JSON
                    .create();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            
            Log.d(TAG, "Retrofit client initialized with base URL: " + BASE_URL);
        }
        return retrofit;
    }
    
    /**
     * Reset the Retrofit client (useful for testing or changing base URL)
     */
    public static void resetClient() {
        retrofit = null;
    }
}
