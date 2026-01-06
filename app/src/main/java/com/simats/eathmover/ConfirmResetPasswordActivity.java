package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.PasswordResetConfirm;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmResetPasswordActivity extends AppCompatActivity {

    private EditText etOtp;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private String phone;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar_confirm_reset);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        phone = getIntent().getStringExtra("phone");
        role = getIntent().getStringExtra("role");
        if (role == null) {
            role = "user";
        }

        etOtp = findViewById(R.id.et_otp);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);

        Button changePasswordButton = findViewById(R.id.btn_change_password);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleChangePassword();
            }
        });
    }

    private void handleChangePassword() {
        String otp = etOtp.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (otp.length() != 6) {
            Toast.makeText(this, "OTP must be 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter new password and confirm it", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone == null || phone.isEmpty()) {
            String errorMsg = "admin".equals(role) ? "Invalid email, please restart reset process" : "Invalid phone number, please restart reset process";
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        PasswordResetConfirm confirm = new PasswordResetConfirm(phone, otp, newPassword, role);

        apiService.confirmPasswordReset(confirm).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ConfirmResetPasswordActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    Intent intent;
                    if ("admin".equalsIgnoreCase(role)) {
                        intent = new Intent(ConfirmResetPasswordActivity.this, AdminLoginActivity.class);
                    } else if ("operator".equalsIgnoreCase(role)) {
                        intent = new Intent(ConfirmResetPasswordActivity.this, OperatorLoginActivity.class);
                    } else {
                        intent = new Intent(ConfirmResetPasswordActivity.this, UserLoginActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Failed to change password";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    Toast.makeText(ConfirmResetPasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                String errorMessage = "Failed to change password";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("JsonReader") || t.getMessage().contains("malformed JSON")) {
                        errorMessage = "Server response error. Please check your API configuration.";
                    } else if (t.getMessage().contains("Unable to resolve host")) {
                        errorMessage = "Network error. Please check your internet connection.";
                    } else {
                        errorMessage = "Error: " + t.getMessage();
                    }
                }
                Toast.makeText(ConfirmResetPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
