# PowerShell Script to Copy PHP Files to XAMPP
# This script copies PHP files from Android project to correct XAMPP location

Write-Host "Copying PHP Files to XAMPP..." -ForegroundColor Cyan
Write-Host ""

$projectRoot = "C:\Users\bhadr\AndroidStudioProjects\Eathmover"
$xamppRoot = "C:\xampp\htdocs\Earth_mover"

# Check if XAMPP directory exists
if (-not (Test-Path $xamppRoot)) {
    Write-Host "ERROR: XAMPP directory not found: $xamppRoot" -ForegroundColor Red
    exit 1
}

Write-Host "XAMPP directory exists: $xamppRoot" -ForegroundColor Green
Write-Host ""

# Files to copy
$files = @(
    "api\user\update_user_profile.php",
    "api\user\test_profile_upload.php",
    "api\user\get_user_bookings.php",
    "api\admin\get_live_bookings.php",
    "api\operator\get_earnings.php",
    "database.php",
    "verify_xampp_setup.php"
)

# Destination mapping
$destMap = @{
    "api\user\update_user_profile.php" = "api\user\update_user_profile.php";
    "api\user\test_profile_upload.php" = "api\user\test_profile_upload.php";
    "api\user\get_user_bookings.php" = "api\user\get_user_bookings.php";
    "api\admin\get_live_bookings.php" = "api\admin\get_live_bookings.php";
    "api\operator\get_earnings.php" = "api\operator\get_earnings.php";
    "database.php" = "config\database.php";
    "verify_xampp_setup.php" = "verify_xampp_setup.php"
}

$copiedCount = 0

foreach ($file in $files) {
    $srcPath = Join-Path $projectRoot $file
    $destRel = $destMap[$file]
    $destPath = Join-Path $xamppRoot $destRel
    $destDir = Split-Path $destPath -Parent
    
    if (-not (Test-Path $srcPath)) {
        Write-Host "SKIP: Missing source: $file" -ForegroundColor Yellow
        continue
    }

    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }

    try {
        Copy-Item -Path $srcPath -Destination $destPath -Force
        Write-Host "COPIED: $file" -ForegroundColor Green
        $copiedCount++
    } catch {
        Write-Host "ERROR: Failed to copy $file" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done. Copied $copiedCount files." -ForegroundColor Cyan
