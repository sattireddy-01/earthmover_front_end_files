# Profile Picture Setup - Complete Guide

## 🚨 IMPORTANT: File Locations

The backend PHP file **MUST** be in the XAMPP directory, not just in your project folder!

### Step 1: Copy Backend File to XAMPP

**Source (in your project):**
```
api/user/update_user_profile.php
```

**Destination (XAMPP):**
```
C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php
```

**How to copy:**
1. Open File Explorer
2. Navigate to: `C:\Users\bhadr\AndroidStudioProjects\Eathmover\api\user\`
3. Copy `update_user_profile.php`
4. Navigate to: `C:\xampp\htdocs\Earth_mover\api\user\`
5. Paste the file (overwrite if it exists)

### Step 2: Create Uploads Directory

Create this folder structure:

```
C:\xampp\htdocs\Earth_mover\uploads\profiles\
```

**Steps:**
1. Navigate to: `C:\xampp\htdocs\Earth_mover\`
2. Create folder `uploads` (if it doesn't exist)
3. Inside `uploads`, create folder `profiles`
4. Right-click `profiles` folder → Properties → Security tab
5. Click "Edit" → Select "Users" → Check "Write" permission → OK

### Step 3: Verify Database Column

Open phpMyAdmin and run:

```sql
DESCRIBE users;
```

Check if `profile_picture` column exists. If not, run:

```sql
ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;
```

### Step 4: Test the Backend

Open browser and go to:
```
http://localhost/Earth_mover/api/user/update_user_profile.php
```

You should see:
```json
{"success":false,"message":"Method not allowed. Use POST."}
```

This confirms the file is accessible.

## 🔍 Debugging Steps

### Check Android Logs (Logcat)

Filter by: `EditProfileActivity`

Look for:
```
=== UPLOADING PROFILE PICTURE ===
ProfilePicture Base64 length: [should be > 0]
JSON contains 'profile_picture': true  ← MUST BE TRUE!
```

### Check PHP Error Logs

**Location:** `C:\xampp\php\logs\php_error_log`

Look for:
```
=== UPDATE USER PROFILE REQUEST ===
Has profile_picture: YES (length: [number])  ← MUST BE YES!
=== PROFILE PICTURE PROCESSING ===
Profile picture saved successfully: uploads/profiles/user_14_[timestamp].jpg
```

### Check File System

Navigate to: `C:\xampp\htdocs\Earth_mover\uploads\profiles\`

You should see files like: `user_14_1735567890.jpg`

### Check Database

Run in phpMyAdmin:
```sql
SELECT user_id, name, profile_picture FROM users WHERE user_id = 14;
```

The `profile_picture` column should contain:
```
uploads/profiles/user_14_1735567890.jpg
```

## 🐛 Common Issues

### Issue 1: "No profile_picture field in request data"
**Cause:** Android is not sending the field
**Solution:** Check Android logs - verify `JSON contains 'profile_picture': true`

### Issue 2: "Failed to save profile picture"
**Cause:** Directory doesn't exist or no write permission
**Solution:** 
- Create `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
- Set write permissions on the folder

### Issue 3: "File saved but not in database"
**Cause:** SQL update failed
**Solution:** Check PHP error log for SQL errors

### Issue 4: "Profile picture saved but path is NULL in database"
**Cause:** Database column doesn't exist or wrong column name
**Solution:** Verify column exists: `DESCRIBE users;`

## ✅ Verification Checklist

- [ ] PHP file copied to `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`
- [ ] Directory `C:\xampp\htdocs\Earth_mover\uploads\profiles\` exists
- [ ] Directory has write permissions
- [ ] `profile_picture` column exists in `users` table
- [ ] Test upload from Android app
- [ ] Check Android logs - `JSON contains 'profile_picture': true`
- [ ] Check PHP logs - `Has profile_picture: YES`
- [ ] Check file exists in `uploads/profiles/`
- [ ] Check database - path stored in `profile_picture` column





















