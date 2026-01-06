# Quick Setup Instructions

## 🚀 Automated Setup (Recommended)

### Option 1: PowerShell Script (Windows)

1. **Right-click** on `setup_xampp.ps1`
2. Select **"Run with PowerShell"**
3. If prompted about execution policy, run this first:
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```
4. Then run the script again

The script will:
- ✅ Create all required directories
- ✅ Copy the PHP file
- ✅ Set permissions
- ✅ Verify everything is set up correctly

### Option 2: Batch Script (Windows)

1. **Right-click** on `setup_xampp.bat`
2. Select **"Run as administrator"**
3. Follow the prompts

### Option 3: Verification Script (Browser)

1. Copy `verify_xampp_setup.php` to: `C:\xampp\htdocs\Earth_mover\`
2. Open in browser: `http://localhost/Earth_mover/verify_xampp_setup.php`
3. The script will check and create directories automatically

## 📋 Manual Setup (If Scripts Don't Work)

### Step 1: Create Directories

Create these folders:
```
C:\xampp\htdocs\Earth_mover\api\user\
C:\xampp\htdocs\Earth_mover\uploads\profiles\
```

### Step 2: Copy PHP File

Copy this file:
```
FROM: C:\Users\bhadr\AndroidStudioProjects\Eathmover\api\user\update_user_profile.php
TO:   C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php
```

### Step 3: Set Permissions

1. Right-click `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
2. Properties → Security → Edit
3. Allow "Write" for Users

### Step 4: Verify Database

Run in phpMyAdmin:
```sql
ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;
```

## ✅ Verification

After setup, verify:

1. **File exists:** `C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php`
2. **Directory exists:** `C:\xampp\htdocs\Earth_mover\uploads\profiles\`
3. **Test in browser:** `http://localhost/Earth_mover/api/user/update_user_profile.php`
   - Should see: `{"success":false,"message":"Method not allowed. Use POST."}`

## 🎯 Recommended: Use PowerShell Script

The PowerShell script (`setup_xampp.ps1`) is the easiest way - it does everything automatically!





















