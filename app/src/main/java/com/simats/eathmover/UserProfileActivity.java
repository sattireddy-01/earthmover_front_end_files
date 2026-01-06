package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.User;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * User Profile Overview page showing profile info and quick access cards.
 */
public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    // Views
    private TextView tvUserName;
    private TextView tvMemberSince;
    private TextView tvUserPhone;
    private TextView tvUserEmail;
    private TextView tvUserAddress;
    private LinearLayout llEmailContainer;
    private LinearLayout llAddressContainer;
    private ProgressBar progressBar;
    private ImageButton btnEditProfile;
    private ImageView ivProfilePicture;

    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_user_profile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        tvUserName = findViewById(R.id.tv_user_name);
        tvMemberSince = findViewById(R.id.tv_member_since);
        tvUserPhone = findViewById(R.id.tv_user_phone);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserAddress = findViewById(R.id.tv_user_address);
        llEmailContainer = findViewById(R.id.ll_email_container);
        llAddressContainer = findViewById(R.id.ll_address_container);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        
        // Debug: Check if button is found
        if (btnEditProfile == null) {
            Log.e(TAG, "CRITICAL: btn_edit_profile is NULL - button not found in layout!");
            Toast.makeText(this, "Edit button not found in layout", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Edit profile button found successfully - ID: " + btnEditProfile.getId());
        }

        // Settings icon click
        ImageView ivSettings = findViewById(R.id.iv_settings);
        if (ivSettings != null) {
            ivSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserProfileActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Service History card click
        CardView cardServiceHistory = findViewById(R.id.card_service_history);
        if (cardServiceHistory != null) {
            cardServiceHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserProfileActivity.this, ServiceHistoryActivity.class);
                    startActivity(intent);
                }
            });
        }



        // Edit profile button click - MUST be set up after all views are initialized
        if (btnEditProfile != null) {
            Log.d(TAG, "Edit profile button found, setting click listener");
            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "=== EDIT PROFILE BUTTON CLICKED ===");
                    navigateToEditProfile();
                }
            });
            // Ensure button is clickable and focusable
            btnEditProfile.setClickable(true);
            btnEditProfile.setFocusable(true);
            btnEditProfile.setFocusableInTouchMode(true);
            Log.d(TAG, "Edit profile button click listener set successfully");
        } else {
            Log.e(TAG, "CRITICAL: Edit profile button not found in layout!");
            Toast.makeText(this, "Edit button not found", Toast.LENGTH_SHORT).show();
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_user_profile);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
            bottomNav.setSelectedItemId(R.id.navigation_profile);
        }

        // Load user profile data (this will also load profile picture)
        loadUserProfile();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "UserProfileActivity onResume() called");
        // Reload profile data when returning to this activity (same pattern as OperatorProfileActivity)
        // This ensures profile picture is updated after editing
        String userId = sessionManager.getUserId();
        if (userId != null && !userId.isEmpty()) {
            loadUserProfile();
        }
    }

    private void navigateToEditProfile() {
        try {
            Log.d(TAG, "=== navigateToEditProfile() START ===");
            
            // Create intent to EditProfileActivity
            Intent intent = new Intent(this, EditProfileActivity.class);
            Log.d(TAG, "Intent created for EditProfileActivity");
            
            // Always use session data as primary source, fallback to currentUser if available
            String userId = sessionManager.getUserId();
            String userName = sessionManager.getUserName();
            String userPhone = sessionManager.getUserPhone();
            String userEmail = sessionManager.getUserEmail();
            String userAddress = "";
            
            // Override with currentUser data if available
            if (currentUser != null) {
                Log.d(TAG, "Using currentUser data");
                userId = currentUser.getUserId() != null ? String.valueOf(currentUser.getUserId()) : userId;
                userName = currentUser.getName() != null ? currentUser.getName() : userName;
                userPhone = currentUser.getPhone() != null ? currentUser.getPhone() : userPhone;
                userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : userEmail;
                userAddress = currentUser.getAddress() != null ? currentUser.getAddress() : "";
            } else {
                Log.d(TAG, "currentUser is null, using session data");
            }
            
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "User ID is null or empty - cannot navigate");
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Set intent extras
            intent.putExtra("user_id", userId);
            intent.putExtra("user_name", userName != null && !userName.isEmpty() ? userName : "");
            intent.putExtra("user_phone", userPhone != null && !userPhone.isEmpty() ? userPhone : "");
            intent.putExtra("user_email", userEmail != null ? userEmail : "");
            intent.putExtra("user_address", userAddress != null && !userAddress.isEmpty() ? userAddress : "");
            
            Log.d(TAG, "Intent extras set - user_id: " + userId + 
                      ", Name: " + intent.getStringExtra("user_name") + 
                      ", Phone: " + intent.getStringExtra("user_phone"));
            
            // Verify the activity exists and can be resolved
            if (intent.resolveActivity(getPackageManager()) == null) {
                Log.e(TAG, "EditProfileActivity cannot be resolved by PackageManager!");
                Toast.makeText(this, "Edit profile screen not available", Toast.LENGTH_LONG).show();
                return;
            }
            
            Log.d(TAG, "About to start EditProfileActivity...");
            // Start the activity - use no flags to allow normal activity stack behavior
            startActivity(intent);
            Log.d(TAG, "startActivity() called - EditProfileActivity should now be visible");
            Log.d(TAG, "=== navigateToEditProfile() END ===");
            
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION in navigateToEditProfile: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadUserProfile() {
        String userId = sessionManager.getUserId();
        
        Log.d(TAG, "Loading user profile for user_id: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            // Navigate to login
            Intent intent = new Intent(this, UserLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Show loading state
        if (tvUserName != null) {
            tvUserName.setText("Loading...");
        }
        if (tvUserPhone != null) {
            tvUserPhone.setText("Loading...");
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<User>> call = apiService.getUserProfile(userId);
        
        Log.d(TAG, "API call created for user_id: " + userId);

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    Log.d(TAG, "API Response - Success: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        currentUser = user; // Store user data for edit navigation
                        
                        // DEBUG: Log the entire user object to see what we're getting
                        Log.d(TAG, "=== USER PROFILE DATA FROM BACKEND ===");
                        Log.d(TAG, "User ID: " + user.getUserId());
                        Log.d(TAG, "Name: " + user.getName());
                        Log.d(TAG, "Phone: " + user.getPhone());
                        Log.d(TAG, "Email: " + user.getEmail());
                        Log.d(TAG, "Address: " + user.getAddress());
                        Log.d(TAG, "Profile Picture: " + (user.getProfilePicture() != null ? user.getProfilePicture() : "NULL"));
                        
                        Log.d(TAG, "User data loaded - Name: " + user.getName() + ", Phone: " + user.getPhone());
                        displayUserProfile(user);
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to load profile";
                        Log.e(TAG, "API returned error: " + errorMsg);
                        showError(errorMsg);
                    }
                } else {
                    String errorMsg = "Failed to load profile: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                            errorMsg += " - " + errorBody;
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Log.e(TAG, errorMsg);
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Network error loading user profile: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayUserProfile(User user) {
        // Display name
        if (tvUserName != null) {
            tvUserName.setText(user.getName() != null ? user.getName() : "User");
        }

        // Display member since
        if (tvMemberSince != null && user.getCreatedAt() != null) {
            String memberSince = formatMemberSince(user.getCreatedAt());
            tvMemberSince.setText(memberSince);
        } else if (tvMemberSince != null) {
            tvMemberSince.setVisibility(View.GONE);
        }

        // Display phone
        if (tvUserPhone != null) {
            tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "Not available");
        }

        // Display email (if available)
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (tvUserEmail != null) {
                tvUserEmail.setText(user.getEmail());
            }
            if (llEmailContainer != null) {
                llEmailContainer.setVisibility(View.VISIBLE);
            }
        } else {
            if (llEmailContainer != null) {
                llEmailContainer.setVisibility(View.GONE);
            }
        }

        // Display address (if available)
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            if (tvUserAddress != null) {
                tvUserAddress.setText(user.getAddress());
            }
            if (llAddressContainer != null) {
                llAddressContainer.setVisibility(View.VISIBLE);
            }
        } else {
            if (llAddressContainer != null) {
                llAddressContainer.setVisibility(View.GONE);
            }
        }

        // Load and display profile picture from backend
        String profilePicturePath = user.getProfilePicture();
        Log.d(TAG, "=== PROFILE PICTURE DEBUG ===");
        Log.d(TAG, "Profile picture path from backend: " + (profilePicturePath != null ? profilePicturePath : "null"));
        Log.d(TAG, "Profile picture path is empty: " + (profilePicturePath == null || profilePicturePath.isEmpty()));
        
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
            Log.d(TAG, "Loading profile image with path: " + profilePicturePath);
            loadProfileImage(profilePicturePath);
        } else {
            // Use default image if no profile picture
            if (ivProfilePicture != null) {
                ivProfilePicture.setImageResource(R.drawable.operator1);
                Log.d(TAG, "No profile picture in backend, using default");
            }
        }
    }

    /**
     * Load profile image from backend URL
     */
    private void loadProfileImage(String imagePath) {
        if (ivProfilePicture == null) {
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            // Use default image if no profile image
            ivProfilePicture.setImageResource(R.drawable.operator1);
            Log.d(TAG, "No profile image path, using default image");
            return;
        }

        // Construct full URL using getRootUrl() method (same as operator profile)
        String rootUrl = com.simats.eathmover.config.ApiConfig.getRootUrl();
        // Ensure imagePath doesn't start with /
        final String finalImagePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        final String fullImageUrl = rootUrl + finalImagePath;

        Log.d(TAG, "=== LOADING PROFILE IMAGE ===");
        Log.d(TAG, "Image path: " + imagePath);
        Log.d(TAG, "Root URL: " + rootUrl);
        Log.d(TAG, "Final image path: " + finalImagePath);
        Log.d(TAG, "Full image URL: " + fullImageUrl);

        // Load image using Picasso (same as operator profile) for better caching and error handling
        Picasso.get()
                .load(fullImageUrl)
                .placeholder(R.drawable.operator1)
                .error(R.drawable.operator1)
                .into(ivProfilePicture);
        
        Log.d(TAG, "Profile image loading initiated with Picasso from: " + fullImageUrl);
    }

    private String formatMemberSince(String createdAt) {
        try {
            // Parse the database timestamp (format: "2025-12-23 03:04:41")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            
            if (date != null) {
                // Format as "Member since YYYY"
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                String year = outputFormat.format(date);
                return "Member since " + year;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        }
        
        // Fallback: try to extract year from string
        if (createdAt != null && createdAt.length() >= 4) {
            try {
                String year = createdAt.substring(0, 4);
                return "Member since " + year;
            } catch (Exception e) {
                Log.e(TAG, "Error extracting year: " + e.getMessage());
            }
        }
        
        return "Member";
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (tvUserName != null) {
            tvUserName.setText("Error loading profile");
        }
        if (tvUserPhone != null) {
            tvUserPhone.setText("Error");
        }
    }

    @Override
    public void onBackPressed() {
        // Navigate to UserDashboardActivity instead of going back
        try {
            Log.d(TAG, "Back button pressed - navigating to UserDashboardActivity");
            Intent intent = new Intent(UserProfileActivity.this, UserDashboardActivity.class);
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
                Intent fallbackIntent = new Intent(UserProfileActivity.this, UserDashboardActivity.class);
                startActivity(fallbackIntent);
                finish();
            } catch (Exception ex) {
                Log.e(TAG, "Fallback navigation also failed: " + ex.getMessage(), ex);
                super.onBackPressed();
            }
        }
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
        
        // Navigate to user login
        Intent intent = new Intent(UserProfileActivity.this, UserLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
