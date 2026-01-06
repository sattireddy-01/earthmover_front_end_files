@echo off
REM Batch Script to Setup XAMPP Directory Structure
REM Run this script as Administrator

echo ========================================
echo XAMPP Setup for Earth Mover
echo ========================================
echo.

set "XAMPP_PATH=C:\xampp\htdocs\Earth_mover"
set "PROJECT_PATH=C:\Users\bhadr\AndroidStudioProjects\Eathmover"
set "API_USER_PATH=%XAMPP_PATH%\api\user"
set "UPLOADS_PROFILES_PATH=%XAMPP_PATH%\uploads\profiles"
set "SOURCE_FILE=%PROJECT_PATH%\api\user\update_user_profile.php"
set "DEST_FILE=%API_USER_PATH%\update_user_profile.php"

echo Checking XAMPP directory...

if not exist "%XAMPP_PATH%" (
    echo ERROR: XAMPP directory not found: %XAMPP_PATH%
    echo Please ensure XAMPP is installed and Earth_mover folder exists.
    pause
    exit /b 1
)

echo [OK] XAMPP directory exists: %XAMPP_PATH%
echo.

echo Step 1: Creating API directory structure...
if not exist "%API_USER_PATH%" (
    mkdir "%API_USER_PATH%" 2>nul
    if exist "%API_USER_PATH%" (
        echo [OK] Created: %API_USER_PATH%
    ) else (
        echo [ERROR] Failed to create: %API_USER_PATH%
    )
) else (
    echo [OK] Already exists: %API_USER_PATH%
)
echo.

echo Step 2: Creating uploads directory structure...
if not exist "%UPLOADS_PROFILES_PATH%" (
    mkdir "%UPLOADS_PROFILES_PATH%" 2>nul
    if exist "%UPLOADS_PROFILES_PATH%" (
        echo [OK] Created: %UPLOADS_PROFILES_PATH%
    ) else (
        echo [ERROR] Failed to create: %UPLOADS_PROFILES_PATH%
    )
) else (
    echo [OK] Already exists: %UPLOADS_PROFILES_PATH%
)
echo.

echo Step 3: Copying update_user_profile.php...
if exist "%SOURCE_FILE%" (
    if exist "%DEST_FILE%" (
        echo File already exists. Overwriting...
    )
    copy /Y "%SOURCE_FILE%" "%DEST_FILE%" >nul 2>&1
    if exist "%DEST_FILE%" (
        echo [OK] File copied: %DEST_FILE%
    ) else (
        echo [ERROR] Failed to copy file
        echo Please copy manually:
        echo   FROM: %SOURCE_FILE%
        echo   TO:   %DEST_FILE%
    )
) else (
    echo [ERROR] Source file not found: %SOURCE_FILE%
    echo Please ensure the file exists in your project.
)
echo.

echo Step 4: Verifying setup...
set "ALL_GOOD=1"

if not exist "%API_USER_PATH%" (
    echo [ERROR] API user directory missing
    set "ALL_GOOD=0"
)

if not exist "%UPLOADS_PROFILES_PATH%" (
    echo [ERROR] Uploads profiles directory missing
    set "ALL_GOOD=0"
)

if not exist "%DEST_FILE%" (
    echo [ERROR] update_user_profile.php file missing
    set "ALL_GOOD=0"
) else (
    echo [OK] update_user_profile.php exists
)

echo.
echo ========================================
echo Setup Summary
echo ========================================

if "%ALL_GOOD%"=="1" (
    echo [OK] All checks passed!
    echo.
    echo Next steps:
    echo 1. Verify database column exists in phpMyAdmin
    echo 2. Test: http://localhost/Earth_mover/verify_xampp_setup.php
    echo 3. Test from Android app
) else (
    echo [WARNING] Some issues found. Please fix them above.
)

echo.
pause





















