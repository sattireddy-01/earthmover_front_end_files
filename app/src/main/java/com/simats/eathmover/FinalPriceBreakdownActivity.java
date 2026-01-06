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
 * Activity showing final price breakdown before payment.
 */
public class FinalPriceBreakdownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_price_breakdown);

        Toolbar toolbar = findViewById(R.id.toolbar_price_breakdown);
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

        // Confirm Payment button
        Button btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
        if (btnConfirmPayment != null) {
            btnConfirmPayment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FinalPriceBreakdownActivity.this, UPIPaymentActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_price_breakdown);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }
}






