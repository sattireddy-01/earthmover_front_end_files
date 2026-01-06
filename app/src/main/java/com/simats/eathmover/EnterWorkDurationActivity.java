package com.simats.eathmover;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for entering work duration (date and time).
 * Date uses a calendar picker; time uses a custom dialog with hour/minute pickers.
 */
public class EnterWorkDurationActivity extends AppCompatActivity {

    private static final String TAG = "EnterWorkDuration";
    private final Calendar selectedCalendar = Calendar.getInstance();
    private TextView tvSelectedMachineName;
    private TextView tvSelectedMachineType;
    private ImageView ivSelectedMachineImage;
    private int machineId;
    private String currentMachineImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_work_duration);

        Toolbar toolbar = findViewById(R.id.toolbar_work_duration);
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

        // Initialize machine display views
        tvSelectedMachineName = findViewById(R.id.tv_selected_machine_name);
        tvSelectedMachineType = findViewById(R.id.tv_selected_machine_type);
        ivSelectedMachineImage = findViewById(R.id.iv_selected_machine_image);

        // Get machine data from intent
        Intent intent = getIntent();
        machineId = intent.getIntExtra("machine_id", 0);
        String machineModel = intent.getStringExtra("machine_model");
        String machineType = intent.getStringExtra("machine_type");
        String machineImage = intent.getStringExtra("machine_image");

        // Display machine data from intent (quick display)
        if (machineModel != null || machineType != null || machineImage != null) {
            displayMachineFromIntent(machineModel, machineType, machineImage);
        }

        // Load full machine details from API if machine_id is available
        if (machineId > 0) {
            loadMachineDetails(machineId);
        }

        EditText etDate = findViewById(R.id.et_date);
        EditText etTime = findViewById(R.id.et_time);

        if (etDate == null || etTime == null) {
            Toast.makeText(this, "Error initializing form fields", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable manual keyboard input; user will select using pickers
        etDate.setInputType(InputType.TYPE_NULL);
        etDate.setFocusable(false);
        etDate.setClickable(true);

        etTime.setInputType(InputType.TYPE_NULL);
        etTime.setFocusable(false);
        etTime.setClickable(true);

        // Date picker (calendar)
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = selectedCalendar.get(Calendar.YEAR);
                int month = selectedCalendar.get(Calendar.MONTH);
                int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EnterWorkDurationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                selectedCalendar.set(Calendar.YEAR, year);
                                selectedCalendar.set(Calendar.MONTH, month);
                                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                etDate.setText(dateFormat.format(selectedCalendar.getTime()));
                            }
                        },
                        year,
                        month,
                        day
                );

                datePickerDialog.show();
            }
        });

        // Time picker using custom dialog with hour and minute NumberPickers
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTimePicker(etTime);
            }
        });

        Button btnContinue = findViewById(R.id.btn_continue_duration);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String date = etDate.getText().toString().trim();
                    String time = etTime.getText().toString().trim();

                    if (date.isEmpty() || time.isEmpty()) {
                        Toast.makeText(EnterWorkDurationActivity.this,
                                "Please select both date and time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(EnterWorkDurationActivity.this, AutoPriceEstimatorActivity.class);
                    intent.putExtra("date", date);
                    intent.putExtra("time", time);
                    if (machineId > 0) {
                        intent.putExtra("machine_id", machineId);
                    }
                    // Pass machine data for display
                    if (tvSelectedMachineName != null) {
                        String machineModel = tvSelectedMachineName.getText().toString();
                        if (machineModel != null && !machineModel.isEmpty() && !machineModel.equals("Machine Model")) {
                            intent.putExtra("machine_model", machineModel);
                        }
                    }
                    if (tvSelectedMachineType != null) {
                        String machineType = tvSelectedMachineType.getText().toString();
                        if (machineType != null && !machineType.isEmpty() && !machineType.equals("Machine Type")) {
                            intent.putExtra("machine_type", machineType);
                        }
                    }
                    // Pass machine image URL if available
                    if (currentMachineImageUrl != null && !currentMachineImageUrl.isEmpty()) {
                        intent.putExtra("machine_image", currentMachineImageUrl);
                    }
                    
                    // Pass location if available
                    if (getIntent().hasExtra("location")) {
                        intent.putExtra("location", getIntent().getStringExtra("location"));
                    }
                    
                    startActivity(intent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_work_duration);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    /**
     * Shows a custom dialog with separate hour and minute pickers.
     */
    private void showCustomTimePicker(EditText etTime) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_time_picker, null);

        NumberPicker npHours = dialogView.findViewById(R.id.np_hours);
        NumberPicker npMinutes = dialogView.findViewById(R.id.np_minutes);

        // Configure hours (0–23)
        npHours.setMinValue(0);
        npHours.setMaxValue(23);
        npHours.setValue(selectedCalendar.get(Calendar.HOUR_OF_DAY));

        // Configure minutes (0–59)
        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(59);
        npMinutes.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.getDefault(), "%02d", value);
            }
        });
        npMinutes.setValue(selectedCalendar.get(Calendar.MINUTE));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Time");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            int hour = npHours.getValue();
            int minute = npMinutes.getValue();

            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectedCalendar.set(Calendar.MINUTE, minute);

            // 24-hour format HH:mm
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etTime.setText(timeFormat.format(selectedCalendar.getTime()));
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set text color after dialog is shown (when views are fully initialized)
        dialogView.post(new Runnable() {
            @Override
            public void run() {
                setNumberPickerTextColor(npHours, Color.WHITE);
                setNumberPickerTextColor(npMinutes, Color.WHITE);
            }
        });
    }

    /**
     * Helper method to set text color of NumberPicker digits to white.
     */
    private void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) child;
                for (int j = 0; j < viewGroup.getChildCount(); j++) {
                    View childView = viewGroup.getChildAt(j);
                    if (childView instanceof TextView) {
                        ((TextView) childView).setTextColor(color);
                    }
                }
            }
        }
    }

    /**
     * Display machine details from intent (quick display before API loads)
     */
    private void displayMachineFromIntent(String model, String type, String image) {
        if (tvSelectedMachineName != null && model != null && !model.isEmpty()) {
            tvSelectedMachineName.setText(model);
        }

        if (tvSelectedMachineType != null && type != null && !type.isEmpty()) {
            tvSelectedMachineType.setText(type);
        } else if (tvSelectedMachineType != null) {
            tvSelectedMachineType.setText("Machine");
        }

        // Load image if provided
        if (ivSelectedMachineImage != null && image != null && !image.isEmpty()) {
            currentMachineImageUrl = image; // Store original image URL
            // If URL doesn't start with http, construct full URL
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
                    .into(ivSelectedMachineImage);
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

        // Display machine type
        if (tvSelectedMachineType != null) {
            String equipmentType = machine.getEquipmentType();
            if (equipmentType != null && !equipmentType.isEmpty()) {
                tvSelectedMachineType.setText(equipmentType);
            } else {
                tvSelectedMachineType.setText("Machine");
            }
        }

        // Load machine image
        if (ivSelectedMachineImage != null) {
            String imageUrl = machine.getImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                currentMachineImageUrl = imageUrl; // Store original URL
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
                        .into(ivSelectedMachineImage);
            } else {
                ivSelectedMachineImage.setImageResource(R.drawable.jcb3dx_1);
            }
        }
    }
}

