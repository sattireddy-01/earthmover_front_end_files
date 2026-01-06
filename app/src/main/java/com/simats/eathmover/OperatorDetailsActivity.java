package com.simats.eathmover;

import android.content.Intent;
import android.net.Uri;
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
import com.simats.eathmover.utils.BottomNavigationHelper;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity showing detailed operator information.
 */
public class OperatorDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OperatorDetails";
    private String operatorId;
    private Integer machineId;
    private OperatorProfile operatorProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_details);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_details);
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

        // Get operator ID or machine ID from intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        machineId = intent.getIntExtra("machine_id", 0);

        // Priority: If machine_id is provided, fetch operator based on machine
        // Otherwise, use operator_id if available
        if (machineId > 0) {
            Log.d(TAG, "Loading operator details for machine_id: " + machineId);
            loadOperatorByMachine(machineId);
        } else if (operatorId != null && !operatorId.isEmpty()) {
            Log.d(TAG, "Loading operator details for operator_id: " + operatorId);
            loadOperatorDetails(operatorId);
        } else {
            Log.w(TAG, "No machine_id or operator_id provided. Cannot load operator details.");
            Toast.makeText(this, "No operator information available", Toast.LENGTH_SHORT).show();
        }

        // Contact button - Open dialer directly
        Button btnContact = findViewById(R.id.btn_contact);
        if (btnContact != null) {
            btnContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phone = null;
                    if (operatorProfile != null) {
                        phone = operatorProfile.getPhone();
                    }
                    if (phone == null || phone.isEmpty()) {
                        // Try to get from intent
                        Intent currentIntent = getIntent();
                        phone = currentIntent.getStringExtra("operator_phone");
                    }
                    // Default phone number from database (Harsha: 7675903108)
                    if (phone == null || phone.isEmpty()) {
                        phone = "7675903108";
                    }
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phone));
                    startActivity(callIntent);
                }
            });
        }

        // View Details button - Navigate to Share Operator Contact page
        Button btnViewDetails = findViewById(R.id.btn_view_details);
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent contactIntent = new Intent(OperatorDetailsActivity.this, OperatorContactActivity.class);
                    if (operatorId != null) {
                        contactIntent.putExtra("operator_id", operatorId);
                    }
                    if (operatorProfile != null) {
                        contactIntent.putExtra("operator_phone", operatorProfile.getPhone());
                        contactIntent.putExtra("operator_name", operatorProfile.getName());
                    } else {
                        // Try to get from intent
                        Intent currentIntent = getIntent();
                        String phone = currentIntent.getStringExtra("operator_phone");
                        String name = currentIntent.getStringExtra("operator_name");
                        if (phone != null) {
                            contactIntent.putExtra("operator_phone", phone);
                        }
                        if (name != null) {
                            contactIntent.putExtra("operator_name", name);
                        }
                    }
                    startActivity(contactIntent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_details);
        if (bottomNav != null) {
            com.simats.eathmover.utils.BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    /**
     * Load operator details based on machine_id
     * First fetches machine details to get operator_id, then fetches operator profile
     */
    private void loadOperatorByMachine(int machineId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<com.simats.eathmover.models.Machine>> machineCall = apiService.getMachineDetails(machineId);

        machineCall.enqueue(new Callback<ApiResponse<com.simats.eathmover.models.Machine>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.simats.eathmover.models.Machine>> call, 
                                 Response<ApiResponse<com.simats.eathmover.models.Machine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<com.simats.eathmover.models.Machine> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        com.simats.eathmover.models.Machine machine = apiResponse.getData();
                        Integer operatorIdFromMachine = machine.getOperatorId();
                        
                        if (operatorIdFromMachine != null && operatorIdFromMachine > 0) {
                            Log.d(TAG, "Found operator_id from machine: " + operatorIdFromMachine);
                            operatorId = String.valueOf(operatorIdFromMachine);
                            // Now load operator details using the operator_id
                            loadOperatorDetails(operatorId);
                        } else {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Machine does not have an operator assigned");
                            Toast.makeText(OperatorDetailsActivity.this, 
                                "No operator assigned to this machine", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to load machine details: " + 
                            (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        Toast.makeText(OperatorDetailsActivity.this, 
                            "Failed to load machine details", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to load machine details: HTTP " + response.code());
                    Toast.makeText(OperatorDetailsActivity.this, 
                        "Failed to load machine details", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.simats.eathmover.models.Machine>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading machine details: " + t.getMessage());
                Toast.makeText(OperatorDetailsActivity.this, 
                    "Network error. Please try again.", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOperatorDetails(String operatorId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorProfileDetails(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        operatorProfile = apiResponse.getData();
                        populateOperatorDetails(operatorProfile);
                    } else {
                        Toast.makeText(OperatorDetailsActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to load operator details",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load operator details: " + response.code());
                    Toast.makeText(OperatorDetailsActivity.this, "Failed to load operator details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading operator details: " + t.getMessage());
                Toast.makeText(OperatorDetailsActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateOperatorDetails(OperatorProfile operator) {
        // Update UI with operator details
        TextView tvOperatorName = findViewById(R.id.tv_operator_name);
        TextView tvOperatorRole = findViewById(R.id.tv_operator_role);
        TextView tvOperatorRating = findViewById(R.id.tv_operator_rating);
        ImageView ivProfilePicture = findViewById(R.id.iv_profile_picture);

        // Load operator name from backend
        if (tvOperatorName != null) {
            String name = operator.getName() != null && !operator.getName().isEmpty() 
                ? operator.getName() 
                : "Operator";
            tvOperatorName.setText(name);
            Log.d(TAG, "Operator name loaded from backend: " + name);
        }
        
        if (tvOperatorRole != null) {
            tvOperatorRole.setText("Operator");
        }
        
        if (tvOperatorRating != null) {
            // Format: "4.8 • 123 reviews"
            double rating = operator.getRating() > 0 ? operator.getRating() : 4.8;
            int reviews = operator.getTotalBookings() > 0 ? operator.getTotalBookings() : 123;
            String ratingText = String.format("%.1f", rating);
            tvOperatorRating.setText(ratingText + " • " + reviews + " reviews");
        }
        
        // Profile picture can be loaded from URL if available
        if (ivProfilePicture != null && operator.getProfileImage() != null && !operator.getProfileImage().isEmpty()) {
            // Load image from URL using Picasso
            String imageUrl = operator.getProfileImage();
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                String baseUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl();
                String rootUrl = baseUrl.replace("/api/", "/");
                if (imageUrl.startsWith("/")) {
                    imageUrl = imageUrl.substring(1);
                }
                imageUrl = rootUrl + imageUrl;
            }
            
            com.squareup.picasso.Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.operator_4)
                    .error(R.drawable.operator_4)
                    .into(ivProfilePicture);
        }
    }
}
