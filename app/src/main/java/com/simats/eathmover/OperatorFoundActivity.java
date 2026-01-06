package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.config.ApiConfig;
import com.simats.eathmover.utils.BottomNavigationHelper;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;
import com.simats.eathmover.utils.RealTimeDataManager;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity shown when an operator is found for the booking.
 */
public class OperatorFoundActivity extends AppCompatActivity {

    private static final String TAG = "OperatorFound";
    private String operatorId;
    private String operatorName;
    private ProgressBar progressBar;
    private ImageView ivOperatorProfile;
    private SessionManager sessionManager;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_found);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_found);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        progressBar = findViewById(R.id.progress_bar);
        ivOperatorProfile = findViewById(R.id.iv_operator_profile);

        // Make ImageView circular (clip to outline)
        if (ivOperatorProfile != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ivOperatorProfile.post(new Runnable() {
                @Override
                public void run() {
                    ivOperatorProfile.setClipToOutline(true);
                    ivOperatorProfile.setOutlineProvider(new android.view.ViewOutlineProvider() {
                        @Override
                        public void getOutline(android.view.View view, android.graphics.Outline outline) {
                            int size = Math.min(view.getWidth(), view.getHeight());
                            outline.setOval(0, 0, size, size);
                        }
                    });
                }
            });
        }

        // Get operator data from intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        operatorName = intent.getStringExtra("operator_name");

        // If operator ID is available, fetch full profile
        if (operatorId != null) {
            loadOperatorProfile(operatorId);
        } else if (operatorName != null) {
            // Display name if available
            updateOperatorName(operatorName);
        }

        // View Profile button - Navigate to Operator Profile (Operator Details)
        Button btnViewProfile = findViewById(R.id.btn_view_profile);
        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(OperatorFoundActivity.this, OperatorDetailsActivity.class);
                    if (operatorId != null) {
                        profileIntent.putExtra("operator_id", operatorId);
                    }
                    // Pass operator name and phone if available
                    Intent currentIntent = getIntent();
                    String operatorName = currentIntent.getStringExtra("operator_name");
                    String operatorPhone = currentIntent.getStringExtra("operator_phone");
                    if (operatorName != null) {
                        profileIntent.putExtra("operator_name", operatorName);
                    }
                    if (operatorPhone != null) {
                        profileIntent.putExtra("operator_phone", operatorPhone);
                    }
                    startActivity(profileIntent);
                }
            });
        }

        // Next button - Create booking request and send to operator
        btnNext = findViewById(R.id.btn_next);
        if (btnNext != null) {
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createBookingRequest();
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_found);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    private void loadOperatorProfile(String operatorId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorProfile(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OperatorProfile operator = apiResponse.getData();
                        updateOperatorInfo(operator);
                    } else {
                        Log.e(TAG, "Failed to load operator profile");
                    }
                } else {
                    Log.e(TAG, "Failed to load operator profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading operator profile: " + t.getMessage());
            }
        });
    }

    private void updateOperatorInfo(OperatorProfile operator) {
        this.operatorName = operator.getName();
        updateOperatorName(operatorName);
        loadOperatorProfileImage(operator.getProfileImage());
    }

    /**
     * Load operator profile image from backend
     */
    private void loadOperatorProfileImage(String imagePath) {
        if (ivOperatorProfile == null) {
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            // Use default image if no profile image
            ivOperatorProfile.setImageResource(R.drawable.operator_4);
            Log.d(TAG, "No profile image path, using default image");
            return;
        }

        // Construct full URL using getRootUrl() method
        String rootUrl = ApiConfig.getRootUrl();
        final String finalImagePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        final String fullImageUrl = rootUrl + finalImagePath;

        Log.d(TAG, "Loading operator profile image from: " + fullImageUrl);

        // Load image using Picasso
        Picasso.get()
                .load(fullImageUrl)
                .placeholder(R.drawable.operator_4)
                .error(R.drawable.operator_4)
                .into(ivOperatorProfile);
    }

    private void updateOperatorName(String name) {
        TextView tvOperatorName = findViewById(R.id.tv_operator_name);
        if (tvOperatorName != null && name != null) {
            tvOperatorName.setText(name);
        }
    }

    /**
     * Create booking request and send it to the operator
     */
    private void createBookingRequest() {
        if (operatorId == null || operatorId.isEmpty()) {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent currentIntent = getIntent();
        if (currentIntent == null) {
            Toast.makeText(this, "Booking details not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get booking details from intent
        String machineId = currentIntent.hasExtra("machine_id") 
            ? String.valueOf(currentIntent.getIntExtra("machine_id", 0)) 
            : null;
        String machineType = currentIntent.getStringExtra("machine_type");
        String machineModel = currentIntent.getStringExtra("machine_model");
        String date = currentIntent.getStringExtra("date");
        String time = currentIntent.getStringExtra("time");
        String location = currentIntent.getStringExtra("location");
        String duration = currentIntent.getStringExtra("duration");
        String estimatedCost = currentIntent.getStringExtra("estimated_cost");

        if (machineId == null || machineId.equals("0")) {
            Toast.makeText(this, "Machine ID is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnNext != null) btnNext.setEnabled(false);

        // Create booking object
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setOperatorId(operatorId);
        booking.setMachineId(machineId);
        booking.setMachineType(machineType);
        booking.setMachineModel(machineModel);
        booking.setBookingDate(date);
        booking.setStartTime(time);
        booking.setLocation(location);
        booking.setDuration(duration);
        booking.setStatus("PENDING");

        // Parse estimated cost if available
        if (estimatedCost != null && !estimatedCost.isEmpty()) {
            try {
                // Remove currency symbols and parse
                String costStr = estimatedCost.replaceAll("[₹,]", "").trim();
                double cost = Double.parseDouble(costStr);
                booking.setTotalAmount(cost);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing estimated cost: " + e.getMessage());
            }
        }

        Log.d(TAG, "Creating booking request - User: " + userId + ", Operator: " + operatorId + ", Machine: " + machineId);

        // Call API to create booking
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.createBooking(booking);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnNext != null) btnNext.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message = apiResponse.getMessage() != null 
                            ? apiResponse.getMessage() 
                            : "Booking request sent to operator successfully";
                        Toast.makeText(OperatorFoundActivity.this, message, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Booking request created successfully");
                        
                        // Trigger immediate data refresh in background
                        RealTimeDataManager.getInstance().setSessionManager(sessionManager);
                        RealTimeDataManager.getInstance().refreshNow();

                        // Navigate to Booking Confirmation page
                        Intent intent = new Intent(OperatorFoundActivity.this, BookingConfirmationActivity.class);
                        intent.putExtra("operator_id", operatorId);
                        intent.putExtra("operator_name", operatorName);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);
                        intent.putExtra("machine_type", machineType);
                        intent.putExtra("location", location);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null 
                            ? apiResponse.getMessage() 
                            : "Failed to create booking request";
                        Toast.makeText(OperatorFoundActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to create booking: " + errorMsg);
                    }
                } else {
                    String errorMsg = "Failed to create booking request. HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(OperatorFoundActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnNext != null) btnNext.setEnabled(true);
                Log.e(TAG, "Error creating booking request: " + t.getMessage());
                Toast.makeText(OperatorFoundActivity.this, "Error creating booking request: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
