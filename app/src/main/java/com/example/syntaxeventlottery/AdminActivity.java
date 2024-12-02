package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The AdminActivity class represents the main admin interface,
 * allowing administrators to browse events, browse users, and navigate back.
 */
public class AdminActivity extends AppCompatActivity {

    /** Button for browsing events. */
    private Button browseEventsButton;

    /** Button for browsing users. */
    private Button browseUsersButton;

    /**
     * Called when the activity is first created.
     * Initializes the UI components and sets up click listeners for buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this contains the most
     *                           recent data supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

        // Initialize buttons
        browseEventsButton = findViewById(R.id.browseEventsButton);
        browseUsersButton = findViewById(R.id.browseUsersButton);

        // Set click listener for the Events button
        browseEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminEventsActivity.class);
            startActivity(intent);
        });

        // Set click listener for the Users button
        browseUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminUsersActivity.class);
            startActivity(intent);
        });

        // Set click listener for the Back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Finish the current activity and return
    }
}
