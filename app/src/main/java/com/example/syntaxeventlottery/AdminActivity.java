package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The {@code AdminActivity} class provides the main interface for administrators,
 * allowing them to browse events and users within the application.
 */
public class AdminActivity extends AppCompatActivity {

    private Button browseEventsButton;
    private Button browseUsersButton;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}.
     *                           <b>Note: Otherwise, it is null.</b>
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
