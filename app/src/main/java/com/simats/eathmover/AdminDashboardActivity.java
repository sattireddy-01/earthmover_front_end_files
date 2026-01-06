package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_dashboard);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Verify New Operators button
        Button btnVerifyOperators = findViewById(R.id.btn_verify_operators);
        if (btnVerifyOperators != null) {
            btnVerifyOperators.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminVerificationActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Manage Machine Pricing button
        Button btnManagePricing = findViewById(R.id.btn_manage_pricing);
        if (btnManagePricing != null) {
            btnManagePricing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminMachinePricingActivity.class);
                    startActivity(intent);
                }
            });
        }

        // View Live Bookings button
        Button btnViewBookings = findViewById(R.id.btn_view_bookings);
        if (btnViewBookings != null) {
            btnViewBookings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminLiveBookingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_admin);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_admin_dashboard) {
                    // Already on dashboard, do nothing
                    return true;
                } else if (itemId == R.id.nav_admin_verification) {
                    // Navigate to admin verification
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminVerificationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_admin_bookings) {
                    // Navigate to live bookings
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminLiveBookingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_admin_reports) {
                    // Navigate to admin reports
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminReportsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_admin_settings) {
                    // Navigate to admin settings
                    Intent intent = new Intent(AdminDashboardActivity.this, SettingsActivity.class);
                    intent.putExtra("is_admin", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });

            // Set the dashboard item as selected
            bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
        }
    }
}

