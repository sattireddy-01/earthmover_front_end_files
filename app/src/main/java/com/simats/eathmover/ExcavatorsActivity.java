package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.adapters.ExcavatorAdapter;
import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.BottomNavigationHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows Excavator models with images and price per hour from machines table.
 */
public class ExcavatorsActivity extends AppCompatActivity {

    private static final String TAG = "ExcavatorsActivity";
    private RecyclerView recyclerViewExcavators;
    private ProgressBar progressBar;
    private ExcavatorAdapter adapter;
    private List<Machine> excavatorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excavators);

        Toolbar toolbar = findViewById(R.id.toolbar_excavators);
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
        recyclerViewExcavators = findViewById(R.id.recycler_view_excavators);
        progressBar = findViewById(R.id.progress_bar_excavators);

        // Setup RecyclerView
        recyclerViewExcavators.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExcavatorAdapter(excavatorList, new ExcavatorAdapter.OnExcavatorClickListener() {
            @Override
            public void onExcavatorClick(Machine machine) {
                // Navigate to machine details with full machine data
                Intent intent = new Intent(ExcavatorsActivity.this, MachineDetailsActivity.class);
                intent.putExtra("machine_id", machine.getMachineId());
                intent.putExtra("machine_model", machine.getModelName());
                intent.putExtra("machine_price", machine.getPricePerHour());
                intent.putExtra("machine_image", machine.getImage());
                intent.putExtra("machine_year", machine.getModelYear() != null ? machine.getModelYear() : 0);
                intent.putExtra("specs", machine.getSpecs());
                intent.putExtra("machine_type", machine.getEquipmentType());
                
                // Pass location if available
                if (getIntent().hasExtra("location")) {
                    intent.putExtra("location", getIntent().getStringExtra("location"));
                }
                
                startActivity(intent);
            }
        });
        recyclerViewExcavators.setAdapter(adapter);

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_excavators);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }

        // Load excavators from backend (category_id = 2)
        loadExcavators();
    }

    /**
     * Load excavators from backend API
     * Filters machines by category_id = 2 (Excavator)
     */
    private void loadExcavators() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<List<Machine>>> call = apiService.getUserMachines();

        call.enqueue(new Callback<ApiResponse<List<Machine>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Machine>>> call, Response<ApiResponse<List<Machine>>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Machine>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Machine> allMachines = apiResponse.getData();
                        
                        // Filter excavators (category_id = 2)
                        excavatorList.clear();
                        for (Machine machine : allMachines) {
                            if (machine.getCategoryId() != null && machine.getCategoryId() == 2) {
                                excavatorList.add(machine);
                                // Log image URLs for debugging
                                Log.d(TAG, "Excavator - ID: " + machine.getMachineId() + 
                                    ", Model: " + machine.getModelName() + 
                                    ", Image: " + machine.getImage() + 
                                    ", Machine_Image_1: " + machine.getMachineImage1());
                            }
                        }

                        if (excavatorList.isEmpty()) {
                            Toast.makeText(ExcavatorsActivity.this, "No excavators available", Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + excavatorList.size() + " excavators");
                        }
                    } else {
                        Log.e(TAG, "Failed to load excavators: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        Toast.makeText(ExcavatorsActivity.this, "Failed to load excavators", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load excavators: HTTP " + response.code());
                    Toast.makeText(ExcavatorsActivity.this, "Failed to load excavators", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Machine>>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Error loading excavators: " + t.getMessage());
                Toast.makeText(ExcavatorsActivity.this, "Error loading excavators", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


