package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private Button browseEventsButton;
    private Button browseUsersButton;

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
