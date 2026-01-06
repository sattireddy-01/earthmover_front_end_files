package com.simats.eathmover.utils;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.eathmover.EditProfileActivity;
import com.simats.eathmover.UserBookingsActivity;
import com.simats.eathmover.UserDashboardActivity;
import com.simats.eathmover.UserProfileActivity;

/**
 * Helper class to handle bottom navigation clicks consistently across all activities.
 */
public class BottomNavigationHelper {

    /**
     * Sets up bottom navigation click listener for any activity.
     * @param activity The activity that contains the bottom navigation
     * @param bottomNav The BottomNavigationView to set up
     */
    public static void setupBottomNavigation(AppCompatActivity activity, BottomNavigationView bottomNav) {
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == com.simats.eathmover.R.id.navigation_home) {
                if (!(activity instanceof UserDashboardActivity)) {
                    activity.startActivity(new Intent(activity, UserDashboardActivity.class));
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                }
                return true;
            } else if (itemId == com.simats.eathmover.R.id.navigation_book) {
                // Navigate to UserDashboardActivity and tell it to open the Book tab
                Intent intent = new Intent(activity, UserDashboardActivity.class);
                intent.putExtra("open_book_tab", true);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            } else if (itemId == com.simats.eathmover.R.id.navigation_history) {
                if (!(activity instanceof com.simats.eathmover.ServiceHistoryActivity)) {
                    activity.startActivity(new Intent(activity, com.simats.eathmover.ServiceHistoryActivity.class));
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                }
                return true;
            } else if (itemId == com.simats.eathmover.R.id.navigation_profile) {
                if (!(activity instanceof UserProfileActivity)) {
                    activity.startActivity(new Intent(activity, UserProfileActivity.class));
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                }
                return true;
            }
            return false;
        });
    }
}

