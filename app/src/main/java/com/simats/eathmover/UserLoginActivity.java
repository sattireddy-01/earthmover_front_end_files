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

import com.simats.eathmover.models.LoginRequest;
import com.simats.eathmover.models.LoginResponse;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLoginActivity extends AppCompatActivity {

    private static final String TAG = "UserLoginActivity";
    
    private EditText etEmailPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        sessionManager = new SessionManager(this);

        // Initialize views
        etEmailPhone = findViewById(R.id.et_login_email_phone);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Forgot password click listener
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserLoginActivity.this, ResetPasswordActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Login button click listener
        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performLogin();
                }
            });
        }
    }

    private void performLogin() {
        String emailPhone = etEmailPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (emailPhone.isEmpty()) {
            etEmailPhone.setError("Phone or Email is required");
            etEmailPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Check if input is email or phone
        boolean isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailPhone).matches();
        
        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
        }

        // Create login request
        LoginRequest loginRequest;
        if (isEmail) {
            loginRequest = new LoginRequest(null, password, "user", emailPhone);
        } else {
            loginRequest = new LoginRequest(emailPhone, password, "user");
        }

        // Make API call
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (btnLogin != null) {
                    btnLogin.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                        // Login successful
                        LoginResponse.LoginData data = loginResponse.getData();
                        
                        // Save session
                        // If user logged in with email, use emailPhone as email, otherwise use empty string
                        String email = isEmail ? emailPhone : "";
                        sessionManager.createLoginSession(
                            String.valueOf(data.getUserId()),
                            data.getName() != null ? data.getName() : "",
                            data.getPhone() != null ? data.getPhone() : emailPhone,
                            email,
                            "user"
                        );

                        // Navigate to user dashboard first
                        Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        
                        // Show success message after navigation
                        Toast.makeText(UserLoginActivity.this, 
                            loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login successful",
                            Toast.LENGTH_SHORT).show();
                        
                        finish();
                    } else {
                        // Login failed
                        String errorMsg = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Invalid credentials";
                        Toast.makeText(UserLoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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
                if (btnLogin != null) {
                    btnLogin.setEnabled(true);
                }
                
                handleLoginFailure(t);
            }
        });
    }

    private void handleLoginError(Response<LoginResponse> response) {
        String errorMsg = "Login failed. Please try again.";
        
        if (response.body() != null && response.body().getMessage() != null) {
            errorMsg = response.body().getMessage();
        } else {
            switch (response.code()) {
                case 401:
                    errorMsg = "Invalid phone/email or password";
                    break;
                case 404:
                    errorMsg = "User not found";
                    break;
                case 500:
                    errorMsg = "Server error. Please try again later.";
                    break;
                default:
                    errorMsg = "Login failed. Please check your credentials and try again.";
                    break;
            }
        }
        
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Login error: HTTP " + response.code());
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
