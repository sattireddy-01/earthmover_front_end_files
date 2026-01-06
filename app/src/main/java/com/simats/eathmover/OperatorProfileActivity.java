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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity showing operator profile with account balance, quick actions, and profile details.
 */
public class OperatorProfileActivity extends AppCompatActivity {

    private static final String TAG = "OperatorProfile";
    private static final int REQUEST_PROFILE_IMAGE = 1001;
    private static final int REQUEST_CAMERA = 2001;
    private static final int REQUEST_GALLERY = 2002;
    private static final int PERMISSION_CAMERA = 3001;
    private static final int PERMISSION_STORAGE = 3002;
    
    private String operatorId;
    private OperatorProfile operatorProfile;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private ImageView ivProfilePicture;
    private Uri cameraImageUri;
    private String profileImageBase64;
    private Bitmap currentProfileBitmap; // Cache current bitmap to prevent reset
    private String currentProfileImagePath; // Track current image path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_profile);

        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_operator_profile);
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

        // Logout icon click (top right)
        ImageView ivLogout = findViewById(R.id.iv_logout);
        if (ivLogout != null) {
            ivLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutDialog();
                }
            });
        }

        // Logout button click (at bottom)
        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutDialog();
                }
            });
        }

        progressBar = findViewById(R.id.progress_bar);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);

        // Get operator ID from intent or session
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        if (operatorId == null) {
            operatorId = sessionManager.getOperatorId();
        }

        // Load operator profile from API
        if (operatorId != null) {
            loadOperatorProfile(operatorId);
        } else {
            // Load from session if API fails
            loadFromSession();
        }

        // Profile picture click - show image selection dialog
        setupProfilePictureClick();

        // Edit Profile Icon in Profile Details section
        ImageView ivEditProfile = findViewById(R.id.iv_edit_profile);
        if (ivEditProfile != null) {
            ivEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Edit profile icon clicked - navigating to edit profile");
                    navigateToEditProfile();
                }
            });
            Log.d(TAG, "Edit profile icon click listener set");
        } else {
            Log.e(TAG, "Edit profile icon not found!");
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_profile);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void setupProfilePictureClick() {
        // Find the FrameLayout container - this is the clickable element
        View profilePictureContainer = findViewById(R.id.fl_profile_picture_container);
        if (profilePictureContainer != null) {
            // Ensure it's clickable and can receive touch events
            profilePictureContainer.setClickable(true);
            profilePictureContainer.setFocusable(true);
            profilePictureContainer.setFocusableInTouchMode(true);
            profilePictureContainer.setEnabled(true);
            
            profilePictureContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "=== Profile picture container clicked ===");
                    showImageSourceDialog();
                }
            });
            
            Log.d(TAG, "Profile picture container click listener set successfully");
        } else {
            Log.e(TAG, "ERROR: Profile picture container (fl_profile_picture_container) not found in layout!");
            Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT).show();
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
            // Android 12 and below use READ_EXTERNAL_STORAGE
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        
        // Handle returning from edit profile activity
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String operatorId = sessionManager.getOperatorId();
            if (operatorId != null) {
                Log.d(TAG, "onActivityResult: Profile was updated, reloading for ID: " + operatorId);
                // Add a small delay to ensure backend has processed the update
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadOperatorProfile(operatorId);
                }, 1000);
            }
            return;
        }
        
        // Handle image selection (camera/gallery)
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
                
                // Store bitmap temporarily to keep it visible during upload
                final Bitmap displayBitmap = bitmap;
                
                // Update ImageView immediately - this will stay visible
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageBitmap(displayBitmap);
                    // Cache the bitmap so it persists even after reload
                    currentProfileBitmap = displayBitmap;
                }
                
                // Upload to server (image will stay visible until reload completes)
                uploadProfileImage();
                
                Toast.makeText(this, "Image selected. Uploading...", Toast.LENGTH_SHORT).show();
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
     */
    private void uploadProfileImage() {
        if (operatorId == null || operatorId.isEmpty()) {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profileImageBase64 == null || profileImageBase64.isEmpty()) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        OperatorProfile profile = new OperatorProfile();
        profile.setOperatorId(operatorId);
        profile.setProfileImage(profileImageBase64);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.updateOperatorProfile(profile);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Profile image updated successfully";
                        Toast.makeText(OperatorProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        
                        // Try to get profile_image from response (if API returns it)
                        // Otherwise reload profile to get updated image URL
                        // The current bitmap is already displayed, so it will stay visible
                        if (operatorId != null) {
                            // Reload profile after a short delay to ensure server has processed the update
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                loadOperatorProfile(operatorId);
                            }, 500);
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update profile image";
                        Toast.makeText(OperatorProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Failed to update profile image. Error code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(OperatorProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to update profile image: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error updating profile image: " + t.getMessage());
                Toast.makeText(OperatorProfileActivity.this, "Error updating profile image: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning from edit
        String operatorId = sessionManager.getOperatorId();
        if (operatorId != null) {
            Log.d(TAG, "onResume: Reloading operator profile for ID: " + operatorId);
            loadOperatorProfile(operatorId);
        } else {
            loadFromSession();
        }
        // Re-setup click listener in case view was recreated
        setupProfilePictureClick();
    }

    private void navigateToEditProfile() {
        try {
            Log.d(TAG, "navigateToEditProfile() called");
            
            // Check if OperatorEditProfileActivity class exists
            try {
                Class<?> editActivityClass = Class.forName("com.simats.eathmover.OperatorEditProfileActivity");
                Log.d(TAG, "OperatorEditProfileActivity class found: " + editActivityClass.getName());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "OperatorEditProfileActivity class not found!", e);
                Toast.makeText(this, "Edit profile activity not found", Toast.LENGTH_LONG).show();
                return;
            }
            
            Intent intent = new Intent(OperatorProfileActivity.this, OperatorEditProfileActivity.class);
            
            // Always use session data as primary source, fallback to operatorProfile if available
            String name = sessionManager.getUserName();
            String phone = sessionManager.getUserPhone();
            String email = sessionManager.getUserEmail();
            
            // Override with operatorProfile data if available
            if (operatorProfile != null) {
                name = operatorProfile.getName() != null ? operatorProfile.getName() : name;
                phone = operatorProfile.getPhone() != null ? operatorProfile.getPhone() : phone;
                email = operatorProfile.getEmail() != null ? operatorProfile.getEmail() : email;
            }
            
            // Set defaults if still null
            intent.putExtra("operator_name", name != null && !name.isEmpty() ? name : "Harsha");
            intent.putExtra("operator_phone", phone != null && !phone.isEmpty() ? phone : "7675903108");
            intent.putExtra("operator_email", email != null ? email : "");
            intent.putExtra("operator_address", operatorProfile != null && operatorProfile.getAddress() != null 
                ? operatorProfile.getAddress() : "Cumbum, Andhra Pradesh");
            
            if (operatorId != null) {
                intent.putExtra("operator_id", operatorId);
                Log.d(TAG, "Passing operator_id: " + operatorId);
            } else {
                String sessionOperatorId = sessionManager.getOperatorId();
                if (sessionOperatorId != null) {
                    intent.putExtra("operator_id", sessionOperatorId);
                    Log.d(TAG, "Passing operator_id from session: " + sessionOperatorId);
                } else {
                    Log.w(TAG, "No operator_id available!");
                }
            }
            
            Log.d(TAG, "Starting OperatorEditProfileActivity");
            Log.d(TAG, "Intent data - Name: " + intent.getStringExtra("operator_name") + 
                      ", Phone: " + intent.getStringExtra("operator_phone") +
                      ", Address: " + intent.getStringExtra("operator_address"));
            
            // Start activity for result to detect when profile is updated
            startActivityForResult(intent, 0);
            Log.d(TAG, "startActivityForResult() called successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to edit profile: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error opening edit profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadOperatorProfile(String operatorId) {
        if (operatorId == null || operatorId.isEmpty()) {
            Log.e(TAG, "Operator ID is null or empty, cannot load profile");
            loadFromSession();
            return;
        }
        
        Log.d(TAG, "=== Loading operator profile for ID: " + operatorId + " ===");
        
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        // Clear cached bitmap to force reload of image
        currentProfileBitmap = null;
        currentProfileImagePath = null;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String apiUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl() + "operator/get_operator_profile.php?operator_id=" + operatorId;
        Log.d(TAG, "API URL: " + apiUrl);
        
        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorProfile(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                Log.d(TAG, "API Response - Code: " + response.code() + ", Successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    Log.d(TAG, "API Response - Success: " + apiResponse.isSuccess() + ", Data: " + (apiResponse.getData() != null ? "not null" : "null"));
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        operatorProfile = apiResponse.getData();
                        String profileImg = operatorProfile.getProfileImage();
                        Log.d(TAG, "Profile loaded successfully. Profile image: " + (profileImg != null ? profileImg : "null"));
                        if (profileImg != null && !profileImg.isEmpty()) {
                            Log.d(TAG, "Profile image path length: " + profileImg.length());
                        }
                        populateOperatorProfile(operatorProfile);
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                        Log.w(TAG, "Profile load failed. Message: " + errorMsg + ". Falling back to session data.");
                        // Fallback to session data
                        loadFromSession();
                    }
                } else {
                    String errorBody = "No error body";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Log.e(TAG, "Failed to load operator profile: HTTP " + response.code() + ", Error: " + errorBody);
                    // Fallback to session data
                    loadFromSession();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading operator profile: " + t.getMessage());
                // Fallback to session data
                loadFromSession();
            }
        });
    }

    private void loadFromSession() {
        // Load data from session as fallback
        String name = sessionManager.getUserName();
        String phone = sessionManager.getUserPhone();
        String email = sessionManager.getUserEmail();

        TextView tvName = findViewById(R.id.tv_operator_name);
        TextView tvPhone = findViewById(R.id.tv_phone);
        TextView tvEmail = findViewById(R.id.tv_email);
        TextView tvAddress = findViewById(R.id.tv_address);

        if (tvName != null) {
            tvName.setText(name != null ? name : "Harsha");
        }
        if (tvPhone != null) {
            tvPhone.setText(phone != null ? phone : "7675903108");
        }
        if (tvEmail != null) {
            tvEmail.setText(email != null && !email.isEmpty() ? email : "harsha@email.com");
        }
        if (tvAddress != null) {
            tvAddress.setText("Cumbum, Andhra Pradesh");
        }
    }

    private void populateOperatorProfile(OperatorProfile operator) {
        TextView tvName = findViewById(R.id.tv_operator_name);
        TextView tvPhone = findViewById(R.id.tv_phone);
        TextView tvEmail = findViewById(R.id.tv_email);
        TextView tvAddress = findViewById(R.id.tv_address);

        if (tvName != null) {
            tvName.setText(operator.getName() != null ? operator.getName() : "Harsha");
        }
        if (tvPhone != null) {
            tvPhone.setText(operator.getPhone() != null ? operator.getPhone() : "7675903108");
        }
        if (tvEmail != null) {
            tvEmail.setText(operator.getEmail() != null && !operator.getEmail().isEmpty() 
                ? operator.getEmail() : "harsha@email.com");
        }
        if (tvAddress != null) {
            tvAddress.setText(operator.getAddress() != null && !operator.getAddress().isEmpty() 
                ? operator.getAddress() : "Cumbum, Andhra Pradesh");
        }

        // Load profile image if available
        String profileImagePath = operator.getProfileImage();
        Log.d(TAG, "Profile image path from operator: " + (profileImagePath != null ? profileImagePath : "null"));
        loadProfileImage(profileImagePath);
    }

    /**
     * Load profile image from backend URL
     */
    private void loadProfileImage(String imagePath) {
        if (ivProfilePicture == null) {
            Log.e(TAG, "ImageView ivProfilePicture is null, cannot load image");
            return;
        }

        Log.d(TAG, "loadProfileImage called with path: " + (imagePath != null ? imagePath : "null"));

        // If image path hasn't changed and we have a cached bitmap, keep it
        if (imagePath != null && imagePath.equals(currentProfileImagePath) && currentProfileBitmap != null) {
            ivProfilePicture.setImageBitmap(currentProfileBitmap);
            Log.d(TAG, "Using cached profile image");
            return;
        }

        if (imagePath == null || imagePath.isEmpty() || imagePath.equals("null")) {
            // Use default image if no profile image, but only if we don't have a cached bitmap
            if (currentProfileBitmap == null) {
                ivProfilePicture.setImageResource(R.drawable.operator_4);
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

        // Construct full URL - create final copy for use in inner class
        String rootUrl = com.simats.eathmover.config.ApiConfig.getRootUrl();
        Log.d(TAG, "Root URL: " + rootUrl);
        
        // Ensure imagePath doesn't start with /
        final String finalImagePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        final String fullImageUrl = rootUrl + finalImagePath;
        final String finalImagePathForStorage = imagePath; // Final copy for assignment

        Log.d(TAG, "Loading profile image from: " + fullImageUrl);
        Log.d(TAG, "Image path: " + imagePath);
        Log.d(TAG, "Final image path: " + finalImagePath);

        // Use Picasso for reliable image loading
        com.squareup.picasso.Picasso.get()
                .load(fullImageUrl)
                .placeholder(currentProfileBitmap != null ? null : R.drawable.operator_4)
                .error(currentProfileBitmap != null ? null : R.drawable.operator_4)
                .into(ivProfilePicture, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Profile image loaded successfully from: " + fullImageUrl);
                        // Update current image path
                        currentProfileImagePath = finalImagePathForStorage;
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading profile image: " + e.getMessage());
                        // Keep current bitmap if loading fails
                        if (currentProfileBitmap != null && ivProfilePicture != null) {
                            ivProfilePicture.setImageBitmap(currentProfileBitmap);
                        } else if (ivProfilePicture != null) {
                            ivProfilePicture.setImageResource(R.drawable.operator_4);
                        }
                    }
                });
    }

    private void setupOperatorBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                Intent intent = new Intent(this, OperatorDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_bookings) {
                Intent intent = new Intent(this, NewBookingRequestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_earnings) {
                // TODO: Navigate to earnings page
                Toast.makeText(this, "Earnings", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Already on profile page
                return true;
            }
            return false;
        });
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            performLogout();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    /**
     * Perform logout - clear session and navigate to login
     */
    private void performLogout() {
        // Clear session
        sessionManager.logout();
        
        // Navigate to operator login
        Intent intent = new Intent(OperatorProfileActivity.this, OperatorLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
