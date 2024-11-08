package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
 * The {@code UserHomeActivity} class represents the main screen for users after logging in.
 * It displays the current date and time, lists future events, and provides navigation buttons
 * to the organizer's functions, user profile, and news center.
 */
public class UserHomeActivity extends AppCompatActivity {

    /** TextView for displaying the current date and time. */
    private TextView dateTextView;

    /** RecyclerView for displaying a list of future events. */
    private RecyclerView futureEventsRecyclerView;

    /** Adapter for the RecyclerView to manage event items. */
    private EventAdapter eventAdapter;

    /** List of events fetched from Firestore to be displayed. */
    private List<Event> eventList;

    /** Firebase Firestore database instance. */
    private FirebaseFirestore db;

    /** ImageButton to navigate to the organizer functions. */
    private ImageButton organizerButton;

    /** ImageButton to navigate to the user profile. */
    private ImageButton profileButton;

    /** ImageButton to navigate to the news center. */
    private ImageButton newsButton;

    /** Unique device ID used to identify the user. */
    private String deviceId;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, then this Bundle contains the data it most recently
     *                           supplied in {@link #onSaveInstanceState}. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize views
        dateTextView = findViewById(R.id.dateTextView);
        organizerButton = findViewById(R.id.organizerButton);
        profileButton = findViewById(R.id.profileButton);
        newsButton = findViewById(R.id.newsButton);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get deviceId
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView for Future Events
        futureEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        futureEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        futureEventsRecyclerView.setAdapter(eventAdapter);

        // Load events from Firestore
        loadEventsFromFirestore();

        // Set click listener for Organizer button
        organizerButton.setOnClickListener(v -> checkUserAndNavigate());

        // Set click listener for Profile button
        profileButton.setOnClickListener(v -> {
            // Navigate to UserProfileActivity
            startActivity(new Intent(UserHomeActivity.this, UserProfileActivity.class));
        });

        // Set click listener for News button
        newsButton.setOnClickListener(v -> {
            // Navigate to NotificationCenterActivity
            startActivity(new Intent(UserHomeActivity.this, NotificationCenterActivity.class));
        });

        // Set up date and time updater
        updateDateTime();
    }

    /**
     * Checks the user's facility attribute and navigates accordingly.
     * If the facility is not set, navigates to {@link FacilityProfileActivity}.
     * If the facility is set, navigates to {@link }.
     */
    private void checkUserAndNavigate() {
        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String facility = document.getString("facility");

                        if (facility == null || facility.isEmpty()) {
                            // Facility is empty, navigate to FacilityProfileActivity
                            startActivity(new Intent(UserHomeActivity.this, FacilityProfileActivity.class));
                        } else {
                            // Facility is set, navigate to OrganizerActivity
                            startActivity(new Intent(UserHomeActivity.this, OrganizerActivity.class));
                        }
                    } else {
                        Log.d("Firestore", "No user found with device ID " + deviceId);
                        Toast.makeText(UserHomeActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking user", e);
                    Toast.makeText(UserHomeActivity.this, "Failed to check user information.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads events from Firestore and populates the future events list.
     * Fetches all events from the "events" collection and updates the RecyclerView.
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
     * Updates the date and time in the TextView every second.
     * Uses a background thread to update the UI thread with the current time.
     */
    private void updateDateTime() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Edmonton"));

        // Update the time every second
        new Thread(() -> {
            while (!isFinishing()) {
                runOnUiThread(() -> {
                    String currentDateTime = dateFormat.format(new Date());
                    dateTextView.setText(currentDateTime);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
