package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private Button adminButton;
    private Button userButton;
    private String deviceId; // Variable to store the device ID
    private UserController userController;
    private User currentUser;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null");
            Toast.makeText(this, "Error: Unable to initialize LocationManager", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize UserController
        userController = new UserController(new UserRepository(), locationManager);


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
        checkAndRequestLocationPermission();
    }


    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
                Log.d(TAG, "Location permission granted.");
            } else {
                // 权限被拒绝
                Toast.makeText(this, "Location permission is required to update user location.", Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Checks if the user with the current device ID exists in the Firestore database.
     * If user does not have an existing profile, launch the activity which creates one
     */
    private void checkUserInDatabase() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        userController = new UserController(new UserRepository(),locationManager);
        Log.d(TAG, "deviceId: " + deviceId);

        // 检查权限是否已授予
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted. Please enable it.", Toast.LENGTH_SHORT).show();
            return; // 停止操作
        }

        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceId);
                Log.d(TAG, "current user " + currentUser);
                if (currentUser == null) {
                    openCreateProfileActivity();
                } else {
                    // 更新用户位置
                    userController.updateUserLocation(currentUser, MainActivity.this, new DataCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            Log.d(TAG, "Location updated successfully for user: " + result.getUserID());
                            openUserHomeActivity(currentUser.getUserID());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error updating location", e);
                            Toast.makeText(MainActivity.this, "Error updating location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            openUserHomeActivity(currentUser.getUserID());
                        }
                    });
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
     * Opens UserProfileActivity and passes the device ID.
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
