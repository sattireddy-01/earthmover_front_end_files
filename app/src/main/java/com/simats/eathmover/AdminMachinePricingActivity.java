package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMachinePricingActivity extends AppCompatActivity {

    private static final String TAG = "AdminMachinePricing";
    private ProgressBar progressBar;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_machine_pricing);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_machine_pricing);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Back button
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }

        progressBar = findViewById(R.id.progress_bar);

        // Search functionality
        EditText etSearch = findViewById(R.id.et_search_machine);
        // TODO: Implement search functionality with filter

        // Load machines from API
        loadMachines();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadMachines() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<Machine>> call = apiService.getMachines();

        call.enqueue(new Callback<ApiResponse<Machine>>() {
            @Override
            public void onResponse(Call<ApiResponse<Machine>> call, Response<ApiResponse<Machine>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Machine> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<Machine> machines = apiResponse.getDataList();
                        if (machines != null && !machines.isEmpty()) {
                            populateMachines(machines);
                        } else {
                            setupDefaultMachineButtons();
                        }
                    } else {
                        setupDefaultMachineButtons();
                        Toast.makeText(AdminMachinePricingActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "No machines found",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setupDefaultMachineButtons();
                    Log.e(TAG, "Failed to load machines: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Machine>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                setupDefaultMachineButtons();
                Log.e(TAG, "Error loading machines: " + t.getMessage());
            }
        });
    }

    private void populateMachines(List<Machine> machines) {
        // Update machine cards with real data from database
        // Show/hide cards based on available machines (layout has 5 cards)
        for (int i = 0; i < 5; i++) {
            if (i < machines.size()) {
                showMachineCard(i + 1);
                updateMachineCard(i + 1, machines.get(i));
            } else {
                hideMachineCard(i + 1);
            }
        }
    }
    
    private void showMachineCard(int cardNumber) {
        TextView tvModel = findViewById(getModelId(cardNumber));
        if (tvModel != null) {
            View parent = (View) tvModel.getParent();
            if (parent != null) {
                View cardView = (View) parent.getParent();
                if (cardView != null) {
                    cardView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    private void hideMachineCard(int cardNumber) {
        TextView tvModel = findViewById(getModelId(cardNumber));
        if (tvModel != null) {
            View parent = (View) tvModel.getParent();
            if (parent != null) {
                View cardView = (View) parent.getParent();
                if (cardView != null) {
                    cardView.setVisibility(View.GONE);
                }
            }
        }
    }
    
    private int getModelId(int cardNumber) {
        switch (cardNumber) {
            case 1: return R.id.tv_machine_1_model;
            case 2: return R.id.tv_machine_2_model;
            case 3: return R.id.tv_machine_3_model;
            case 4: return R.id.tv_machine_4_model;
            case 5: return R.id.tv_machine_5_model;
            default: return R.id.tv_machine_1_model;
        }
    }

    private void updateMachineCard(int cardNumber, Machine machine) {
        int modelId, priceId, updatedId, btnId;
        
        switch (cardNumber) {
            case 1:
                modelId = R.id.tv_machine_1_model;
                priceId = R.id.tv_machine_1_price;
                updatedId = R.id.tv_machine_1_updated;
                btnId = R.id.btn_edit_jcb;
                break;
            case 2:
                modelId = R.id.tv_machine_2_model;
                priceId = R.id.tv_machine_2_price;
                updatedId = R.id.tv_machine_2_updated;
                btnId = R.id.btn_edit_caterpillar;
                break;
            case 3:
                modelId = R.id.tv_machine_3_model;
                priceId = R.id.tv_machine_3_price;
                updatedId = R.id.tv_machine_3_updated;
                btnId = R.id.btn_edit_komatsu;
                break;
            case 4:
                modelId = R.id.tv_machine_4_model;
                priceId = R.id.tv_machine_4_price;
                updatedId = R.id.tv_machine_4_updated;
                btnId = R.id.btn_edit_john_deere_1;
                break;
            case 5:
                modelId = R.id.tv_machine_5_model;
                priceId = R.id.tv_machine_5_price;
                updatedId = R.id.tv_machine_5_updated;
                btnId = R.id.btn_edit_john_deere_2;
                break;
            default:
                return;
        }

        TextView tvModel = findViewById(modelId);
        TextView tvPrice = findViewById(priceId);
        TextView tvUpdated = findViewById(updatedId);
        Button btnEdit = findViewById(btnId);

        if (tvModel != null) {
            String modelName = machine.getModel() != null ? machine.getModel() : "N/A";
            tvModel.setText("Model: " + modelName);
        }
        if (tvPrice != null) {
            double price = machine.getPricePerHour();
            // Format price in Indian Rupees (₹) or use currency format
            String priceText = "₹" + String.format("%.2f", price) + "/hr";
            tvPrice.setText("Current Price: " + priceText);
        }
        if (tvUpdated != null) {
            String updated = machine.getLastUpdated() != null && !machine.getLastUpdated().isEmpty() 
                ? machine.getLastUpdated() : "Not updated";
            tvUpdated.setText("Last Updated: " + updated);
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditDialog(machine));
        }
    }

    private void showEditDialog(Machine machine) {
        // Create a dialog for editing machine price
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Edit Machine Pricing");
        
        // Create EditText for price input
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter price per hour");
        input.setText(String.valueOf(machine.getPricePerHour()));
        input.setPadding(50, 20, 50, 20);
        
        builder.setView(input);
        
        builder.setPositiveButton("Update", (dialog, which) -> {
            String priceText = input.getText().toString().trim();
            if (priceText.isEmpty()) {
                Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double newPrice = Double.parseDouble(priceText);
                if (newPrice < 0) {
                    Toast.makeText(this, "Price cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Update machine price
                updateMachinePrice(machine, newPrice);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void updateMachinePrice(Machine machine, double newPrice) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        // Update the machine object with new price
        machine.setPricePerHour(newPrice);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<com.simats.eathmover.models.GenericResponse> call = apiService.updateMachinePricing(machine);
        
        call.enqueue(new Callback<com.simats.eathmover.models.GenericResponse>() {
            @Override
            public void onResponse(Call<com.simats.eathmover.models.GenericResponse> call, Response<com.simats.eathmover.models.GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    com.simats.eathmover.models.GenericResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AdminMachinePricingActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Price updated successfully",
                            Toast.LENGTH_SHORT).show();
                        // Reload machines to show updated price
                        loadMachines();
                    } else {
                        Toast.makeText(AdminMachinePricingActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update price",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminMachinePricingActivity.this, "Failed to update price", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update machine price: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<com.simats.eathmover.models.GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminMachinePricingActivity.this, "Error updating price", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating machine price: " + t.getMessage());
            }
        });
    }

    private void setupDefaultMachineButtons() {
        // Hide all machine cards when no data is available
        hideMachineCard(1);
        hideMachineCard(2);
        hideMachineCard(3);
        hideMachineCard(4);
        hideMachineCard(5);
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
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
