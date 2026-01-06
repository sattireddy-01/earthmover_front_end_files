package com.simats.eathmover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.utils.SessionManager;

/**
 * Activity for navigating to user location with map view.
 */
public class NavigationActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private Button btnNavigate;
    private Button btnCallUser;
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button btnCenterMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        sessionManager = new SessionManager(this);

        // Initialize views
        btnNavigate = findViewById(R.id.btn_navigate);
        btnCallUser = findViewById(R.id.btn_call_user);
        btnZoomIn = findViewById(R.id.btn_zoom_in);
        btnZoomOut = findViewById(R.id.btn_zoom_out);
        btnCenterMap = findViewById(R.id.btn_center_map);

        // Setup toolbar (if needed)
        // Toolbar can be added if back navigation is required

        // Navigate button
        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startNavigation();
                }
            });
        }

        // Call User button
        if (btnCallUser != null) {
            btnCallUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callUser();
                }
            });
        }

        // Zoom In button
        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomIn();
                }
            });
        }

        // Zoom Out button
        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomOut();
                }
            });
        }

        // Center Map button
        if (btnCenterMap != null) {
            btnCenterMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    centerMap();
                }
            });
        }

        // TODO: Initialize map (Google Maps, Mapbox, etc.)
        initializeMap();
    }

    private void initializeMap() {
        // TODO: Initialize actual map view (Google Maps, Mapbox, etc.)
        // For now, using placeholder image
        Toast.makeText(this, "Map initialized", Toast.LENGTH_SHORT).show();
    }

    private void startNavigation() {
        // TODO: Get user location from booking/API
        // For now, using a sample location
        String destination = "User Location"; // Replace with actual coordinates
        
        // Open in Google Maps or navigation app
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to web maps
                Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destination);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Navigation not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void callUser() {
        // TODO: Get user phone number from booking/API
        String phoneNumber = "+1234567890"; // Replace with actual phone number
        
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomIn() {
        // TODO: Implement map zoom in
        Toast.makeText(this, "Zooming in", Toast.LENGTH_SHORT).show();
    }

    private void zoomOut() {
        // TODO: Implement map zoom out
        Toast.makeText(this, "Zooming out", Toast.LENGTH_SHORT).show();
    }

    private void centerMap() {
        // TODO: Center map on user location
        Toast.makeText(this, "Centering map", Toast.LENGTH_SHORT).show();
    }
}

