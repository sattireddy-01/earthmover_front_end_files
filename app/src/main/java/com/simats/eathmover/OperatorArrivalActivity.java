package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

/**
 * Activity showing operator arrival countdown and details.
 */
public class OperatorArrivalActivity extends AppCompatActivity {

    private static final String TAG = "OperatorArrival";
    private TextView tvHours, tvMinutes, tvSeconds;
    private CountDownTimer countDownTimer;
    private String operatorId;
    private String bookingId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_arrival);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_arrival);
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

        progressBar = findViewById(R.id.progress_bar);

        tvHours = findViewById(R.id.tv_hours);
        tvMinutes = findViewById(R.id.tv_minutes);
        tvSeconds = findViewById(R.id.tv_seconds);

        // Get booking data from intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        bookingId = intent.getStringExtra("booking_id");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        // If booking ID is available, fetch booking details
        if (bookingId != null) {
            loadBookingDetails(bookingId);
        } else {
            // Use default countdown (90 minutes = 5400000 milliseconds)
            startCountdown(5400000);
        }

        // Confirm Operator button
        Button btnConfirmOperator = findViewById(R.id.btn_confirm_operator);
        if (btnConfirmOperator != null) {
            btnConfirmOperator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to work summary after work completion
                    Intent intent = new Intent(OperatorArrivalActivity.this, WorkSummaryActivity.class);
                    if (bookingId != null) {
                        intent.putExtra("booking_id", bookingId);
                    }
                    startActivity(intent);
                }
            });
        }

        // Cancel Booking button
        Button btnCancelBooking = findViewById(R.id.btn_cancel_booking);
        if (btnCancelBooking != null) {
            btnCancelBooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Call API to cancel booking
                    finish();
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_arrival);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    private void loadBookingDetails(String bookingId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // TODO: Add API endpoint to get booking by ID
        // For now, use default countdown
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        startCountdown(5400000);
    }

    private void startCountdown(long millisInFuture) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                if (tvHours != null) tvHours.setText(String.format("%02d", hours));
                if (tvMinutes != null) tvMinutes.setText(String.format("%02d", minutes));
                if (tvSeconds != null) tvSeconds.setText(String.format("%02d", seconds));
            }

            @Override
            public void onFinish() {
                if (tvHours != null) tvHours.setText("00");
                if (tvMinutes != null) tvMinutes.setText("00");
                if (tvSeconds != null) tvSeconds.setText("00");
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
