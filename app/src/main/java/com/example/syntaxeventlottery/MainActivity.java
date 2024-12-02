package com.example.syntaxeventlottery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * MainActivity is the entry point of the application.
 * It checks the user's device ID against predefined administrator IDs or database records to determine their role.
 * This activity manages user permissions, notification services, and navigation to Admin or User modes.
 */
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
    private TextView details;

    /**
     * Called when the activity is starting. Initializes UI components, services, and checks user permissions and roles.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayList<String> adminIds = new ArrayList<>(Arrays.asList("07c9f475dcac6a3e", "7c207232961b9c19", "93fe8a6349183a29"));

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

        // Check and request notification permission
        checkAndRequestNotificationPermission();

        // Start the foreground service
        startNotificationListenerService();

        // Check and request notification permission
        checkAndRequestNotificationPermission();

        // Retrieve the device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UserController
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null");
            Toast.makeText(this, "Error: Unable to initialize LocationManager", Toast.LENGTH_LONG).show();
            return;
        }
        userController = new UserController(new UserRepository(), locationManager);

        // Initialize UI
        adminButton = findViewById(R.id.adminButton);
        userButton = findViewById(R.id.userButton);
        details = findViewById(R.id.adminDetails);

        if (!adminIds.contains(deviceId)) {
            adminButton.setEnabled(false);
            adminButton.setVisibility(View.GONE);
            userButton.setEnabled(false);
            userButton.setVisibility(View.GONE);
            details.setVisibility(View.GONE);
            checkUserInDatabase();
        } else {
            details.setText("Your device has been recognized as an administrator. \n"+"How would you like to use the application?");
        }

        // Set click listener for the Admin button
        adminButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Admin Mode Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AdminActivity.class); // This launches AdminActivity
            startActivity(intent);
        });

        // Set click listener for the User button
        userButton.setOnClickListener(v -> checkUserInDatabase());

        // Check and request location permission
        // checkAndRequestLocationPermission();
    }


    /**
     * Starts the NotificationListenerService as a foreground service.
     */
    private void startNotificationListenerService() {
        Intent serviceIntent = new Intent(this, NotificationListenerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * Checks and requests notification permission for Android 13 and above.
     */
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Checks and requests location permission.
     */
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handles the result of permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted.");
            } else {
                Toast.makeText(this, "Notification permission is required to receive notifications.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Checks if the user with the current device ID exists in the Firestore database.
     * If the user does not have an existing profile, launch the activity to create one.
     */
    private void checkUserInDatabase() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "Location permission not granted. Please enable it.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceId);
                Log.d(TAG, "Current user: " + currentUser);
                if (currentUser == null) {
                    openCreateProfileActivity();
                } else {
                    userController.updateUserLocation_main(currentUser, MainActivity.this, new DataCallback<User>() {
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
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    /**
     * Opens CreateUserProfileActivity and passes the device ID.
     */
    private void openCreateProfileActivity() {
        Toast.makeText(this, "User Mode Selected", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, CreateUserProfileActivity.class);
        intent.putExtra("DEVICE_ID", deviceId);
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
