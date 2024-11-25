package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private Button adminButton;
    private Button userButton;
    private String deviceId; // Variable to store the device ID
    private UserController userController;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize userController
        userController = new UserController(new UserRepository());

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
     * If user does not have an existing profile, launch the activity which creates one
     */
    private void checkUserInDatabase() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getEntrantByDeviceID(deviceId);
                if (currentUser == null) { // this means user does not have an associated account
                    openUserProfileActivity();
                } else {
                    openUserHomeActivity(currentUser.getUserID());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG,"Main Activity", e);
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
     * Opens UserProfileActivity and passes the device ID.
     */
    private void openUserProfileActivity() {
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
