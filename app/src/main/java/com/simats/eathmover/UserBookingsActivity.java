package com.simats.eathmover;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.adapters.BookingAdapter;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;
import com.simats.eathmover.utils.RealTimeDataManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserBookingsActivity extends AppCompatActivity implements RealTimeDataManager.UserBookingsListener {

    private static final String TAG = "UserBookingsActivity";
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private ProgressBar progressBar;
    private TextView tvNoBookings;
    private TextView tvStatTotal;
    private TextView tvStatPending;
    private TextView tvStatActive;
    private SessionManager sessionManager;
    private List<Booking> fullBookingList = new ArrayList<>();
    private String currentFilter = "ALL";
    private View llStatTotal, llStatPending, llStatActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bookings);

        sessionManager = new SessionManager(this);

        recyclerView = findViewById(R.id.rv_user_bookings);
        progressBar = findViewById(R.id.progress_bar);
        tvNoBookings = findViewById(R.id.tv_no_bookings);

        // Debug Toast
        Toast.makeText(this, "Emulator Config Loaded (10.0.2.2)", Toast.LENGTH_LONG).show();

        tvStatTotal = findViewById(R.id.tv_stat_total_count);
        tvStatPending = findViewById(R.id.tv_stat_pending_count);
        tvStatActive = findViewById(R.id.tv_stat_active_count);

        View btnBack = findViewById(R.id.btn_back_user_booking);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(bookingList, new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(Booking booking) {
                Log.d(TAG, "Booking clicked: " + booking.getBookingId());
            }

            @Override
            public void onCancelClick(Booking booking) {
                showCancelConfirmationDialog(booking);
            }
            
            @Override
            public void onCompleteClick(Booking booking) {
                showCompleteConfirmationDialog(booking);
            }
        });
        adapter.setOperatorView(false);
        adapter.setForceUserLayout(true); // Enforce new UI
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_user_bookings);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_book);
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }

        // Initialize Filter Buttons
        llStatTotal = findViewById(R.id.ll_stat_total);
        llStatPending = findViewById(R.id.ll_stat_pending);
        llStatActive = findViewById(R.id.ll_stat_active);

        if (llStatTotal != null) llStatTotal.setOnClickListener(v -> {
            setFilter("ALL");
            // Also refresh data when clicking Total, acting as a Refresh button
            Toast.makeText(this, "Refreshing bookings...", Toast.LENGTH_SHORT).show();
            loadUserBookings();
        });
        if (llStatPending != null) llStatPending.setOnClickListener(v -> setFilter("PENDING"));
        if (llStatActive != null) llStatActive.setOnClickListener(v -> setFilter("ACTIVE"));

        if (getIntent().hasExtra("filter")) {
            String intentFilter = getIntent().getStringExtra("filter");
            if (intentFilter != null) {
                currentFilter = intentFilter;
            }
        }

        updateFilterButtonsUI(); // Initial UI state
        loadUserBookings();
    }

    private void setFilter(String filter) {
        this.currentFilter = filter;
        updateFilterButtonsUI();
        updateBookingsUI(fullBookingList);
    }
    
    private void updateFilterButtonsUI() {
        // Reset backgrounds
        if (llStatTotal != null) llStatTotal.setAlpha(0.6f);
        if (llStatPending != null) llStatPending.setAlpha(0.6f);
        if (llStatActive != null) llStatActive.setAlpha(0.6f);

        // Highlight selected
        if ("ALL".equals(currentFilter) && llStatTotal != null) llStatTotal.setAlpha(1.0f);
        else if ("PENDING".equals(currentFilter) && llStatPending != null) llStatPending.setAlpha(1.0f);
        else if ("ACTIVE".equals(currentFilter) && llStatActive != null) llStatActive.setAlpha(1.0f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force refresh when returning to this screen (e.g., from Booking Confirmation)
        loadUserBookings();
    }

    @Override
    protected void onStart() {
        super.onStart();
        RealTimeDataManager.getInstance().setSessionManager(sessionManager);
        RealTimeDataManager.getInstance().setUserBookingsListener(this);
        RealTimeDataManager.getInstance().startPolling();
        // Refresh on start to be sure
        loadUserBookings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RealTimeDataManager.getInstance().removeUserBookingsListener();
        RealTimeDataManager.getInstance().stopPolling();
    }

    @Override
    public void onUserBookingsUpdated(List<Booking> bookings) {
        runOnUiThread(() -> updateBookingsUI(bookings));
    }

    private void loadUserBookings() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Diagnostic Toast (Re-added for debugging)
        // Toast.makeText(this, "Debug User ID: " + userId, Toast.LENGTH_LONG).show();
        Log.d(TAG, "Loading bookings for User ID: " + userId);

        progressBar.setVisibility(View.VISIBLE);
        tvNoBookings.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Booking>>> call = apiService.getUserBookings(userId);

        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    Log.d(TAG, "API Response: Success=" + apiResponse.isSuccess() + ", DataSize=" + (apiResponse.getData() != null ? apiResponse.getData().size() : "null"));

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Booking> data = apiResponse.getData();
                        // Toast.makeText(UserBookingsActivity.this, "Loaded " + data.size() + " bookings", Toast.LENGTH_SHORT).show();
                        if (data.isEmpty()) {
                             tvNoBookings.setText("No bookings found for User ID: " + userId);
                        }
                        updateBookingsUI(data);
                    } else {
                        Log.e(TAG, "Failed to load bookings or empty: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        tvNoBookings.setText("No bookings found (Server Message: " + apiResponse.getMessage() + ")");
                        tvNoBookings.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to load bookings: HTTP " + response.code());
                    tvNoBookings.setText("Server Error: " + response.code());
                    tvNoBookings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(UserBookingsActivity.this, "Connection Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + t.getMessage());
                if (tvNoBookings != null) {
                    tvNoBookings.setText("Connection failed. Tap Total to retry.");
                    tvNoBookings.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateBookingsUI(List<Booking> list) {
        Log.d(TAG, "Updating UI with " + (list != null ? list.size() : "null") + " bookings");
        if (list == null) return;
        
        // Save full list for counts
        if (list != fullBookingList) {
            fullBookingList.clear();
            fullBookingList.addAll(list);
            
            // Sort by ID descending (newest first)
            // Assuming bookingId is a number or lexicographically comparable string
            java.util.Collections.sort(fullBookingList, (b1, b2) -> {
                try {
                    int id1 = Integer.parseInt(b1.getBookingId());
                    int id2 = Integer.parseInt(b2.getBookingId());
                    return Integer.compare(id2, id1); // Descending
                } catch (Exception e) {
                    return b2.getBookingId().compareTo(b1.getBookingId());
                }
            });
        }
        
        int total = 0;
        int pending = 0;
        int active = 0;

        List<Booking> filteredList = new ArrayList<>();

        for (Booking b : fullBookingList) {
            String status = b.getStatus();
            
            // Definitions matching dashboard (roughly) but permissive for Total
            boolean isPending = status != null && (status.toLowerCase().contains("pending"));
            boolean isHistory = status != null && ("completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status) || "declined".equalsIgnoreCase(status));
            boolean isActive = status != null && !isHistory; // Active is anything NOT history

            // Total is everything.
            total++; 

            if (isPending) pending++;
            if (isActive) active++; // "Active" tab logic

            // Apply filter
            if ("ALL".equals(currentFilter)) {
                // Show everything in ALL, absolutely everything
                filteredList.add(b);
            } else if ("PENDING".equals(currentFilter) && isPending) {
                if (!isHistory) filteredList.add(b);
            } else if ("ACTIVE".equals(currentFilter) && isActive) {
                if (!isHistory) filteredList.add(b);
            }
        }

        if (tvStatTotal != null) tvStatTotal.setText(String.valueOf(total));
        if (tvStatPending != null) tvStatPending.setText(String.valueOf(pending));
        if (tvStatActive != null) tvStatActive.setText(String.valueOf(active));

        bookingList.clear();
        bookingList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (tvNoBookings != null) {
            tvNoBookings.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
            if (bookingList.isEmpty()) {
                 if (!fullBookingList.isEmpty()) {
                    tvNoBookings.setText("No " + currentFilter.toLowerCase() + " bookings found");
                 }
                 // If full list is empty, message was set in loadUserBookings
            }
        }
    }

    private void showCancelConfirmationDialog(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking))
            .setNegativeButton("No", null)
            .show();
    }

    private void cancelBooking(Booking booking) {
        Toast.makeText(this, "Cancelling booking " + booking.getBookingId(), Toast.LENGTH_SHORT).show();
        // Refresh after cancellation
        // Real implementation would call API
        loadUserBookings();
    }

    private void showCompleteConfirmationDialog(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Complete Booking")
            .setMessage("Are you sure you want to mark this booking as complete? This action cannot be undone.")
            .setPositiveButton("Yes", (dialog, which) -> completeBooking(booking))
            .setNegativeButton("No", null)
            .show();
    }

    private void completeBooking(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<com.simats.eathmover.models.GenericResponse> call = apiService.completeBooking(booking);
        
        call.enqueue(new Callback<com.simats.eathmover.models.GenericResponse>() {
            @Override
            public void onResponse(Call<com.simats.eathmover.models.GenericResponse> call, Response<com.simats.eathmover.models.GenericResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UserBookingsActivity.this, "Booking marked as complete", Toast.LENGTH_SHORT).show();
                    RealTimeDataManager.getInstance().refreshNow();
                    loadUserBookings();
                } else {
                    Toast.makeText(UserBookingsActivity.this, "Failed to complete booking", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.simats.eathmover.models.GenericResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserBookingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
