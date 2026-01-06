package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLiveBookingsActivity extends AppCompatActivity {

    private static final String TAG = "AdminLiveBookings";
    private ProgressBar progressBar;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private com.simats.eathmover.adapters.BookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_live_bookings);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_live_bookings);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Back button
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }

        progressBar = findViewById(R.id.progress_bar);
        
        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view_bookings);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        
        // Load live bookings from API
        loadLiveBookings();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadLiveBookings() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Booking>>> call = apiService.getLiveBookings();

        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<Booking> bookings = apiResponse.getData();
                        if (bookings != null && !bookings.isEmpty()) {
                            populateBookings(bookings);
                        } else {
                            Toast.makeText(AdminLiveBookingsActivity.this, "No live bookings found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminLiveBookingsActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to load bookings",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load bookings: " + response.code());
                    Toast.makeText(AdminLiveBookingsActivity.this, "Failed to load bookings: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading bookings: " + t.getMessage());
                Toast.makeText(AdminLiveBookingsActivity.this, "Network error (List): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateBookings(List<Booking> bookings) {
        if (adapter == null) {
            adapter = new com.simats.eathmover.adapters.BookingAdapter(this, bookings);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(bookings);
        }
        Log.d(TAG, "Loaded " + bookings.size() + " bookings");
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
                    return true;
                } else if (itemId == R.id.nav_admin_reports) {
                    navigateToActivity(AdminReportsActivity.class);
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
            bottomNav.setSelectedItemId(R.id.nav_admin_bookings);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
