package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

/**
 * Activity showing work completion summary.
 */
public class WorkSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_summary);

        Toolbar toolbar = findViewById(R.id.toolbar_work_summary);
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

        // Back to Home button
        Button btnBackToHome = findViewById(R.id.btn_back_to_home);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to final price breakdown first
                    Intent intent = new Intent(WorkSummaryActivity.this, FinalPriceBreakdownActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_work_summary);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }
}






