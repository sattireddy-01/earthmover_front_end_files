package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows detailed information about a machine including specifications and images.
 */
public class MachineDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MachineDetailsActivity";
    private ImageView ivMachineMainImage;
    private TextView tvMachineName;
    private TextView tvMachineYear;
    private TextView tvMachineAvailability;
    private TextView tvSpecModel;
    private TextView tvSpecYear;
    private TextView tvSpecType;
    private TextView tvSpecSpecs;
    private TextView tvSpecPrice;
    private TextView tvSpecLocation;
    private ProgressBar progressBar;
    private int machineId;
    private Machine currentMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_details);

        Toolbar toolbar = findViewById(R.id.toolbar_machine_details);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Initialize views
        initializeViews();

        // Get machine_id from intent
        Intent intent = getIntent();
        machineId = intent.getIntExtra("machine_id", 0);
        
        // If machine_id is 0, try to get from passed Machine object data
        if (machineId == 0) {
            String machineModel = intent.getStringExtra("machine_model");
            String machineImage = intent.getStringExtra("machine_image");
            double machinePrice = intent.getDoubleExtra("machine_price", 0);
            int machineYear = intent.getIntExtra("machine_year", 0);
            String specs = intent.getStringExtra("specs");
            String machineType = intent.getStringExtra("machine_type");
            
            // Create a temporary machine object from intent data for passing to next activity
            if (machineModel != null) {
                currentMachine = new Machine();
                currentMachine.setModelName(machineModel);
                currentMachine.setImage(machineImage);
                currentMachine.setPricePerHour(machinePrice);
                currentMachine.setModelYear(machineYear);
                currentMachine.setSpecs(specs);
                currentMachine.setEquipmentType(machineType);
                
                displayMachineFromIntent(machineModel, machineImage, machinePrice, machineYear, specs);
            }
        }

        // Load machine details from API
        if (machineId > 0) {
            loadMachineDetails(machineId);
        } else {
            // Try to load from passed data or show error
            Toast.makeText(this, "Machine ID not found", Toast.LENGTH_SHORT).show();
        }

        // Book Now button
        Button btnBookNow = findViewById(R.id.btn_book_now);
        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent bookingIntent = new Intent(MachineDetailsActivity.this, EnterWorkDurationActivity.class);
                    if (machineId > 0) {
                        bookingIntent.putExtra("machine_id", machineId);
                    }
                    // Pass machine data for quick display
                    if (currentMachine != null) {
                        bookingIntent.putExtra("machine_model", currentMachine.getModelName());
                        bookingIntent.putExtra("machine_type", currentMachine.getEquipmentType());
                        bookingIntent.putExtra("machine_image", currentMachine.getImage());
                    }
                    
                    // Pass location if available
                    if (getIntent().hasExtra("location")) {
                        bookingIntent.putExtra("location", getIntent().getStringExtra("location"));
                    }
                    
                    startActivity(bookingIntent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_machine_details);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    private void initializeViews() {
        ivMachineMainImage = findViewById(R.id.iv_machine_main_image);
        tvMachineName = findViewById(R.id.tv_machine_name);
        tvMachineYear = findViewById(R.id.tv_machine_year);
        tvMachineAvailability = findViewById(R.id.tv_machine_availability);
        tvSpecModel = findViewById(R.id.tv_spec_model);
        tvSpecYear = findViewById(R.id.tv_spec_year);
        tvSpecType = findViewById(R.id.tv_spec_type);
        tvSpecSpecs = findViewById(R.id.tv_spec_specs);
        tvSpecPrice = findViewById(R.id.tv_spec_price);
        tvSpecLocation = findViewById(R.id.tv_spec_location);
        progressBar = findViewById(R.id.progress_bar_machine_details);
    }

    /**
     * Load machine details from backend API
     */
    private void loadMachineDetails(int machineId) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<Machine>> call = apiService.getMachineDetails(machineId);

        call.enqueue(new Callback<ApiResponse<Machine>>() {
            @Override
            public void onResponse(Call<ApiResponse<Machine>> call, Response<ApiResponse<Machine>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Machine> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Machine machine = apiResponse.getData();
                        currentMachine = machine; // Store machine object
                        displayMachineDetails(machine);
                    } else {
                        Log.e(TAG, "Failed to load machine details: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        Toast.makeText(MachineDetailsActivity.this, "Failed to load machine details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load machine details: HTTP " + response.code());
                    Toast.makeText(MachineDetailsActivity.this, "Failed to load machine details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Machine>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Error loading machine details: " + t.getMessage());
                Toast.makeText(MachineDetailsActivity.this, "Error loading machine details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display machine details from API response
     */
    private void displayMachineDetails(Machine machine) {
        // Display machine name
        if (tvMachineName != null) {
            String modelName = machine.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                tvMachineName.setText(modelName);
            } else {
                tvMachineName.setText("Machine");
            }
        }

        // Display machine year
        if (tvMachineYear != null) {
            Integer year = machine.getModelYear();
            if (year != null && year > 0) {
                tvMachineYear.setText(year + " Model");
            } else {
                tvMachineYear.setText("Model");
            }
        }

        // Display availability
        if (tvMachineAvailability != null) {
            String availability = machine.getAvailability();
            if (availability != null && !availability.isEmpty()) {
                if (availability.equalsIgnoreCase("ONLINE")) {
                    tvMachineAvailability.setText("Available");
                    tvMachineAvailability.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    tvMachineAvailability.setText("Unavailable");
                    tvMachineAvailability.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }
            } else {
                tvMachineAvailability.setText("Status Unknown");
            }
        }

        // Display specifications
        if (tvSpecModel != null) {
            String model = machine.getModelName();
            tvSpecModel.setText(model != null && !model.isEmpty() ? model : "N/A");
        }

        if (tvSpecYear != null) {
            Integer year = machine.getModelYear();
            tvSpecYear.setText(year != null && year > 0 ? String.valueOf(year) : "N/A");
        }

        if (tvSpecType != null) {
            String equipmentType = machine.getEquipmentType();
            tvSpecType.setText(equipmentType != null && !equipmentType.isEmpty() ? equipmentType : "N/A");
        }

        if (tvSpecSpecs != null) {
            String specs = machine.getSpecs();
            tvSpecSpecs.setText(specs != null && !specs.isEmpty() ? specs : "N/A");
        }

        if (tvSpecPrice != null) {
            double price = machine.getPricePerHour();
            tvSpecPrice.setText("₹" + String.format(Locale.getDefault(), "%.0f", price) + "/hr");
        }

        if (tvSpecLocation != null) {
            String address = machine.getAddress();
            tvSpecLocation.setText(address != null && !address.isEmpty() ? address : "Location not specified");
        }

        // Load main image
        if (ivMachineMainImage != null) {
            String imageUrl = machine.getImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // If URL doesn't start with http, construct full URL
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    String baseUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl();
                    String rootUrl = baseUrl.replace("/api/", "/");
                    if (imageUrl.startsWith("/")) {
                        imageUrl = imageUrl.substring(1);
                    }
                    imageUrl = rootUrl + imageUrl;
                }
                
                Log.d(TAG, "Loading machine image: " + imageUrl);
                
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.jcb3dx_1)
                        .error(R.drawable.jcb3dx_1)
                        .into(ivMachineMainImage);
            } else {
                ivMachineMainImage.setImageResource(R.drawable.jcb3dx_1);
            }
        }
    }

    /**
     * Display machine details from intent (quick display before API loads)
     */
    private void displayMachineFromIntent(String model, String image, double price, int year, String specs) {
        if (tvMachineName != null && model != null) {
            tvMachineName.setText(model);
        }
        
        if (tvMachineYear != null && year > 0) {
            tvMachineYear.setText(year + " Model");
        }
        
        if (tvSpecModel != null && model != null) {
            tvSpecModel.setText(model);
        }
        
        if (tvSpecYear != null && year > 0) {
            tvSpecYear.setText(String.valueOf(year));
        }
        
        if (tvSpecPrice != null) {
            tvSpecPrice.setText("₹" + String.format(Locale.getDefault(), "%.0f", price) + "/hr");
        }
        
        if (tvSpecSpecs != null && specs != null) {
            tvSpecSpecs.setText(specs);
        }
        
        // Load image if provided
        if (ivMachineMainImage != null && image != null && !image.isEmpty()) {
            if (!image.startsWith("http://") && !image.startsWith("https://")) {
                String baseUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl();
                String rootUrl = baseUrl.replace("/api/", "/");
                if (image.startsWith("/")) {
                    image = image.substring(1);
                }
                image = rootUrl + image;
            }
            
            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.jcb3dx_1)
                    .error(R.drawable.jcb3dx_1)
                    .into(ivMachineMainImage);
        }
    }
}

