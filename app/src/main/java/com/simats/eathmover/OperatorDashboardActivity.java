package com.simats.eathmover;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.RealTimeDataManager;
import com.simats.eathmover.utils.SessionManager;

import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class OperatorDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SessionManager sessionManager;
    private TextView tvOperatorName;
    private TextView tvOperatorId;
    private TextView tvBookingTitle;
    private TextView tvBookingAddress;
    private TextView tvEarningsAmount;
    private TextView tvAvailabilityStatus;
    private ImageView ivOperatorProfile;
    private RealTimeDataManager realTimeDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        sessionManager = new SessionManager(this);
        
        // Initialize real-time data manager
        realTimeDataManager = RealTimeDataManager.getInstance();
        realTimeDataManager.setSessionManager(sessionManager);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_operator_dashboard);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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

        // Setup drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Initialize views
        initializeViews();

        // Load operator data
        loadOperatorData();

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_dashboard);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }

        // Setup card click listeners
        setupCardListeners();
    }

    private void initializeViews() {
        tvOperatorName = findViewById(R.id.tv_operator_name);
        tvOperatorId = findViewById(R.id.tv_operator_id);
        tvBookingTitle = findViewById(R.id.tv_booking_title);
        tvBookingAddress = findViewById(R.id.tv_booking_address);
        tvEarningsAmount = findViewById(R.id.tv_earnings_amount);
        tvAvailabilityStatus = findViewById(R.id.tv_availability_status);

        // Set operator profile image
        ivOperatorProfile = findViewById(R.id.iv_operator_profile);
        if (ivOperatorProfile != null) {
            // Default image will be set, but will be updated when profile data loads
            ivOperatorProfile.setImageResource(R.drawable.operator_4);
            // Make profile image clickable to navigate to profile
            ivOperatorProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToProfile();
                }
            });
        }

        // Make operator name clickable to navigate to profile
        if (tvOperatorName != null) {
            tvOperatorName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToProfile();
                }
            });
        }
    }

    private void navigateToProfile() {
        Intent intent = new Intent(OperatorDashboardActivity.this, OperatorProfileActivity.class);
        String operatorId = sessionManager.getOperatorId();
        if (operatorId != null) {
            intent.putExtra("operator_id", operatorId);
        }
        startActivity(intent);
    }

    private void navigateToSetAvailability() {
        Intent intent = new Intent(OperatorDashboardActivity.this, SetAvailabilityActivity.class);
        startActivity(intent);
    }

    private void navigateToNavigation() {
        Intent intent = new Intent(OperatorDashboardActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    private void loadOperatorData() {
        // Load operator data from session
        String operatorName = sessionManager.getUserName();
        String operatorId = sessionManager.getOperatorId();

        if (tvOperatorName != null) {
            tvOperatorName.setText(operatorName != null ? operatorName : "Operator");
        }

        if (tvOperatorId != null) {
            tvOperatorId.setText(operatorId != null ? "ID: " + operatorId : "ID: N/A");
        }

        // Load dashboard data from API
        if (operatorId != null) {
            loadDashboardData(operatorId);
            setupRealTimeUpdates(operatorId);
        } else {
            // Fallback to default values
            setDefaultValues();
        }
    }

    private void setupRealTimeUpdates(String operatorId) {
        // Set up real-time dashboard data listener
        realTimeDataManager.setDashboardListener(new RealTimeDataManager.DashboardDataListener() {
            @Override
            public void onDashboardDataUpdated(OperatorProfile profile) {
                updateDashboardUI(profile);
            }
        });

        // Set up real-time booking status listener
        realTimeDataManager.setBookingStatusListener(new RealTimeDataManager.BookingStatusListener() {
            @Override
            public void onBookingStatusChanged(Booking booking) {
                if (booking != null) {
                    updateBookingUI(booking);
                } else {
                    // No active booking
                    if (tvBookingTitle != null) {
                        tvBookingTitle.setText("No active booking");
                    }
                    if (tvBookingAddress != null) {
                        tvBookingAddress.setText("Waiting for new bookings...");
                    }
                }
            }
        });

        // Start real-time polling
        realTimeDataManager.startPolling();
    }

    private void loadDashboardData(String operatorId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorDashboard(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OperatorProfile profile = apiResponse.getData();
                        updateDashboardUI(profile);
                    } else {
                        setDefaultValues();
                    }
                } else {
                    Log.e("OperatorDashboard", "Failed to load dashboard: " + response.code());
                    setDefaultValues();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                Log.e("OperatorDashboard", "Error loading dashboard: " + t.getMessage());
                setDefaultValues();
            }
        });

        // Load current booking
        loadCurrentBooking(operatorId);
    }

    private void loadCurrentBooking(String operatorId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Booking>>> call = apiService.getOperatorBookings(operatorId);

        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        // Get the first active booking
                        for (Booking booking : apiResponse.getData()) {
                            if ("active".equalsIgnoreCase(booking.getStatus()) || "in_progress".equalsIgnoreCase(booking.getStatus())) {
                                updateBookingUI(booking);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e("OperatorDashboard", "Error loading bookings: " + t.getMessage());
            }
        });
    }

    private void updateDashboardUI(OperatorProfile profile) {
        if (tvEarningsAmount != null) {
            // Calculate total earnings from bookings or use profile data
            tvEarningsAmount.setText("$2,500"); // TODO: Get from earnings API
        }
        if (tvAvailabilityStatus != null) {
            String status = profile.getStatus();
            if (status != null) {
                tvAvailabilityStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            } else {
                tvAvailabilityStatus.setText("Available");
            }
        }
        
        // Load profile image if available
        if (profile != null) {
            loadProfileImage(profile.getProfileImage());
        }
    }

    private void updateBookingUI(Booking booking) {
        if (tvBookingTitle != null && booking.getMachineType() != null) {
            tvBookingTitle.setText(booking.getMachineType() + " Rental");
        }
        if (tvBookingAddress != null && booking.getLocation() != null) {
            tvBookingAddress.setText(booking.getLocation());
        }
    }

    private void setDefaultValues() {
        if (tvBookingTitle != null) {
            tvBookingTitle.setText("Site Preparation");
        }
        if (tvBookingAddress != null) {
            tvBookingAddress.setText("123 Elm Street, Anytown");
        }
        if (tvEarningsAmount != null) {
            tvEarningsAmount.setText("$2,500");
        }
        if (tvAvailabilityStatus != null) {
            tvAvailabilityStatus.setText("Available");
        }
    }

    private void setupCardListeners() {
        // Current Booking - View Details button
        Button btnViewBookingDetails = findViewById(R.id.btn_view_booking_details);
        if (btnViewBookingDetails != null) {
            btnViewBookingDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Work Timer page
                    Intent intent = new Intent(OperatorDashboardActivity.this, WorkTimerActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Earnings - View Details button
        Button btnViewEarningsDetails = findViewById(R.id.btn_view_earnings_details);
        if (btnViewEarningsDetails != null) {
            btnViewEarningsDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to earnings page
                    Intent intent = new Intent(OperatorDashboardActivity.this, OperatorEarningsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Availability - Update button
        Button btnUpdateAvailability = findViewById(R.id.btn_update_availability);
        if (btnUpdateAvailability != null) {
            btnUpdateAvailability.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to set availability page
                    navigateToSetAvailability();
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_set_available) {
            // Navigate to set availability page
            navigateToSetAvailability();
        } else if (id == R.id.nav_navigate_to_user) {
            // Navigate to navigation page
            navigateToNavigation();
        } else if (id == R.id.nav_start_work) {
            // Navigate to work timer page
            Intent intent = new Intent(OperatorDashboardActivity.this, WorkTimerActivity.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupOperatorBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                // Already on dashboard
                return true;
            } else if (itemId == R.id.nav_bookings) {
                // Navigate to all bookings page
                Intent intent = new Intent(this, OperatorBookingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_earnings) {
                // Navigate to earnings page
                Intent intent = new Intent(this, OperatorEarningsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                navigateToProfile();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume real-time updates when activity is visible
        if (realTimeDataManager != null) {
            realTimeDataManager.startPolling();
        }
        // Reload operator data when returning to dashboard (e.g., after updating profile image)
        String operatorId = sessionManager.getOperatorId();
        if (operatorId != null) {
            loadDashboardData(operatorId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause real-time updates when activity is not visible
        if (realTimeDataManager != null) {
            realTimeDataManager.stopPolling();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners
        if (realTimeDataManager != null) {
            realTimeDataManager.removeDashboardListener();
            realTimeDataManager.removeBookingStatusListener();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Load profile image from backend URL
     */
    private void loadProfileImage(String imagePath) {
        if (ivOperatorProfile == null) {
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            // Use default image if no profile image
            ivOperatorProfile.setImageResource(R.drawable.operator_4);
            Log.d("OperatorDashboard", "No profile image path, using default image");
            return;
        }

        // Construct full URL using getRootUrl() method
        String rootUrl = com.simats.eathmover.config.ApiConfig.getRootUrl();
        // Ensure imagePath doesn't start with /
        final String finalImagePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        final String fullImageUrl = rootUrl + finalImagePath;

        Log.d("OperatorDashboard", "Loading profile image from: " + fullImageUrl);

        // Load image in background thread
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(fullImageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000); // 10 seconds timeout
                connection.setReadTimeout(10000);
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (bitmap != null && ivOperatorProfile != null) {
                            ivOperatorProfile.setImageBitmap(bitmap);
                            Log.d("OperatorDashboard", "Profile image loaded successfully from: " + fullImageUrl);
                        } else {
                            // Fallback to default image
                            Log.w("OperatorDashboard", "Failed to decode bitmap, using default image");
                            if (ivOperatorProfile != null) {
                                ivOperatorProfile.setImageResource(R.drawable.operator_4);
                            }
                        }
                    });
                } else {
                    Log.e("OperatorDashboard", "Failed to load image. HTTP response code: " + responseCode);
                    // Fallback to default image on error
                    runOnUiThread(() -> {
                        if (ivOperatorProfile != null) {
                            ivOperatorProfile.setImageResource(R.drawable.operator_4);
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("OperatorDashboard", "Error loading profile image from " + fullImageUrl + ": " + e.getMessage(), e);
                // Fallback to default image on error
                runOnUiThread(() -> {
                    if (ivOperatorProfile != null) {
                        ivOperatorProfile.setImageResource(R.drawable.operator_4);
                    }
                });
            }
        }).start();
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
        
        // Stop real-time updates
        if (realTimeDataManager != null) {
            realTimeDataManager.stopPolling();
            realTimeDataManager.removeDashboardListener();
            realTimeDataManager.removeBookingStatusListener();
        }
        
        // Navigate to operator login
        Intent intent = new Intent(OperatorDashboardActivity.this, OperatorLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}

