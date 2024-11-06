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

import com.example.syntaxeventlottery.EventAdapter;
import com.example.syntaxeventlottery.OrganizerActivity;
import com.example.syntaxeventlottery.UserProfileActivity;
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
    private List<Event> eventList;  // List to hold future events
    private FirebaseFirestore db;
    private ImageButton organizerButton, profileButton, newsButton;
    private String deviceId;

    // New variables for "Events I Attend"
    private RecyclerView attendedEventsRecyclerView;
    private EventAdapter attendedEventAdapter;
    private List<Event> attendedEventList; // List to hold attended events
    private String deviceID;

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

        // Initialize event list and adapter for future events
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        futureEventsRecyclerView.setAdapter(eventAdapter);
        Log.d("RecyclerView", "Adapter set for RecyclerView with initial item count: " + eventAdapter.getItemCount());

        // Set up RecyclerView for "Events I Attend"
        attendedEventsRecyclerView = findViewById(R.id.attendingEventsRecyclerView);
        attendedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialize event list and adapter for attended events
        attendedEventList = new ArrayList<>();
        attendedEventAdapter = new EventAdapter(attendedEventList, this);
        attendedEventsRecyclerView.setAdapter(attendedEventAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();


        // Load future events and attended events from Firestore

        // Get deviceId
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Load events from Firestore

        loadEventsFromFirestore();
        loadAttendedEventsFromFirestore(); // Load events the user is attending

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

    // New method: Load the events the user is attending
    private void loadAttendedEventsFromFirestore() {
        // Get device's unique ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("Users").document(deviceID).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> waitingListEvents = (List<String>) document.get("waitingListEvents");
                        if (waitingListEvents != null && !waitingListEvents.isEmpty()) {
                            // Fetch each event in the waitingListEvents list
                            for (String eventID : waitingListEvents) {
                                db.collection("events").document(eventID).get()
                                        .addOnSuccessListener(eventDoc -> {
                                            if (eventDoc.exists()) {
                                                String eventName = eventDoc.getString("eventName");
                                                String facility = eventDoc.getString("facility");
                                                Date startDate = eventDoc.getDate("startDate");
                                                Date endDate = eventDoc.getDate("endDate");

                                                if (startDate != null && endDate != null) {
                                                    Event attendedEvent = new Event(eventID, eventName, null, facility, 1, startDate, endDate, "123");
                                                    attendedEventList.add(attendedEvent);
                                                    attendedEventAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error fetching event " + eventID, e));
                            }
                        } else {
                            Log.d("Firestore", "No events found in waitingListEvents for this user.");
                        }
                    } else {
                        Log.d("Firestore", "User document not found.");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserHomeActivity.this, "Failed to load attended events.", Toast.LENGTH_SHORT).show());
    }

    // Method to update the date and time every second
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
