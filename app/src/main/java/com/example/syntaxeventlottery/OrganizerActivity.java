// OrganizerActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * The {@code OrganizerActivity} class allows event organizers to view and manage their events.
 * It provides functionality to create new events, manage facilities, and view the list of events
 * created by the organizer.
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";

    // UI Components
    private Button createEventButton;
    private Button backButton;
    private Button manageFacilityButton;
    private RecyclerView eventRecyclerView;
    private TextView organizerEventDetailTextView;

    // Controllers
    private EventController eventController;
    private UserController userController;

    // Data
    private String deviceID;
    public User currentUser;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        manageFacilityButton = findViewById(R.id.manageFacilityButton);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        organizerEventDetailTextView = findViewById(R.id.organizerEventDetailTextView);

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), this);
        eventRecyclerView.setAdapter(eventAdapter);

        // Initialize controllers
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository());

        // Fetch updated user information
        fetchUpdatedUserInfo();

        // Set up button listeners
        manageFacilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerActivity.this, ManageFacilityProfileActivity.class);
            startActivity(intent);
        });

        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerActivity.this, OrganizerCreateEvent.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerActivity.this, UserHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Clears the back stack
            startActivity(intent);
            finish(); // Close OrganizerActivity
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list
        fetchUpdatedUserInfo();
    }

    /**
     * Fetches updated user information from the repository.
     * If the user does not have a facility profile, launches the facility profile creation activity.
     */
    private void fetchUpdatedUserInfo() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceID);
                if (currentUser == null) {
                    Log.e(TAG, "Failed to get updated user info");
                    finish();
                }
                if (currentUser.getFacility() == null) {
                    // Launch facility creation if the user does not have a facility profile
                    Log.d(TAG, "Current user facility not found, launching FacilityProfileActivity");
                    Intent intent = new Intent(OrganizerActivity.this, FacilityProfileActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    startActivity(intent);
                } else {
                    loadEvents(currentUser.getUserID());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching user data", e);
                Toast.makeText(OrganizerActivity.this, "Couldn't find user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Loads the events created by the organizer and updates the RecyclerView.
     *
     * @param userId The ID of the user (organizer) whose events are to be loaded.
     */
    public void loadEvents(String userId) {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                ArrayList<Event> updatedEvents = eventController.getOrganizerEvents(userId);

                if (updatedEvents.isEmpty()) {
                    organizerEventDetailTextView.setText("You have not created any events.\nClick \"Create Event\" to host an event!");
                } else {
                    organizerEventDetailTextView.setText("My Events");
                }
                eventAdapter.updateEvents(updatedEvents);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing events: " + e.getMessage(), e);
                Toast.makeText(OrganizerActivity.this, "Failed to load events. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
