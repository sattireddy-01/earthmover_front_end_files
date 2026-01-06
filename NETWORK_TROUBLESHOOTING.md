# Network Connection Troubleshooting Guide

## Changes Made

### 1. Updated RetrofitClient.java
- ✅ Added proper timeout configuration (15s connect, 30s read, 15s write)
- ✅ Added HTTP logging interceptor for debugging
- ✅ Added network interceptor for better error tracking
- ✅ Disabled keep-alive to fix XAMPP EOF issues
- ✅ Added detailed logging for network requests

### 2. Updated build.gradle.kts
- ✅ Added OkHttp dependency (4.12.0)
- ✅ Added OkHttp logging interceptor

### 3. Created Network Security Config
- ✅ Created `network_security_config.xml` to allow cleartext traffic
- ✅ Updated AndroidManifest.xml to use the network security config

### 4. Verified Configuration
- ✅ IP Address: `10.159.154.247` (correctly set in ApiConfig.java)
- ✅ Base URL: `http://10.159.154.247/Earth_mover/api/`
- ✅ All activities properly registered in AndroidManifest.xml

## Troubleshooting Steps

### Step 1: Verify XAMPP is Running
1. Open **XAMPP Control Panel**
2. Ensure **Apache** is running (green status)
3. Ensure **MySQL** is running (green status)
4. If not running, click **Start** for both services

### Step 2: Test Server Accessibility
Open your phone's browser and try:
```
http://10.159.154.247/Earth_mover/api/auth/user_login.php
```

If this doesn't load, the server is not accessible from your phone.

### Step 3: Verify Network Connection
1. **Ensure both devices are on the same Wi-Fi network**
   - Your computer: `10.159.154.247`
   - Your phone should have an IP like `10.159.154.x` (same subnet)

2. **Check Windows Firewall**
   - Open Windows Defender Firewall
   - Ensure Apache HTTP Server is allowed on port 80
   - Or temporarily disable firewall to test

### Step 4: Verify Backend Files
Check that your PHP files exist at:
```
C:\xampp\htdocs\Earth_mover\api\auth\user_login.php
C:\xampp\htdocs\Earth_mover\api\auth\admin_login.php
C:\xampp\htdocs\Earth_mover\api\auth\operator_signup.php
```

### Step 5: Check Logcat for Detailed Errors
After rebuilding the app, check Logcat with filter:
```
package:com.simats.eathmover
```

Look for:
- `RetrofitClient: Request to: ...` - Shows the actual URL being called
- `RetrofitClient: Network error for: ...` - Shows connection errors
- HTTP response codes (200 = success, 404 = not found, 500 = server error)

### Step 6: Test from Computer Browser
Open on your computer:
```
http://localhost/Earth_mover/api/auth/user_login.php
```

If this works but phone doesn't, it's a network connectivity issue.

## Common Error Messages

### "Cannot connect to server"
- **Cause**: Phone and computer not on same network
- **Fix**: Connect both to same Wi-Fi network

### "Connection timeout"
- **Cause**: Firewall blocking or server not running
- **Fix**: Check firewall settings, ensure Apache is running

### "Server error. Please try again later"
- **Cause**: Server is reachable but PHP script has errors
- **Fix**: Check XAMPP error logs at `C:\xampp\apache\logs\error.log`

### "404 Not Found"
- **Cause**: Wrong URL path or file doesn't exist
- **Fix**: Verify file paths in `C:\xampp\htdocs\Earth_mover\api\`

## Quick Test Commands

### Test Apache from Command Prompt:
```cmd
curl http://localhost/Earth_mover/api/auth/user_login.php
```

### Test from Phone (using ADB):
```cmd
adb shell am start -a android.intent.action.VIEW -d "http://10.159.154.247/Earth_mover/api/"
```

## Next Steps

1. **Rebuild the app** in Android Studio
2. **Sync Gradle** to download new dependencies
3. **Install the updated app** on your phone
4. **Check Logcat** for detailed network logs
5. **Test login/signup** and observe the logs

## Expected Logcat Output (Success)
```
RetrofitClient: Retrofit client initialized with base URL: http://10.159.154.247/Earth_mover/api/
RetrofitClient: Request to: http://10.159.154.247/Earth_mover/api/auth/user_login.php
RetrofitClient: Response: 200
```

## Expected Logcat Output (Error)
```
RetrofitClient: Network error for: http://10.159.154.247/Earth_mover/api/auth/user_login.php
RetrofitClient: Error: Failed to connect to /10.159.154.247 (port 80)
```

This will help identify the exact issue!























