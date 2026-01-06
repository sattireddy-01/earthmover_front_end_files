package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for auto price estimation showing selected machine and work duration.
 */
public class AutoPriceEstimatorActivity extends AppCompatActivity {

    private static final String TAG = "AutoPriceEstimator";
    private ImageView ivHeaderMachineImage;
    private ImageView ivSelectedMachineThumbnail;
    private ImageView ivWorkDurationImage;
    private TextView tvSelectedMachineName;
    private TextView tvWorkDuration;
    private int machineId;
    private String machineImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_price_estimator);

        Toolbar toolbar = findViewById(R.id.toolbar_price_estimator);
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
        ivHeaderMachineImage = findViewById(R.id.iv_header_machine_image);
        ivSelectedMachineThumbnail = findViewById(R.id.iv_selected_machine_thumbnail);
        ivWorkDurationImage = findViewById(R.id.iv_work_duration_image);
        tvSelectedMachineName = findViewById(R.id.tv_selected_machine_name);
        tvWorkDuration = findViewById(R.id.tv_work_duration);

        // Get data from previous activity
        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        machineId = intent.getIntExtra("machine_id", 0);
        String machineModel = intent.getStringExtra("machine_model");
        String machineType = intent.getStringExtra("machine_type");
        String machineImage = intent.getStringExtra("machine_image");

        // Display work duration from time
        if (time != null && !time.isEmpty()) {
            // Parse time (HH:mm format) and display as duration
            String duration = formatTimeAsDuration(time);
            if (tvWorkDuration != null) {
                tvWorkDuration.setText(duration);
            }
        } else {
            if (tvWorkDuration != null) {
                tvWorkDuration.setText("Not Set");
            }
        }

        // Display machine data from intent (quick display)
        if (machineModel != null || machineImage != null) {
            displayMachineFromIntent(machineModel, machineImage);
        }

        // Load full machine details from API if machine_id is available
        if (machineId > 0) {
            loadMachineDetails(machineId);
        }

        // Change Machine button - Navigate to JCB Models page
        Button btnChangeMachine = findViewById(R.id.btn_change_machine);
        if (btnChangeMachine != null) {
            btnChangeMachine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to JCB Models page
                    Intent intent = new Intent(AutoPriceEstimatorActivity.this, Jcb3dxModelsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Change Duration button
        Button btnChangeDuration = findViewById(R.id.btn_change_duration);
        if (btnChangeDuration != null) {
            btnChangeDuration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate back to duration entry with machine data
                    Intent backIntent = new Intent(AutoPriceEstimatorActivity.this, EnterWorkDurationActivity.class);
                    if (machineId > 0) {
                        backIntent.putExtra("machine_id", machineId);
                    }
                    if (machineImageUrl != null) {
                        backIntent.putExtra("machine_image", machineImageUrl);
                    }
                    startActivity(backIntent);
                    finish();
                }
            });
        }

        // Continue button
        Button btnContinue = findViewById(R.id.btn_continue_estimator);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent nextIntent = new Intent(AutoPriceEstimatorActivity.this, BookingSummaryActivity.class);
                    nextIntent.putExtra("date", date);
                    nextIntent.putExtra("time", time);
                    String durationText = tvWorkDuration != null ? tvWorkDuration.getText().toString() : "Not Set";
                    nextIntent.putExtra("duration", durationText);
                    // Pass machine data
                    if (machineId > 0) {
                        nextIntent.putExtra("machine_id", machineId);
                    }
                    Intent currentIntent = getIntent();
                    String machineType = currentIntent.getStringExtra("machine_type");
                    String machineModel = currentIntent.getStringExtra("machine_model");
                    String location = currentIntent.getStringExtra("location");
                    if (machineType != null) {
                        nextIntent.putExtra("machine_type", machineType);
                    }
                    if (machineModel != null) {
                        nextIntent.putExtra("machine_model", machineModel);
                    }
                    if (location != null) {
                        nextIntent.putExtra("location", location);
                    }
                    startActivity(nextIntent);
                }
            });
        }
    }

    /**
     * Format time (HH:mm) as duration string
     */
    private String formatTimeAsDuration(String time) {
        try {
            // Parse HH:mm format
            String[] parts = time.split(":");
            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                
                if (hours > 0) {
                    if (minutes > 0) {
                        return hours + " Hour" + (hours > 1 ? "s" : "") + " " + minutes + " Min";
                    } else {
                        return hours + " Hour" + (hours > 1 ? "s" : "");
                    }
                } else if (minutes > 0) {
                    return minutes + " Min";
                } else {
                    return "0 Hours";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time: " + e.getMessage());
        }
        return time; // Return original if parsing fails
    }

    /**
     * Display machine details from intent (quick display before API loads)
     */
    private void displayMachineFromIntent(String model, String image) {
        if (tvSelectedMachineName != null && model != null && !model.isEmpty()) {
            tvSelectedMachineName.setText(model);
        }

        // Load images if provided
        if (image != null && !image.isEmpty()) {
            machineImageUrl = image;
            loadMachineImage(image);
        }
    }

    /**
     * Load machine image to all image views
     */
    private void loadMachineImage(String imageUrl) {
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

        // Load to header image
        if (ivHeaderMachineImage != null) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.jcb3dx_1)
                    .error(R.drawable.jcb3dx_1)
                    .into(ivHeaderMachineImage);
        }

        // Load to selected machine thumbnail
        if (ivSelectedMachineThumbnail != null) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.jcb3dx_1)
                    .error(R.drawable.jcb3dx_1)
                    .into(ivSelectedMachineThumbnail);
        }

        // Load to work duration image (same machine)
        if (ivWorkDurationImage != null) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.earthmover_featured_1)
                    .error(R.drawable.earthmover_featured_1)
                    .into(ivWorkDurationImage);
        }
    }

    /**
     * Load machine details from backend API
     */
    private void loadMachineDetails(int machineId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<Machine>> call = apiService.getMachineDetails(machineId);

        call.enqueue(new Callback<ApiResponse<Machine>>() {
            @Override
            public void onResponse(Call<ApiResponse<Machine>> call, Response<ApiResponse<Machine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Machine> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Machine machine = apiResponse.getData();
                        displayMachineDetails(machine);
                    } else {
                        Log.e(TAG, "Failed to load machine details: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                    }
                } else {
                    Log.e(TAG, "Failed to load machine details: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Machine>> call, Throwable t) {
                Log.e(TAG, "Error loading machine details: " + t.getMessage());
            }
        });
    }

    /**
     * Display machine details from API response
     */
    private void displayMachineDetails(Machine machine) {
        // Display machine name
        if (tvSelectedMachineName != null) {
            String modelName = machine.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                tvSelectedMachineName.setText(modelName);
            } else {
                tvSelectedMachineName.setText("Machine");
            }
        }

        // Load machine image
        String imageUrl = machine.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            machineImageUrl = imageUrl;
            loadMachineImage(imageUrl);
        }
    }
}






