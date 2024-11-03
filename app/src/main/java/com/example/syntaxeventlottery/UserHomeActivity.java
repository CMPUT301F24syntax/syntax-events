package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class UserHomeActivity extends AppCompatActivity {

    private TextView dateTextView;
    private Handler handler;
    private Runnable updateTimeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize the TextView for displaying the date and time
        dateTextView = findViewById(R.id.dateTextView);

        // Initialize the Organizer button
        ImageButton organizerButton = findViewById(R.id.organizerButton);

        // Set a click listener on the Organizer button
        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the OrganizerActivity
                Intent intent = new Intent(UserHomeActivity.this, OrganizerActivity.class);
                startActivity(intent);
            }
        });

        // Handler to update the time every second
        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // Set up the date format and timezone for Edmonton
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Edmonton"));

                // Get the current date and time in Edmonton
                String currentDateTime = dateFormat.format(new Date());

                // Set the current date and time to the TextView
                dateTextView.setText(currentDateTime);

                // Schedule the next update after 1 second
                handler.postDelayed(this, 1000);
            }
        };

        // Start the update process
        handler.post(updateTimeRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to avoid memory leaks
        if (handler != null && updateTimeRunnable != null) {
            handler.removeCallbacks(updateTimeRunnable);
        }
    }
}
