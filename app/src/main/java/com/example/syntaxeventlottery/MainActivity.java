package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The {@code MainActivity} class serves as the entry point of the application.
 * It provides options for the user to select either Admin or User mode.
 * Depending on the selection, it navigates to the appropriate activity.
 */
public class MainActivity extends AppCompatActivity {

    /** Button to select Admin mode. */
    private Button adminButton;

    /** Button to select User mode. */
    private Button userButton;

    /** Variable to store the device ID. */
    private String deviceId;

    /** User repository instance for database operations. */
    private UserRepository userRepository;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState}. Note: Otherwise, it is null.
     */
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
            Intent intent = new Intent(MainActivity.this, AdminActivity.class); // Launches AdminActivity
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
     * Opens the {@link UserHomeActivity} with the given user ID.
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
     * Opens the {@link CreateUserProfileActivity} and passes the device ID.
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
