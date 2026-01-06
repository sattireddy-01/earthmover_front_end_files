package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_options);

        Toolbar toolbar = findViewById(R.id.toolbar_login_options);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button userLoginButton = findViewById(R.id.btn_user_login_option);
        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginOptionsActivity.this, UserLoginActivity.class);
                startActivity(intent);
            }
        });

        Button operatorLoginButton = findViewById(R.id.btn_operator_login_option);
        operatorLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginOptionsActivity.this, OperatorLoginActivity.class);
                startActivity(intent);
            }
        });

        Button adminLoginButton = findViewById(R.id.btn_admin_login_option);
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginOptionsActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
