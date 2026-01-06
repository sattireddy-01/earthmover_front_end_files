package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for searching available operators.
 */
public class OperatorSearchActivity extends AppCompatActivity {

    private static final String TAG = "OperatorSearch";
    private ProgressBar progressBar;
    private String searchLocation;
    private String machineType;
    private String date;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_search);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_search);
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

        progressBar = findViewById(R.id.progress_bar_search);
        if (progressBar == null) {
            progressBar = findViewById(R.id.progress_bar); // Fallback
        }

        // Get search parameters from intent
        Intent intent = getIntent();
        searchLocation = intent.getStringExtra("location");
        machineType = intent.getStringExtra("machine_type");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        
        // Store all booking details for passing to OperatorFoundActivity
        // These will be accessed in navigateToOperatorFound method

        // Start searching for operators
        searchOperators();

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_search);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    private void searchOperators() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorProfile>> call = apiService.searchOperators(
            searchLocation != null ? searchLocation : "",
            machineType != null ? machineType : "",
            date != null ? date : "",
            time != null ? time : ""
        );

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<OperatorProfile> operators = apiResponse.getDataList();
                        if (operators != null && !operators.isEmpty()) {
                            // Navigate to Operator Found page with operator data
                            navigateToOperatorFound(operators.get(0)); // Use first operator found
                        } else {
                            Toast.makeText(OperatorSearchActivity.this, "No operators found", Toast.LENGTH_SHORT).show();
                            // Still navigate but with no operator data
                            navigateToOperatorFound(null);
                        }
                    } else {
                        Toast.makeText(OperatorSearchActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "No operators found",
                            Toast.LENGTH_SHORT).show();
                        navigateToOperatorFound(null);
                    }
                } else {
                    Log.e(TAG, "Failed to search operators: " + response.code());
                    navigateToOperatorFound(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error searching operators: " + t.getMessage());
                // Navigate anyway (fallback to default flow)
                navigateToOperatorFound(null);
            }
        });
    }

    private void navigateToOperatorFound(OperatorProfile operator) {
        Intent intent = new Intent(OperatorSearchActivity.this, OperatorFoundActivity.class);
        if (operator != null) {
            intent.putExtra("operator_id", operator.getOperatorId());
            
            // Pass all booking details from the original intent
            Intent originalIntent = getIntent();
            if (originalIntent != null) {
                if (originalIntent.hasExtra("machine_id")) {
                    intent.putExtra("machine_id", originalIntent.getIntExtra("machine_id", 0));
                }
                if (originalIntent.hasExtra("machine_model")) {
                    intent.putExtra("machine_model", originalIntent.getStringExtra("machine_model"));
                }
                if (originalIntent.hasExtra("machine_type")) {
                    intent.putExtra("machine_type", originalIntent.getStringExtra("machine_type"));
                }
                if (originalIntent.hasExtra("date")) {
                    intent.putExtra("date", originalIntent.getStringExtra("date"));
                }
                if (originalIntent.hasExtra("time")) {
                    intent.putExtra("time", originalIntent.getStringExtra("time"));
                }
                if (originalIntent.hasExtra("location")) {
                    intent.putExtra("location", originalIntent.getStringExtra("location"));
                }
                if (originalIntent.hasExtra("duration")) {
                    intent.putExtra("duration", originalIntent.getStringExtra("duration"));
                }
                if (originalIntent.hasExtra("estimated_cost")) {
                    intent.putExtra("estimated_cost", originalIntent.getStringExtra("estimated_cost"));
                }
            }
            intent.putExtra("operator_name", operator.getName());
            intent.putExtra("operator_phone", operator.getPhone());
        }
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("machine_type", machineType);
        intent.putExtra("location", searchLocation);
        startActivity(intent);
        finish();
    }
}
