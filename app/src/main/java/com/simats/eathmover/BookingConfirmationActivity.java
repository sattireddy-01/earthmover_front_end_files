package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.utils.BottomNavigationHelper;

public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        Toolbar toolbar = findViewById(R.id.toolbar_booking_confirmation);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Get data from intent
        Intent intent = getIntent();
        String operatorName = intent.getStringExtra("operator_name");
        String machineType = intent.getStringExtra("machine_type");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String location = intent.getStringExtra("location");

        // Set data to views
        TextView tvOperator = findViewById(R.id.tv_conf_operator_name);
        TextView tvMachine = findViewById(R.id.tv_conf_machine_type);
        TextView tvDate = findViewById(R.id.tv_conf_date);
        TextView tvTime = findViewById(R.id.tv_conf_time);
        TextView tvLocation = findViewById(R.id.tv_conf_location);

        if (operatorName != null) tvOperator.setText(operatorName);
        if (machineType != null) tvMachine.setText(machineType);
        if (date != null) tvDate.setText(date);
        if (time != null) tvTime.setText(time);
        if (location != null) tvLocation.setText(location);

        Button btnViewBookings = findViewById(R.id.btn_view_bookings);
        btnViewBookings.setOnClickListener(v -> {
            Intent bookingsIntent = new Intent(BookingConfirmationActivity.this, UserBookingsActivity.class);
            bookingsIntent.putExtra("filter", "ACTIVE");
            // Add clear top to ensure onNewIntent or onCreate is called if it was already in stack, 
            // but since we are finishing subsequent activities, onResume in UserBookings will handle it.
            // Using CLEAR_TOP and SINGLE_TOP is safer navigation practice here.
            bookingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(bookingsIntent);
            finish();
        });

        Button btnBackHome = findViewById(R.id.btn_back_to_home);
        btnBackHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(BookingConfirmationActivity.this, UserDashboardActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Go back to home instead of previous screens to avoid re-booking
        Intent homeIntent = new Intent(BookingConfirmationActivity.this, UserDashboardActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}
