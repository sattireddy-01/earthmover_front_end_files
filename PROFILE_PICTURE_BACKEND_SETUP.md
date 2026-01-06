# Profile Picture Backend Setup Guide

## 📋 Overview
This guide will help you set up the backend to store user profile pictures in the `users` table.

## ✅ Prerequisites
- XAMPP installed and running
- MySQL database `earthmover` exists
- `users` table has `profile_picture` column (varchar(500))

## 📁 File Locations

### 1. Backend PHP File
**Location:** `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`

The file has been created in your project at: `api/user/update_user_profile.php`

**Action Required:** Copy this file to your XAMPP directory:
```bash
Copy: api/user/update_user_profile.php
To: C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php
```

### 2. Uploads Directory
**Location:** `C:\xampp\htdocs\Earth_mover\uploads\profiles\`

**Action Required:** Create this directory structure:
1. Navigate to `C:\xampp\htdocs\Earth_mover\`
2. Create folder `uploads` (if it doesn't exist)
3. Inside `uploads`, create folder `profiles`
4. Full path: `C:\xampp\htdocs\Earth_mover\uploads\profiles\`

**Set Permissions (Windows):**
- Right-click on `profiles` folder
- Properties → Security
- Ensure "Users" group has "Write" permission
- Or give "Everyone" full control (for development only)

## 🔧 Database Verification

### Check if `profile_picture` column exists:
```sql
DESCRIBE users;
```

### If column doesn't exist, add it:
```sql
ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;
```

## 🧪 Testing the Backend

### 1. Test PHP File Directly
Open in browser: `http://localhost/Earth_mover/api/user/update_user_profile.php`

You should see: `{"success":false,"message":"Method not allowed. Use POST."}`

This confirms the file is accessible.

### 2. Test with Postman/curl
```bash
POST http://localhost/Earth_mover/api/user/update_user_profile.php
Content-Type: application/json

{
  "user_id": 14,
  "profile_picture": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
}
```

### 3. Check PHP Error Logs
**Location:** `C:\xampp\php\logs\php_error_log`

The backend script includes extensive logging. Check this file if uploads fail.

## 🐛 Troubleshooting

### Issue: "No fields to update"
**Cause:** Profile picture not being sent or Base64 decode failed
**Solution:** 
- Check Android logs for "Base64 preview"
- Verify `profile_picture` field is in the request
- Check PHP error log for decode errors

### Issue: "Failed to create upload directory"
**Cause:** Insufficient permissions
**Solution:**
- Manually create `uploads/profiles/` folder
- Set write permissions on the folder

### Issue: "Upload directory is not writable"
**Cause:** Folder permissions
**Solution:**
- Right-click folder → Properties → Security
- Add "Write" permission for "Users" or "Everyone"

### Issue: Profile picture not saving to database
**Cause:** SQL update failing
**Solution:**
- Check PHP error log for SQL errors
- Verify `profile_picture` column exists in database
- Check column size (should be VARCHAR(500) or larger)

### Issue: File saved but path not in database
**Cause:** SQL update succeeded but path not returned
**Solution:**
- Check PHP error log for "Profile picture saved successfully"
- Verify the UPDATE query includes `profile_picture = ?`
- Check database directly: `SELECT profile_picture FROM users WHERE user_id = 14;`

## 📊 Expected Database Values

After successful upload, the `profile_picture` column should contain:
```
uploads/profiles/user_14_1735567890.jpg
```

## 🔍 Debugging Steps

1. **Check Android Logs:**
   - Look for "=== UPLOADING PROFILE PICTURE ==="
   - Verify Base64 length > 0
   - Check "ProfilePicture set: true"

2. **Check PHP Error Log:**
   - Look for "=== UPDATE USER PROFILE REQUEST ==="
   - Verify "Has profile_picture: YES"
   - Check "Profile picture saved successfully"

3. **Check File System:**
   - Navigate to `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
   - Verify image file exists (e.g., `user_14_1735567890.jpg`)

4. **Check Database:**
   ```sql
   SELECT user_id, name, profile_picture FROM users WHERE user_id = 14;
   ```

## ✅ Verification Checklist

- [ ] PHP file copied to `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`
- [ ] Directory `C:\xampp\htdocs\Earth_mover\uploads\profiles\` exists
- [ ] Directory has write permissions
- [ ] `profile_picture` column exists in `users` table
- [ ] PHP error logging enabled
- [ ] Test upload from Android app
- [ ] Check file exists in `uploads/profiles/`
- [ ] Check database has path in `profile_picture` column

## 📝 Notes

- Profile pictures are saved as JPEG files
- Filename format: `user_{user_id}_{timestamp}.jpg`
- Path stored in database: `uploads/profiles/user_{user_id}_{timestamp}.jpg`
- Images are resized to max 800x800 before upload (Android side)
- Base64 encoding quality: 80% (Android side)





















