package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.models.Admin;
import com.simats.eathmover.models.SignUpResponse;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSignupActivity extends AppCompatActivity {

    private EditText etAdminName, etAdminEmail, etAdminPassword, etAdminConfirmPassword;
    private Button adminSignupButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_signup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etAdminName = findViewById(R.id.et_admin_name);
        etAdminEmail = findViewById(R.id.et_admin_email);
        etAdminPassword = findViewById(R.id.et_admin_password);
        etAdminConfirmPassword = findViewById(R.id.et_admin_confirm_password);
        progressBar = findViewById(R.id.progress_bar); // May be null if not in layout

        adminSignupButton = findViewById(R.id.btn_admin_signup);
        adminSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    signUpAdmin();
                }
            }
        });

        TextView adminLoginTextView = findViewById(R.id.tv_admin_login);
        adminLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminSignupActivity.this, AdminLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateInput() {
        String name = etAdminName.getText().toString().trim();
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();
        String confirmPassword = etAdminConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Name, email and password are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void signUpAdmin() {
        String name = etAdminName.getText().toString().trim();
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (adminSignupButton != null) {
            adminSignupButton.setEnabled(false);
        }

        Admin admin = new Admin(name, email, password);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<SignUpResponse> call = apiService.createAdmin(admin);

        call.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (adminSignupButton != null) {
                    adminSignupButton.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminSignupActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminSignupActivity.this, AdminLoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Signup failed";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(AdminSignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (adminSignupButton != null) {
                    adminSignupButton.setEnabled(true);
                }
                
                String errorMessage = "Network error. Please check your connection.";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Unable to resolve host") || t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Cannot connect to server. Please check your internet connection.";
                    } else {
                        errorMessage = "Network error: " + t.getMessage();
                    }
                }
                Toast.makeText(AdminSignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
