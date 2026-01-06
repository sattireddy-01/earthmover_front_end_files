package com.simats.eathmover;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.simats.eathmover.adapters.BookingAdapter;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import com.google.android.material.button.MaterialButton;
import android.widget.EditText;
import android.content.DialogInterface;
import com.simats.eathmover.models.User;
import com.simats.eathmover.models.GenericResponse;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;
import com.simats.eathmover.utils.RealTimeDataManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.text.Editable;
import android.text.TextWatcher;
import com.simats.eathmover.models.NominatimResult;
import com.simats.eathmover.retrofit.NominatimApiService;
import com.simats.eathmover.adapters.LocationSuggestionAdapter;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserDashboardActivity extends AppCompatActivity implements RealTimeDataManager.UserBookingsListener {

    private static final String TAG = "UserDashboard";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentLocation; 
    private MaterialButton btnUseCurrentLocation;
    private MaterialButton btnEnterManually;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    
    // Machine Category Views
    // Excavators (category_id = 2)
    private ImageView ivExcavator;
    private TextView tvExcavatorName;
    private TextView tvExcavatorPrice;
    
    // JCBs (category_id = 1)
    private ImageView ivJcb;
    private TextView tvJcbName;
    private TextView tvJcbPrice;
    
    // Dozers (category_id = 3)
    private ImageView ivDozer;
    private TextView tvDozerName;
    private TextView tvDozerPrice;
    
    // Session
    private SessionManager sessionManager;
    
    // Bookings
    private RecyclerView rvPendingBookings;
    private RecyclerView rvCompletedBookings;
    private RecyclerView rvUserBookingsNew;
    private TextView tvPendingBookingsTitle;
    private TextView tvCompletedBookingsTitle;
    private TextView tvNoBookings;
    private TextView tvStatTotal;
    private TextView tvStatPending;
    private TextView tvStatActive;
    private BookingAdapter pendingBookingsAdapter;
    private BookingAdapter completedBookingsAdapter;
    private BookingAdapter userBookingsNewAdapter;
    
    private View llHomeContent;
    private View llBookContent;
    private View etSearch;
    private View llCategories;
    private View tvCategoriesLabel;
    private android.widget.ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_dashboard);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // Settings icon click
        ImageView ivSettings = findViewById(R.id.iv_settings);
        if (ivSettings != null) {
            ivSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserDashboardActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Logout icon click (top right)
        ImageView ivLogout = findViewById(R.id.iv_logout);
        if (ivLogout != null) {
            ivLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutDialog();
                }
            });
        }

        // Location views
        // Location views
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);
        btnEnterManually = findViewById(R.id.btn_enter_manually);
        
        if (btnUseCurrentLocation != null) {
            btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchCurrentLocation();
                }
            });
        }
        
        if (btnEnterManually != null) {
            btnEnterManually.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showManualLocationDialog();
                }
            });
        }

        // Category buttons
        Button btnCranes = findViewById(R.id.btn_category_cranes);
        Button btnJcbs = findViewById(R.id.btn_category_jcbs);
        Button btnDozers = findViewById(R.id.btn_category_dozers);

        View.OnClickListener openExcavatorsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, ExcavatorsActivity.class);
                if (tvCurrentLocation != null) {
                    intent.putExtra("location", tvCurrentLocation.getText().toString());
                }
                startActivity(intent);
            }
        };

        View.OnClickListener openJcbModelsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, Jcb3dxModelsActivity.class);
                if (tvCurrentLocation != null) {
                    intent.putExtra("location", tvCurrentLocation.getText().toString());
                }
                startActivity(intent);
            }
        };

        if (btnCranes != null) {
            btnCranes.setOnClickListener(openExcavatorsClickListener);
        }

        if (btnJcbs != null) {
            btnJcbs.setOnClickListener(openJcbModelsClickListener);
        }

        View.OnClickListener openDozerModelsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, DozerModelsActivity.class);
                startActivity(intent);
            }
        };

        if (btnDozers != null) {
            btnDozers.setOnClickListener(openDozerModelsClickListener);
        }

        // Initialize section containers
        llHomeContent = findViewById(R.id.ll_home_content);
        llBookContent = findViewById(R.id.ll_book_content);
        etSearch = findViewById(R.id.et_search);
        llCategories = findViewById(R.id.ll_categories);
        tvCategoriesLabel = findViewById(R.id.tv_categories);
        scrollView = findViewById(R.id.sv_dashboard);

        // Bottom navigation setup (after container initialization)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            setupBottomNavigation(bottomNav);
        }

        // Initialize machine category views
        initializeMachineViews();
        
        // Initialize booking views
        initializeBookingViews();

        // Default to home view
        showHomeView();

        // Request location permission and fetch location
        if (checkLocationPermission()) {
            fetchCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // Load machines from backend
        loadMachinesByCategory();
        
        // Load user bookings
        loadUserBookings();

        // Check if we should open the book tab (from deep link or other activities)
        if (getIntent().getBooleanExtra("open_book_tab", false)) {
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_book);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("open_book_tab", false)) {
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_book);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        RealTimeDataManager.getInstance().setSessionManager(sessionManager);
        RealTimeDataManager.getInstance().setUserBookingsListener(this);
        RealTimeDataManager.getInstance().startPolling();
        RealTimeDataManager.getInstance().refreshNow();
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
    
    /**
     * Initialize booking RecyclerViews and adapters
     */
    private void initializeBookingViews() {
        rvPendingBookings = findViewById(R.id.rv_pending_bookings);
        rvCompletedBookings = findViewById(R.id.rv_completed_bookings);
        rvUserBookingsNew = findViewById(R.id.rv_user_bookings_new);
        tvPendingBookingsTitle = findViewById(R.id.tv_pending_bookings_title);
        tvCompletedBookingsTitle = findViewById(R.id.tv_completed_bookings_title);
        tvNoBookings = findViewById(R.id.tv_no_bookings);
        
        tvStatTotal = findViewById(R.id.tv_stat_total_count);
        tvStatPending = findViewById(R.id.tv_stat_pending_count);
        tvStatActive = findViewById(R.id.tv_stat_active_count);

        View btnBack = findViewById(R.id.btn_back_booking);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> showHomeView());
        }
        
        // Setup New RecyclerView
        if (rvUserBookingsNew != null) {
            rvUserBookingsNew.setLayoutManager(new LinearLayoutManager(this));
            userBookingsNewAdapter = new BookingAdapter(java.util.Collections.emptyList(), new BookingAdapter.OnBookingClickListener() {
                @Override
                public void onBookingClick(Booking booking) {
                    Log.d(TAG, "Booking clicked: " + booking.getBookingId());
                }

                @Override
                public void onCancelClick(Booking booking) {
                    showCancelConfirmationDialog(booking);
                }
            });
            rvUserBookingsNew.setAdapter(userBookingsNewAdapter);
        }

        // Keep legacy for compatibility
        if (rvPendingBookings != null) {
            rvPendingBookings.setLayoutManager(new LinearLayoutManager(this));
            pendingBookingsAdapter = new BookingAdapter(java.util.Collections.emptyList(), booking -> {});
            rvPendingBookings.setAdapter(pendingBookingsAdapter);
        }
        
        if (rvCompletedBookings != null) {
            rvCompletedBookings.setLayoutManager(new LinearLayoutManager(this));
            completedBookingsAdapter = new BookingAdapter(java.util.Collections.emptyList(), booking -> {
                // Handle booking click - can navigate to booking details
                Log.d(TAG, "Completed booking clicked: " + booking.getBookingId());
            });
            rvCompletedBookings.setAdapter(completedBookingsAdapter);
        }
    }
    
    /**
     * Load user bookings from backend
     */
    /**
     * Load user bookings from backend
     */
    private void loadUserBookings() {
        String userId = sessionManager.getUserId();
        Log.d(TAG, "Loading bookings for User ID: " + userId);
        
        if (userId == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Booking>>> call = apiService.getUserBookings(userId);
        
        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    Log.d(TAG, "API Response: Success=" + apiResponse.isSuccess() + ", DataSize=" + (apiResponse.getData() != null ? apiResponse.getData().size() : "null"));
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        updateBookingsUI(apiResponse.getData());
                    } else {
                        Log.e(TAG, "Failed to load bookings or empty: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        // Clear UI if empty
                        updateBookingsUI(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "Failed to load bookings: HTTP " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error loading bookings: " + t.getMessage(), t);
            }
        });
    }

    private void updateBookingsUI(List<Booking> allBookings) {
        Log.d(TAG, "Updating UI with " + (allBookings != null ? allBookings.size() : "null") + " bookings");
        if (allBookings == null) return;

        int total = allBookings.size();
        int pending = 0;
        int active = 0;

        for (Booking b : allBookings) {
            String status = b.getStatus();
            if ("PENDING".equalsIgnoreCase(status) || "PENDING approval".equalsIgnoreCase(status)) {
                pending++;
                active++; // Count as active too for the summary view
            } else if ("ACCEPTED".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status)) {
                active++;
            }
        }

        // Update stats UI
        if (tvStatTotal != null) tvStatTotal.setText(String.valueOf(total));
        if (tvStatPending != null) tvStatPending.setText(String.valueOf(pending));
        if (tvStatActive != null) tvStatActive.setText(String.valueOf(active));

        // Create a list of active bookings only
        List<Booking> activeBookings = new ArrayList<>();
        for (Booking b : allBookings) {
            String status = b.getStatus();
            // Filter logic: Exclude finished states
            if (!"COMPLETED".equalsIgnoreCase(status) && 
                !"CANCELLED".equalsIgnoreCase(status) && 
                !"REJECTED".equalsIgnoreCase(status) && 
                !"DECLINED".equalsIgnoreCase(status)) {
                activeBookings.add(b);
            }
        }

        // Update new adapter with ONLY active bookings
        if (userBookingsNewAdapter != null) {
            userBookingsNewAdapter.setOperatorView(false);
            userBookingsNewAdapter.updateList(activeBookings);
        }

        // Handle empty state
        if (tvNoBookings != null) {
            tvNoBookings.setVisibility(allBookings.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (rvUserBookingsNew != null) {
            rvUserBookingsNew.setVisibility(allBookings.isEmpty() ? View.GONE : View.VISIBLE);
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
        // TODO: Implement actual cancel API call if available
        Toast.makeText(this, "Cancelling booking " + booking.getBookingId(), Toast.LENGTH_SHORT).show();
        // For now just refresh
        loadUserBookings();
    }
    
    /**
     * Display pending bookings
     */
    private void displayPendingBookings(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            if (tvPendingBookingsTitle != null) {
                tvPendingBookingsTitle.setVisibility(View.GONE);
            }
            if (rvPendingBookings != null) {
                rvPendingBookings.setVisibility(View.GONE);
            }
        } else {
            if (tvPendingBookingsTitle != null) {
                tvPendingBookingsTitle.setVisibility(View.VISIBLE);
            }
            if (rvPendingBookings != null) {
                rvPendingBookings.setVisibility(View.VISIBLE);
                pendingBookingsAdapter.updateList(bookings);
            }
        }
        updateNoBookingsVisibility();
    }
    
    /**
     * Display completed bookings
     */
    private void displayCompletedBookings(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            if (tvCompletedBookingsTitle != null) {
                tvCompletedBookingsTitle.setVisibility(View.GONE);
            }
            if (rvCompletedBookings != null) {
                rvCompletedBookings.setVisibility(View.GONE);
            }
        } else {
            if (tvCompletedBookingsTitle != null) {
                tvCompletedBookingsTitle.setVisibility(View.VISIBLE);
            }
            if (rvCompletedBookings != null) {
                rvCompletedBookings.setVisibility(View.VISIBLE);
                completedBookingsAdapter.updateList(bookings);
            }
        }
        updateNoBookingsVisibility();
    }
    
    /**
     * Update visibility of "No bookings" message
     */
    private void updateNoBookingsVisibility() {
        boolean hasPending = rvPendingBookings != null && rvPendingBookings.getVisibility() == View.VISIBLE;
        boolean hasCompleted = rvCompletedBookings != null && rvCompletedBookings.getVisibility() == View.VISIBLE;
        
        if (tvNoBookings != null) {
            if (!hasPending && !hasCompleted) {
                tvNoBookings.setVisibility(View.VISIBLE);
            } else {
                tvNoBookings.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Initialize machine category views
     */
    private void initializeMachineViews() {
        // Excavators (category_id = 2)
        ivExcavator = findViewById(R.id.iv_excavator);
        tvExcavatorName = findViewById(R.id.tv_excavator_name);
        tvExcavatorPrice = findViewById(R.id.tv_excavator_price);

        // JCBs (category_id = 1)
        ivJcb = findViewById(R.id.iv_jcb);
        tvJcbName = findViewById(R.id.tv_jcb_name);
        tvJcbPrice = findViewById(R.id.tv_jcb_price);

        // Dozers (category_id = 3)
        ivDozer = findViewById(R.id.iv_dozer);
        tvDozerName = findViewById(R.id.tv_dozer_name);
        tvDozerPrice = findViewById(R.id.tv_dozer_price);

        // Set click listeners for cards
        androidx.cardview.widget.CardView cardExcavators = findViewById(R.id.card_excavators);
        if (cardExcavators != null) {
            cardExcavators.setOnClickListener(v -> {
                Intent intent = new Intent(UserDashboardActivity.this, ExcavatorsActivity.class);
                startActivity(intent);
            });
        }

        androidx.cardview.widget.CardView cardJcbs = findViewById(R.id.card_jcbs);
        if (cardJcbs != null) {
            cardJcbs.setOnClickListener(v -> {
                Intent intent = new Intent(UserDashboardActivity.this, Jcb3dxModelsActivity.class);
                startActivity(intent);
            });
        }

        androidx.cardview.widget.CardView cardDozers = findViewById(R.id.card_dozers);
        if (cardDozers != null) {
            cardDozers.setOnClickListener(v -> {
                Intent intent = new Intent(UserDashboardActivity.this, DozerModelsActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Load machines from backend and display by category
     */
    private void loadMachinesByCategory() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Machine>>> call = apiService.getUserMachines();

        call.enqueue(new Callback<ApiResponse<List<Machine>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Machine>>> call, Response<ApiResponse<List<Machine>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Machine>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Machine> machines = apiResponse.getData();
                        displayMachinesByCategory(machines);
                    } else {
                        Log.e(TAG, "Failed to load machines: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                    }
                } else {
                    Log.e(TAG, "Failed to load machines: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Machine>>> call, Throwable t) {
                Log.e(TAG, "Error loading machines: " + t.getMessage());
            }
        });
    }

    /**
     * Display machines by category
     */
    private void displayMachinesByCategory(List<Machine> machines) {
        Machine excavatorMachine = null;
        Machine jcbMachine = null;
        Machine dozerMachine = null;

        // Find first machine for each category
        for (Machine machine : machines) {
            if (machine.getCategoryId() != null) {
                if (machine.getCategoryId() == 2 && excavatorMachine == null) {
                    excavatorMachine = machine;
                } else if (machine.getCategoryId() == 1 && jcbMachine == null) {
                    jcbMachine = machine;
                } else if (machine.getCategoryId() == 3 && dozerMachine == null) {
                    dozerMachine = machine;
                }
            }
        }

        // Display Excavator (category_id = 2)
        if (excavatorMachine != null) {
            displayMachine(excavatorMachine, ivExcavator, tvExcavatorName, tvExcavatorPrice);
        } else {
            if (tvExcavatorName != null) tvExcavatorName.setText("No Excavators available");
            if (tvExcavatorPrice != null) tvExcavatorPrice.setText("");
        }

        // Display JCB (category_id = 1)
        if (jcbMachine != null) {
            displayMachine(jcbMachine, ivJcb, tvJcbName, tvJcbPrice);
        } else {
            if (tvJcbName != null) tvJcbName.setText("No JCBs available");
            if (tvJcbPrice != null) tvJcbPrice.setText("");
        }

        // Display Dozer (category_id = 3)
        if (dozerMachine != null) {
            displayMachine(dozerMachine, ivDozer, tvDozerName, tvDozerPrice);
        } else {
            if (tvDozerName != null) tvDozerName.setText("No Dozers available");
            if (tvDozerPrice != null) tvDozerPrice.setText("");
        }
    }

    /**
     * Display machine information and image
     */
    private void displayMachine(Machine machine, ImageView imageView, TextView nameView, TextView priceView) {
        if (nameView != null) {
            nameView.setText(machine.getModelName() != null ? machine.getModelName() : "Machine");
        }

        if (priceView != null) {
            String priceText = "₹" + String.format("%.2f", machine.getPricePerHour()) + "/hr";
            priceView.setText(priceText);
        }

        // Load image if available
        String imagePath = machine.getImage();
        if (imageView != null && imagePath != null && !imagePath.isEmpty()) {
            loadMachineImage(imagePath, imageView);
        } else if (imageView != null) {
            // Use default image if no image available
            imageView.setImageResource(R.drawable.jcb3dx_1);
        }
    }

    /**
     * Load machine image from backend URL
     */
    private void loadMachineImage(String imageUrl, ImageView imageView) {
        if (imageView == null || imageUrl == null || imageUrl.isEmpty()) {
            if (imageView != null) {
                imageView.setImageResource(R.drawable.jcb3dx_1);
            }
            return;
        }

        // If imageUrl already contains full URL, use it directly
        // Otherwise construct the full URL
        String fullImageUrl;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            fullImageUrl = imageUrl;
        } else {
            String baseUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl();
            String rootUrl = baseUrl.replace("/api/", "/");
            if (imageUrl.startsWith("/")) {
                imageUrl = imageUrl.substring(1);
            }
            fullImageUrl = rootUrl + imageUrl;
        }

        Log.d(TAG, "Loading machine image from: " + fullImageUrl);

        // Load image in background thread
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(fullImageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
                    input.close();

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (bitmap != null && imageView != null) {
                            imageView.setImageBitmap(bitmap);
                            Log.d(TAG, "Machine image loaded successfully");
                        } else {
                            Log.w(TAG, "Failed to decode bitmap");
                            if (imageView != null) {
                                imageView.setImageResource(R.drawable.jcb3dx_1);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to load image. HTTP response code: " + responseCode);
                    runOnUiThread(() -> {
                        if (imageView != null) {
                            imageView.setImageResource(R.drawable.jcb3dx_1);
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error loading machine image: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (imageView != null) {
                        imageView.setImageResource(R.drawable.jcb3dx_1);
                    }
                });
            }
        }).start();
    }

    // ========== LOCATION METHODS ==========

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                if (tvCurrentLocation != null) tvCurrentLocation.setText("Location permission denied");
                Toast.makeText(this, "Location permission is required to show your location", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        if (tvCurrentLocation != null) tvCurrentLocation.setText("Fetching location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        getAddressFromLocation(currentLatitude, currentLongitude);
                    } else {
                        if (tvCurrentLocation != null) tvCurrentLocation.setText("Location not available");
                        Toast.makeText(this, "Unable to get location. Please enable GPS.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error getting location: " + e.getMessage());
                    if (tvCurrentLocation != null) tvCurrentLocation.setText("Error fetching location");
                    Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();
                
                // Build address string
                if (address.getAddressLine(0) != null) {
                    addressString.append(address.getAddressLine(0));
                } else {
                    if (address.getLocality() != null) {
                        addressString.append(address.getLocality());
                    }
                    if (address.getAdminArea() != null) {
                        if (addressString.length() > 0) addressString.append(", ");
                        addressString.append(address.getAdminArea());
                    }
                }
                
                final String finalAddress = addressString.toString();
                if (finalAddress.length() > 0) {
                    if (tvCurrentLocation != null) tvCurrentLocation.setText(finalAddress);
                    // Update backend with lat/long
                    updateUserLocation(finalAddress, latitude, longitude);
                } else {
                    String coordAddr = String.format(Locale.getDefault(), "%.4f, %.4f", latitude, longitude);
                    if (tvCurrentLocation != null) tvCurrentLocation.setText(coordAddr);
                    updateUserLocation(coordAddr, latitude, longitude);
                }
            } else {
                String coordAddr = String.format(Locale.getDefault(), "%.4f, %.4f", latitude, longitude);
                if (tvCurrentLocation != null) tvCurrentLocation.setText(coordAddr);
                updateUserLocation(coordAddr, latitude, longitude);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
            String coordAddr = String.format(Locale.getDefault(), "%.4f, %.4f", latitude, longitude);
            if (tvCurrentLocation != null) tvCurrentLocation.setText(coordAddr);
            updateUserLocation(coordAddr, latitude, longitude);
        }
    }

    private void showManualLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_manual_location, null); 
        
        final EditText input = dialogView.findViewById(R.id.et_location_search);
        final RecyclerView rvSuggestions = dialogView.findViewById(R.id.rv_location_suggestions);
        final View btnUseCurrentConfig = dialogView.findViewById(R.id.btn_dialog_current_location);
        final View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Suggestions Adapter
        LocationSuggestionAdapter adapter = new LocationSuggestionAdapter(new ArrayList<>(), result -> {
            // On Click
            String address = result.getDisplayName();
            double lat = Double.parseDouble(result.getLat());
            double lon = Double.parseDouble(result.getLon());
            
            if (tvCurrentLocation != null) tvCurrentLocation.setText(address);
            updateUserLocation(address, lat, lon);
            dialog.dismiss();
        });
        rvSuggestions.setAdapter(adapter);

        // "Use Current Location" button inside dialog
        if (btnUseCurrentConfig != null) {
            btnUseCurrentConfig.setOnClickListener(v -> {
                dialog.dismiss();
                fetchCurrentLocation();
            });
        }

        // Cancel button
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchLocations(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Background transparent for custom rounded corners if needed
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        dialog.show();
    }

    private void searchLocations(String query, LocationSuggestionAdapter adapter) {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("User-Agent", "EathmoverApp/1.0 (com.simats.eathmover)")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NominatimApiService service = retrofit.create(NominatimApiService.class);
        Call<List<NominatimResult>> call = service.searchPoints(query, "json", 1, 5);
        call.enqueue(new Callback<List<NominatimResult>>() {
            @Override
            public void onResponse(Call<List<NominatimResult>> call, Response<List<NominatimResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateSuggestions(response.body());
                } else {
                    Log.e(TAG, "Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NominatimResult>> call, Throwable t) {
                Log.e(TAG, "Search failed: " + t.getMessage());
            }
        });
    }

    private void updateUserLocation(String location) {
        updateUserLocation(location, 0.0, 0.0);
    }
    
    private void updateUserLocation(String location, double latitude, double longitude) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;
        
        Log.d(TAG, "Updating location: " + location + " (" + latitude + ", " + longitude + ")");

        User user = new User();
        user.setUserId(Integer.parseInt(userId));
        user.setLocation(location);
        if (latitude != 0.0) user.setLatitude(latitude);
        if (longitude != 0.0) user.setLongitude(longitude);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.updateUserProfile(user);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Log.d(TAG, "Location updated successfully");
                        Toast.makeText(UserDashboardActivity.this, "Location updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to update location: " + response.body().getMessage());
                    }
                } else {
                    Log.e(TAG, "Failed to update location: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.e(TAG, "Error updating location: " + t.getMessage());
            }
        });
    }

    /**
     * Setup bottom navigation specifically for this dashboard
     */
    private void setupBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                showHomeView();
                return true;
            } else if (itemId == R.id.navigation_book) {
                showBookView();
                return true;
            } else if (itemId == R.id.navigation_history) {
                Intent intent = new Intent(this, ServiceHistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void showHomeView() {
        if (llHomeContent != null) llHomeContent.setVisibility(View.VISIBLE);
        if (llBookContent != null) llBookContent.setVisibility(View.GONE);
        if (etSearch != null) etSearch.setVisibility(View.VISIBLE);
        if (llCategories != null) llCategories.setVisibility(View.VISIBLE);
        if (tvCategoriesLabel != null) tvCategoriesLabel.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.smoothScrollTo(0, 0);
    }

    private void showBookView() {
        if (llHomeContent != null) llHomeContent.setVisibility(View.GONE);
        if (llBookContent != null) llBookContent.setVisibility(View.VISIBLE);
        if (etSearch != null) etSearch.setVisibility(View.GONE); 
        if (llCategories != null) llCategories.setVisibility(View.GONE);
        if (tvCategoriesLabel != null) tvCategoriesLabel.setVisibility(View.GONE);
        if (scrollView != null) scrollView.smoothScrollTo(0, 0);
        
        // Refresh bookings when switching to this tab
        loadUserBookings();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            performLogout();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    /**
     * Perform logout - clear session and navigate to login
     */
    private void performLogout() {
        // Clear session
        sessionManager.logout();
        
        // Navigate to user login
        Intent intent = new Intent(UserDashboardActivity.this, UserLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
