package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.ReportsData;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportsActivity extends AppCompatActivity {

    private static final String TAG = "AdminReports";
    private ProgressBar progressBar;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_reports);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Back button
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }

        progressBar = findViewById(R.id.progress_bar);

        // Load reports data from API
        loadReportsData();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadReportsData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<ReportsData>> call = apiService.getReports();

        call.enqueue(new Callback<ApiResponse<ReportsData>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReportsData>> call, Response<ApiResponse<ReportsData>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ReportsData> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ReportsData reportsData = apiResponse.getData();
                        populateReportsData(reportsData);
                    } else {
                        Toast.makeText(AdminReportsActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to load reports",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load reports: " + response.code());
                    Toast.makeText(AdminReportsActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReportsData>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading reports: " + t.getMessage());
                Toast.makeText(AdminReportsActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateReportsData(ReportsData data) {
        // App Performance Section
        TextView tvActiveUsers = findViewById(R.id.tv_active_users);
        TextView tvNewUsers = findViewById(R.id.tv_new_users);

        if (tvActiveUsers != null) {
            tvActiveUsers.setText(numberFormat.format(data.getActiveUsers()));
        }
        if (tvNewUsers != null) {
            tvNewUsers.setText(numberFormat.format(data.getNewUsers()));
        }

        // Update change indicators (you may need to add these TextViews to layout)
        // TextView tvActiveUsersChange = findViewById(R.id.tv_active_users_change);
        // if (tvActiveUsersChange != null && data.getActiveUsersChange() != null) {
        //     tvActiveUsersChange.setText(data.getActiveUsersChange());
        // }

        // Revenue Section
        TextView tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        TextView tvAvgBookingValue = findViewById(R.id.tv_avg_booking_value);

        if (tvTotalRevenue != null) {
            tvTotalRevenue.setText(currencyFormat.format(data.getTotalRevenue()));
        }
        if (tvAvgBookingValue != null) {
            tvAvgBookingValue.setText(currencyFormat.format(data.getAvgBookingValue()));
        }

        // Machine Usage Section
        TextView tvTotalBookings = findViewById(R.id.tv_total_bookings);
        TextView tvMostBookedMachine = findViewById(R.id.tv_most_booked_machine);
        TextView tvMachineBookings = findViewById(R.id.tv_machine_bookings);

        if (tvTotalBookings != null) {
            tvTotalBookings.setText(numberFormat.format(data.getTotalBookings()));
        }
        if (tvMostBookedMachine != null) {
            tvMostBookedMachine.setText(data.getMostBookedMachine() != null ? data.getMostBookedMachine() : "N/A");
        }
        if (tvMachineBookings != null) {
            tvMachineBookings.setText(data.getMachineBookingsCount() + " bookings");
        }

        // Operator Activity Section
        TextView tvActiveOperators = findViewById(R.id.tv_active_operators);
        TextView tvTopOperator = findViewById(R.id.tv_top_operator);
        TextView tvOperatorBookings = findViewById(R.id.tv_operator_bookings);

        if (tvActiveOperators != null) {
            tvActiveOperators.setText(numberFormat.format(data.getActiveOperators()));
        }
        if (tvTopOperator != null) {
            tvTopOperator.setText(data.getTopOperator() != null ? data.getTopOperator() : "N/A");
        }
        if (tvOperatorBookings != null) {
            tvOperatorBookings.setText(data.getOperatorBookingsCount() + " bookings");
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_admin);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_admin_dashboard) {
                    navigateToActivity(AdminDashboardActivity.class);
                    return true;
                } else if (itemId == R.id.nav_admin_verification) {
                    navigateToActivity(AdminVerificationActivity.class);
                    return true;
                } else if (itemId == R.id.nav_admin_bookings) {
                    navigateToActivity(AdminLiveBookingsActivity.class);
                    return true;
                } else if (itemId == R.id.nav_admin_reports) {
                    return true;
                } else if (itemId == R.id.nav_admin_settings) {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.putExtra("is_admin", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            bottomNav.setSelectedItemId(R.id.nav_admin_reports);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
