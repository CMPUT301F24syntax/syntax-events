package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button adminButton;
    private Button userButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        adminButton = findViewById(R.id.adminButton);
        userButton = findViewById(R.id.userButton);

        // Set click listener for the Admin button
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a Toast message or navigate to another activity
                Toast.makeText(MainActivity.this, "Admin Mode Selected", Toast.LENGTH_SHORT).show();

                // Example: Navigate to Admin activity
                // Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                // startActivity(intent);
            }
        });

        // Set click listener for the User button
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a Toast message or navigate to another activity
                Toast.makeText(MainActivity.this, "User Mode Selected", Toast.LENGTH_SHORT).show();

                // Example: Navigate to User activity
                // Intent intent = new Intent(MainActivity.this, UserActivity.class);
                // startActivity(intent);
            }
        });
    }
}
