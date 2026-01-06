package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

/**
 * Settings page with options: Profile, Notifications, Privacy Policy, Terms, About.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        // Notifications option
        View llNotifications = findViewById(R.id.ll_notifications);
        if (llNotifications != null) {
            llNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Privacy Policy, Terms, About - TODO: Implement these pages if needed
        // For now, they can show a toast or open a web view

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_settings);
        if (bottomNav != null) {
            boolean isAdmin = getIntent().getBooleanExtra("is_admin", false);
            
            if (isAdmin) {
                // Admin Navigation
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.admin_bottom_nav);
                
                bottomNav.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_admin_dashboard) {
                        startActivity(new Intent(SettingsActivity.this, AdminDashboardActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_admin_verification) {
                        startActivity(new Intent(SettingsActivity.this, AdminVerificationActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_admin_bookings) {
                        startActivity(new Intent(SettingsActivity.this, AdminLiveBookingsActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_admin_reports) {
                        startActivity(new Intent(SettingsActivity.this, AdminReportsActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_admin_settings) {
                        return true;
                    }
                    return false;
                });
                bottomNav.setSelectedItemId(R.id.nav_admin_settings);
                
            } else {
                // User Navigation (Default)
                BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
            }
        }
    }
}
































