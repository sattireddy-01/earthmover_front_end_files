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
import com.simats.eathmover.adapters.DozerAdapter;
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
 * Shows Dozer models with images and price per hour from machines table.
 */
public class DozerModelsActivity extends AppCompatActivity {

    private static final String TAG = "DozerModelsActivity";
    private RecyclerView recyclerViewDozers;
    private ProgressBar progressBar;
    private DozerAdapter adapter;
    private List<Machine> dozerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dozer_models);

        Toolbar toolbar = findViewById(R.id.toolbar_dozer_models);
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
        recyclerViewDozers = findViewById(R.id.recycler_view_dozers);
        progressBar = findViewById(R.id.progress_bar_dozers);

        // Setup RecyclerView
        recyclerViewDozers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DozerAdapter(dozerList, new DozerAdapter.OnDozerClickListener() {
            @Override
            public void onDozerClick(Machine machine) {
                // Navigate to machine details with full machine data
                Intent intent = new Intent(DozerModelsActivity.this, MachineDetailsActivity.class);
                intent.putExtra("machine_id", machine.getMachineId());
                intent.putExtra("machine_model", machine.getModelName());
                intent.putExtra("machine_price", machine.getPricePerHour());
                intent.putExtra("machine_image", machine.getImage());
                intent.putExtra("machine_year", machine.getModelYear() != null ? machine.getModelYear() : 0);
                intent.putExtra("specs", machine.getSpecs());
                intent.putExtra("machine_type", machine.getEquipmentType());
                startActivity(intent);
            }
        });
        recyclerViewDozers.setAdapter(adapter);

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_dozer);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }

        // Load dozers from backend (category_id = 3)
        loadDozers();
    }

    /**
     * Load dozers from backend API
     * Filters machines by category_id = 3 (Dozer)
     */
    private void loadDozers() {
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
                        
                        // Filter dozers (category_id = 3)
                        dozerList.clear();
                        for (Machine machine : allMachines) {
                            if (machine.getCategoryId() != null && machine.getCategoryId() == 3) {
                                dozerList.add(machine);
                                // Log image URLs for debugging
                                Log.d(TAG, "Dozer - ID: " + machine.getMachineId() + 
                                    ", Model: " + machine.getModelName() + 
                                    ", Image: " + machine.getImage() + 
                                    ", Machine_Image_1: " + machine.getMachineImage1());
                            }
                        }

                        if (dozerList.isEmpty()) {
                            Toast.makeText(DozerModelsActivity.this, "No dozers available", Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + dozerList.size() + " dozers");
                        }
                    } else {
                        Log.e(TAG, "Failed to load dozers: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"));
                        Toast.makeText(DozerModelsActivity.this, "Failed to load dozers", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load dozers: HTTP " + response.code());
                    Toast.makeText(DozerModelsActivity.this, "Failed to load dozers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Machine>>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Error loading dozers: " + t.getMessage());
                Toast.makeText(DozerModelsActivity.this, "Error loading dozers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}