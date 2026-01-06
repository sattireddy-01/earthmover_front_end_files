package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.models.GenericResponse;
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
 * Activity for displaying and handling new booking requests.
 */
public class NewBookingRequestActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvBookingTitle;
    private TextView tvBookingLocation;
    private TextView tvBookingDuration;
    private ImageView ivBookingImage;
    private Button btnAccept;
    private Button btnDecline;
    private ProgressBar progressBar;
    private Booking currentBooking;
    private RealTimeDataManager realTimeDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_booking_request);

        sessionManager = new SessionManager(this);
        
        // Initialize real-time data manager
        realTimeDataManager = RealTimeDataManager.getInstance();
        realTimeDataManager.setSessionManager(sessionManager);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_new_booking);
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

        // Initialize views
        tvBookingTitle = findViewById(R.id.tv_booking_title);
        tvBookingLocation = findViewById(R.id.tv_booking_location);
        tvBookingDuration = findViewById(R.id.tv_booking_duration);
        ivBookingImage = findViewById(R.id.iv_booking_image);
        btnAccept = findViewById(R.id.btn_accept);
        btnDecline = findViewById(R.id.btn_decline);
        progressBar = findViewById(R.id.progress_bar);

        // Load booking data
        loadBookingData();

        // Accept button
        if (btnAccept != null) {
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptBooking();
                }
            });
        }

        // Decline button
        if (btnDecline != null) {
            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    declineBooking();
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_new_booking);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_bookings);
        }

        // Setup real-time booking request updates
        setupRealTimeBookingUpdates();
    }

    private void setupRealTimeBookingUpdates() {
        String operatorId = sessionManager.getOperatorId();
        if (operatorId == null) return;

        // Set up real-time booking request listener
        realTimeDataManager.setBookingRequestListener(new RealTimeDataManager.BookingRequestListener() {
            @Override
            public void onNewBookingRequest(List<Booking> bookings) {
                if (bookings != null && !bookings.isEmpty()) {
                    // Update with the latest booking request
                    Booking latestBooking = bookings.get(0);
                    if (currentBooking == null || !latestBooking.getBookingId().equals(currentBooking.getBookingId())) {
                        currentBooking = latestBooking;
                        populateBookingData(currentBooking);
                    }
                }
            }
        });

        // Start fast polling for booking requests
        realTimeDataManager.startFastPolling();
    }

    private void loadBookingData() {
        // Load booking data from intent if available
        Intent intent = getIntent();
        String bookingId = intent != null ? intent.getStringExtra("booking_id") : null;
        String operatorId = sessionManager.getOperatorId();

        if (bookingId != null) {
            // Load specific booking
            loadBookingById(bookingId);
        } else if (operatorId != null) {
            // Load first pending booking for operator
            loadPendingBookings(operatorId);
        } else {
            // Fallback to default values
            setDefaultBookingData();
        }
    }

    private void loadPendingBookings(String operatorId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Log.d("NewBookingRequest", "Loading pending bookings for operator_id: " + operatorId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<Booking>> call = apiService.getPendingBookings(operatorId);

        call.enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                Log.d("NewBookingRequest", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    Log.d("NewBookingRequest", "API Success: " + apiResponse.isSuccess());
                    Log.d("NewBookingRequest", "Message: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "No message"));
                    
                    if (apiResponse.isSuccess() && apiResponse.getDataList() != null) {
                        Log.d("NewBookingRequest", "Bookings count: " + apiResponse.getDataList().size());
                        if (!apiResponse.getDataList().isEmpty()) {
                            currentBooking = apiResponse.getDataList().get(0);
                            Log.d("NewBookingRequest", "Loaded booking: " + currentBooking.getBookingId() + 
                                ", Status: " + currentBooking.getStatus() + 
                                ", Machine: " + currentBooking.getMachineType());
                            populateBookingData(currentBooking);
                        } else {
                            Log.d("NewBookingRequest", "No pending bookings found for operator");
                            setDefaultBookingData();
                        }
                    } else {
                        Log.e("NewBookingRequest", "API response not successful or data list is null");
                        setDefaultBookingData();
                    }
                } else {
                    Log.e("NewBookingRequest", "Failed to load bookings: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("NewBookingRequest", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("NewBookingRequest", "Error reading error body: " + e.getMessage());
                    }
                    setDefaultBookingData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("NewBookingRequest", "Error loading bookings: " + t.getMessage(), t);
                setDefaultBookingData();
            }
        });
    }

    private void loadBookingById(String bookingId) {
        // TODO: Add endpoint to get booking by ID if needed
        setDefaultBookingData();
    }

    private void populateBookingData(Booking booking) {
        if (booking == null) {
            setDefaultBookingData();
            return;
        }

        if (tvBookingTitle != null && booking.getMachineType() != null) {
            tvBookingTitle.setText(booking.getMachineType() + " Booking");
        }
        if (tvBookingLocation != null && booking.getLocation() != null) {
            tvBookingLocation.setText("Location: " + booking.getLocation());
        }
        if (tvBookingDuration != null && booking.getTotalHours() > 0) {
            tvBookingDuration.setText("Estimated Duration: " + booking.getTotalHours() + " hours");
        }
    }

    private void setDefaultBookingData() {
        if (tvBookingTitle != null) {
            tvBookingTitle.setText("Crane Booking");
        }
        if (tvBookingLocation != null) {
            tvBookingLocation.setText("Location: Construction Site, 123 Main St");
        }
        if (tvBookingDuration != null) {
            tvBookingDuration.setText("Estimated Duration: 4 hours");
        }
    }

    private void acceptBooking() {
        if (currentBooking == null) {
            Toast.makeText(this, "No booking to accept", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnAccept != null) btnAccept.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String operatorId = sessionManager.getOperatorId();
        if (operatorId != null) {
            currentBooking.setOperatorId(operatorId);
        }
        Call<GenericResponse> call = apiService.acceptBooking(currentBooking);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnAccept != null) btnAccept.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(NewBookingRequestActivity.this, "Booking accepted!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(NewBookingRequestActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewBookingRequestActivity.this, "Failed to accept booking", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnAccept != null) btnAccept.setEnabled(true);
                Log.e("NewBookingRequest", "Error accepting booking: " + t.getMessage());
                Toast.makeText(NewBookingRequestActivity.this, "Error accepting booking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declineBooking() {
        if (currentBooking == null) {
            Toast.makeText(this, "No booking to decline", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnDecline != null) btnDecline.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.declineBooking(currentBooking);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnDecline != null) btnDecline.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(NewBookingRequestActivity.this, "Booking declined", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(NewBookingRequestActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewBookingRequestActivity.this, "Failed to decline booking", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnDecline != null) btnDecline.setEnabled(true);
                Log.e("NewBookingRequest", "Error declining booking: " + t.getMessage());
                Toast.makeText(NewBookingRequestActivity.this, "Error declining booking", Toast.LENGTH_SHORT).show();
            }
        });
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
                // Navigate to all bookings page
                Intent intent = new Intent(this, OperatorBookingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_earnings) {
                Intent intent = new Intent(this, OperatorEarningsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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
        // Resume fast polling when activity is visible
        if (realTimeDataManager != null) {
            realTimeDataManager.startFastPolling();
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
            realTimeDataManager.removeBookingRequestListener();
        }
    }
}

