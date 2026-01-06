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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.adapters.BookingAdapter;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;
import com.simats.eathmover.utils.RealTimeDataManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorBookingsActivity extends AppCompatActivity implements RealTimeDataManager.OperatorBookingsListener {

    private static final String TAG = "OperatorBookings";
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private ProgressBar progressBar;
    private TextView tvNoBookings;
    private TextView tvStatTotal;
    private TextView tvStatPending;
    private TextView tvStatActive;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_bookings);

        sessionManager = new SessionManager(this);


        recyclerView = findViewById(R.id.rv_operator_bookings);
        progressBar = findViewById(R.id.progress_bar);
        tvNoBookings = findViewById(R.id.tv_no_bookings);

        tvStatTotal = findViewById(R.id.tv_stat_total_count);
        tvStatPending = findViewById(R.id.tv_stat_pending_count);
        tvStatActive = findViewById(R.id.tv_stat_active_count);

        View btnBack = findViewById(R.id.btn_back_operator_booking);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(bookingList, new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(Booking booking) {
                // If PENDING, go to NewBookingRequestActivity
                if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                    Intent intent = new Intent(OperatorBookingsActivity.this, NewBookingRequestActivity.class);
                    intent.putExtra("booking_id", booking.getBookingId());
                    startActivity(intent);
                }
            }
            
            @Override
            public void onAcceptClick(Booking booking) {
                 acceptBooking(booking);
            }
            
            @Override
            public void onDeclineClick(Booking booking) {
                 declineBooking(booking);
            }
        });
        adapter.setOperatorView(true);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_bookings);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_bookings);
        }

        // Load operator bookings
        loadOperatorBookings();
    }

    @Override
    protected void onStart() {
        super.onStart();
        RealTimeDataManager.getInstance().setSessionManager(sessionManager);
        RealTimeDataManager.getInstance().setOperatorBookingsListener(this);
        RealTimeDataManager.getInstance().startPolling();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RealTimeDataManager.getInstance().removeOperatorBookingsListener();
        RealTimeDataManager.getInstance().stopPolling();
    }

    @Override
    public void onOperatorBookingsUpdated(List<Booking> bookings) {
        runOnUiThread(() -> updateBookingsUI(bookings));
    }

    private void loadOperatorBookings() {
        String operatorId = sessionManager.getOperatorId();
        if (operatorId == null) {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvNoBookings.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Booking>>> call = apiService.getOperatorBookings(operatorId);

        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        updateBookingsUI(apiResponse.getData());
                    }
 else {
                        tvNoBookings.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to load bookings: " + response.code());
                    tvNoBookings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error: " + t.getMessage());
                tvNoBookings.setVisibility(View.VISIBLE);
                Toast.makeText(OperatorBookingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookingsUI(List<Booking> list) {
        if (list == null) return;
        
        bookingList.clear();
        
        // Filter out history items (Completed, Cancelled, etc.) from the Active Bookings view
        for (Booking b : list) {
            String status = b.getStatus();
            boolean isHistory = "COMPLETED".equalsIgnoreCase(status) || 
                                "CANCELLED".equalsIgnoreCase(status) || 
                                "REJECTED".equalsIgnoreCase(status) || 
                                "DECLINED".equalsIgnoreCase(status);
            
            if (!isHistory) {
                bookingList.add(b);
            }
        }
        
        int total = bookingList.size();
        int pending = 0;
        int active = 0;

        for (Booking b : bookingList) {
            String status = b.getStatus();
            if ("PENDING".equalsIgnoreCase(status)) {
                pending++;
            } else if ("ACCEPTED".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status)) {
                active++;
            }
        }

        if (tvStatTotal != null) tvStatTotal.setText(String.valueOf(total));
        if (tvStatPending != null) tvStatPending.setText(String.valueOf(pending));
        if (tvStatActive != null) tvStatActive.setText(String.valueOf(active));

        adapter.notifyDataSetChanged();

        if (tvNoBookings != null) {
            tvNoBookings.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
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
                return true;
            } else if (itemId == R.id.nav_earnings) {
                Intent intent = new Intent(this, OperatorEarningsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, OperatorProfileActivity.class);
                String oId = sessionManager.getOperatorId();
                if (oId != null) {
                    intent.putExtra("operator_id", oId);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void acceptBooking(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String operatorId = sessionManager.getOperatorId();
        if (operatorId != null) {
            booking.setOperatorId(operatorId);
        }
        Call<com.simats.eathmover.models.GenericResponse> call = apiService.acceptBooking(booking);
        call.enqueue(new Callback<com.simats.eathmover.models.GenericResponse>() {
            @Override
            public void onResponse(Call<com.simats.eathmover.models.GenericResponse> call, Response<com.simats.eathmover.models.GenericResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(OperatorBookingsActivity.this, "Booking Accepted", Toast.LENGTH_SHORT).show();
                    // Status will naturally update via polling, but we can speed it up
                    RealTimeDataManager.getInstance().refreshNow(); 
                    loadOperatorBookings();
                } else {
                    Toast.makeText(OperatorBookingsActivity.this, "Failed to accept", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.simats.eathmover.models.GenericResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OperatorBookingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declineBooking(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<com.simats.eathmover.models.GenericResponse> call = apiService.declineBooking(booking);
        call.enqueue(new Callback<com.simats.eathmover.models.GenericResponse>() {
            @Override
            public void onResponse(Call<com.simats.eathmover.models.GenericResponse> call, Response<com.simats.eathmover.models.GenericResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(OperatorBookingsActivity.this, "Booking Declined", Toast.LENGTH_SHORT).show();
                    RealTimeDataManager.getInstance().refreshNow();
                    loadOperatorBookings();
                } else {
                    Toast.makeText(OperatorBookingsActivity.this, "Failed to decline", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.simats.eathmover.models.GenericResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OperatorBookingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
