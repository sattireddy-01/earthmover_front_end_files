package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.BottomNavigationHelper;

/**
 * Activity for UPI payment processing.
 */
public class UPIPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upi_payment);

        Toolbar toolbar = findViewById(R.id.toolbar_upi_payment);
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

        EditText etUpiId = findViewById(R.id.et_upi_id);

        // Payment app options
        View cardPaytm = findViewById(R.id.card_paytm);
        View cardPhonepe = findViewById(R.id.card_phonepe);
        View cardGooglePay = findViewById(R.id.card_google_pay);

        View.OnClickListener paymentAppClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Open respective payment app
                // For now, proceed to payment successful
                proceedToPayment();
            }
        };

        if (cardPaytm != null) {
            cardPaytm.setOnClickListener(paymentAppClickListener);
        }
        if (cardPhonepe != null) {
            cardPhonepe.setOnClickListener(paymentAppClickListener);
        }
        if (cardGooglePay != null) {
            cardGooglePay.setOnClickListener(paymentAppClickListener);
        }

        // Proceed to Pay button
        Button btnProceedToPay = findViewById(R.id.btn_proceed_to_pay);
        if (btnProceedToPay != null) {
            btnProceedToPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String upiId = etUpiId.getText().toString().trim();
                    if (upiId.isEmpty()) {
                        // TODO: Show error message
                        return;
                    }
                    proceedToPayment();
                }
            });
        }
    }

    private void proceedToPayment() {
        Intent intent = new Intent(UPIPaymentActivity.this, PaymentSuccessfulActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_upi_payment);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }
}

