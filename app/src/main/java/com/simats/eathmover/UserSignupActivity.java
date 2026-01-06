package com.simats.eathmover;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simats.eathmover.models.SignUpResponse;
import com.simats.eathmover.models.User;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSignupActivity extends AppCompatActivity {

    private static final String TAG = "UserSignupActivity";
    private EditText etName, etPhone, etAddress, etEmail, etPassword, etConfirmPassword;
    private Button signupButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);

        Toolbar toolbar = findViewById(R.id.toolbar_user_signup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        progressBar = findViewById(R.id.progress_bar); // May be null if not in layout

        signupButton = findViewById(R.id.btn_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    signUpUser();
                }
            }
        });
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void signUpUser() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate password
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format if provided
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (signupButton != null) {
            signupButton.setEnabled(false);
        }

        User user = new User(name, phone, address, email, password);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<SignUpResponse> call = apiService.createUser(user);

        call.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (signupButton != null) {
                    signupButton.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UserSignupActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserSignupActivity.this, UserLoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Show a detailed error dialog
                    String errorDetails = "An unknown error occurred.";
                    if (response.errorBody() != null) {
                        try {
                            errorDetails = "Error Code: " + response.code() + "\n\n" + response.errorBody().string();
                        } catch (IOException e) {
                            errorDetails = "Error reading server response.";
                        }
                    } else if (response.body() != null) {
                        errorDetails = response.body().getMessage();
                    } else {
                        errorDetails = "Response was not successful and the body was null.";
                    }
                    showErrorDialog("Signup Failed", errorDetails);
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (signupButton != null) {
                    signupButton.setEnabled(true);
                }
                // Show a detailed error dialog for network failures
                showErrorDialog("Network Error", "Could not connect to the server. Please check your connection.\n\nDetails: " + t.getMessage());
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
