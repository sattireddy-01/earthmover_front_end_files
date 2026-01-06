package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.OperatorVerification;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminVerificationActivity extends AppCompatActivity {

    private static final String TAG = "AdminVerification";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verification);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_verification);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Back button
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Get progress bar
        progressBar = findViewById(R.id.progress_bar);

        // Setup bottom navigation
        setupBottomNavigation();

        // Load operators from API (only operators, no machines)
        loadPendingOperators();
    }

    private void loadPendingOperators() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorVerification>> call = apiService.getPendingOperators();

        call.enqueue(new Callback<ApiResponse<OperatorVerification>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorVerification>> call, Response<ApiResponse<OperatorVerification>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorVerification> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<OperatorVerification> operators = apiResponse.getDataList();
                        if (operators != null && !operators.isEmpty()) {
                            populateOperators(operators);
                        } else {
                            // Hide operator cards if no pending operators
                            hideOperatorCards();
                            Toast.makeText(AdminVerificationActivity.this, 
                                "No pending operators to verify", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideOperatorCards();
                        Toast.makeText(AdminVerificationActivity.this, 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "No pending operators", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    hideOperatorCards();
                    Log.e(TAG, "Failed to load operators: " + response.code());
                    Toast.makeText(AdminVerificationActivity.this, 
                        "Failed to load operators. Please try again.", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorVerification>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                hideOperatorCards();
                Log.e(TAG, "Error loading operators: " + t.getMessage());
                Toast.makeText(AdminVerificationActivity.this, 
                    "Network error. Please check your connection and try again.", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void populateOperators(List<OperatorVerification> operators) {
        // Show and update operator cards with real data from database
        if (operators.size() > 0) {
            showOperatorCard(1);
            updateOperatorCard(1, operators.get(0));
        } else {
            hideOperatorCard(1);
        }
        
        if (operators.size() > 1) {
            showOperatorCard(2);
            updateOperatorCard(2, operators.get(1));
        } else {
            hideOperatorCard(2);
        }
    }
    
    /**
     * Show operator card
     */
    private void showOperatorCard(int cardNumber) {
        TextView tvName = findViewById(cardNumber == 1 ? R.id.tv_operator_1_name : R.id.tv_operator_2_name);
        if (tvName != null) {
            View parent = (View) tvName.getParent();
            if (parent != null) {
                View cardView = (View) parent.getParent();
                if (cardView != null) {
                    cardView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    /**
     * Hide operator card
     */
    private void hideOperatorCard(int cardNumber) {
        TextView tvName = findViewById(cardNumber == 1 ? R.id.tv_operator_1_name : R.id.tv_operator_2_name);
        if (tvName != null) {
            View parent = (View) tvName.getParent();
            if (parent != null) {
                View cardView = (View) parent.getParent();
                if (cardView != null) {
                    cardView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void updateOperatorCard(int cardNumber, OperatorVerification operator) {
        int nameId = cardNumber == 1 ? R.id.tv_operator_1_name : R.id.tv_operator_2_name;
        int idId = cardNumber == 1 ? R.id.tv_operator_1_id : R.id.tv_operator_2_id;
        int btnId = cardNumber == 1 ? R.id.btn_view_operator_1 : R.id.btn_view_operator_2;

        TextView tvName = findViewById(nameId);
        TextView tvId = findViewById(idId);
        Button btnView = findViewById(btnId);

        if (tvName != null) tvName.setText(operator.getName() != null ? operator.getName() : "N/A");
        if (tvId != null) tvId.setText("ID: " + (operator.getOperatorId() != null ? operator.getOperatorId() : "N/A"));

        if (btnView != null) {
            btnView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminVerificationActivity.this, AdminOperatorVerificationActivity.class);
                intent.putExtra("operator_id", operator.getOperatorId());
                intent.putExtra("operator_name", operator.getName());
                startActivity(intent);
            });
        }
    }

    /**
     * Hide all operator cards when there are no pending operators
     */
    private void hideOperatorCards() {
        hideOperatorCard(1);
        hideOperatorCard(2);
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_admin);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_admin_dashboard) {
                    Intent intent = new Intent(AdminVerificationActivity.this, AdminDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_admin_verification) {
                    return true;
                } else if (itemId == R.id.nav_admin_bookings) {
                    Intent intent = new Intent(AdminVerificationActivity.this, AdminLiveBookingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_admin_reports) {
                    Intent intent = new Intent(AdminVerificationActivity.this, AdminReportsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_admin_settings) {
                    Intent intent = new Intent(AdminVerificationActivity.this, SettingsActivity.class);
                    intent.putExtra("is_admin", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            bottomNav.setSelectedItemId(R.id.nav_admin_verification);
        }
    }
}
