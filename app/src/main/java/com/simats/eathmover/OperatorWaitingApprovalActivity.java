package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.OperatorVerification;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity shown after operator submits license details.
 * Displays "Waiting for Approval" message and checks approval status periodically.
 * Navigates to Operator Dashboard when approved.
 */
public class OperatorWaitingApprovalActivity extends AppCompatActivity {

    private static final String TAG = "OperatorWaitingApproval";
    private static final long CHECK_INTERVAL_MS = 5000; // Check every 5 seconds
    
    private TextView tvStatus;
    private TextView tvMessage;
    private ProgressBar progressBar;
    private Button btnCheckStatus;
    private String operatorId;
    private Handler handler;
    private Runnable checkStatusRunnable;
    private boolean isChecking = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_waiting_approval);

        // Get operator ID from intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        
        if (operatorId == null || operatorId.isEmpty()) {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_waiting_approval);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Initialize views
        tvStatus = findViewById(R.id.tv_status);
        tvMessage = findViewById(R.id.tv_message);
        progressBar = findViewById(R.id.progress_bar);
        btnCheckStatus = findViewById(R.id.btn_check_status);

        // Setup check status button
        if (btnCheckStatus != null) {
            btnCheckStatus.setOnClickListener(v -> checkApprovalStatus());
        }

        // Start automatic status checking
        handler = new Handler(Looper.getMainLooper());
        startPeriodicStatusCheck();
    }

    private void startPeriodicStatusCheck() {
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isChecking) {
                    checkApprovalStatus();
                }
                // Schedule next check
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        };
        // Start checking immediately
        handler.post(checkStatusRunnable);
    }

    private void checkApprovalStatus() {
        if (isChecking) return;
        
        isChecking = true;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnCheckStatus != null) btnCheckStatus.setEnabled(false);

        Log.d(TAG, "Checking approval status for operator_id: " + operatorId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorVerification>> call = apiService.getOperatorDetails(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorVerification>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorVerification>> call, Response<ApiResponse<OperatorVerification>> response) {
                isChecking = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnCheckStatus != null) btnCheckStatus.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorVerification> apiResponse = response.body();
                    Log.d(TAG, "API Response - Success: " + apiResponse.isSuccess());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OperatorVerification operator = apiResponse.getData();
                        // Check both status and approve_status fields
                        String status = operator.getStatus() != null ? operator.getStatus().toUpperCase() : 
                                      (operator.getApproveStatus() != null ? operator.getApproveStatus().toUpperCase() : "PENDING");
                        
                        Log.d(TAG, "Operator status: " + status);
                        updateStatusDisplay(status);
                        
                        // Check if approved
                        if (status.equals("APPROVED")) {
                            // Stop periodic checking
                            stopPeriodicStatusCheck();
                            
                            // Show success message
                            Toast.makeText(OperatorWaitingApprovalActivity.this, 
                                "Congratulations! Your account has been approved.", 
                                Toast.LENGTH_LONG).show();
                            
                            // Navigate to operator dashboard after a short delay
                            handler.postDelayed(() -> {
                                Log.d(TAG, "Navigating to operator dashboard");
                                navigateToDashboard();
                            }, 2000);
                        } else if (status.equals("REJECTED")) {
                            // Stop periodic checking
                            stopPeriodicStatusCheck();
                            
                            // Show rejection message
                            if (tvStatus != null) {
                                tvStatus.setText("Account Rejected");
                                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                            if (tvMessage != null) {
                                tvMessage.setText("Your account has been rejected. Please contact support for more information.");
                            }
                        }
                    } else {
                        String errorMsg = apiResponse != null && apiResponse.getMessage() != null ? 
                                         apiResponse.getMessage() : "Unknown error";
                        Log.e(TAG, "Failed to get operator details: " + errorMsg);
                        if (tvMessage != null) {
                            tvMessage.setText("Error checking status: " + errorMsg);
                        }
                    }
                } else {
                    String errorMsg = "Failed to check approval status. Error code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Log.e(TAG, errorMsg);
                    if (tvMessage != null) {
                        tvMessage.setText("Error checking status. Please try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorVerification>> call, Throwable t) {
                isChecking = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnCheckStatus != null) btnCheckStatus.setEnabled(true);
                Log.e(TAG, "Error checking approval status: " + t.getMessage(), t);
                if (tvMessage != null) {
                    tvMessage.setText("Network error. Retrying...");
                }
            }
        });
    }

    private void updateStatusDisplay(String status) {
        if (tvStatus != null) {
            switch (status) {
                case "APPROVED":
                    tvStatus.setText("Approved!");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "REJECTED":
                    tvStatus.setText("Rejected");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    tvStatus.setText("Waiting for Approval");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
            }
        }
        
        if (tvMessage != null) {
            switch (status) {
                case "APPROVED":
                    tvMessage.setText("Your account has been approved! Redirecting to dashboard...");
                    break;
                case "REJECTED":
                    tvMessage.setText("Your account has been rejected. Please contact support.");
                    break;
                default:
                    tvMessage.setText("Your application is under review. We will notify you once it's approved.");
                    break;
            }
        }
    }

    private void navigateToDashboard() {
        // Create session for operator (if not already created)
        if (sessionManager != null && !sessionManager.isLoggedIn()) {
            // You may need to get operator details to create session
            // For now, just navigate - session will be created on login
        }
        
        Intent intent = new Intent(OperatorWaitingApprovalActivity.this, OperatorDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void stopPeriodicStatusCheck() {
        if (handler != null && checkStatusRunnable != null) {
            handler.removeCallbacks(checkStatusRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPeriodicStatusCheck();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back - operator must wait for approval
        Toast.makeText(this, "Please wait for admin approval", Toast.LENGTH_SHORT).show();
    }
}

