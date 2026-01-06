# Fix Database Connection Error

## Error Message
```
Database connection error: $conn variable not set after including databas...
```

## Problem
The PHP file `admin_login.php` is trying to use a database connection variable `$conn` but it's not being set properly.

## Solution

### Step 1: Check Database Connection File

Your PHP files should include a database connection file. Check if this file exists:
```
C:\xampp\htdocs\Earth_mover\api\config\database.php
```
OR
```
C:\xampp\htdocs\Earth_mover\api\database.php
```

### Step 2: Create/Update Database Connection File

Create or update the database connection file. Here's the correct structure:

**File: `C:\xampp\htdocs\Earth_mover\api\config\database.php`**

```php
<?php
// Database configuration
$host = 'localhost';
$dbname = 'earthmover';  // Your database name
$username = 'root';       // Default XAMPP username
$password = '';           // Default XAMPP password (empty)

// Create connection
$conn = new mysqli($host, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode([
        'success' => false,
        'message' => 'Database connection failed: ' . $conn->connect_error
    ]));
}

// Set charset to utf8
$conn->set_charset("utf8");

// Return connection (if using include/require)
return $conn;
?>
```

### Step 3: Fix admin_login.php

**File: `C:\xampp\htdocs\Earth_mover\api\auth\admin_login.php`**

```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Include database connection
require_once __DIR__ . '/../config/database.php';
// OR if database.php is in api folder:
// require_once __DIR__ . '/../database.php';

// Check if $conn is set
if (!isset($conn) || $conn === null) {
    echo json_encode([
        'success' => false,
        'message' => 'Database connection error: Connection not established'
    ]);
    exit;
}

// Get JSON input
$data = json_decode(file_get_contents('php://input'), true);

if (!$data) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid JSON input'
    ]);
    exit;
}

// Get email and password
$email = isset($data['email']) ? trim($data['email']) : '';
$password = isset($data['password']) ? $data['password'] : '';
$role = isset($data['role']) ? $data['role'] : 'admin';

// Validate input
if (empty($email) || empty($password)) {
    echo json_encode([
        'success' => false,
        'message' => 'Email and password are required'
    ]);
    exit;
}

// Prepare SQL statement
$stmt = $conn->prepare("SELECT admin_id, name, email, password FROM admins WHERE email = ? AND role = ?");
if (!$stmt) {
    echo json_encode([
        'success' => false,
        'message' => 'Database query preparation failed: ' . $conn->error
    ]);
    exit;
}

$stmt->bind_param("ss", $email, $role);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid email or password'
    ]);
    exit;
}

$admin = $result->fetch_assoc();

// Verify password (assuming passwords are hashed)
if (password_verify($password, $admin['password'])) {
    // Login successful
    echo json_encode([
        'success' => true,
        'ok' => true,
        'message' => 'Login successful',
        'data' => [
            'user_id' => $admin['admin_id'],
            'name' => $admin['name'],
            'email' => $admin['email']
        ]
    ]);
} else {
    // Password incorrect
    echo json_encode([
        'success' => false,
        'message' => 'Invalid email or password'
    ]);
}

$stmt->close();
$conn->close();
?>
```

### Step 4: Alternative Database Connection (If using PDO)

If you prefer PDO instead of mysqli:

**File: `C:\xampp\htdocs\Earth_mover\api\config\database.php`**

```php
<?php
$host = 'localhost';
$dbname = 'earthmover';
$username = 'root';
$password = '';

try {
    $conn = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $conn;
} catch(PDOException $e) {
    die(json_encode([
        'success' => false,
        'message' => 'Database connection failed: ' . $e->getMessage()
    ]));
}
?>
```

### Step 5: Verify Database and Table

1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. Check if database `earthmover` exists
3. Check if table `admins` exists with columns:
   - `admin_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
   - `name` (VARCHAR)
   - `email` (VARCHAR, UNIQUE)
   - `password` (VARCHAR - should be hashed)
   - `role` (VARCHAR, default 'admin')

### Step 6: Create Admin Table (If doesn't exist)

```sql
CREATE TABLE IF NOT EXISTS `admins` (
  `admin_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `role` VARCHAR(20) DEFAULT 'admin',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### Step 7: Insert Test Admin (If needed)

```sql
INSERT INTO `admins` (`name`, `email`, `password`, `role`) 
VALUES ('Admin User', 'sattireddysabbella7@gmail.com', '$2y$10$YourHashedPasswordHere', 'admin');
```

**Note:** Replace `$2y$10$YourHashedPasswordHere` with actual hashed password. You can use PHP's `password_hash()` function:

```php
echo password_hash('yourpassword', PASSWORD_DEFAULT);
```

## Quick Checklist

- [ ] Database connection file exists at correct path
- [ ] `$conn` variable is properly set in database.php
- [ ] `admin_login.php` includes database.php correctly
- [ ] Database `earthmover` exists in MySQL
- [ ] Table `admins` exists with correct structure
- [ ] Admin account exists in database
- [ ] Password is properly hashed in database
- [ ] XAMPP MySQL is running

## Testing

After fixing, test the API directly:

```bash
curl -X POST http://localhost/Earth_mover/api/auth/admin_login.php \
  -H "Content-Type: application/json" \
  -d '{"email":"sattireddysabbella7@gmail.com","password":"yourpassword","role":"admin"}'
```

Or test from your phone's browser (though POST won't work, you can check if file exists).

## Common Issues

1. **Wrong file path**: Check the exact path in `require_once`
2. **Database name mismatch**: Ensure database name matches in database.php
3. **MySQL not running**: Check XAMPP Control Panel
4. **Wrong password hash**: Ensure passwords are hashed with `password_hash()`
5. **Table name mismatch**: Check if table is `admins` or `admin` (singular)























