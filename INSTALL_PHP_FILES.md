# Install PHP Files - Step by Step Guide

## Problem
The error "Database connection error: $conn variable not set after including databas..." means the database connection file is missing or not returning `$conn` properly.

## Solution

### Step 1: Create Database Connection File

**File Location:** `C:\xampp\htdocs\Earth_mover\config\database.php`

1. Navigate to: `C:\xampp\htdocs\Earth_mover\`
2. Create a folder named `config` if it doesn't exist
3. Create a new file named `database.php` inside the `config` folder
4. Copy the content from `database.php` file I created in your project root

**OR** Copy this content directly:

```php
<?php
/**
 * Database Connection Configuration
 * Location: C:\xampp\htdocs\Earth_mover\config\database.php
 */

$host = 'localhost';
$dbname = 'earthmover';
$username = 'root';
$password = '';

$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    error_log("Database connection failed: " . $conn->connect_error);
    if (php_sapi_name() !== 'cli') {
        header('Content-Type: application/json');
        http_response_code(500);
        die(json_encode([
            'success' => false,
            'message' => 'Database connection failed: ' . $conn->connect_error
        ]));
    } else {
        die("Database connection failed: " . $conn->connect_error . "\n");
    }
}

$conn->set_charset("utf8");
return $conn;
?>
```

### Step 2: Update admin_login.php

**File Location:** `C:\xampp\htdocs\Earth_mover\api\auth\admin_login.php`

1. Open the file in a text editor
2. Find this line:
   ```php
   require_once $db_path;
   ```
3. Replace it with:
   ```php
   $conn = require_once $db_path;
   ```
   **IMPORTANT:** The `$conn =` part is crucial! This captures the return value from database.php

4. Also, make sure the response includes `'ok' => true` for successful login:
   ```php
   echo json_encode([
       'success' => true,
       'ok' => true,  // Add this line
       'message' => 'Login successful',
       'data' => [
           'user_id' => (int)$admin['admin_id'],
           'name' => $admin['name'],
           'phone' => $admin['email'],
           'email' => $admin['email']
       ]
   ], JSON_UNESCAPED_UNICODE);
   ```

### Step 3: Verify File Structure

Your file structure should look like this:
```
C:\xampp\htdocs\Earth_mover\
├── config\
│   └── database.php          ← CREATE THIS FILE
├── api\
│   ├── auth\
│   │   └── admin_login.php   ← UPDATE THIS FILE
│   └── ...
└── ...
```

### Step 4: Verify Database

1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. Check if database `earthmover` exists
3. Check if table `admins` exists
4. Verify table structure:
   ```sql
   DESCRIBE admins;
   ```
   Should show: `admin_id`, `name`, `email`, `password`, etc.

### Step 5: Test Database Connection

Create a test file: `C:\xampp\htdocs\Earth_mover\test_db.php`

```php
<?php
require_once __DIR__ . '/config/database.php';

if (isset($conn) && $conn) {
    echo "Database connection successful!<br>";
    echo "Database: " . $conn->query("SELECT DATABASE()")->fetch_row()[0];
} else {
    echo "Database connection failed!";
}
?>
```

Open in browser: `http://localhost/Earth_mover/test_db.php`

If it shows "Database connection successful!", the database.php file is working.

### Step 6: Test Admin Login API

After fixing, test the API:

**Using curl (from command prompt):**
```cmd
curl -X POST http://localhost/Earth_mover/api/auth/admin_login.php -H "Content-Type: application/json" -d "{\"email\":\"sattireddysabbella7@gmail.com\",\"password\":\"yourpassword\",\"role\":\"admin\"}"
```

**Or use Postman:**
- URL: `http://localhost/Earth_mover/api/auth/admin_login.php`
- Method: POST
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "email": "sattireddysabbella7@gmail.com",
  "password": "yourpassword",
  "role": "admin"
}
```

## Quick Fix Summary

1. ✅ Create `C:\xampp\htdocs\Earth_mover\config\database.php` with the code above
2. ✅ Change `require_once $db_path;` to `$conn = require_once $db_path;` in admin_login.php
3. ✅ Add `'ok' => true` to successful login response
4. ✅ Test the API

## Common Issues

### Issue 1: "Database config file not found"
- **Fix:** Create the `config` folder and `database.php` file at the correct path

### Issue 2: "$conn variable not set"
- **Fix:** Change `require_once` to `$conn = require_once` to capture the return value

### Issue 3: "Database connection failed"
- **Fix:** Check if MySQL is running in XAMPP
- **Fix:** Verify database name is correct (should be `earthmover`)
- **Fix:** Check if database exists in phpMyAdmin

### Issue 4: "Table 'admins' doesn't exist"
- **Fix:** Create the table using the SQL in FIX_DATABASE_CONNECTION.md

## After Fixing

1. **Rebuild your Android app** (no changes needed, but good practice)
2. **Try login again** from the app
3. **Check Logcat** for detailed logs
4. **The error should be resolved!**























