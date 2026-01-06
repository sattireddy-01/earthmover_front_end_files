# PowerShell Script to Setup XAMPP Directory Structure
# Run this script as Administrator for best results
# Right-click and "Run with PowerShell"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "XAMPP Setup for Earth Mover" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Define paths
$projectPath = "C:\Users\bhadr\AndroidStudioProjects\Eathmover"
$xamppPath = "C:\xampp\htdocs\Earth_mover"
$apiUserPath = "$xamppPath\api\user"
$uploadsProfilesPath = "$xamppPath\uploads\profiles"
$sourceFile = "$projectPath\api\user\update_user_profile.php"
$destFile = "$apiUserPath\update_user_profile.php"

Write-Host "Checking XAMPP directory..." -ForegroundColor Yellow

# Check if XAMPP base directory exists
if (-not (Test-Path $xamppPath)) {
    Write-Host "ERROR: XAMPP directory not found: $xamppPath" -ForegroundColor Red
    Write-Host "Please ensure XAMPP is installed and Earth_mover folder exists." -ForegroundColor Red
    exit 1
}

Write-Host "✓ XAMPP directory exists: $xamppPath" -ForegroundColor Green
Write-Host ""

# Step 1: Create API directory structure
Write-Host "Step 1: Creating API directory structure..." -ForegroundColor Yellow
if (-not (Test-Path $apiUserPath)) {
    try {
        New-Item -ItemType Directory -Path $apiUserPath -Force | Out-Null
        Write-Host "✓ Created: $apiUserPath" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to create: $apiUserPath" -ForegroundColor Red
        Write-Host "  Error: $_" -ForegroundColor Red
    }
} else {
    Write-Host "✓ Already exists: $apiUserPath" -ForegroundColor Green
}
Write-Host ""

# Step 2: Create uploads directory structure
Write-Host "Step 2: Creating uploads directory structure..." -ForegroundColor Yellow
if (-not (Test-Path $uploadsProfilesPath)) {
    try {
        New-Item -ItemType Directory -Path $uploadsProfilesPath -Force | Out-Null
        Write-Host "✓ Created: $uploadsProfilesPath" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to create: $uploadsProfilesPath" -ForegroundColor Red
        Write-Host "  Error: $_" -ForegroundColor Red
    }
} else {
    Write-Host "✓ Already exists: $uploadsProfilesPath" -ForegroundColor Green
}
Write-Host ""

# Step 3: Set permissions on uploads/profiles directory
Write-Host "Step 3: Setting permissions on uploads/profiles..." -ForegroundColor Yellow
if (Test-Path $uploadsProfilesPath) {
    try {
        $acl = Get-Acl $uploadsProfilesPath
        $permission = "BUILTIN\Users", "Modify", "ContainerInherit,ObjectInherit", "None", "Allow"
        $accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule $permission
        $acl.SetAccessRule($accessRule)
        Set-Acl $uploadsProfilesPath $acl
        Write-Host "✓ Permissions set on: $uploadsProfilesPath" -ForegroundColor Green
    } catch {
        Write-Host "⚠ Could not set permissions automatically. Please set manually:" -ForegroundColor Yellow
        Write-Host "  Right-click folder → Properties → Security → Allow Write" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠ Directory doesn't exist, skipping permissions" -ForegroundColor Yellow
}
Write-Host ""

# Step 4: Copy PHP file
Write-Host "Step 4: Copying update_user_profile.php..." -ForegroundColor Yellow
if (Test-Path $sourceFile) {
    try {
        if (Test-Path $destFile) {
            $overwrite = Read-Host "File already exists. Overwrite? (Y/N)"
            if ($overwrite -eq "Y" -or $overwrite -eq "y") {
                Copy-Item -Path $sourceFile -Destination $destFile -Force
                Write-Host "✓ File copied/updated: $destFile" -ForegroundColor Green
            } else {
                Write-Host "⚠ Skipped copying (file already exists)" -ForegroundColor Yellow
            }
        } else {
            Copy-Item -Path $sourceFile -Destination $destFile -Force
            Write-Host "✓ File copied: $destFile" -ForegroundColor Green
        }
    } catch {
        Write-Host "✗ Failed to copy file" -ForegroundColor Red
        Write-Host "  Error: $_" -ForegroundColor Red
        Write-Host "  Please copy manually:" -ForegroundColor Yellow
        Write-Host "    FROM: $sourceFile" -ForegroundColor Yellow
        Write-Host "    TO:   $destFile" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ Source file not found: $sourceFile" -ForegroundColor Red
    Write-Host "  Please ensure the file exists in your project." -ForegroundColor Yellow
}
Write-Host ""

# Step 5: Verify setup
Write-Host "Step 5: Verifying setup..." -ForegroundColor Yellow
$allGood = $true

if (-not (Test-Path $apiUserPath)) {
    Write-Host "✗ API user directory missing" -ForegroundColor Red
    $allGood = $false
}

if (-not (Test-Path $uploadsProfilesPath)) {
    Write-Host "✗ Uploads profiles directory missing" -ForegroundColor Red
    $allGood = $false
}

if (-not (Test-Path $destFile)) {
    Write-Host "✗ update_user_profile.php file missing" -ForegroundColor Red
    $allGood = $false
} else {
    $fileSize = (Get-Item $destFile).Length
    if ($fileSize -eq 0) {
        Write-Host "✗ update_user_profile.php file is empty" -ForegroundColor Red
        $allGood = $false
    } else {
        Write-Host "✓ update_user_profile.php exists ($fileSize bytes)" -ForegroundColor Green
    }
}

if (Test-Path $uploadsProfilesPath) {
    try {
        $testFile = "$uploadsProfilesPath\test_write_$(Get-Date -Format 'yyyyMMddHHmmss').txt"
        "test" | Out-File -FilePath $testFile -ErrorAction Stop
        Remove-Item $testFile -ErrorAction Stop
        Write-Host "✓ Uploads directory is writable" -ForegroundColor Green
    } catch {
        Write-Host "✗ Uploads directory is NOT writable" -ForegroundColor Red
        Write-Host "  Please set write permissions manually" -ForegroundColor Yellow
        $allGood = $false
    }
}

Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($allGood) {
    Write-Host "✓ All checks passed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Verify database column exists:" -ForegroundColor White
    Write-Host "   Run in phpMyAdmin: ALTER TABLE users ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL;" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Test the setup:" -ForegroundColor White
    Write-Host "   Open: http://localhost/Earth_mover/verify_xampp_setup.php" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Test from Android app" -ForegroundColor White
} else {
    Write-Host "⚠ Some issues found. Please fix them above." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")





















