# Profile Picture Upload - Debugging Guide

## ✅ What Has Been Fixed

### 1. **Enhanced Logging in Android App**
- Added JSON serialization logging before API call
- Added request body logging in Retrofit interceptor
- Enhanced error response logging
- All logs will show if `profile_picture` field is being sent

### 2. **Retrofit Configuration**
- Configured Gson to serialize nulls (ensures all fields are included)
- HTTP logging enabled at BODY level
- Custom interceptor logs actual request body being sent

### 3. **Backend PHP Script**
- Comprehensive error logging
- Validates Base64 image data
- Creates upload directory if needed
- Updates database with profile picture path

## 🔍 How to Debug

### Step 1: Check Android Logs (Logcat)

Filter by tag: `EditProfileActivity` or `RetrofitClient`

**Look for these log entries:**

1. **Before API Call:**
   ```
   === UPLOADING PROFILE PICTURE ===
   User ID: 14
   ProfilePicture Base64 length: [should be > 0]
   ProfilePicture set: true
   ```

2. **JSON Payload:**
   ```
   === JSON PAYLOAD BEING SENT ===
   Full JSON length: [should be large, e.g., 50000+]
   JSON contains 'profile_picture': true
   profile_picture value length in JSON: [should match Base64 length]
   ```

3. **Retrofit Request:**
   ```
   === RETROFIT REQUEST ===
   URL: http://10.159.154.247/Earth_mover/api/user/update_user_profile.php
   Request body contains 'profile_picture': true
   ```

4. **Response:**
   ```
   === UPLOAD RESPONSE ===
   HTTP Code: 200
   Successful: true
   API Response - Success: true
   ```

### Step 2: Check PHP Error Logs

**Location:** `C:\xampp\php\logs\php_error_log`

**Look for these entries:**

1. **Request Received:**
   ```
   === UPDATE USER PROFILE REQUEST ===
   Has profile_picture: YES (length: [number])
   ```

2. **Profile Picture Processing:**
   ```
   === PROFILE PICTURE PROCESSING ===
   Profile picture received for user 14
   Base64 length: [number]
   Profile picture saved successfully: uploads/profiles/user_14_[timestamp].jpg
   ```

3. **Database Update:**
   ```
   SQL: UPDATE users SET profile_picture = ? WHERE user_id = ?
   Profile updated successfully for user 14
   ```

### Step 3: Verify File System

Check if image file was created:
```
C:\xampp\htdocs\Earth_mover\uploads\profiles\user_14_[timestamp].jpg
```

### Step 4: Check Database

Run this SQL query:
```sql
SELECT user_id, name, profile_picture FROM users WHERE user_id = 14;
```

The `profile_picture` column should contain:
```
uploads/profiles/user_14_1735567890.jpg
```

## 🐛 Common Issues & Solutions

### Issue 1: "JSON contains 'profile_picture': false"
**Problem:** The User object is not being serialized with profile_picture field.

**Solution:**
- Check if `profileImageBase64` is null or empty before setting
- Verify `user.setProfilePicture(profileImageBase64)` is being called
- Check User.java has `@SerializedName("profile_picture")` annotation

### Issue 2: "Request body contains 'profile_picture': false"
**Problem:** Retrofit is not including the field in the HTTP request.

**Solution:**
- Check RetrofitClient.java has `serializeNulls()` configured
- Verify Gson is serializing the User object correctly
- Check if there's a size limit issue (Base64 strings can be large)

### Issue 3: "Has profile_picture: NO" in PHP logs
**Problem:** Backend is not receiving the profile_picture field.

**Solution:**
- Verify the PHP file is at correct location: `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`
- Check PHP error log for JSON decode errors
- Verify Content-Type header is `application/json`

### Issue 4: "Failed to save profile picture"
**Problem:** Backend cannot save the image file.

**Solution:**
- Check if `uploads/profiles/` directory exists
- Verify directory has write permissions
- Check PHP error log for file system errors

### Issue 5: "Profile picture saved but not in database"
**Problem:** File is saved but database update fails.

**Solution:**
- Check SQL query in PHP error log
- Verify `profile_picture` column exists in database
- Check column size (should be VARCHAR(500) or larger)
- Verify user_id is correct

## 📋 Verification Checklist

After uploading a profile picture:

- [ ] Android logs show "JSON contains 'profile_picture': true"
- [ ] Android logs show "Request body contains 'profile_picture': true"
- [ ] Android logs show "HTTP Code: 200" and "Success: true"
- [ ] PHP error log shows "Has profile_picture: YES"
- [ ] PHP error log shows "Profile picture saved successfully"
- [ ] Image file exists in `uploads/profiles/` directory
- [ ] Database query shows path in `profile_picture` column
- [ ] Profile picture displays in app after reload

## 🔧 Next Steps

1. **Test the upload** - Try uploading a profile picture
2. **Check Android logs** - Verify profile_picture is in JSON
3. **Check PHP logs** - Verify backend receives and processes it
4. **Check database** - Verify path is stored
5. **Check file system** - Verify image file exists

If all logs show the data is being sent correctly but it's still not saving, the issue is likely:
- Backend file not in correct location
- Directory permissions
- Database column issue
- PHP error that's not being logged

## 📝 Important Notes

- Base64 encoded images can be very large (50KB - 500KB+)
- Retrofit/OkHttp should handle large payloads, but check timeout settings
- PHP has default `post_max_size` and `upload_max_filesize` limits
- Check `php.ini` if you get "POST Content-Length exceeded" errors





















