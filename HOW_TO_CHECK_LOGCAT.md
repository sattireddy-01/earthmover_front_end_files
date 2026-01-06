# How to Check Logcat for Earth Mover App

## Filter Logcat for Earth Mover App

### Method 1: Filter by Package Name
In Android Studio Logcat, use this filter:
```
package:com.simats.eathmover
```

### Method 2: Filter by Tag
Filter by the tag we're using:
```
tag:OperatorProfileActivity
```

### Method 3: Combined Filter
To see all relevant logs:
```
package:com.simats.eathmover | tag:OperatorProfileActivity
```

## What to Look For

When you open the operator profile page, you should see logs like:

1. **API Call Logs**:
   ```
   D/OperatorProfileActivity: === Loading operator profile for ID: 51 ===
   D/OperatorProfileActivity: API URL: http://10.118.154.247/Earth_mover/api/operator/get_operator_profile.php?operator_id=51
   ```

2. **API Response Logs**:
   ```
   D/OperatorProfileActivity: API Response - Code: 200, Successful: true
   D/OperatorProfileActivity: API Response - Success: true, Data: not null
   D/OperatorProfileActivity: Profile loaded successfully. Profile image: uploads/profile_images/operator_51_profile_1767423272.jpg
   ```

3. **Image Loading Logs**:
   ```
   D/OperatorProfileActivity: Profile image path from operator: uploads/profile_images/operator_51_profile_1767423272.jpg
   D/OperatorProfileActivity: loadProfileImage called with path: uploads/profile_images/operator_51_profile_1767423272.jpg
   D/OperatorProfileActivity: Root URL: http://10.118.154.247/Earth_mover/
   D/OperatorProfileActivity: Loading profile image from: http://10.118.154.247/Earth_mover/uploads/profile_images/operator_51_profile_1767423272.jpg
   ```

4. **Success/Error Logs**:
   ```
   D/OperatorProfileActivity: Profile image loaded successfully from: http://...
   ```
   OR
   ```
   E/OperatorProfileActivity: Error loading profile image: ...
   ```

## Steps to Get the Right Logs

1. **Open Android Studio**
2. **Connect your device/emulator**
3. **Open Logcat tab** (usually at bottom of Android Studio)
4. **Clear the log** (trash icon)
5. **Set filter to**: `package:com.simats.eathmover`
6. **Open the Earth Mover app**
7. **Navigate to Operator Profile page**
8. **Copy all the logs** that appear

## Common Issues to Check

### Issue 1: No Logs Appearing
- Make sure the app is actually running
- Check if Logcat is connected to the right device
- Try restarting Logcat

### Issue 2: API Returns Error
Look for:
```
E/OperatorProfileActivity: Failed to load operator profile: HTTP 404
```
or
```
E/OperatorProfileActivity: Error loading operator profile: ...
```

### Issue 3: Profile Image is NULL
Look for:
```
D/OperatorProfileActivity: Profile image path from operator: null
```
This means the database doesn't have a profile_image value.

### Issue 4: Image URL Error
Look for:
```
E/OperatorProfileActivity: Error loading profile image: ...
```
This means the image file doesn't exist or URL is wrong.

## Share the Logs

Once you have the filtered logs, share them and I can help identify the exact issue!





