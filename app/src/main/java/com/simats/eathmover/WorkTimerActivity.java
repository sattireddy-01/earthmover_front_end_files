package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.SessionManager;

/**
 * Activity for tracking work time with timer functionality.
 */
public class WorkTimerActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private EditText etHours;
    private EditText etMinutes;
    private EditText etSeconds;
    private Button btnStart;
    private Button btnPause;
    private Button btnStop;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private long timeRemaining = 0;
    private long totalTimeInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_timer);

        sessionManager = new SessionManager(this);

        // Initialize views
        etHours = findViewById(R.id.et_hours);
        etMinutes = findViewById(R.id.et_minutes);
        etSeconds = findViewById(R.id.et_seconds);
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);

        // Initialize timer fields
        if (etHours != null) {
            etHours.setText("00");
        }
        if (etMinutes != null) {
            etMinutes.setText("00");
        }
        if (etSeconds != null) {
            etSeconds.setText("00");
        }

        // Start button
        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTimer();
                }
            });
        }

        // Pause button
        if (btnPause != null) {
            btnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pauseTimer();
                }
            });
        }

        // Stop button
        if (btnStop != null) {
            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopTimer();
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_work_timer);
        if (bottomNav != null) {
            setupOperatorBottomNavigation(bottomNav);
        }
    }

    private void startTimer() {
        if (isRunning && !isPaused) {
            // Timer is already running
            return;
        }

        if (isPaused) {
            // Resume from paused state
            resumeTimer();
            return;
        }

        // Get time from input fields
        int hours = 0, minutes = 0, seconds = 0;
        try {
            hours = Integer.parseInt(etHours.getText().toString());
        } catch (NumberFormatException e) {
            hours = 0;
        }
        try {
            minutes = Integer.parseInt(etMinutes.getText().toString());
        } catch (NumberFormatException e) {
            minutes = 0;
        }
        try {
            seconds = Integer.parseInt(etSeconds.getText().toString());
        } catch (NumberFormatException e) {
            seconds = 0;
        }

        if (hours == 0 && minutes == 0 && seconds == 0) {
            Toast.makeText(this, "Please set a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total time in milliseconds
        totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;
        timeRemaining = totalTimeInMillis;

        // Disable input fields
        etHours.setEnabled(false);
        etMinutes.setEnabled(false);
        etSeconds.setEnabled(false);

        // Start countdown
        startCountDown();
        isRunning = true;
        isPaused = false;

        // Update button states
        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                updateTimerDisplay();
                stopTimer();
                Toast.makeText(WorkTimerActivity.this, "Timer finished!", Toast.LENGTH_SHORT).show();
            }
        };
        countDownTimer.start();
    }

    private void pauseTimer() {
        if (!isRunning) {
            return;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isPaused = true;
        btnStart.setEnabled(true);
        btnStart.setText("Resume");
        btnPause.setEnabled(false);
    }

    private void resumeTimer() {
        if (!isPaused) {
            return;
        }

        startCountDown();
        isPaused = false;
        btnStart.setEnabled(false);
        btnStart.setText("Start");
        btnPause.setEnabled(true);
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        isRunning = false;
        isPaused = false;
        timeRemaining = 0;

        // Reset display
        etHours.setText("00");
        etMinutes.setText("00");
        etSeconds.setText("00");

        // Enable input fields
        etHours.setEnabled(true);
        etMinutes.setEnabled(true);
        etSeconds.setEnabled(true);

        // Update button states
        btnStart.setEnabled(true);
        btnStart.setText("Start");
        btnPause.setEnabled(true);
    }

    private void updateTimerDisplay() {
        long hours = timeRemaining / (1000 * 60 * 60);
        long minutes = (timeRemaining % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (timeRemaining % (1000 * 60)) / 1000;

        etHours.setText(String.format("%02d", hours));
        etMinutes.setText(String.format("%02d", minutes));
        etSeconds.setText(String.format("%02d", seconds));
    }

    private void setupOperatorBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                Intent intent = new Intent(this, OperatorDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_bookings) {
                Intent intent = new Intent(this, NewBookingRequestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_earnings) {
                Intent intent = new Intent(this, OperatorEarningsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, OperatorProfileActivity.class);
                String operatorId = sessionManager.getOperatorId();
                if (operatorId != null) {
                    intent.putExtra("operator_id", operatorId);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

