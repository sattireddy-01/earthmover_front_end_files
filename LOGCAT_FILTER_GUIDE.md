# Logcat Filter Guide

## Problem
The Logcat is showing continuous Bluetooth logs from `vendor.qti.ibs_handler` service. These are **normal system logs**, not errors. They're just very verbose.

## Solution: Filter Logcat in Android Studio

### Method 1: Filter by Package Name (Recommended)
1. Open **Logcat** panel in Android Studio
2. In the filter box at the top, enter:
   ```
   package:com.simats.eathmover
   ```
   This will show **only your app's logs**.

### Method 2: Filter by Tag
Filter by your app's log tags:
```
tag:UserSignupActivity | tag:OperatorLoginActivity | tag:AdminLoginActivity | tag:AdminOperatorVerification
```

### Method 3: Exclude Bluetooth Logs
To exclude Bluetooth logs, use:
```
-package:vendor.qti.ibs_handler -package:android.hardware.bluetooth
```

### Method 4: Filter by Log Level
Show only errors and warnings:
```
level:error | level:warning
```

### Method 5: Combined Filter (Best for Development)
Show only your app's logs with errors/warnings:
```
package:com.simats.eathmover | (level:error | level:warning)
```

## Quick Steps in Android Studio:
1. Click on the **Logcat** tab
2. Click the **filter dropdown** (shows "Show only selected application" or "No Filters")
3. Select **"Edit Filter Configuration"**
4. Create a new filter:
   - **Name**: "My App Only"
   - **Package Name**: `com.simats.eathmover`
   - Click **OK**
5. Select your new filter from the dropdown

## Alternative: Use Regex Filter
In the Logcat search box, use:
```
^(?!.*vendor\.qti\.ibs_handler)(?!.*bluetooth).*
```
This excludes all Bluetooth-related logs.

## Note
These Bluetooth logs are **harmless** and don't affect your app's functionality. They're just verbose system logs from the device's Bluetooth stack.























