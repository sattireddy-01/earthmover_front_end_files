package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.Operator;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

/**
 * Activity for setting operator availability status (ONLINE/OFFLINE).
 */
public class SetAvailabilityActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private MaterialButtonToggleGroup toggleAvailabilityStatus;
    private MaterialButton btnOnline;
    private MaterialButton btnOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);

        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_set_availability);
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
        toggleAvailabilityStatus = findViewById(R.id.toggle_availability_status);
        btnOnline = findViewById(R.id.btn_online);
        btnOffline = findViewById(R.id.btn_offline);

        // Setup availability status toggle
        if (toggleAvailabilityStatus != null) {
            toggleAvailabilityStatus.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                    if (isChecked) {
                        if (checkedId == R.id.btn_online) {
                            updateAvailabilityStatus(true);
                            // Update button colors
                            if (btnOnline != null) btnOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                            if (btnOffline != null) btnOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF3C3C3C));
                        } else if (checkedId == R.id.btn_offline) {
                            updateAvailabilityStatus(false);
                            // Update button colors
                            if (btnOffline != null) btnOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF44336));
                            if (btnOnline != null) btnOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF3C3C3C));
                        }
                    }
                }
            });
        }

        // Load current availability settings
        loadAvailabilitySettings();

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_set_availability);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
        }
    }

    private void loadAvailabilitySettings() {
        // Load current availability from API
        String operatorId = sessionManager.getOperatorId();
        if (operatorId == null) {
            // Default to offline if operator ID not found
            if (toggleAvailabilityStatus != null) {
                toggleAvailabilityStatus.check(R.id.btn_offline);
            }
            return;
        }

        // TODO: Load actual availability from API
        // For now, default to offline
        if (toggleAvailabilityStatus != null) {
            toggleAvailabilityStatus.check(R.id.btn_offline);
        }
    }

    private void updateAvailabilityStatus(boolean isOnline) {
        String operatorId = sessionManager.getOperatorId();
        if (operatorId == null) {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use uppercase to match database enum (ONLINE/OFFLINE)
        String status = isOnline ? "ONLINE" : "OFFLINE";
        
        Operator operator = new Operator();
        operator.setOperatorId(operatorId);
        operator.setStatus(status);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.updateOperatorStatus(operator);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Availability updated successfully";
                        Toast.makeText(SetAvailabilityActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update availability";
                        Toast.makeText(SetAvailabilityActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Failed to update availability. Error code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("SetAvailability", "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(SetAvailabilityActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("SetAvailability", "Failed to update status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.e("SetAvailability", "Error updating status: " + t.getMessage());
                Toast.makeText(SetAvailabilityActivity.this, "Error updating availability: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
                Intent intent = new Intent(this, NewBookingRequestActivity.class);
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
}

