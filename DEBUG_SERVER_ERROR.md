# Debugging "Server error. Please try again later"

## What Changed

I've updated `AdminLoginActivity.java` to:
1. ✅ **Read error response body** - Now captures actual server error messages
2. ✅ **Detect HTML error pages** - Identifies PHP fatal errors
3. ✅ **Parse JSON error messages** - Extracts error details from server
4. ✅ **Enhanced logging** - Logs full error details to Logcat
5. ✅ **Better error messages** - Shows specific error instead of generic message

## How to Debug

### Step 1: Check Logcat
After attempting login, check Logcat with filter:
```
package:com.simats.eathmover
```

Look for these logs:
```
AdminLoginActivity: Attempting admin login for: [email]
AdminLoginActivity: Base URL: http://10.159.154.247/Earth_mover/api/
AdminLoginActivity: Full URL: http://10.159.154.247/Earth_mover/api/auth/admin_login.php
AdminLoginActivity: Error response body: [actual error message]
AdminLoginActivity: Login error: HTTP 500 | URL: [full URL]
```

### Step 2: Check XAMPP Error Logs
Open the Apache error log:
```
C:\xampp\apache\logs\error.log
```

Look for PHP errors related to `admin_login.php`

### Step 3: Test the API Directly
Open your phone's browser and test:
```
http://10.159.154.247/Earth_mover/api/auth/admin_login.php
```

Or use a tool like Postman/curl to test:
```bash
curl -X POST http://10.159.154.247/Earth_mover/api/auth/admin_login.php \
  -H "Content-Type: application/json" \
  -d '{"email":"sattireddysabbella7@gmail.com","password":"yourpassword","role":"admin"}'
```

### Step 4: Verify PHP File Exists
Check that the file exists:
```
C:\xampp\htdocs\Earth_mover\api\auth\admin_login.php
```

### Step 5: Check PHP File Syntax
Open the PHP file and check for:
- Syntax errors
- Missing database connection
- Incorrect JSON response format
- Missing error handling

## Common PHP Errors That Cause HTTP 500

1. **Database Connection Error**
   ```php
   // Check if database connection is working
   $conn = new mysqli($host, $user, $pass, $db);
   if ($conn->connect_error) {
       die("Connection failed: " . $conn->connect_error);
   }
   ```

2. **Missing Headers**
   ```php
   // PHP file should start with:
   header('Content-Type: application/json');
   ```

3. **Undefined Variables**
   ```php
   // Check if $_POST or JSON input is properly parsed
   $data = json_decode(file_get_contents('php://input'), true);
   if (!$data) {
       echo json_encode(['success' => false, 'message' => 'Invalid JSON']);
       exit;
   }
   ```

4. **SQL Errors**
   ```php
   // Check SQL query syntax
   $result = $conn->query($sql);
   if (!$result) {
       echo json_encode(['success' => false, 'message' => $conn->error]);
       exit;
   }
   ```

## Expected Server Response Format

The PHP file should return JSON like:
```json
{
  "success": true,
  "ok": true,
  "message": "Login successful",
  "data": {
    "user_id": 1,
    "name": "Admin Name",
    "email": "admin@example.com"
  }
}
```

Or on error:
```json
{
  "success": false,
  "ok": false,
  "message": "Invalid email or password",
  "error": "Authentication failed"
}
```

## Quick Fix Checklist

- [ ] XAMPP Apache is running
- [ ] MySQL is running
- [ ] File exists: `C:\xampp\htdocs\Earth_mover\api\auth\admin_login.php`
- [ ] PHP file has proper JSON headers
- [ ] Database connection is configured correctly
- [ ] Admin account exists in database
- [ ] Phone and computer are on same Wi-Fi network
- [ ] Windows Firewall allows Apache on port 80

## Next Steps

1. **Rebuild the app** to get the updated error handling
2. **Try login again** and check Logcat for detailed error
3. **Share the Logcat error message** - it will now show the actual server error
4. **Check XAMPP error logs** for PHP errors

The updated code will now show you the **exact error message** from the server instead of the generic "Server error. Please try again later."























