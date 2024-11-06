// UserHomeActivity.java

package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Activity representing the user's home screen.
 */
public class UserHomeActivity extends AppCompatActivity {

    private TextView dateTextView;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private RecyclerView futureEventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;  // List to hold events
    private FirebaseFirestore db;
    private ImageButton organizerButton, profileButton, newsButton;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize the TextView for displaying the date and time
        dateTextView = findViewById(R.id.dateTextView);

        // Initialize the buttons
        organizerButton = findViewById(R.id.organizerButton);
        profileButton = findViewById(R.id.profileButton);
        newsButton = findViewById(R.id.newsButton);

        // Set up RecyclerView for Future Events
        futureEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        futureEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        futureEventsRecyclerView.setAdapter(eventAdapter);
        Log.d("RecyclerView", "Adapter set for RecyclerView with initial item count: " + eventAdapter.getItemCount());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get deviceId
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Load events from Firestore
        loadEventsFromFirestore();

        // Set click listener on Organizer, Profile, and News buttons
        organizerButton.setOnClickListener(v -> startActivity(new Intent(UserHomeActivity.this, OrganizerActivity.class)));
        profileButton.setOnClickListener(v -> startActivity(new Intent(UserHomeActivity.this, UserProfileActivity.class)));
        newsButton.setOnClickListener(v -> startActivity(new Intent(UserHomeActivity.this, NotificationCenterActivity.class)));

        // Set up the date and time updater
        updateDateTime();
    }

    /**
     * Loads events from Firestore.
     */
    private void loadEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        eventList.clear(); // Clear the list to avoid duplication
                        Log.d("Firestore", "Fetched " + task.getResult().size() + " events from Firestore.");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Parse document data into an Event object
                            String eventName = document.getString("eventName");
                            String eventID = document.getId();
                            String description = document.getString("description");
                            String facility = document.getString("facility");
                            String qrCodeUrl = document.getString("qrCodeUrl");
                            String posterUrl = document.getString("posterUrl");

                            // Get startDate and endDate as Date objects
                            Date startDate = document.getDate("startDate");
                            Date endDate = document.getDate("endDate");

                            if (startDate != null && endDate != null) {
                                int capacity = document.getLong("capacity").intValue();

                                // Create and add the Event object to the list
                                Event event = new Event(eventID, eventName, description, facility, capacity, startDate, endDate, qrCodeUrl);
                                event.setEventID(eventID);
                                event.setQrCodeUrl(qrCodeUrl);
                                event.setPosterUrl(posterUrl);
                                eventList.add(event);
                                Log.d("Firestore", "Added event: " + eventName);
                            } else {
                                Log.e("Firestore", "startDate or endDate is null for event: " + eventName);
                            }
                        }
                        eventAdapter.notifyDataSetChanged();  // Refresh adapter to display data
                        Log.d("RecyclerView", "Adapter updated with item count: " + eventAdapter.getItemCount());
                    } else {
                        Log.e("Firestore", "Error loading events: ", task.getException());
                    }
                });
    }

    /**
     * Updates the date and time display.
     */
    private void updateDateTime() {
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