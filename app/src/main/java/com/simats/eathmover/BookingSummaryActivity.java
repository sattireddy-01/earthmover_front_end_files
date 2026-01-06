package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.config.ApiConfig;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.models.MachineRequest;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.models.User;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;
import com.simats.eathmover.utils.SessionManager;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingSummaryActivity extends AppCompatActivity {

    private static final String TAG = "BookingSummaryActivity";

    private SessionManager sessionManager;

    private Machine currentMachine;
    private String durationString;

    private TextView tvMachineName, tvMachineType, tvDuration,
            tvStartDate, tvEstimatedCost,
            tvUserName, tvUserContact, tvUserAddress;

    private ImageView ivMachineThumbnail;
    private Button btnConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_booking_summary);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        tvMachineName = findViewById(R.id.tv_machine_name);
        tvMachineType = findViewById(R.id.tv_machine_type);
        tvDuration = findViewById(R.id.tv_duration);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEstimatedCost = findViewById(R.id.tv_estimated_cost);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserContact = findViewById(R.id.tv_user_contact);
        tvUserAddress = findViewById(R.id.tv_user_address);
        ivMachineThumbnail = findViewById(R.id.iv_machine_thumbnail);
        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);

        loadUserInformation();
        loadBookingDetails();
        loadMachineDetails();

        // ✅ FINAL CONFIRM BOOKING LOGIC
        btnConfirmBooking.setOnClickListener(v -> fetchOperatorAndProceed());

        BottomNavigationView bottomNav =
                findViewById(R.id.bottom_navigation_booking_summary);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    // =====================================================
    // 🔥 CORE FIX : FIND OPERATOR USING MACHINE_ID
    // =====================================================
    private void fetchOperatorAndProceed() {

        int machineId = getIntent().getIntExtra("machine_id", 0);

        if (machineId <= 0) {
            Toast.makeText(this, "Machine ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Confirm Booking clicked. Machine ID = " + machineId);

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        MachineRequest request = new MachineRequest(machineId);

        Call<ApiResponse<OperatorProfile>> call =
                apiService.getOperatorByMachine(request);

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<OperatorProfile>> call,
                    Response<ApiResponse<OperatorProfile>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    ApiResponse<OperatorProfile> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {

                        OperatorProfile operator = apiResponse.getData();

                        // ✅ OPERATOR FOUND → GO TO OperatorFoundActivity
                        Intent intent = new Intent(
                                BookingSummaryActivity.this,
                                OperatorFoundActivity.class
                        );

                        intent.putExtra("operator_id", operator.getOperatorId());
                        intent.putExtra("operator_name", operator.getName());
                        intent.putExtra("operator_phone", operator.getPhone());
                        intent.putExtra("machine_id", machineId);

                        // pass booking info forward
                        Intent src = getIntent();
                        intent.putExtra("machine_type", src.getStringExtra("machine_type"));
                        intent.putExtra("machine_model", src.getStringExtra("machine_model"));
                        intent.putExtra("date", src.getStringExtra("date"));
                        intent.putExtra("time", src.getStringExtra("time"));
                        intent.putExtra("location", src.getStringExtra("location"));
                        intent.putExtra("duration", src.getStringExtra("duration"));
                        intent.putExtra("estimated_cost", tvEstimatedCost.getText().toString());

                        startActivity(intent);

                    } else {
                        Toast.makeText(
                                BookingSummaryActivity.this,
                                "No operator available for this machine",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } else {
                    Toast.makeText(
                            BookingSummaryActivity.this,
                            "Server error while finding operator",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<OperatorProfile>> call,
                    Throwable t) {

                Toast.makeText(
                        BookingSummaryActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // =====================================================
    // USER INFO
    // =====================================================
    private void loadUserInformation() {
        String userId = sessionManager.getUserId();

        if (userId == null) return;

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        apiService.getUserProfile(userId)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<User>> call,
                            Response<ApiResponse<User>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            User user = response.body().getData();
                            tvUserName.setText(user.getName());
                            tvUserContact.setText("Contact: " + user.getPhone());
                            tvUserAddress.setText("Address: " + user.getAddress());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<User>> call,
                            Throwable t) {
                        Log.e(TAG, "User load failed", t);
                    }
                });
    }

    // =====================================================
    // BOOKING DETAILS
    // =====================================================
    private void loadBookingDetails() {
        Intent intent = getIntent();

        tvMachineName.setText(intent.getStringExtra("machine_model"));
        tvMachineType.setText(intent.getStringExtra("machine_type"));

        durationString = intent.getStringExtra("duration");
        tvDuration.setText(durationString);

        String date = intent.getStringExtra("date");
        tvStartDate.setText("Starting from " + date);
    }

    // =====================================================
    // MACHINE DETAILS + COST
    // =====================================================
    private void loadMachineDetails() {

        int machineId = getIntent().getIntExtra("machine_id", 0);
        if (machineId <= 0) return;

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        apiService.getMachineDetails(machineId)
                .enqueue(new Callback<ApiResponse<Machine>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Machine>> call,
                            Response<ApiResponse<Machine>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            currentMachine = response.body().getData();
                            calculateEstimatedCost();

                            String imageUrl =
                                    ApiConfig.getRootUrl() + currentMachine.getImage();

                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.jcb3dx_1)
                                    .error(R.drawable.jcb3dx_1)
                                    .into(ivMachineThumbnail);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<Machine>> call,
                            Throwable t) {
                        Log.e(TAG, "Machine load failed", t);
                    }
                });
    }

    private void calculateEstimatedCost() {
        if (currentMachine == null || durationString == null) {
            tvEstimatedCost.setText("₹0");
            return;
        }

        int minutes = parseDurationToMinutes(durationString);
        double cost =
                (currentMachine.getPricePerHour() / 60.0) * minutes;

        tvEstimatedCost.setText("₹" + Math.round(cost));
    }

    private int parseDurationToMinutes(String duration) {
        int total = 0;

        Matcher h =
                Pattern.compile("(\\d+)\\s*hour")
                        .matcher(duration.toLowerCase());
        Matcher m =
                Pattern.compile("(\\d+)\\s*min")
                        .matcher(duration.toLowerCase());

        if (h.find()) total += Integer.parseInt(h.group(1)) * 60;
        if (m.find()) total += Integer.parseInt(m.group(1));

        return total;
    }
}
