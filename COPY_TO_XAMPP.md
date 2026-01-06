# Copy Files to XAMPP - Step by Step Guide

## 📋 Required Files and Directories

### Files to Copy:
1. `api/user/update_user_profile.php` → `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`

### Directories to Create:
1. `C:\xampp\htdocs\Earth_mover\api\user\` (if not exists)
2. `C:\xampp\htdocs\Earth_mover\uploads\profiles\` (if not exists)

## 🚀 Quick Setup Method

### Option 1: Use Verification Script (Recommended)

1. **Copy verification script to XAMPP:**
   - Copy `verify_xampp_setup.php` from your project
   - Paste to: `C:\xampp\htdocs\Earth_mover\verify_xampp_setup.php`

2. **Open in browser:**
   ```
   http://localhost/Earth_mover/verify_xampp_setup.php
   ```

3. **The script will:**
   - ✅ Check if directories exist (create if missing)
   - ✅ Check if PHP file exists
   - ✅ Test write permissions
   - ✅ Check database connection
   - ✅ Check database column
   - ✅ Show you exactly what's missing

### Option 2: Manual Setup

#### Step 1: Create Directory Structure

1. Navigate to: `C:\xampp\htdocs\Earth_mover\`

2. Create these folders (if they don't exist):
   - `api`
   - `api\user`
   - `uploads`
   - `uploads\profiles`

#### Step 2: Copy PHP File

1. Open File Explorer
2. Navigate to your project: `C:\Users\bhadr\AndroidStudioProjects\Eathmover\api\user\`
3. Copy `update_user_profile.php`
4. Navigate to: `C:\xampp\htdocs\Earth_mover\api\user\`
5. Paste the file

#### Step 3: Set Permissions

1. Right-click on `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
2. Properties → Security tab
3. Click "Edit"
4. Select "Users" (or "Everyone" for testing)
5. Check "Write" permission
6. Click OK

#### Step 4: Verify Database Column

1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. Select database: `earthmover`
3. Select table: `users`
4. Check if `profile_picture` column exists

If not, run this SQL:
```sql
ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;
```

## ✅ Verification Checklist

After setup, verify:

- [ ] `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php` exists
- [ ] `C:\xampp\htdocs\Earth_mover\uploads\profiles\` exists
- [ ] `uploads\profiles\` folder has write permissions
- [ ] `profile_picture` column exists in `users` table
- [ ] Test script runs without errors

## 🧪 Test the Setup

1. **Run verification script:**
   ```
   http://localhost/Earth_mover/verify_xampp_setup.php
   ```

2. **Or test manually:**
   - Open: `http://localhost/Earth_mover/api/user/update_user_profile.php`
   - Should see: `{"success":false,"message":"Method not allowed. Use POST."}`

3. **Test from Android app:**
   - Upload a profile picture
   - Check Android logs
   - Check PHP error log: `C:\xampp\php\logs\php_error_log`
   - Check if file created in `uploads\profiles\`
   - Check database for `profile_picture` value

## 📝 File Locations Summary

```
C:\xampp\htdocs\Earth_mover\
├── api\
│   └── user\
│       └── update_user_profile.php  ← COPY THIS FILE
└── uploads\
    └── profiles\                    ← CREATE THIS FOLDER
```

## 🐛 Troubleshooting

### "File not found" error
- Verify file is at: `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`
- Check file name spelling (case-sensitive on some servers)

### "Permission denied" error
- Set write permissions on `uploads\profiles\` folder
- Right-click → Properties → Security → Allow Write

### "No fields to update" error
- Check PHP error log for why profile picture processing failed
- Verify `uploads\profiles\` directory exists and is writable

### Database column missing
- Run: `ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;`
