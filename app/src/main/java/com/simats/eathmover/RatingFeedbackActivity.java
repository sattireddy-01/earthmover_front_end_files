package com.simats.eathmover;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

/**
 * Screen shown after payment to collect rating and feedback from the user.
 */
public class RatingFeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_feedback);

        Toolbar toolbar = findViewById(R.id.toolbar_rating_feedback);
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

        RatingBar ratingBarInput = findViewById(R.id.rating_bar_input);
        EditText etFeedback = findViewById(R.id.et_feedback);
        Button btnSubmit = findViewById(R.id.btn_submit_feedback);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBarInput.getRating();
                String feedback = etFeedback.getText().toString().trim();

                if (rating == 0) {
                    Toast.makeText(RatingFeedbackActivity.this,
                            "Please select a rating", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: send rating/feedback to backend when API is ready
                Toast.makeText(RatingFeedbackActivity.this,
                        "Thank you for your feedback!", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_rating_feedback);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }
}


