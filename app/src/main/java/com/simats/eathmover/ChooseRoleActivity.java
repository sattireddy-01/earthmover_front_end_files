package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        Toolbar toolbar = findViewById(R.id.toolbar_choose_role);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button userSignupButton = findViewById(R.id.btn_user_signup);
        userSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseRoleActivity.this, UserSignupActivity.class);
                startActivity(intent);
            }
        });

        Button operatorSignupButton = findViewById(R.id.btn_operator_signup);
        operatorSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseRoleActivity.this, OperatorSignupActivity.class);
                startActivity(intent);
            }
        });

        Button adminSignupButton = findViewById(R.id.btn_admin_signup);
        adminSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseRoleActivity.this, AdminSignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
