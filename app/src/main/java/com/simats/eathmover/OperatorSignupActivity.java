package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.models.Operator;
import com.simats.eathmover.models.SignUpResponse;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorSignupActivity extends AppCompatActivity {

    private EditText etOperatorName, etOperatorPhone, etOperatorAddress, etOperatorEmail, etOperatorPassword, etOperatorConfirmPassword;
    private Button btnOperatorSignup;
    private ProgressBar progressBar;
    private static final String TAG = "OperatorSignup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_signup);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_signup);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Initialize views
        etOperatorName = findViewById(R.id.et_operator_name);
        etOperatorPhone = findViewById(R.id.et_operator_phone);
        etOperatorAddress = findViewById(R.id.et_operator_address);
        etOperatorEmail = findViewById(R.id.et_operator_email);
        etOperatorPassword = findViewById(R.id.et_operator_password);
        etOperatorConfirmPassword = findViewById(R.id.et_operator_confirm_password);
        btnOperatorSignup = findViewById(R.id.btn_operator_signup);
        progressBar = findViewById(R.id.progress_bar);

        if (btnOperatorSignup != null) {
            btnOperatorSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateInput()) {
                        performSignup();
                    }
                }
            });
        }
    }

    private boolean validateInput() {
        String name = etOperatorName.getText().toString().trim();
        String phone = etOperatorPhone.getText().toString().trim();
        String address = etOperatorAddress.getText().toString().trim();
        String password = etOperatorPassword.getText().toString().trim();
        String confirmPassword = etOperatorConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etOperatorName.setError("Name is required");
            etOperatorName.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            etOperatorPhone.setError("Phone number is required");
            etOperatorPhone.requestFocus();
            return false;
        }

        if (address.isEmpty()) {
            etOperatorAddress.setError("Address is required");
            etOperatorAddress.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etOperatorPassword.setError("Password is required");
            etOperatorPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etOperatorPassword.setError("Password must be at least 6 characters");
            etOperatorPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etOperatorConfirmPassword.setError("Passwords do not match");
            etOperatorConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performSignup() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnOperatorSignup != null) btnOperatorSignup.setEnabled(false);

        String name = etOperatorName.getText().toString().trim();
        String phone = etOperatorPhone.getText().toString().trim();
        String address = etOperatorAddress.getText().toString().trim();
        String email = etOperatorEmail != null ? etOperatorEmail.getText().toString().trim() : "";
        String password = etOperatorPassword.getText().toString().trim();

        Operator operator = new Operator(name, phone, address, email, password);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<SignUpResponse> call = apiService.createOperator(operator);

        call.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnOperatorSignup != null) btnOperatorSignup.setEnabled(true);

                Log.d(TAG, "=== SIGNUP RESPONSE RECEIVED ===");
                Log.d(TAG, "HTTP Code: " + response.code());
                Log.d(TAG, "Is Successful: " + response.isSuccessful());
                
                // Get phone number from form - we'll need this regardless
                String phone = etOperatorPhone.getText().toString().trim();
                Log.d(TAG, "Phone from form: " + phone);
                
                // Check if response is successful and has body
                if (response.isSuccessful() && response.body() != null) {
                    SignUpResponse signUpResponse = response.body();
                    boolean isSuccess = signUpResponse.isSuccess();
                    String message = signUpResponse.getMessage();
                    
                    Log.d(TAG, "Response body - success: " + isSuccess + ", message: " + message);
                    
                    // Get operator ID from response if available
                    int operatorId = 0;
                    if (signUpResponse.getData() != null) {
                        operatorId = signUpResponse.getData().getOperatorId();
                        if (operatorId <= 0) {
                            operatorId = signUpResponse.getData().getUserId();
                        }
                        Log.d(TAG, "Operator ID from response: " + operatorId);
                    }
                    
                    // Only navigate if signup was successful
                    if (isSuccess) {
                        // Show success message
                        String toastMsg = message != null && !message.isEmpty() ? message : "Operator registered successfully";
                        Toast.makeText(OperatorSignupActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Signup successful! Operator ID: " + operatorId + ", Phone: " + phone);
                        Log.d(TAG, "About to call navigateToLicenseDetails...");
                        
                        // Ensure we're still in a valid state before navigating
                        if (!isFinishing() && !isDestroyed()) {
                            // Navigate to License Details page
                            navigateToLicenseDetails(phone, operatorId);
                        } else {
                            Log.e(TAG, "Cannot navigate - activity is finishing or destroyed");
                            // Try navigation anyway as last resort
                            try {
                                navigateToLicenseDetails(phone, operatorId);
                            } catch (Exception e) {
                                Log.e(TAG, "Navigation failed: " + e.getMessage(), e);
                            }
                        }
                    } else {
                        // Signup failed - show error and don't navigate
                        String errorMsg = message != null && !message.isEmpty() ? message : "Signup failed. Please try again.";
                        Toast.makeText(OperatorSignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Signup failed - success flag is false: " + errorMsg);
                        // Don't navigate or finish - let user try again
                    }
                } else {
                    // Response not successful or body is null
                    Log.e(TAG, "Response not successful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    
                    // Even if response structure is unexpected, if HTTP is 200, try to navigate
                    if (response.code() == 200) {
                        Log.w(TAG, "HTTP 200 but response structure unexpected - attempting navigation anyway");
                        Toast.makeText(OperatorSignupActivity.this, "Please complete your license details", Toast.LENGTH_SHORT).show();
                        navigateToLicenseDetails(phone, 0);
                    } else {
                        Toast.makeText(OperatorSignupActivity.this, 
                            "Signup failed. Error code: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                        // Don't navigate or finish on error
                    }
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnOperatorSignup != null) btnOperatorSignup.setEnabled(true);
                
                Log.e(TAG, "Signup API call failed: " + t.getMessage(), t);
                t.printStackTrace();
                Toast.makeText(OperatorSignupActivity.this, 
                    "Network error. Please check your connection and try again.", 
                    Toast.LENGTH_SHORT).show();
                // Don't finish activity on network failure - let user retry
            }
        });
    }
    
    /**
     * Navigate to License Details Activity
     * This method ensures navigation always happens after signup
     */
    private void navigateToLicenseDetails(String phone, int operatorId) {
        Log.d(TAG, "=== STARTING NAVIGATION TO LICENSE DETAILS ===");
        Log.d(TAG, "Phone: " + phone + ", Operator ID: " + operatorId);
        Log.d(TAG, "Current Activity: " + this.getClass().getSimpleName());
        Log.d(TAG, "Is Activity Finishing: " + isFinishing());
        
        // Validate phone number
        if (phone == null || phone.isEmpty()) {
            Log.e(TAG, "Phone number is empty! Cannot navigate.");
            Toast.makeText(OperatorSignupActivity.this, 
                "Error: Phone number is required. Please try again.", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Ensure we're on the UI thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.w(TAG, "Not on main thread, posting to main thread");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navigateToLicenseDetails(phone, operatorId);
                }
            });
            return;
        }
        
        try {
            // Create intent - use simple constructor
            Intent intent = new Intent(OperatorSignupActivity.this, OperatorLicenseDetailsActivity.class);
            
            // Always pass phone number
            intent.putExtra("operator_phone", phone);
            Log.d(TAG, "Added operator_phone to intent: " + phone);
            
            // ALWAYS pass operator ID - even if 0, pass it as string
            intent.putExtra("operator_id", String.valueOf(operatorId));
            Log.d(TAG, "Added operator_id to intent: " + operatorId);
            
            // Don't use flags that might interfere - use standard navigation
            Log.d(TAG, "About to start OperatorLicenseDetailsActivity...");
            Log.d(TAG, "Intent extras - operator_id: " + intent.getStringExtra("operator_id") + 
                      ", operator_phone: " + intent.getStringExtra("operator_phone"));
            
            // Start the activity
            startActivity(intent);
            
            Log.d(TAG, "Activity started successfully! startActivity() returned");
            
            // Finish immediately - the new activity should handle itself
            Log.d(TAG, "Finishing OperatorSignupActivity immediately");
            finish();
            
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(OperatorSignupActivity.this, 
                "Error: License Details page not found. Please contact support.", 
                Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR in navigateToLicenseDetails: " + e.getMessage(), e);
            e.printStackTrace();
            
            // Try alternative navigation method using class name string
            try {
                Log.d(TAG, "Attempting alternative navigation method using class name string...");
                Intent intent = new Intent();
                intent.setClassName("com.simats.eathmover", "com.simats.eathmover.OperatorLicenseDetailsActivity");
                intent.putExtra("operator_phone", phone);
                intent.putExtra("operator_id", String.valueOf(operatorId));
                startActivity(intent);
                finish();
                Log.d(TAG, "Alternative navigation successful");
            } catch (Exception e2) {
                Log.e(TAG, "Alternative navigation also failed: " + e2.getMessage(), e2);
                e2.printStackTrace();
                Toast.makeText(OperatorSignupActivity.this, 
                    "Error navigating to license details: " + e2.getMessage() + 
                    ". Please restart the app and try again.", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }
}
