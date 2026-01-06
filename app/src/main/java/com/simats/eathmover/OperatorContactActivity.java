package com.simats.eathmover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for operator contact with share number option.
 */
public class OperatorContactActivity extends AppCompatActivity {

    private static final String TAG = "OperatorContact";
    private String operatorId;
    private String operatorPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_contact);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_contact);
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

        // Get operator data from intent
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        operatorPhone = intent.getStringExtra("operator_phone");

        // If we have operator ID but not phone, fetch it
        if (operatorId != null && operatorPhone == null) {
            loadOperatorPhone(operatorId);
        } else if (operatorPhone != null) {
            updatePhoneDisplay(operatorPhone);
        } else {
            // Default phone number from database (Harsha: 7675903108)
            operatorPhone = "7675903108";
            updatePhoneDisplay(operatorPhone);
        }

        // Share Number button
        Button btnShareNumber = findViewById(R.id.btn_share_number);
        if (btnShareNumber != null) {
            btnShareNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (operatorPhone != null && !operatorPhone.isEmpty()) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        String operatorName = intent.getStringExtra("operator_name");
                        String shareText = "Operator Contact: " + operatorPhone;
                        if (operatorName != null) {
                            shareText = operatorName + " - " + operatorPhone;
                        }
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                        startActivity(Intent.createChooser(shareIntent, "Share Number"));
                    } else {
                        Toast.makeText(OperatorContactActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Call button (if exists in layout)
        Button btnCall = findViewById(R.id.btn_call);
        if (btnCall != null) {
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (operatorPhone != null && !operatorPhone.isEmpty()) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + operatorPhone));
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(OperatorContactActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Chat button - Navigate to Chat Activity
        Button btnChat = findViewById(R.id.btn_chat);
        if (btnChat != null) {
            btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(OperatorContactActivity.this, ChatActivity.class);
                    if (operatorId != null) {
                        chatIntent.putExtra("operator_id", operatorId);
                    }
                    if (operatorPhone != null) {
                        chatIntent.putExtra("operator_phone", operatorPhone);
                    }
                    String operatorName = intent.getStringExtra("operator_name");
                    if (operatorName != null) {
                        chatIntent.putExtra("operator_name", operatorName);
                    }
                    startActivity(chatIntent);
                }
            });
        }

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_operator_contact);
        if (bottomNav != null) {
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    private void loadOperatorPhone(String operatorId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorProfile(operatorId);

        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OperatorProfile operator = apiResponse.getData();
                        operatorPhone = operator.getPhone();
                        updatePhoneDisplay(operatorPhone);
                    } else {
                        // Fallback to default phone number
                        operatorPhone = "7675903108";
                        updatePhoneDisplay(operatorPhone);
                    }
                } else {
                    Log.e(TAG, "Failed to load operator phone: " + response.code());
                    // Fallback to default phone number
                    operatorPhone = "7675903108";
                    updatePhoneDisplay(operatorPhone);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                Log.e(TAG, "Error loading operator phone: " + t.getMessage());
                // Fallback to default phone number
                operatorPhone = "7675903108";
                updatePhoneDisplay(operatorPhone);
            }
        });
    }

    private void updatePhoneDisplay(String phone) {
        // Try to find phone TextView, but don't fail if it doesn't exist
        TextView tvPhone = findViewById(R.id.tv_operator_phone);
        if (tvPhone != null && phone != null) {
            tvPhone.setText(phone);
        }
        // Phone is stored in operatorPhone variable for use in buttons
    }
}
