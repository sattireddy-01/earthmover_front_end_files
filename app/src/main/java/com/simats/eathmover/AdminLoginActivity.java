package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.config.ApiConfig;
import com.simats.eathmover.models.LoginRequest;
import com.simats.eathmover.models.LoginResponse;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "AdminLoginActivity";
    
    private EditText etAdminEmail;
    private EditText etAdminPassword;
    private Button btnAdminLogin;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_login);
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
        etAdminEmail = findViewById(R.id.et_admin_login_email);
        etAdminPassword = findViewById(R.id.et_admin_login_password);
        btnAdminLogin = findViewById(R.id.btn_admin_login_submit);
        tvForgotPassword = findViewById(R.id.tv_admin_forgot_password);
        progressBar = findViewById(R.id.progress_bar); // May be null if not in layout

        // Forgot password click listener
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminLoginActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("role", "admin");
                    startActivity(intent);
                }
            });
        }

        // Login button click listener
        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performLogin();
                }
            });
        }
    }

    private void performLogin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etAdminEmail.setError("Email is required");
            etAdminEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etAdminEmail.setError("Please enter a valid email address");
            etAdminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etAdminPassword.setError("Password is required");
            etAdminPassword.requestFocus();
            return;
        }

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnAdminLogin != null) {
            btnAdminLogin.setEnabled(false);
        }

        // Create login request - Admin uses email
        LoginRequest loginRequest = new LoginRequest(null, password, "admin", email);

        // Log the request details for debugging
        Log.d(TAG, "Attempting admin login for: " + email);
        Log.d(TAG, "Base URL: " + ApiConfig.getBaseUrl());
        Log.d(TAG, "Full URL: " + ApiConfig.getBaseUrl() + "auth/admin_login.php");

        // Make API call using RetrofitClient
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.adminLogin(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (btnAdminLogin != null) {
                    btnAdminLogin.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                        // Login successful
                        LoginResponse.LoginData data = loginResponse.getData();
                        
                        // Save session
                        sessionManager.createAdminSession(
                            String.valueOf(data.getUserId()),
                            data.getName() != null ? data.getName() : "",
                            email,
                            "admin"
                        );

                        // Navigate to admin dashboard
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        
                        // Show success message
                        Toast.makeText(AdminLoginActivity.this, 
                            loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login successful",
                            Toast.LENGTH_SHORT).show();
                        
                        finish();
                    } else {
                        // Login failed
                        String errorMsg = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Invalid email or password";
                        Toast.makeText(AdminLoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (btnAdminLogin != null) {
                    btnAdminLogin.setEnabled(true);
                }
                
                handleLoginFailure(t);
            }
        });
    }

    private void handleLoginError(Response<LoginResponse> response) {
        String errorMsg = "Login failed. Please try again.";
        
        try {
            // First, try to read error body for detailed error message
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Error response body: " + errorBody);
                
                // Check if server returned HTML error page (common with PHP errors)
                if (errorBody.contains("<!DOCTYPE") || errorBody.contains("<html") || errorBody.contains("Fatal error") || errorBody.contains("Parse error")) {
                    errorMsg = "Server configuration error. Check XAMPP error logs.";
                    Log.e(TAG, "Server returned HTML error page - PHP script may have errors");
                } else {
                    // Try to parse as JSON
                    try {
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBody);
                        if (errorJson.has("message")) {
                            errorMsg = errorJson.getString("message");
                        } else if (errorJson.has("error")) {
                            errorMsg = errorJson.getString("error");
                        } else {
                            errorMsg = "Server error: " + errorBody.substring(0, Math.min(100, errorBody.length()));
                        }
                    } catch (Exception e) {
                        // Not valid JSON, use raw error
                        errorMsg = "Server error: " + errorBody.substring(0, Math.min(100, errorBody.length()));
                    }
                }
            } else if (response.body() != null && response.body().getMessage() != null) {
                errorMsg = response.body().getMessage();
            } else {
                // Use HTTP status code to determine error
                switch (response.code()) {
                    case 401:
                        errorMsg = "Invalid email or password";
                        break;
                    case 404:
                        errorMsg = "Admin login endpoint not found. Check API path.";
                        Log.e(TAG, "404 Error - URL might be wrong. Expected: " + ApiConfig.getBaseUrl() + "auth/admin_login.php");
                        break;
                    case 500:
                        errorMsg = "Server internal error. Check XAMPP error logs.";
                        Log.e(TAG, "500 Error - PHP script may have errors. Check: C:\\xampp\\apache\\logs\\error.log");
                        break;
                    default:
                        errorMsg = "Login failed (HTTP " + response.code() + "). Please try again.";
                        break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading response body: " + e.getMessage());
            errorMsg = "Error reading server response: " + e.getMessage();
        }
        
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Login error: HTTP " + response.code() + " | URL: " + response.raw().request().url());
    }

    private void handleLoginFailure(Throwable t) {
        String errorMessage = "Network error. Please check your connection.";
        if (t.getMessage() != null) {
            String errorMsg = t.getMessage();
            if (errorMsg.contains("Unable to resolve host") || errorMsg.contains("Failed to connect")) {
                errorMessage = "Cannot connect to server. Please check your internet connection.";
            } else if (errorMsg.contains("timeout")) {
                errorMessage = "Connection timeout. Please try again.";
            } else if (errorMsg.contains("JsonReader") || errorMsg.contains("malformed JSON")) {
                errorMessage = "Server response error. Expected JSON but received invalid data.";
            } else {
                errorMessage = "Error: " + errorMsg;
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Login API call failed: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"), t);
    }
}
