package com.simats.eathmover;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.User;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Edit Profile page for updating user information.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_CAMERA = 2001;
    private static final int REQUEST_GALLERY = 2002;
    private static final int PERMISSION_CAMERA = 3001;
    private static final int PERMISSION_STORAGE = 3002;

    private EditText etName, etPhone, etEmail, etAddress;
    private Button btnSaveChanges;
    private ProgressBar progressBar;
    private TextView tvUserName, tvMemberSince;
    private ImageView ivProfilePicture;
    private String userId;
    private SessionManager sessionManager;
    private Uri cameraImageUri;
    private String profileImageBase64;
    private Bitmap currentProfileBitmap;
    private String currentProfileImagePath;
    private boolean profilePictureUploaded = false; // Track if profile picture was already uploaded
    private boolean hasNewImageSelected = false; // Track if user just selected a new image (not yet uploaded)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== EditProfileActivity onCreate() STARTED ===");
        
        try {
            setContentView(R.layout.activity_edit_profile);
            Log.d(TAG, "Layout set successfully - activity_edit_profile.xml loaded");
        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR setting content view: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error loading edit profile page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        Log.d(TAG, "SessionManager initialized");

        Toolbar toolbar = findViewById(R.id.toolbar_edit_profile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // Back button navigation
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Initialize views
        try {
            etName = findViewById(R.id.et_name);
            etPhone = findViewById(R.id.et_phone);
            etEmail = findViewById(R.id.et_email);
            etAddress = findViewById(R.id.et_address);
            tvUserName = findViewById(R.id.tv_user_name);
            tvMemberSince = findViewById(R.id.tv_member_since);
            btnSaveChanges = findViewById(R.id.btn_save_changes);
            progressBar = findViewById(R.id.progress_bar);
            ivProfilePicture = findViewById(R.id.iv_profile_picture);
            
            Log.d(TAG, "Views initialized - Name: " + (etName != null) + ", Phone: " + (etPhone != null) + 
                      ", Email: " + (etEmail != null) + ", Address: " + (etAddress != null) +
                      ", SaveButton: " + (btnSaveChanges != null) + ", ProfilePicture: " + (ivProfilePicture != null));
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing edit page: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Setup profile picture click listener
        setupProfilePictureClick();

        // Get user ID from intent or session
        Intent intent = getIntent();
        Log.d(TAG, "Intent received: " + (intent != null ? "not null" : "null"));
        
        if (intent != null) {
            userId = intent.getStringExtra("user_id");
            Log.d(TAG, "User ID from intent: " + userId);
            
            String name = intent.getStringExtra("user_name");
            String phone = intent.getStringExtra("user_phone");
            String email = intent.getStringExtra("user_email");
            String address = intent.getStringExtra("user_address");
            Log.d(TAG, "Intent extras - name: " + name + ", phone: " + phone + ", email: " + email);
        }
        
        if (userId == null || userId.isEmpty()) {
            userId = sessionManager.getUserId();
            Log.d(TAG, "User ID from session: " + userId);
        }

        // Load existing data from intent first (this ensures fields are populated immediately)
        loadExistingData(intent);
        
        // Also load from backend to get latest data (including profile picture)
        // Use a small delay to ensure UI is ready and initial data is loaded
        // This will only update fields if backend has different/updated values
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Loading profile from backend after initial data load");
            loadUserProfileFromBackend();
        }, 500);

        // Save Changes button
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveProfileChanges();
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_edit_profile);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
            // Don't set selected item here - it will trigger navigation back to UserProfileActivity
            // The bottom nav will show the profile icon as selected by default based on menu state
        }
        
        Log.d(TAG, "=== EditProfileActivity onCreate() COMPLETED - Activity should be visible now ===");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "EditProfileActivity onStart() called");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "EditProfileActivity onResume() called - Activity is now visible");
        // Reload user profile (including profile picture) when returning to this activity
        // BUT: Don't reload if user just selected a new image (not yet uploaded)
        // This follows the same pattern as OperatorProfileActivity
        // Note: loadUserProfileFromBackend() now only updates fields if backend has different values
        // so it won't clear user's current input
        if (userId != null && !userId.isEmpty()) {
            // Only reload if no new image was just selected
            if (!hasNewImageSelected) {
                Log.d(TAG, "onResume: Reloading profile from backend (no new image selected)");
                // Only reload profile picture, not form fields, to avoid clearing user input
                // The form fields are already loaded from intent/session
                loadUserProfileFromBackend();
            } else {
                Log.d(TAG, "onResume: Skipping profile reload - user just selected a new image");
            }
        }
    }

    private void loadExistingData(Intent intent) {
        // Load data from intent extras
        String name = intent.getStringExtra("user_name");
        String phone = intent.getStringExtra("user_phone");
        String email = intent.getStringExtra("user_email");
        String address = intent.getStringExtra("user_address");

        if (name != null && !name.isEmpty()) {
            if (etName != null) {
                etName.setText(name);
            }
            if (tvUserName != null) {
                tvUserName.setText(name);
            }
        } else {
            // Fallback to session data
            String sessionName = sessionManager.getUserName();
            if (sessionName != null && !sessionName.isEmpty()) {
                if (etName != null) {
                    etName.setText(sessionName);
                }
                if (tvUserName != null) {
                    tvUserName.setText(sessionName);
                }
            }
        }

        if (phone != null && !phone.isEmpty()) {
            if (etPhone != null) {
                etPhone.setText(phone);
            }
        } else {
            String sessionPhone = sessionManager.getUserPhone();
            if (sessionPhone != null && !sessionPhone.isEmpty() && etPhone != null) {
                etPhone.setText(sessionPhone);
            }
        }

        if (email != null && !email.isEmpty()) {
            if (etEmail != null) {
                etEmail.setText(email);
            }
        } else {
            String sessionEmail = sessionManager.getUserEmail();
            if (sessionEmail != null && !sessionEmail.isEmpty() && etEmail != null) {
                etEmail.setText(sessionEmail);
            }
        }

        if (address != null && !address.isEmpty()) {
            if (etAddress != null) {
                etAddress.setText(address);
            }
        }
    }

    private void saveProfileChanges() {
        if (etName == null || etPhone == null || etEmail == null || etAddress == null) {
            Toast.makeText(this, "Error: Fields not initialized", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Fields not initialized - Name: " + (etName != null) + ", Phone: " + (etPhone != null) + 
                  ", Email: " + (etEmail != null) + ", Address: " + (etAddress != null));
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        Log.d(TAG, "=== SAVE PROFILE CHANGES ===");
        Log.d(TAG, "Name: '" + name + "' (length: " + name.length() + ")");
        Log.d(TAG, "Phone: '" + phone + "' (length: " + phone.length() + ")");
        Log.d(TAG, "Email: '" + email + "' (length: " + email.length() + ")");
        Log.d(TAG, "Address: '" + address + "' (length: " + address.length() + ")");

        // Validation - check if fields are actually empty
        if (name == null || name.isEmpty()) {
            Log.e(TAG, "Validation failed: Name is empty");
            etName.setError("Name is required");
            etName.requestFocus();
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }

        // Validate phone format (basic check)
        if (phone.length() < 10) {
            etPhone.setError("Phone number must be at least 10 digits");
            etPhone.requestFocus();
            return;
        }

        // Validate email format if provided
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (userId == null || userId.isEmpty()) {
            userId = sessionManager.getUserId();
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(false);
        }

        // Update user profile
        updateUserProfile(name, phone, email, address);
    }

    private void updateUserProfile(String name, String phone, String email, String address) {
        Log.d(TAG, "Updating user profile - ID: " + userId + ", Name: " + name + ", Phone: " + phone);
        
        User user = new User();
        user.setUserId(Integer.parseInt(userId));
        user.setName(name);
        user.setPhone(phone);
        // Set email to null if empty (since it's optional in database)
        user.setEmail(email.isEmpty() ? null : email);
        // Set address to null if empty (since it's optional in database)
        user.setAddress(address.isEmpty() ? null : address);
        
        // Include profile picture if one was selected
        // Always include it when clicking "Save Changes" to ensure it's saved
        // The backend will handle duplicate uploads gracefully
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            user.setProfilePicture(profileImageBase64);
            Log.d(TAG, "=== INCLUDING PROFILE PICTURE IN PROFILE UPDATE ===");
            Log.d(TAG, "Profile picture Base64 length: " + profileImageBase64.length());
            Log.d(TAG, "Profile picture will be sent to backend");
        } else {
            Log.d(TAG, "No profile picture to include in profile update (profileImageBase64 is null or empty)");
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.updateUserProfile(user);
        
        Log.d(TAG, "API call created for updating user profile");

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (btnSaveChanges != null) {
                    btnSaveChanges.setEnabled(true);
                }

                Log.d(TAG, "API Response - Code: " + response.code() + ", Successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    Log.d(TAG, "API Response - Success: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess()) {
                        // Update session
                        sessionManager.createLoginSession(
                            userId,
                            name,
                            phone,
                            email,
                            "user"
                        );

                        Toast.makeText(EditProfileActivity.this, 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Profile updated successfully!", 
                            Toast.LENGTH_SHORT).show();
                        
                        // Clear profile picture cache if it was included in the update
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                        // Mark as uploaded
                        profilePictureUploaded = true;
                        hasNewImageSelected = false; // Reset flag after successful upload
                        currentProfileImagePath = null;
                        // Keep currentProfileBitmap visible until we reload from backend
                        profileImageBase64 = null; // Clear after successful upload
                        Log.d(TAG, "Cleared profile picture cache after successful update, reset hasNewImageSelected flag");
                            
                            // Reload profile to get updated profile picture path
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                loadUserProfileFromBackend();
                            }, 1000); // Increased delay to ensure backend has processed
                        }
                        
                        // Set result to refresh profile page
                        setResult(RESULT_OK);
                        
                        // Return to profile page
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update profile", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (btnSaveChanges != null) {
                    btnSaveChanges.setEnabled(true);
                }
                Log.e(TAG, "Error updating profile: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Setup profile picture click listener
     */
    private void setupProfilePictureClick() {
        if (ivProfilePicture != null) {
            ivProfilePicture.setClickable(true);
            ivProfilePicture.setFocusable(true);
            ivProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Profile picture clicked - showing image source dialog");
                    showImageSourceDialog();
                }
            });
            Log.d(TAG, "Profile picture click listener set");
        } else {
            Log.e(TAG, "Profile picture ImageView not found");
        }
    }

    /**
     * Show dialog to choose image source (Camera or Gallery)
     */
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
            if (which == 0) {
                // Take Photo
                if (checkCameraPermission()) {
                    openCamera();
                }
            } else {
                // Choose from Gallery
                if (checkStoragePermission()) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    /**
     * Check and request camera permission
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            return false;
        }
        return true;
    }

    /**
     * Check and request storage permission
     */
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_STORAGE);
                return false;
            }
        } else {
            // Android 12 and below uses READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Open camera to take photo
     */
    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = new File(getExternalFilesDir(null), "profile_temp_" + System.currentTimeMillis() + ".jpg");
                cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera: " + e.getMessage());
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open gallery to select image
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_CAMERA) {
                openCamera();
            } else if (requestCode == PERMISSION_STORAGE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            
            if (requestCode == REQUEST_CAMERA && cameraImageUri != null) {
                // Image from camera
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cameraImageUri));
                } catch (Exception e) {
                    Log.e(TAG, "Error loading camera image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                // Image from gallery
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading gallery image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            if (bitmap != null) {
                // Resize bitmap to reduce size (max 1024px)
                bitmap = resizeBitmap(bitmap, 1024);
                
                // Convert to Base64
                profileImageBase64 = bitmapToBase64(bitmap);
                
                // Reset upload flag since we have a new image
                profilePictureUploaded = false;
                hasNewImageSelected = true; // Mark that a new image was just selected
                Log.d(TAG, "New profile picture selected, reset upload flag and set hasNewImageSelected = true");
                
                // Store bitmap temporarily to keep it visible during upload
                final Bitmap displayBitmap = bitmap;
                
                // Update ImageView immediately
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageBitmap(displayBitmap);
                    currentProfileBitmap = displayBitmap;
                    Log.d(TAG, "Profile picture displayed from gallery/camera");
                }
                
                // Upload to server immediately when image is selected
                // This provides immediate feedback to the user
                uploadProfileImage();
                
                Toast.makeText(this, "Image selected. Uploading...", Toast.LENGTH_SHORT).show();
                
                // Note: Profile picture is also included when clicking "Save Changes" button
                // if it hasn't been uploaded yet (prevents duplicate uploads)
            }
        }
    }

    /**
     * Resize bitmap to reduce file size
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Convert bitmap to Base64 string
     */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // 80% quality
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    /**
     * Upload profile image to server
     * Also sends current form data to preserve existing fields
     */
    private void uploadProfileImage() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profileImageBase64 == null || profileImageBase64.isEmpty()) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Uploading profile image - User ID: " + userId + ", Base64 length: " + profileImageBase64.length());

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Get current form values to preserve them when uploading profile picture
        String currentName = (etName != null) ? etName.getText().toString().trim() : "";
        String currentPhone = (etPhone != null) ? etPhone.getText().toString().trim() : "";
        String currentEmail = (etEmail != null) ? etEmail.getText().toString().trim() : "";
        String currentAddress = (etAddress != null) ? etAddress.getText().toString().trim() : "";

        User user = new User();
        user.setUserId(Integer.parseInt(userId));
        user.setProfilePicture(profileImageBase64);
        
        // Include current form data to prevent data loss
        if (!currentName.isEmpty()) {
            user.setName(currentName);
        }
        if (!currentPhone.isEmpty()) {
            user.setPhone(currentPhone);
        }
        if (!currentEmail.isEmpty()) {
            user.setEmail(currentEmail);
        }
        if (!currentAddress.isEmpty()) {
            user.setAddress(currentAddress);
        }
        
        Log.d(TAG, "Including form data with profile upload - Name: " + currentName + ", Phone: " + currentPhone);
        
        // Log the user object being sent
        Log.d(TAG, "=== UPLOADING PROFILE PICTURE ===");
        Log.d(TAG, "User ID: " + user.getUserId());
        Log.d(TAG, "ProfilePicture Base64 length: " + (profileImageBase64 != null ? profileImageBase64.length() : 0));
        Log.d(TAG, "ProfilePicture set: " + (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()));
        
        // Log first 100 characters of Base64 to verify it's being sent
        if (profileImageBase64 != null && profileImageBase64.length() > 0) {
            String preview = profileImageBase64.substring(0, Math.min(100, profileImageBase64.length()));
            Log.d(TAG, "Base64 preview (first 100 chars): " + preview);
        }

        // Serialize User object to JSON to verify what's being sent
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String jsonPayload = gson.toJson(user);
            Log.d(TAG, "=== JSON PAYLOAD BEING SENT ===");
            Log.d(TAG, "Full JSON length: " + jsonPayload.length());
            // Log first 500 chars to see structure
            Log.d(TAG, "JSON preview (first 500 chars): " + jsonPayload.substring(0, Math.min(500, jsonPayload.length())));
            // Check if profile_picture is in JSON
            boolean hasProfilePicture = jsonPayload.contains("profile_picture");
            Log.d(TAG, "JSON contains 'profile_picture': " + hasProfilePicture);
            if (hasProfilePicture) {
                // Find the profile_picture value in JSON
                int startIdx = jsonPayload.indexOf("\"profile_picture\":\"");
                if (startIdx >= 0) {
                    int valueStart = startIdx + 19; // Length of "profile_picture":"
                    int valueEnd = jsonPayload.indexOf("\"", valueStart);
                    if (valueEnd > valueStart) {
                        String profilePicValue = jsonPayload.substring(valueStart, valueEnd);
                        Log.d(TAG, "profile_picture value length in JSON: " + profilePicValue.length());
                        Log.d(TAG, "profile_picture value preview: " + profilePicValue.substring(0, Math.min(100, profilePicValue.length())));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error serializing User to JSON: " + e.getMessage(), e);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.updateUserProfile(user);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                Log.d(TAG, "=== UPLOAD RESPONSE ===");
                Log.d(TAG, "HTTP Code: " + response.code());
                Log.d(TAG, "Successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    Log.d(TAG, "API Response - Success: " + apiResponse.isSuccess());
                    Log.d(TAG, "API Response - Message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess()) {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Profile image updated successfully";
                        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Profile picture upload SUCCESSFUL");
                        
                        // Mark as uploaded to prevent duplicate uploads
                        profilePictureUploaded = true;
                        hasNewImageSelected = false; // Reset flag after successful upload
                        Log.d(TAG, "Marked profile picture as uploaded, reset hasNewImageSelected flag");
                        
                        // Clear cached image path to force reload from backend
                        currentProfileImagePath = null;
                        Log.d(TAG, "Cleared cached profile image path to force reload");
                        
                        // Reload entire user profile after a short delay to ensure server has processed the update
                        // This will reload the profile picture path from backend
                        if (userId != null) {
                            Log.d(TAG, "Reloading user profile in 1500ms to get updated profile picture path");
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                loadUserProfileFromBackend();
                            }, 1500); // Increased delay to ensure backend has processed
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update profile image";
                        Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API returned error: " + errorMsg);
                    }
                } else {
                    String errorMsg = "Failed to update profile image: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "=== ERROR RESPONSE BODY ===");
                            Log.e(TAG, "Full error body: " + errorBody);
                            errorMsg += " - " + errorBody;
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage(), e);
                        }
                    } else {
                        Log.e(TAG, "Error response body is null");
                    }
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "UPLOAD FAILED: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "=== NETWORK FAILURE ===");
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Error class: " + t.getClass().getName());
                if (t.getCause() != null) {
                    Log.e(TAG, "Cause: " + t.getCause().getMessage());
                }
                Log.e(TAG, "Stack trace:", t);
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Load user profile from backend (includes profile picture)
     * This follows the same pattern as OperatorProfileActivity
     */
    private void loadUserProfileFromBackend() {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "Cannot load user profile - User ID is null or empty");
            return;
        }

        // Don't reload profile picture if user just selected a new image (not yet uploaded)
        if (hasNewImageSelected && currentProfileBitmap != null) {
            Log.d(TAG, "Skipping profile picture reload - user just selected a new image that hasn't been uploaded yet");
            // Still load other profile data, just skip the profile picture part
            // We'll handle this by checking the flag before loading the image
        }

        Log.d(TAG, "Loading user profile from backend for user ID: " + userId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<com.simats.eathmover.models.ApiResponse<User>> call = apiService.getUserProfile(userId);

        call.enqueue(new Callback<com.simats.eathmover.models.ApiResponse<User>>() {
            @Override
            public void onResponse(Call<com.simats.eathmover.models.ApiResponse<User>> call, Response<com.simats.eathmover.models.ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.simats.eathmover.models.ApiResponse<User> apiResponse = response.body();
                    Log.d(TAG, "Profile API Response - Success: " + apiResponse.isSuccess());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        
                        // DEBUG: Log the entire user object to see what we're getting
                        Log.d(TAG, "=== USER PROFILE DATA FROM BACKEND ===");
                        Log.d(TAG, "User ID: " + user.getUserId());
                        Log.d(TAG, "Name: " + user.getName());
                        Log.d(TAG, "Phone: " + user.getPhone());
                        Log.d(TAG, "Email: " + user.getEmail());
                        Log.d(TAG, "Address: " + user.getAddress());
                        Log.d(TAG, "Profile Picture: " + (user.getProfilePicture() != null ? user.getProfilePicture() : "NULL"));
                        
                        // Update form fields with latest data from backend
                        // Only update if backend has valid non-empty values to prevent clearing user's input
                        if (user.getName() != null && !user.getName().trim().isEmpty() && etName != null) {
                            String currentName = etName.getText().toString().trim();
                            // Only update if different to avoid unnecessary changes
                            if (!currentName.equals(user.getName().trim())) {
                                etName.setText(user.getName());
                                Log.d(TAG, "Updated name field from backend: " + user.getName());
                            }
                        }
                        if (user.getPhone() != null && !user.getPhone().trim().isEmpty() && etPhone != null) {
                            String currentPhone = etPhone.getText().toString().trim();
                            if (!currentPhone.equals(user.getPhone().trim())) {
                                etPhone.setText(user.getPhone());
                                Log.d(TAG, "Updated phone field from backend: " + user.getPhone());
                            }
                        }
                        if (user.getEmail() != null && !user.getEmail().trim().isEmpty() && etEmail != null) {
                            String currentEmail = etEmail.getText().toString().trim();
                            if (!currentEmail.equals(user.getEmail().trim())) {
                                etEmail.setText(user.getEmail());
                                Log.d(TAG, "Updated email field from backend: " + user.getEmail());
                            }
                        }
                        if (user.getAddress() != null && !user.getAddress().trim().isEmpty() && etAddress != null) {
                            String currentAddress = etAddress.getText().toString().trim();
                            if (!currentAddress.equals(user.getAddress().trim())) {
                                etAddress.setText(user.getAddress());
                                Log.d(TAG, "Updated address field from backend: " + user.getAddress());
                            }
                        }
                        if (user.getName() != null && !user.getName().trim().isEmpty() && tvUserName != null) {
                            tvUserName.setText(user.getName());
                        }
                        
                        // Load profile picture if available
                        // BUT: Don't overwrite if user just selected a new image (not yet uploaded)
                        if (!hasNewImageSelected || currentProfileBitmap == null) {
                            String profilePicturePath = user.getProfilePicture();
                            Log.d(TAG, "=== PROFILE PICTURE DEBUG ===");
                            Log.d(TAG, "Profile picture path from backend: " + (profilePicturePath != null ? profilePicturePath : "null"));
                            Log.d(TAG, "Profile picture path is empty: " + (profilePicturePath == null || profilePicturePath.isEmpty()));
                            Log.d(TAG, "hasNewImageSelected: " + hasNewImageSelected);
                            
                            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                                Log.d(TAG, "Loading profile image with path: " + profilePicturePath);
                                loadProfileImage(profilePicturePath);
                            } else {
                                // Only set default if we don't have a cached bitmap
                                if (currentProfileBitmap == null && ivProfilePicture != null) {
                                    ivProfilePicture.setImageResource(R.drawable.operator1);
                                    Log.d(TAG, "No profile picture in backend, using default");
                                } else if (currentProfileBitmap != null && ivProfilePicture != null) {
                                    // Keep the current bitmap if we have one
                                    ivProfilePicture.setImageBitmap(currentProfileBitmap);
                                    Log.d(TAG, "No profile picture in backend, keeping current bitmap");
                                }
                            }
                        } else {
                            Log.d(TAG, "Skipping profile picture reload - keeping newly selected image: " + (currentProfileBitmap != null ? "bitmap exists" : "no bitmap"));
                            // Keep the newly selected image visible
                            if (ivProfilePicture != null && currentProfileBitmap != null) {
                                ivProfilePicture.setImageBitmap(currentProfileBitmap);
                            }
                        }
                    } else {
                        Log.w(TAG, "API response not successful or data is null");
                    }
                } else {
                    Log.e(TAG, "Failed to load profile - HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.simats.eathmover.models.ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Error loading user profile: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Load profile image from backend URL
     */
    private void loadProfileImage(String imagePath) {
        if (ivProfilePicture == null) {
            return;
        }

        // If image path hasn't changed and we have a cached bitmap, keep it
        // But only if the path is not null (to allow reloading after upload)
        if (imagePath != null && imagePath.equals(currentProfileImagePath) && currentProfileBitmap != null && currentProfileImagePath != null) {
            ivProfilePicture.setImageBitmap(currentProfileBitmap);
            Log.d(TAG, "Using cached profile image for path: " + imagePath);
            return;
        }
        
        // Clear cached bitmap if path has changed to force reload
        if (imagePath != null && !imagePath.equals(currentProfileImagePath)) {
            Log.d(TAG, "Profile image path changed from '" + currentProfileImagePath + "' to '" + imagePath + "' - clearing cache");
            currentProfileBitmap = null;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            // Use default image if no profile image, but only if we don't have a cached bitmap
            if (currentProfileBitmap == null) {
                ivProfilePicture.setImageResource(R.drawable.operator1);
                Log.d(TAG, "No profile image path, using default image");
            } else {
                // Keep current bitmap if available
                ivProfilePicture.setImageBitmap(currentProfileBitmap);
                Log.d(TAG, "No profile image path, but keeping current bitmap");
            }
            return;
        }

        // Update current path
        currentProfileImagePath = imagePath;

        // Construct full URL
        String baseUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl();
        // Remove /api/ from base URL to get the root
        String rootUrl = baseUrl.replace("/api/", "/");
        // Ensure imagePath doesn't start with /
        if (imagePath.startsWith("/")) {
            imagePath = imagePath.substring(1);
        }
        String fullImageUrl = rootUrl + imagePath;

        Log.d(TAG, "=== LOADING PROFILE IMAGE ===");
        Log.d(TAG, "Image path: " + imagePath);
        Log.d(TAG, "Base URL: " + com.simats.eathmover.config.ApiConfig.getBaseUrl());
        Log.d(TAG, "Root URL: " + rootUrl);
        Log.d(TAG, "Full image URL: " + fullImageUrl);

        // Load image in background thread
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(fullImageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "HTTP Response Code: " + responseCode);
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (bitmap != null && ivProfilePicture != null) {
                            // Cache the bitmap
                            currentProfileBitmap = bitmap;
                            ivProfilePicture.setImageBitmap(bitmap);
                            Log.d(TAG, "Profile image loaded successfully from: " + fullImageUrl);
                        } else {
                            // Keep current bitmap if loading fails
                            Log.w(TAG, "Failed to decode bitmap, keeping current image if available");
                            if (currentProfileBitmap != null && ivProfilePicture != null) {
                                ivProfilePicture.setImageBitmap(currentProfileBitmap);
                            } else if (ivProfilePicture != null) {
                                ivProfilePicture.setImageResource(R.drawable.operator1);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "=== FAILED TO LOAD IMAGE ===");
                    Log.e(TAG, "HTTP response code: " + responseCode);
                    Log.e(TAG, "Image URL was: " + fullImageUrl);
                    // Keep current bitmap if loading fails
                    runOnUiThread(() -> {
                        if (currentProfileBitmap != null && ivProfilePicture != null) {
                            ivProfilePicture.setImageBitmap(currentProfileBitmap);
                            Log.d(TAG, "Image load failed, keeping current bitmap");
                        } else if (ivProfilePicture != null) {
                            ivProfilePicture.setImageResource(R.drawable.operator1);
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "=== ERROR LOADING PROFILE IMAGE ===");
                Log.e(TAG, "Image URL: " + fullImageUrl);
                Log.e(TAG, "Error message: " + e.getMessage(), e);
                // Keep current bitmap if loading fails
                runOnUiThread(() -> {
                    if (currentProfileBitmap != null && ivProfilePicture != null) {
                        ivProfilePicture.setImageBitmap(currentProfileBitmap);
                        Log.d(TAG, "Image load error, keeping current bitmap");
                    } else if (ivProfilePicture != null) {
                        ivProfilePicture.setImageResource(R.drawable.operator1);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        // Navigate to UserDashboardActivity instead of going back
        try {
            Log.d(TAG, "Back button pressed - navigating to UserDashboardActivity");
            Intent intent = new Intent(EditProfileActivity.this, UserDashboardActivity.class);
            // Use FLAG_ACTIVITY_CLEAR_TOP to bring existing UserDashboardActivity to front if it exists
            // If it doesn't exist in stack, it will create a new one
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            Log.d(TAG, "Navigation to UserDashboardActivity completed");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to UserDashboardActivity: " + e.getMessage(), e);
            e.printStackTrace();
            // Fallback: navigate without flags
            try {
                Intent fallbackIntent = new Intent(EditProfileActivity.this, UserDashboardActivity.class);
                startActivity(fallbackIntent);
                finish();
            } catch (Exception ex) {
                Log.e(TAG, "Fallback navigation also failed: " + ex.getMessage(), ex);
                super.onBackPressed();
            }
        }
    }
}
