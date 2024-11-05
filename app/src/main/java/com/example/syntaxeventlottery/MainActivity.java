package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button adminButton;
    private Button userButton;
    private String deviceId; // Variable to store the device ID
    private UserRepository userRepository; // User repository instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UserRepository
        userRepository = new UserRepository();

        // Initialize buttons
        adminButton = findViewById(R.id.adminButton);
        userButton = findViewById(R.id.userButton);

        // Set click listener for the Admin button
        adminButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Admin Mode Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        // Set click listener for the User button
        userButton.setOnClickListener(v -> checkUserInDatabase());
    }

    /**
     * Checks if the user with the current device ID exists in the Firestore database.
     */
    private void checkUserInDatabase() {
        userRepository.checkEntrantExists(deviceId, new UserRepository.OnCheckEntrantExistsListener() {
            @Override
            public void onCheckComplete(boolean exists, Entrant entrant) {
                if (exists) {
                    openUserHomeActivity(entrant.getUserID());
                } else {
                    openUserProfileActivity();
                }
            }

            @Override
            public void onCheckError(Exception e) {
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
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
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
