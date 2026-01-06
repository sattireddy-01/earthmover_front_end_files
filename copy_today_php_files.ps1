# Copy PHP files created today to XAMPP location
$projectRoot = "C:\Users\bhadr\AndroidStudioProjects\Eathmover"
$xamppRoot = "C:\xampp\htdocs\Earth_mover"

Write-Host "Copying PHP files created today to XAMPP..." -ForegroundColor Cyan
Write-Host ""

# Files to copy with their destination paths
$filesToCopy = @(
    @{
        Source = "$projectRoot\api\user\update_user_profile.php"
        Destination = "$xamppRoot\api\user\update_user_profile.php"
        CreateDir = "$xamppRoot\api\user"
    },
    @{
        Source = "$projectRoot\api\user\test_profile_upload.php"
        Destination = "$xamppRoot\api\user\test_profile_upload.php"
        CreateDir = "$xamppRoot\api\user"
    },
    @{
        Source = "$projectRoot\database.php"
        Destination = "$xamppRoot\config\database.php"
        CreateDir = "$xamppRoot\config"
    },
    @{
        Source = "$projectRoot\verify_xampp_setup.php"
        Destination = "$xamppRoot\verify_xampp_setup.php"
        CreateDir = "$xamppRoot"
    }
)

$copiedCount = 0
$skippedCount = 0
$errorCount = 0

foreach ($file in $filesToCopy) {
    $source = $file.Source
    $dest = $file.Destination
    $dir = $file.CreateDir
    
    # Check if source file exists
    if (-not (Test-Path $source)) {
        Write-Host "[SKIP] Source file not found: $source" -ForegroundColor Yellow
        $skippedCount++
        continue
    }
    
    # Create destination directory if it doesn't exist
    if (-not (Test-Path $dir)) {
        Write-Host "Creating directory: $dir" -ForegroundColor Gray
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    
    # Copy file
    try {
        Copy-Item -Path $source -Destination $dest -Force
        Write-Host "[OK] COPIED: $(Split-Path $source -Leaf) -> $dest" -ForegroundColor Green
        $copiedCount++
    } catch {
        Write-Host "[ERROR] Copying $source : $_" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host "Copied: $copiedCount files" -ForegroundColor Green
Write-Host "Skipped: $skippedCount files" -ForegroundColor Yellow
Write-Host "Errors: $errorCount files" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Green" })
Write-Host ""

# Verify critical files
Write-Host "Verifying copied files..." -ForegroundColor Cyan
foreach ($file in $filesToCopy) {
    if (Test-Path $file.Destination) {
        Write-Host "[OK] EXISTS: $($file.Destination)" -ForegroundColor Green
    } else {
        Write-Host "[MISSING] $($file.Destination)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done! PHP files copied to XAMPP location." -ForegroundColor Green

