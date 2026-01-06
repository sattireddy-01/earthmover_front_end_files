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
 * Activity shown after successful payment.
 */
public class PaymentSuccessfulActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_successful);

        Toolbar toolbar = findViewById(R.id.toolbar_payment_successful);
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

        // Rating & Feedback button → open rating screen
        Button btnRatingFeedback = findViewById(R.id.btn_rating_feedback);
        if (btnRatingFeedback != null) {
            btnRatingFeedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PaymentSuccessfulActivity.this, RatingFeedbackActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Return to Home button
        Button btnReturnToHome = findViewById(R.id.btn_return_to_home);
        if (btnReturnToHome != null) {
            btnReturnToHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToHome();
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_payment_successful);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    private void goToHome() {
        Intent intent = new Intent(PaymentSuccessfulActivity.this, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}


