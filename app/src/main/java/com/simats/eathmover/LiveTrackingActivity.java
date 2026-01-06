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
 * Activity for live tracking of machine location.
 */
public class LiveTrackingActivity extends AppCompatActivity {

    private String operatorId;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);

        Toolbar toolbar = findViewById(R.id.toolbar_live_tracking);
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

        // Get booking data from intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        bookingId = intent.getStringExtra("booking_id");

        // Continue/Next button - Navigate to Operator Arrival page
        Button btnContinue = findViewById(R.id.btn_continue_tracking);
        if (btnContinue == null) {
            btnContinue = findViewById(R.id.btn_next);
        }
        if (btnContinue != null) {
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent arrivalIntent = new Intent(LiveTrackingActivity.this, OperatorArrivalActivity.class);
                    if (operatorId != null) {
                        arrivalIntent.putExtra("operator_id", operatorId);
                    }
                    if (bookingId != null) {
                        arrivalIntent.putExtra("booking_id", bookingId);
                    }
                    // Pass booking details
                    arrivalIntent.putExtra("date", intent.getStringExtra("date"));
                    arrivalIntent.putExtra("time", intent.getStringExtra("time"));
                    arrivalIntent.putExtra("machine_type", intent.getStringExtra("machine_type"));
                    arrivalIntent.putExtra("location", intent.getStringExtra("location"));
                    startActivity(arrivalIntent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_live_tracking);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }
}






