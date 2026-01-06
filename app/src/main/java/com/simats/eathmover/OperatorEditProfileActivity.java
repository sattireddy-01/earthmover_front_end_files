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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for editing operator profile information.
 */
public class OperatorEditProfileActivity extends AppCompatActivity {

    private static final String TAG = "OperatorEditProfile";
    private static final int REQUEST_CAMERA = 2001;
    private static final int REQUEST_GALLERY = 2002;
    private static final int PERMISSION_CAMERA = 3001;
    private static final int PERMISSION_STORAGE = 3002;
    
    private EditText etName;
    private EditText etPhone;
    private EditText etAddress;
    private Button btnSaveChanges;
    private ProgressBar progressBar;
    private ImageView ivProfilePicture;
    private String operatorId;
    private SessionManager sessionManager;
    private Uri cameraImageUri;
    private String profileImageBase64;
    private Bitmap currentProfileBitmap;
    private boolean hasNewImageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OperatorEditProfileActivity onCreate() called");
        
        try {
            setContentView(R.layout.activity_operator_edit_profile);
            Log.d(TAG, "Layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting content view: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading edit profile page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        Log.d(TAG, "SessionManager initialized");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_operator_edit_profile);
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
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        progressBar = findViewById(R.id.progress_bar);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        
        // Log if views are found
        if (etName == null) Log.e(TAG, "et_name not found in layout!");
        if (etPhone == null) Log.e(TAG, "et_phone not found in layout!");
        if (etAddress == null) Log.e(TAG, "et_address not found in layout!");
        if (btnSaveChanges == null) Log.e(TAG, "btn_save_changes not found in layout!");
        if (progressBar == null) Log.e(TAG, "progress_bar not found in layout!");
        if (ivProfilePicture == null) Log.e(TAG, "iv_profile_picture not found in layout!");
        
        Log.d(TAG, "Views initialized - Name: " + (etName != null) + ", Phone: " + (etPhone != null) + ", Address: " + (etAddress != null));

        // Get operator ID from intent or session
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        if (operatorId == null) {
            operatorId = sessionManager.getOperatorId();
        }
        
        Log.d(TAG, "Operator ID: " + operatorId);

        // Load existing data
        loadExistingData(intent);
        
        // Load existing profile image
        loadExistingProfileImage();

        // Profile picture click listener
        setupProfilePictureClick();

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
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_edit_profile);
        if (bottomNav != null) {
            // Set selected item BEFORE setting up listener to prevent immediate navigation
            bottomNav.setSelectedItemId(R.id.nav_profile);
            setupOperatorBottomNavigation(bottomNav);
        }
        
        Log.d(TAG, "OperatorEditProfileActivity onCreate() completed successfully");
    }

    private void loadExistingData(Intent intent) {
        // Load from intent extras first
        String name = intent.getStringExtra("operator_name");
        String phone = intent.getStringExtra("operator_phone");
        String address = intent.getStringExtra("operator_address");

        // Fallback to session data if intent extras are null
        if (name == null || name.isEmpty()) {
            name = sessionManager.getUserName();
        }
        if (phone == null || phone.isEmpty()) {
            phone = sessionManager.getUserPhone();
        }
        if (address == null || address.isEmpty()) {
            address = "Cumbum, Andhra Pradesh"; // Default address
        }

        // Set default values if still null
        if (name == null || name.isEmpty()) {
            name = "Harsha";
        }
        if (phone == null || phone.isEmpty()) {
            phone = "7675903108";
        }
        if (address == null || address.isEmpty()) {
            address = "Cumbum, Andhra Pradesh";
        }

        if (etName != null) {
            etName.setText(name);
        }
        if (etPhone != null) {
            etPhone.setText(phone);
        }
        if (etAddress != null) {
            etAddress.setText(address);
        }

        // Update member since text
        TextView tvMemberSince = findViewById(R.id.tv_member_since);
        if (tvMemberSince != null) {
            tvMemberSince.setText("Member since 2021");
        }
    }

    private void saveProfileChanges() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        // Show progress
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        // Update operator profile via API
        updateOperatorProfile(name, phone, address);
    }

    private void setupProfilePictureClick() {
        if (ivProfilePicture != null) {
            ivProfilePicture.setClickable(true);
            ivProfilePicture.setFocusable(true);
            ivProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Profile picture clicked");
                    showImageSourceDialog();
                }
            });
        }
    }

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

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_STORAGE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

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
        
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            
            if (requestCode == REQUEST_CAMERA && cameraImageUri != null) {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cameraImageUri));
                } catch (Exception e) {
                    Log.e(TAG, "Error loading camera image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
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
                hasNewImageSelected = true;
                
                // Update ImageView immediately
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageBitmap(bitmap);
                    currentProfileBitmap = bitmap;
                }
                
                Toast.makeText(this, "Image selected. It will be saved when you click Save Changes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void loadExistingProfileImage() {
        if (operatorId == null) {
            operatorId = sessionManager.getOperatorId();
        }
        
        if (operatorId == null || ivProfilePicture == null) {
            return;
        }
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorProfile(operatorId);
        
        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OperatorProfile profile = apiResponse.getData();
                        String profileImagePath = profile.getProfileImage();
                        
                        if (profileImagePath != null && !profileImagePath.isEmpty() && !hasNewImageSelected) {
                            loadProfileImage(profileImagePath);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                Log.e(TAG, "Error loading profile image: " + t.getMessage());
            }
        });
    }

    private void loadProfileImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || ivProfilePicture == null) {
            return;
        }
        
        // Use getRootUrl() method for correct URL construction
        String rootUrl = com.simats.eathmover.config.ApiConfig.getRootUrl();
        final String finalImagePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        final String fullImageUrl = rootUrl + finalImagePath;
        
        Log.d(TAG, "Loading profile image from: " + fullImageUrl);
        
        Picasso.get()
                .load(fullImageUrl)
                .placeholder(R.drawable.operator_4)
                .error(R.drawable.operator_4)
                .into(ivProfilePicture);
    }

    private void updateOperatorProfile(String name, String phone, String address) {
        if (operatorId == null) {
            operatorId = sessionManager.getOperatorId();
        }

        if (operatorId == null) {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (btnSaveChanges != null) btnSaveChanges.setEnabled(true);
            return;
        }

        OperatorProfile profile = new OperatorProfile();
        profile.setOperatorId(operatorId);
        profile.setName(name);
        profile.setPhone(phone);
        profile.setAddress(address);
        profile.setEmail(sessionManager.getUserEmail());
        
        // Include profile image if a new one was selected
        if (hasNewImageSelected && profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            profile.setProfileImage(profileImageBase64);
            Log.d(TAG, "Including profile image in update. Base64 length: " + profileImageBase64.length());
        } else {
            Log.d(TAG, "No new profile image selected. hasNewImageSelected: " + hasNewImageSelected + ", profileImageBase64 is null: " + (profileImageBase64 == null));
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<com.simats.eathmover.models.GenericResponse> call = apiService.updateOperatorProfile(profile);

        call.enqueue(new Callback<com.simats.eathmover.models.GenericResponse>() {
            @Override
            public void onResponse(Call<com.simats.eathmover.models.GenericResponse> call, Response<com.simats.eathmover.models.GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSaveChanges != null) btnSaveChanges.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    com.simats.eathmover.models.GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Log the response for debugging
                        Log.d(TAG, "Profile update successful. Message: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "No message"));
                        
                        // Update session
                        sessionManager.createOperatorSession(
                            operatorId,
                            name,
                            phone,
                            sessionManager.getUserEmail() != null ? sessionManager.getUserEmail() : ""
                        );
                        Toast.makeText(OperatorEditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        
                        // Reset image selection flag
                        hasNewImageSelected = false;
                        profileImageBase64 = null;
                        
                        // Set result to indicate profile was updated
                        setResult(RESULT_OK);
                        
                        finish(); // Return to profile page
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update profile";
                        Log.e(TAG, "Profile update failed: " + errorMsg);
                        Toast.makeText(OperatorEditProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Failed to update profile. HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            Log.e(TAG, "Error response body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(OperatorEditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.simats.eathmover.models.GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSaveChanges != null) btnSaveChanges.setEnabled(true);
                Log.e(TAG, "Error updating profile: " + t.getMessage());
                Toast.makeText(OperatorEditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupOperatorBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom nav item selected: " + itemId);
            
            // Don't navigate if clicking the currently selected item (profile)
            if (itemId == R.id.nav_profile) {
                Log.d(TAG, "Profile nav selected - staying on edit page");
                // Don't navigate, just stay on edit page
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                Intent intent = new Intent(this, OperatorDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_bookings) {
                // TODO: Navigate to bookings page
                Toast.makeText(this, "Bookings", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_earnings) {
                // TODO: Navigate to earnings page
                Toast.makeText(this, "Earnings", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}

