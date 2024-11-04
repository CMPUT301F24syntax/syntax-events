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

        // Initialize user repository
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
        userButton.setOnClickListener(v -> {
            // Check if the device ID exists in the Firestore database
            userRepository.checkEntrantExists(deviceId, new UserRepository.OnCheckEntrantExistsListener() {
                @Override
                public void onCheckComplete(boolean exists, Entrant entrant) {
                    if (exists) {
                        // Device ID exists, go to UserHomeActivity
                        Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                        intent.putExtra("ENTRANT_DATA", entrant); // Pass the Entrant data if needed
                        startActivity(intent);
                    } else {
                        // Device ID does not exist, go to UserProfileActivity
                        Toast.makeText(MainActivity.this, "User Mode Selected", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, CreateUserProfileActivity.class);
                        intent.putExtra("DEVICE_ID", deviceId); // Pass the device ID to UserProfileActivity
                        startActivity(intent);
                    }
                }

                @Override
                public void onCheckError(Exception e) {
                    // Handle the error (e.g., display a message)
                    Toast.makeText(MainActivity.this, "Error checking device ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}