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

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.OperatorVerification;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOperatorVerificationActivity extends AppCompatActivity {

    private static final String TAG = "AdminOperatorVerification";
    private ProgressBar progressBar;
    private String operatorId;
    private String operatorName;
    private OperatorVerification operatorData;
    private ApiService apiService; // Reuse ApiService instance

    // UI Views
    private ImageView ivOperatorProfile;
    private TextView tvOperatorName;
    private TextView tvOperatorId;
    private TextView tvStatus;
    private TextView tvFullName;
    private TextView tvDateOfBirth;
    private TextView tvAddress;
    private TextView tvPhone;
    private TextView tvEmail;
    private TextView tvLicenseNumber;
    private TextView tvExpiryDate;
    private TextView tvMachineType;
    private TextView tvTotalHours;
    private Button btnApprove;
    private Button btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_operator_verification);

        // Get operator ID and name from Intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        operatorName = intent.getStringExtra("operator_name");

        if (operatorId == null || operatorId.isEmpty()) {
            Toast.makeText(this, "Operator ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Retrofit API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Load operator details
        loadOperatorDetails();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progress_bar);
        ivOperatorProfile = findViewById(R.id.iv_operator_profile);
        tvOperatorName = findViewById(R.id.tv_operator_name);
        tvOperatorId = findViewById(R.id.tv_operator_id);
        tvStatus = findViewById(R.id.tv_status);
        tvFullName = findViewById(R.id.tv_full_name);
        tvDateOfBirth = findViewById(R.id.tv_date_of_birth);
        tvAddress = findViewById(R.id.tv_address);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        tvLicenseNumber = findViewById(R.id.tv_license_number);
        tvExpiryDate = findViewById(R.id.tv_expiry_date);
        tvMachineType = findViewById(R.id.tv_machine_type);
        tvTotalHours = findViewById(R.id.tv_total_hours);
        btnApprove = findViewById(R.id.btn_approve);
        btnReject = findViewById(R.id.btn_reject);

        // Setup button listeners
        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> approveOperator());
        }

        if (btnReject != null) {
            btnReject.setOnClickListener(v -> rejectOperator());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_operator_verification);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Back button
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void loadOperatorDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<OperatorVerification>> call = apiService.getOperatorDetails(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorVerification>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorVerification>> call, Response<ApiResponse<OperatorVerification>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorVerification> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        operatorData = apiResponse.getData();
                        populateOperatorData(operatorData);
                    } else {
                        Toast.makeText(AdminOperatorVerificationActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to load operator details",
                            Toast.LENGTH_SHORT).show();
                        // Show default data if available
                        if (operatorName != null) {
                            tvOperatorName.setText(operatorName);
                            tvOperatorId.setText("Operator ID: " + operatorId);
                        }
                    }
                } else {
                    Toast.makeText(AdminOperatorVerificationActivity.this, "Failed to load operator details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load operator details: " + response.code());
                    // Show default data if available
                    if (operatorName != null) {
                        tvOperatorName.setText(operatorName);
                        tvOperatorId.setText("Operator ID: " + operatorId);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorVerification>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminOperatorVerificationActivity.this, "Error loading operator details", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading operator details: " + t.getMessage());
                // Show default data if available
                if (operatorName != null) {
                    tvOperatorName.setText(operatorName);
                    tvOperatorId.setText("Operator ID: " + operatorId);
                }
            }
        });
    }

    private void populateOperatorData(OperatorVerification operator) {
        // Profile section
        if (tvOperatorName != null) {
            String name = operator.getName() != null ? operator.getName() : 
                         (operator.getFullName() != null ? operator.getFullName() : "N/A");
            tvOperatorName.setText(name);
        }

        if (tvOperatorId != null) {
            tvOperatorId.setText("Operator ID: " + (operator.getOperatorId() != null ? operator.getOperatorId() : operatorId));
        }

        if (tvStatus != null) {
            // Get status from operator, default to "PENDING" if not available
            String status = operator.getStatus();
            if (status == null || status.isEmpty()) {
                status = "PENDING";
            }
            tvStatus.setText(status.toUpperCase());
            
            // Update button states based on status
            updateButtonStates(status);
        }

        // Personal Details
        if (tvFullName != null) {
            tvFullName.setText(operator.getFullName() != null ? operator.getFullName() : 
                              (operator.getName() != null ? operator.getName() : "N/A"));
        }

        if (tvDateOfBirth != null) {
            tvDateOfBirth.setText(operator.getDateOfBirth() != null ? operator.getDateOfBirth() : "N/A");
        }

        if (tvAddress != null) {
            tvAddress.setText(operator.getAddress() != null ? operator.getAddress() : "N/A");
        }

        // Contact Information
        if (tvPhone != null) {
            tvPhone.setText(operator.getPhone() != null ? operator.getPhone() : "N/A");
        }

        if (tvEmail != null) {
            tvEmail.setText(operator.getEmail() != null ? operator.getEmail() : "N/A");
        }

        // License Status
        if (tvLicenseNumber != null) {
            tvLicenseNumber.setText(operator.getLicenseNumber() != null ? operator.getLicenseNumber() : "N/A");
        }

        if (tvExpiryDate != null) {
            tvExpiryDate.setText(operator.getLicenseExpiry() != null ? operator.getLicenseExpiry() : "N/A");
        }

        // Machine Details
        if (tvMachineType != null) {
            tvMachineType.setText(operator.getMachineType() != null ? operator.getMachineType() : "N/A");
        }

        // Service History
        if (tvTotalHours != null) {
            tvTotalHours.setText(String.valueOf(operator.getTotalHours()));
        }

        // TODO: Load profile image if available
        // if (operator.getProfileImage() != null && !operator.getProfileImage().isEmpty()) {
        //     // Use image loading library like Glide or Picasso
        // }
    }
    
    /**
     * Update button states based on operator status
     */
    private void updateButtonStates(String status) {
        if (status == null) status = "PENDING";
        String upperStatus = status.toUpperCase();
        
        if (upperStatus.equals("APPROVED") || upperStatus.equals("REJECTED")) {
            // Disable buttons if already approved or rejected
            if (btnApprove != null) {
                btnApprove.setEnabled(false);
                btnApprove.setAlpha(0.5f);
            }
            if (btnReject != null) {
                btnReject.setEnabled(false);
                btnReject.setAlpha(0.5f);
            }
        } else {
            // Enable buttons if status is PENDING
            if (btnApprove != null) {
                btnApprove.setEnabled(true);
                btnApprove.setAlpha(1.0f);
            }
            if (btnReject != null) {
                btnReject.setEnabled(true);
                btnReject.setAlpha(1.0f);
            }
        }
    }

    private void approveOperator() {
        if (operatorData == null) {
            Toast.makeText(this, "Operator data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnApprove != null) btnApprove.setEnabled(false);
        if (btnReject != null) btnReject.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Call<GenericResponse> call = apiService.approveOperator(operatorData);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AdminOperatorVerificationActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Operator approved successfully",
                            Toast.LENGTH_SHORT).show();
                        
                        // Update UI to show approved status
                        updateStatusToApproved();
                        
                        // Reload operator details to get updated status from server
                        loadOperatorDetails();
                    } else {
                        if (btnApprove != null) btnApprove.setEnabled(true);
                        if (btnReject != null) btnReject.setEnabled(true);
                        Toast.makeText(AdminOperatorVerificationActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to approve operator",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (btnApprove != null) btnApprove.setEnabled(true);
                    if (btnReject != null) btnReject.setEnabled(true);
                    Toast.makeText(AdminOperatorVerificationActivity.this, "Failed to approve operator", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to approve operator: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnApprove != null) btnApprove.setEnabled(true);
                if (btnReject != null) btnReject.setEnabled(true);
                Toast.makeText(AdminOperatorVerificationActivity.this, "Error approving operator", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error approving operator: " + t.getMessage());
            }
        });
    }

    private void rejectOperator() {
        if (operatorData == null) {
            Toast.makeText(this, "Operator data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnApprove != null) btnApprove.setEnabled(false);
        if (btnReject != null) btnReject.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Call<GenericResponse> call = apiService.rejectOperator(operatorData);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AdminOperatorVerificationActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Operator rejected",
                            Toast.LENGTH_SHORT).show();
                        
                        // Update UI to show rejected status
                        updateStatusToRejected();
                        
                        // Reload operator details to get updated status from server
                        loadOperatorDetails();
                    } else {
                        if (btnApprove != null) btnApprove.setEnabled(true);
                        if (btnReject != null) btnReject.setEnabled(true);
                        Toast.makeText(AdminOperatorVerificationActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to reject operator",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (btnApprove != null) btnApprove.setEnabled(true);
                    if (btnReject != null) btnReject.setEnabled(true);
                    Toast.makeText(AdminOperatorVerificationActivity.this, "Failed to reject operator", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to reject operator: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnApprove != null) btnApprove.setEnabled(true);
                if (btnReject != null) btnReject.setEnabled(true);
                Toast.makeText(AdminOperatorVerificationActivity.this, "Error rejecting operator", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error rejecting operator: " + t.getMessage());
            }
        });
    }
    
    /**
     * Update UI to show approved status immediately
     */
    private void updateStatusToApproved() {
        if (tvStatus != null) {
            tvStatus.setText("APPROVED");
            // Optionally change text color to green
            // tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        
        // Disable approve/reject buttons since operator is already approved
        if (btnApprove != null) {
            btnApprove.setEnabled(false);
            btnApprove.setAlpha(0.5f); // Make button appear disabled
        }
        if (btnReject != null) {
            btnReject.setEnabled(false);
            btnReject.setAlpha(0.5f); // Make button appear disabled
        }
    }
    
    /**
     * Update UI to show rejected status immediately
     */
    private void updateStatusToRejected() {
        if (tvStatus != null) {
            tvStatus.setText("REJECTED");
            // Optionally change text color to red
            // tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Disable approve/reject buttons since operator is already rejected
        if (btnApprove != null) {
            btnApprove.setEnabled(false);
            btnApprove.setAlpha(0.5f); // Make button appear disabled
        }
        if (btnReject != null) {
            btnReject.setEnabled(false);
            btnReject.setAlpha(0.5f); // Make button appear disabled
        }
    }
}
