# Profile Picture Upload Setup Guide

## Step 1: Copy PHP Backend File

**Copy the file `api_user_update_user_profile.php` to your backend:**

```
Source: api_user_update_user_profile.php (in your project root)
Destination: C:\xampp\htdocs\Earth_mover\api\user\update_user_profile.php
```

**Important:** The file must be named `update_user_profile.php` and placed in the `api/user/` directory.

## Step 2: Create Uploads Directory

Create the following directory structure:

```
C:\xampp\htdocs\Earth_mover\uploads\profiles\
```

**To create it:**
1. Navigate to `C:\xampp\htdocs\Earth_mover\`
2. Create folder `uploads` if it doesn't exist
3. Inside `uploads`, create folder `profiles`
4. Make sure the folder has write permissions (right-click → Properties → Security → Allow write access)

## Step 3: Verify Database Column Exists

Run this SQL query in phpMyAdmin to verify the column exists:

```sql
DESCRIBE users;
```

You should see `profile_picture` column with type `varchar(500)`.

If it doesn't exist, run:

```sql
ALTER TABLE `users` 
ADD COLUMN `profile_picture` VARCHAR(500) DEFAULT NULL 
AFTER `address`;
```

## Step 4: Test the Backend

1. Start XAMPP (Apache and MySQL)
2. Test the endpoint using Postman or curl:

```bash
POST http://10.159.154.247/Earth_mover/api/user/update_user_profile.php
Content-Type: application/json

{
  "user_id": 14,
  "profile_picture": "base64_encoded_string_here"
}
```

## Step 5: Check Logs

Check PHP error logs for debugging:
- Location: `C:\xampp\php\logs\php_error_log`
- Or: `C:\xampp\apache\logs\error.log`

The script logs detailed information about:
- Received data
- File save operations
- Database updates
- Any errors

## Step 6: Verify File Was Created

After uploading, check:
1. File exists: `C:\xampp\htdocs\Earth_mover\uploads\profiles\user_14_timestamp.jpg`
2. Database updated: Run `SELECT profile_picture FROM users WHERE user_id = 14;`

## Troubleshooting

### Issue: Profile picture not saving
- Check PHP error logs
- Verify uploads/profiles/ directory exists and is writable
- Check database connection in PHP script matches your setup

### Issue: Image not displaying
- Verify the file path in database is correct
- Check file exists at the path
- Verify URL construction in Android code matches your server structure

### Issue: Permission denied
- Right-click on `uploads` folder → Properties → Security
- Add "Everyone" or your user with "Write" permission
- Apply to all subfolders





















