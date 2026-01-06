package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.RealTimeDataManager;
import com.simats.eathmover.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

/**
 * Activity showing operator booking history.
 */
public class OperatorEarningsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private RealTimeDataManager realTimeDataManager;
    private android.widget.TextView tvEmptyHistory;
    private androidx.recyclerview.widget.RecyclerView rvBookingHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_earnings);

        sessionManager = new SessionManager(this);
        
        // Initialize real-time data manager
        realTimeDataManager = RealTimeDataManager.getInstance();
        realTimeDataManager.setSessionManager(sessionManager);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_operator_earnings);
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

        progressBar = findViewById(R.id.progress_bar);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        rvBookingHistory = findViewById(R.id.rv_booking_history);

        // Setup RecyclerView
        if (rvBookingHistory != null) {
            rvBookingHistory.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            rvBookingHistory.setHasFixedSize(true);
        }

        // Load booking history
        loadBookingHistory();

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_earnings);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_earnings);
        }

        // Setup real-time earnings updates
        setupRealTimeEarningsUpdates();
    }

    private void setupRealTimeEarningsUpdates() {
        // Set up real-time booking history listener
        realTimeDataManager.setEarningsListener(new RealTimeDataManager.EarningsDataListener() {
            @Override
            public void onEarningsUpdated(List<Booking> transactions) {
                displayBookingHistory(transactions);
            }
        });

        // Start polling for booking history updates
        realTimeDataManager.startPolling();
    }

    private void loadBookingHistory() {
        String operatorId = sessionManager.getOperatorId();
        if (operatorId == null) {
            showEmptyState();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Booking>>> call = apiService.getOperatorEarnings(operatorId);

        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    // The data comes in 'data' field because T is List<Booking>
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        displayBookingHistory(apiResponse.getData());
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e("OperatorHistory", "Failed to load booking history: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("OperatorHistory", "Error loading booking history: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void displayBookingHistory(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            showEmptyState();
            return;
        }

        // Filter for History bookings (Completed, Cancelled, Rejected, Declined)
        List<Booking> historyBookings = new java.util.ArrayList<>();
        for (Booking b : bookings) {
            String status = b.getStatus();
            if (status != null) {
                if ("COMPLETED".equalsIgnoreCase(status) || 
                    "CANCELLED".equalsIgnoreCase(status) || 
                    "REJECTED".equalsIgnoreCase(status) || 
                    "DECLINED".equalsIgnoreCase(status)) {
                    historyBookings.add(b);
                }
            }
        }

        if (historyBookings.isEmpty()) {
            showEmptyState();
            return;
        }

        // Hide empty state
        if (tvEmptyHistory != null) {
            tvEmptyHistory.setVisibility(View.GONE);
        }
        if (rvBookingHistory != null) {
            rvBookingHistory.setVisibility(View.VISIBLE);
            
            // Set up RecyclerView adapter
            com.simats.eathmover.adapters.BookingAdapter adapter = new com.simats.eathmover.adapters.BookingAdapter(historyBookings, new com.simats.eathmover.adapters.BookingAdapter.OnBookingClickListener() {
                @Override
                public void onBookingClick(Booking booking) {
                    // Show details
                }
            });
            adapter.setOperatorView(true);
            rvBookingHistory.setAdapter(adapter);
            
            Log.d("OperatorHistory", "Loaded " + historyBookings.size() + " history items");
        }
    }

    private void showEmptyState() {
        if (tvEmptyHistory != null) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
        }
        if (rvBookingHistory != null) {
            rvBookingHistory.setVisibility(View.GONE);
        }
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
                // Already on earnings page
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, OperatorProfileActivity.class);
                String operatorId = sessionManager.getOperatorId();
                if (operatorId != null) {
                    intent.putExtra("operator_id", operatorId);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume polling when activity is visible
        if (realTimeDataManager != null) {
            realTimeDataManager.startPolling();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling when activity is not visible
        if (realTimeDataManager != null) {
            realTimeDataManager.stopPolling();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listener
        if (realTimeDataManager != null) {
            realTimeDataManager.removeEarningsListener();
        }
    }
}

