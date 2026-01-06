# Quick Fix: Profile Picture Not Storing

## ⚡ Quick Checklist (Do These First!)

### 1. ✅ Copy PHP File to XAMPP
```
Copy from: C:\Users\bhadr\AndroidStudioProjects\Eathmover\api\user\update_user_profile.php
Copy to:   C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php
```

### 2. ✅ Create Uploads Folder
```
Create: C:\xampp\htdocs\Earth_mover\uploads\profiles\
```

### 3. ✅ Test Backend
Open browser: `http://localhost/Earth_mover/test_profile_upload.php`

This will test everything automatically!

## 🔍 If Still Not Working

### Check These Logs:

**1. Android Logcat:**
- Filter: `EditProfileActivity`
- Look for: `JSON contains 'profile_picture': true`

**2. PHP Error Log:**
- File: `C:\xampp\php\logs\php_error_log`
- Look for: `Has profile_picture: YES`

**3. Database:**
```sql
SELECT user_id, name, profile_picture FROM users WHERE user_id = 14;
```

## 🎯 Most Common Issues

1. **File not in XAMPP** → Copy to `C:\xampp\htdocs\Earth_mover\api\user\`
2. **Directory missing** → Create `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
3. **No permissions** → Right-click folder → Properties → Security → Allow Write
4. **Column missing** → Run: `ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;`

## 📞 Still Stuck?

Run the test script and share the output:
```
http://localhost/Earth_mover/test_profile_upload.php
```





















