package com.simats.eathmover;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;
import com.simats.eathmover.config.ApiConfig;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service History screen showing a list of completed bookings.
 */
public class ServiceHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ServiceHistoryActivity";
    private RecyclerView recyclerView;
    // private ServiceHistoryAdapter adapter; // Removed to use BookingAdapter
    private List<Booking> historyList;
    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private TextView tvNoHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_history);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_service_history);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        recyclerView = findViewById(R.id.rv_service_history);
        // Add progress bar if not present in layout, usually it might be separate or we need to add it dynamically or just handle it if layout supports it.
        // Assuming layout might not have it, let's look for it or add logic to handle loading state implicitly.
        // For safety, let's assume we can add a progress bar ID if we edited the layout, but I can't edit layout XML easily.
        // I'll try to find it by ID just in case, or show Toast.
        
        tvNoHistory = new TextView(this); // Fallback if not in layout
        // Check if layout has a textview for empty state? UserBookings had tv_no_bookings.
        // Let's assume the RecyclerView is the main thing.

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        historyList = new ArrayList<>();
        // Adapter will be set when data loads
        // adapter = new ServiceHistoryAdapter(historyList);
        // recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_service_history);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_history);
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }

        loadHistory();
    }

    private void loadHistory() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Toast.makeText(this, "History Debug: User " + userId, Toast.LENGTH_SHORT).show();
        Call<ApiResponse<List<Booking>>> call = apiService.getUserBookings(userId);

        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> allBookings = response.body().getData();
                    if (allBookings != null) {
                        Toast.makeText(ServiceHistoryActivity.this, "Fetched " + allBookings.size() + " items", Toast.LENGTH_SHORT).show();
                        filterAndDisplayHistory(allBookings);
                    }
                } else {
                    Log.e(TAG, "Failed to load history");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(ServiceHistoryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterAndDisplayHistory(List<Booking> bookings) {
        historyList.clear();
        for (Booking b : bookings) {
            String status = b.getStatus();
            if (status != null) {
                if ("COMPLETED".equalsIgnoreCase(status) || 
                    "CANCELLED".equalsIgnoreCase(status) || 
                    "REJECTED".equalsIgnoreCase(status) || 
                    "DECLINED".equalsIgnoreCase(status)) {
                    historyList.add(b);
                }
            }
        }
        
        // Sort by date/ID descending
        java.util.Collections.sort(historyList, (b1, b2) -> {
            try {
                int id1 = Integer.parseInt(b1.getBookingId());
                int id2 = Integer.parseInt(b2.getBookingId());
                return Integer.compare(id2, id1);
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Use BookingAdapter instead of custom adapter
        com.simats.eathmover.adapters.BookingAdapter adapter = new com.simats.eathmover.adapters.BookingAdapter(this, historyList);
        adapter.setOperatorView(false); // User view
        recyclerView.setAdapter(adapter);
        
        if (historyList.isEmpty()) {
            if (tvNoHistory != null) tvNoHistory.setVisibility(View.VISIBLE);
            Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show();
        } else {
             if (tvNoHistory != null) tvNoHistory.setVisibility(View.GONE);
        }
    }
}


