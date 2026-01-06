# API Client Setup Guide

This project now includes a Java-based API client similar to your Kotlin reference, designed to work with your PHP backend (XAMPP + MySQL).

## 📁 File Structure

```
app/src/main/java/com/simats/eathmover/
├── config/
│   └── ApiConfig.java          # Centralized API configuration
├── utils/
│   ├── ApiClient.java          # Direct HTTP API client (Java version)
│   └── ApiClientExample.java   # Usage examples
└── retrofit/
    ├── RetrofitClient.java     # Updated to use ApiConfig
    └── ApiService.java         # Retrofit interface (existing)
```

## 🔧 Configuration

### ApiConfig.java
Centralized configuration for all API endpoints.

**Location:** `app/src/main/java/com/simats/eathmover/config/ApiConfig.java`

**Key Settings:**
- `BASE_URL`: Base URL for your PHP API
- Default: `http://10.0.2.2/Earth_mover/api/` (for emulator)
- For physical device: Change to your computer's IP (e.g., `http://192.168.1.100/Earth_mover/api/`)

**To find your IP on Windows:**
```bash
ipconfig
# Look for "IPv4 Address" under your network adapter
```

## 🚀 Usage

### Option 1: Using ApiClient (Direct HTTP)

Similar to your Kotlin reference, use `ApiClient` for direct HTTP calls:

```java
import com.simats.eathmover.utils.ApiClient;
import com.simats.eathmover.utils.ApiClient.ApiResult;
import org.json.JSONObject;

// POST request
JSONObject body = new JSONObject();
body.put("phone", "1234567890");
body.put("password", "password123");

ApiResult result = ApiClient.postJson("auth/user_login.php", body);

if (result.ok && result.json != null) {
    // Success
    String userId = result.json.optString("user_id", "");
} else {
    // Error
    String error = result.errorMessage;
}
```

**With Authentication:**
```java
ApiResult result = ApiClient.postJsonWithAuth(
    "auth/user_login.php", 
    body, 
    "your_bearer_token"
);
```

**GET Request:**
```java
Map<String, String> params = new HashMap<>();
params.put("operator_id", "123");

ApiResult result = ApiClient.getJson("operator/get_operator_profile.php", params);
```

### Option 2: Using Retrofit (Existing)

Your existing Retrofit setup continues to work and now uses `ApiConfig`:

```java
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
Call<LoginResponse> call = apiService.login(loginRequest);

call.enqueue(new Callback<LoginResponse>() {
    @Override
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        // Handle response
    }
    
    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        // Handle error
    }
});
```

## 📋 API Client Methods

### POST Methods
- `postJson(path, body)` - Basic POST
- `postJson(path, body, timeoutMs)` - With custom timeout
- `postJson(path, body, timeoutMs, headers)` - With custom headers
- `postJsonAuth(path, body, token)` - With Bearer token
- `postJsonWithAuth(path, body, token)` - Alias for postJsonAuth

### GET Methods
- `getJson(path, queryParams)` - Basic GET
- `getJsonWithAuth(path, queryParams, token)` - With Bearer token

## 🔍 ApiResult Structure

```java
public class ApiResult {
    public final boolean ok;           // Request successful
    public final int httpCode;         // HTTP status code
    public final JSONObject json;      // Response JSON (null if parse failed)
    public final String errorMessage;  // Error message (if any)
    public final String url;           // Full URL that was called
}
```

## ⚙️ Backend Structure

Your PHP backend should be located at:
```
C:\xampp\htdocs\Earth_mover\
├── api/
│   ├── auth/
│   │   ├── user_login.php
│   │   ├── user_signup.php
│   │   ├── operator_signup.php
│   │   └── ...
│   ├── operator/
│   │   ├── get_operator_profile.php
│   │   ├── get_dashboard.php
│   │   └── ...
│   ├── admin/
│   │   └── ...
│   └── ...
```

## 🔄 Response Format

Your PHP backend should return JSON in one of these formats:

**Success:**
```json
{
  "success": true,
  "ok": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "ok": false,
  "error": "Error message",
  "message": "Error description"
}
```

## 🛠️ Features

### 1. XAMPP EOF Fix
The ApiClient includes a fix for XAMPP's intermittent EOF errors:
- Sets `http.keepAlive = false`
- Retries on ProtocolException
- Uses `Connection: close` header

### 2. Timeout Configuration
- Default: 15 seconds (`DEFAULT_TIMEOUT_MS`)
- Extended: 30 seconds (`EXTENDED_TIMEOUT_MS`)
- Fast: 10 seconds (`FAST_TIMEOUT_MS`)

### 3. Error Handling
- Automatic JSON parsing
- Error message extraction from response
- HTTP status code tracking
- Exception handling with retry logic

## 📝 Examples

See `ApiClientExample.java` for complete usage examples:
- Login
- GET requests
- Authentication
- Custom timeouts
- Response handling

## 🔗 Integration with Real-Time Updates

The `RealTimeDataManager` uses Retrofit, but you can also use `ApiClient` for real-time polling if needed.

## ⚠️ Important Notes

1. **Network Security:** For Android 9+, you may need to allow cleartext traffic:
   - Add to `AndroidManifest.xml`:
   ```xml
   <application
       android:usesCleartextTraffic="true"
       ...>
   ```

2. **IP Address:** 
   - Emulator: Use `10.0.2.2` to access host's localhost
   - Physical Device: Use your computer's actual IP on the same network

3. **Backend URL:** Make sure your PHP backend is accessible at the configured URL

## 🎯 Next Steps

1. Update `ApiConfig.java` with your computer's IP if using a physical device
2. Test API connectivity
3. Use either `ApiClient` or `Retrofit` based on your preference
4. Both approaches work with your PHP backend structure




























