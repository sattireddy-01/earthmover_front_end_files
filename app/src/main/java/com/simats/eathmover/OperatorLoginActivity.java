package com.simats.eathmover;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.simats.eathmover.models.LoginRequest;
import com.simats.eathmover.models.LoginResponse;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;
import com.simats.eathmover.utils.SessionManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorLoginActivity extends AppCompatActivity {

    private EditText etOperatorPhone;
    private EditText etOperatorPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;
    private static final String TAG = "OperatorLoginActivity";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_login);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_operator_login);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Initialize views
        etOperatorPhone = findViewById(R.id.et_operator_login_email); // Layout uses email field for phone/email
        etOperatorPassword = findViewById(R.id.et_operator_login_password);
        btnLogin = findViewById(R.id.btn_operator_login);
        tvForgotPassword = findViewById(R.id.tv_operator_forgot_password);
        progressBar = findViewById(R.id.progress_bar); // May be null if not in layout

        // Forgot Password link
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(OperatorLoginActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("role", "operator");
                    startActivity(intent);
                }
            });
        }

        // Login button
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
        String phone = etOperatorPhone.getText().toString().trim();
        String password = etOperatorPassword.getText().toString().trim();

        // Validation
        if (phone.isEmpty()) {
            etOperatorPhone.setError("Phone number is required");
            etOperatorPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etOperatorPassword.setError("Password is required");
            etOperatorPassword.requestFocus();
            return;
        }

        // Check if input is email or phone
        boolean isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(phone).matches();

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
        }

        // Make API call - support both email and phone
        LoginRequest loginRequest;
        if (isEmail) {
            loginRequest = new LoginRequest(null, password, "operator", phone);
        } else {
            loginRequest = new LoginRequest(phone, password, "operator");
        }
        
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
                        sessionManager.createOperatorSession(
                            String.valueOf(data.getUserId()),
                            data.getName() != null ? data.getName() : "",
                            data.getPhone() != null ? data.getPhone() : phone,
                            "" // Email not in LoginData, can be fetched later
                        );

                        Toast.makeText(OperatorLoginActivity.this, 
                            loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login successful",
                            Toast.LENGTH_SHORT).show();

                        // Navigate to operator dashboard
                        Intent intent = new Intent(OperatorLoginActivity.this, OperatorDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        String errorMsg = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Invalid phone or password";
                        Toast.makeText(OperatorLoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                if (errorBody.contains("<!DOCTYPE") || errorBody.contains("<html")) {
                    errorMsg = "Server returned an error page. Please check backend configuration.";
                } else {
                    errorMsg = "Error: " + errorBody;
                }
            } else {
                switch (response.code()) {
                    case 401:
                        errorMsg = "Invalid phone or password";
                        break;
                    case 404:
                        errorMsg = "Login service not found. Please check backend configuration.";
                        break;
                    case 500:
                        errorMsg = "Server error. Please try again later.";
                        break;
                    default:
                        errorMsg = "Login failed. HTTP " + response.code();
                        break;
                }
            }
        } catch (IOException e) {
            errorMsg = "Error reading server response: " + e.getMessage();
        }
        Toast.makeText(OperatorLoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    private void handleLoginFailure(Throwable t) {
        String errorMessage = "Network error. Please check your connection.";
        if (t.getMessage() != null) {
            if (t.getMessage().contains("Unable to resolve host") || t.getMessage().contains("Failed to connect")) {
                errorMessage = "Cannot connect to server. Please check your internet connection.";
            } else if (t.getMessage().contains("timeout")) {
                errorMessage = "Connection timeout. Please try again.";
            } else if (t.getMessage().contains("JsonReader") || t.getMessage().contains("malformed JSON")) {
                errorMessage = "Server response error. Expected JSON but received invalid data.";
            } else {
                errorMessage = "Error: " + t.getMessage();
            }
        }
        Toast.makeText(OperatorLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Login API call failed: " + t.getMessage(), t);
    }
}
