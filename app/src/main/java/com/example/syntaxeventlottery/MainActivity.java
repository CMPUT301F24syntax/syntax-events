package com.example.syntaxeventlottery;

import android.content.Intent;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private static final String TAG = "MainActivity";

    private Button adminButton;
    private Button userButton;
    private String deviceId;
    private UserController userController;
    private User currentUser;

    private ActivityResultLauncher<String> requestPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel
        NotificationUtils.createNotificationChannel(this);

        // Initialize the permission launcher for notifications
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Notification permission granted.");
                    } else {
                        Toast.makeText(this, "Notification permission is required to receive notifications.", Toast.LENGTH_LONG).show();
                    }
                }
        );
        

        // Retrieve the device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UserController
        userController = new UserController(new UserRepository());

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null");
            Toast.makeText(this, "Error: Unable to initialize LocationManager", Toast.LENGTH_LONG).show();
            return;
        }
        

        // Initialize buttons
        adminButton = findViewById(R.id.adminButton);
        userButton = findViewById(R.id.userButton);

        // Set click listener for the Admin button
        adminButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Admin Mode Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AdminActivity.class); // This launches AdminActivity
            startActivity(intent);
        });

        // Set click listener for the User button
        userButton.setOnClickListener(v -> checkUserInDatabase());

    }

    /**
     * Checks if the user with the current device ID exists in the Firestore database.
     * If the user does not have an existing profile, launch the activity to create one.
     */
    private void checkUserInDatabase() {
        Log.d(TAG, "deviceId: " + deviceId);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted. Please enable it.", Toast.LENGTH_SHORT).show();
            return;
        }

        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceId);
                Log.d(TAG, "current user " + currentUser);
                if (currentUser == null) {
                    openCreateProfileActivity();
                } else {
                    openUserHomeActivity(currentUser.getUserID());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Main Activity", e);
                handleDatabaseError(e);
            }
        });
    }

    /**
     * Opens UserHomeActivity with the given user ID.
     *
     * @param userId The user ID to pass to the activity.
     */
    private void openUserHomeActivity(String userId) {
        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    /**
     * Opens CreateUserProfileActivity and passes the device ID.
     */
    private void openCreateProfileActivity() {
        Toast.makeText(this, "User Mode Selected", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, CreateUserProfileActivity.class);
        intent.putExtra("deviceId", deviceId);
        startActivity(intent);
    }

    /**
     * Handles database errors by showing an error message.
     *
     * @param e The exception that occurred.
     */
    private void handleDatabaseError(Exception e) {
        Toast.makeText(this, "Error checking device ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}