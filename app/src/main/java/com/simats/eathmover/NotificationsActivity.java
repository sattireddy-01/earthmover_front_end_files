package com.simats.eathmover;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

/**
 * Notifications settings screen with toggles for different notification types.
 */
public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar_notifications);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Example toggle (booking status) – you can add logic to persist these later
        Switch swBookingStatus = findViewById(R.id.sw_booking_status);
        if (swBookingStatus != null) {
            swBookingStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(
                        NotificationsActivity.this,
                        isChecked ? "Booking Status notifications ON" : "Booking Status notifications OFF",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_notifications);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
            bottomNav.setSelectedItemId(R.id.navigation_profile);
        }
    }
}

































