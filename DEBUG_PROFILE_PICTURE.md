# Debug Profile Picture Not Saving

## 🔍 Step-by-Step Debugging

### Step 1: Verify Backend File Location

**CRITICAL:** The PHP file MUST be in XAMPP, not just in your project!

**Check if file exists:**
```
C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php
```

**If it doesn't exist:**
1. Copy from: `C:\Users\bhadr\AndroidStudioProjects\Eathmover\api\user\update_user_profile.php`
2. To: `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`

### Step 2: Check PHP Error Logs

**Location:** `C:\xampp\php\logs\php_error_log`

**Look for these entries when uploading:**

```
=== UPDATE USER PROFILE REQUEST ===
Has profile_picture: YES (length: [number])  ← MUST BE YES!
=== PROFILE PICTURE PROCESSING ===
Profile picture received for user 14
Base64 length: [number]
=== saveProfilePicture() called ===
Profile picture saved successfully: uploads/profiles/user_14_[timestamp].jpg
Profile picture path added to update query
SQL: UPDATE users SET profile_picture = ? WHERE user_id = ?
SQL UPDATE executed successfully
=== DATABASE VERIFICATION ===
Verified profile_picture in database: uploads/profiles/user_14_[timestamp].jpg
```

**If you see "Has profile_picture: NO":**
- Android is not sending the field
- Check Android logs (see Step 3)

**If you see "Failed to save profile picture":**
- Directory doesn't exist or no write permission
- Check Step 4

**If you see "SQL UPDATE executed successfully" but database is NULL:**
- SQL binding issue
- Check Step 5

### Step 3: Check Android Logs

**Filter:** `EditProfileActivity`

**When selecting image, look for:**
```
=== UPLOADING PROFILE PICTURE ===
ProfilePicture Base64 length: [should be > 0]
JSON contains 'profile_picture': true  ← MUST BE TRUE!
```

**When clicking "Save Changes", look for:**
```
=== SAVE PROFILE CHANGES ===
Including profile picture in profile update (Base64 length: [number])  ← MUST APPEAR!
```

**If "JSON contains 'profile_picture': false":**
- The User object is not being serialized correctly
- Check if `profileImageBase64` is set before creating User object

### Step 4: Verify Uploads Directory

**Check if directory exists:**
```
C:\xampp\htdocs\Earth_mover\uploads\profiles\
```

**If it doesn't exist:**
1. Create the folder manually
2. Or run the setup script: `setup_xampp.ps1`

**Check permissions:**
- Right-click folder → Properties → Security
- Ensure "Users" or "Everyone" has "Write" permission

**Test write:**
- Try creating a test file in the folder
- If it fails, fix permissions

### Step 5: Test Backend Directly

**Option 1: Use test endpoint**

1. Copy `api/user/test_profile_upload.php` to:
   ```
   C:\xampp\htdocs\Earth_mover\api\user\test_profile_upload.php
   ```

2. Test with Postman or curl:
   ```json
   POST http://localhost/Earth_mover/api/user/test_profile_upload.php
   Content-Type: application/json
   
   {
     "user_id": 14,
     "profile_picture": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
   }
   ```

3. Check response - should show `"has_profile_picture": true`

**Option 2: Check actual update endpoint**

1. Test with Postman:
   ```json
   POST http://localhost/Earth_mover/api/user/update_user_profile.php
   Content-Type: application/json
   
   {
     "user_id": 14,
     "name": "Test User",
     "phone": "7995778148",
     "profile_picture": "[base64_string]"
   }
   ```

2. Check PHP error log for response

### Step 6: Check Database Directly

**Run in phpMyAdmin:**
```sql
SELECT user_id, name, profile_picture FROM users WHERE user_id = 14;
```

**Expected result:**
```
profile_picture: uploads/profiles/user_14_1735567890.jpg
```

**If NULL:**
- Check PHP error log for SQL errors
- Verify the UPDATE query was executed
- Check if profile_picture column exists

### Step 7: Verify File Was Created

**Check file system:**
```
C:\xampp\htdocs\Earth_mover\uploads\profiles\user_14_*.jpg
```

**If file doesn't exist:**
- `saveProfilePicture()` function failed
- Check PHP error log for errors
- Verify directory permissions

## 🐛 Common Issues & Solutions

### Issue 1: "Has profile_picture: NO" in PHP logs

**Problem:** Backend not receiving the field

**Solutions:**
1. Check Android logs - is `JSON contains 'profile_picture': true`?
2. Verify PHP file is at correct location in XAMPP
3. Check if JSON is being decoded correctly
4. Verify Content-Type header is `application/json`

### Issue 2: "Failed to save profile picture"

**Problem:** File save failing

**Solutions:**
1. Create `uploads/profiles/` directory
2. Set write permissions on folder
3. Check PHP error log for specific error
4. Verify disk space available

### Issue 3: "SQL UPDATE executed successfully" but database is NULL

**Problem:** SQL binding issue

**Solutions:**
1. Check PHP error log for SQL errors
2. Verify `$types` string matches number of values
3. Check if `bind_param()` is working correctly
4. Verify column name is correct: `profile_picture`

### Issue 4: File saved but database not updated

**Problem:** SQL update not including profile_picture

**Solutions:**
1. Check if `$updateFields` includes `profile_picture = ?`
2. Verify `$updateValues` includes the path
3. Check `$types` includes 's' for string
4. Verify SQL query in error log

## 📋 Complete Checklist

- [ ] PHP file exists at: `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`
- [ ] Directory exists: `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
- [ ] Directory has write permissions
- [ ] `profile_picture` column exists in database
- [ ] Android logs show: `JSON contains 'profile_picture': true`
- [ ] PHP logs show: `Has profile_picture: YES`
- [ ] PHP logs show: `Profile picture saved successfully`
- [ ] PHP logs show: `SQL UPDATE executed successfully`
- [ ] PHP logs show: `Verified profile_picture in database: [path]`
- [ ] File exists in: `uploads/profiles/user_14_*.jpg`
- [ ] Database shows path in `profile_picture` column

## 🎯 Quick Test

Run this SQL to check current state:
```sql
SELECT user_id, name, profile_picture, 
       CASE 
           WHEN profile_picture IS NULL THEN 'NULL - Not set'
           WHEN profile_picture = '' THEN 'EMPTY - Set but empty'
           ELSE CONCAT('SET - ', profile_picture)
       END as status
FROM users WHERE user_id = 14;
```

This will show you exactly what's in the database.





















