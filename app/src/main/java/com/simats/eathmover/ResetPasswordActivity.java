package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.PasswordResetRequest;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etResetPhone;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar_reset_password);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        role = getIntent().getStringExtra("role");
        if (role == null) {
            role = "user"; // default
        }

        etResetPhone = findViewById(R.id.et_reset_email_phone);
        TextView tvDescription = findViewById(R.id.tv_reset_description);
        
        // Update hint and description based on role
        if ("admin".equals(role)) {
            etResetPhone.setHint("Registered Email");
            tvDescription.setText("Enter your registered email to receive an OTP.");
        } else if ("operator".equals(role)) {
            etResetPhone.setHint("Registered Mobile Number / Email");
            tvDescription.setText("Enter your registered mobile number or email to receive an OTP.");
        } else {
            // For users, use email for password reset
            etResetPhone.setHint("Registered Email");
            tvDescription.setText("Enter your registered email to receive an OTP.");
        }

        Button sendCodeButton = findViewById(R.id.btn_send_code);
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String identifier = etResetPhone.getText().toString().trim();

                if (identifier.isEmpty()) {
                    if ("admin".equals(role) || "user".equals(role)) {
                        Toast.makeText(ResetPasswordActivity.this, "Please enter registered email", Toast.LENGTH_SHORT).show();
                    } else if ("operator".equals(role)) {
                        Toast.makeText(ResetPasswordActivity.this, "Please enter registered mobile number or email", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                
                // Validate email format for user and admin
                if (("user".equals(role) || "admin".equals(role)) && !android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                requestOtp(identifier, role);
            }
        });
    }

    private void requestOtp(final String identifier, final String role) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        PasswordResetRequest request;
        if ("admin".equals(role) || "user".equals(role)) {
            // For admin and user, use email field
            request = new PasswordResetRequest(identifier, role, true);
        } else {
            // For operator, use phone field (can be phone or email)
            request = new PasswordResetRequest(identifier, role);
        }

        apiService.requestPasswordReset(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    GenericResponse body = response.body();
                    if (body != null) {
                        if (body.isSuccess()) {
                            Toast.makeText(ResetPasswordActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPasswordActivity.this, ConfirmResetPasswordActivity.class);
                            intent.putExtra("phone", identifier);
                            intent.putExtra("role", role);
                            startActivity(intent);
                        } else {
                            String msg = body.getMessage();
                            if (msg == null || msg.isEmpty()) {
                                msg = "Failed to send OTP";
                            }
                            Toast.makeText(ResetPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Server returned empty response. Code: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle error response - check if it's HTML (backend error page)
                    String errorMsg = "Failed to send OTP";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            
                            // Check if response is HTML (backend error page)
                            if (errorBody.contains("<!DOCTYPE") || errorBody.contains("<html") || errorBody.contains("DOCTYPE HTML")) {
                                // Backend returned HTML error page instead of JSON
                                switch (response.code()) {
                                    case 404:
                                        errorMsg = "Password reset endpoint not found. Please check backend configuration.";
                                        break;
                                    case 500:
                                        errorMsg = "Server error. Please contact support.";
                                        break;
                                    default:
                                        errorMsg = "Server returned an error page. Please check backend configuration.";
                                        break;
                                }
                            } else {
                                // Try to parse as JSON error message
                                if (errorBody.length() > 200) {
                                    errorBody = errorBody.substring(0, 200) + "...";
                                }
                                // Only show error body if it's not HTML
                                if (!errorBody.trim().startsWith("<")) {
                                    errorMsg = "Error: " + errorBody;
                                } else {
                                    errorMsg = "Server error (HTTP " + response.code() + "). Please try again.";
                                }
                            }
                        } else {
                            // Use status code to determine error
                            switch (response.code()) {
                                case 404:
                                    errorMsg = "Password reset service not found. Please check backend configuration.";
                                    break;
                                case 500:
                                    errorMsg = "Server error. Please try again later.";
                                    break;
                                case 400:
                                    errorMsg = "Invalid request. Please check your email and try again.";
                                    break;
                                default:
                                    errorMsg = "Error: HTTP " + response.code();
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        errorMsg = "Error: HTTP " + response.code() + ". Please try again.";
                    }
                    Toast.makeText(ResetPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                String errorMessage = "Failed to send OTP";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("JsonReader") || t.getMessage().contains("malformed JSON")) {
                        errorMessage = "Server response error. Please check your API configuration.";
                    } else if (t.getMessage().contains("Unable to resolve host")) {
                        errorMessage = "Network error. Please check your internet connection.";
                    } else {
                        errorMessage = "Error: " + t.getMessage();
                    }
                }
                Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
