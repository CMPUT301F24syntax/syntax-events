// OrganizerActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerActivity allows the event organizer to view and manage their created events.
 */
public class OrganizerActivity extends AppCompatActivity {
    private static final String TAG = "Organizer Activity";

    private Button createEventButton;
    private Button backButton;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private String deviceID;
    private EventController eventController;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), this);
        eventRecyclerView.setAdapter(eventAdapter);

        // Initialize controllers
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository());

        fetchUpdatedUserInfo();

        // Create event button listener
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerActivity.this, OrganizerCreateEvent.class);
            startActivity(intent);
        });

        // Back button listener
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list
        fetchUpdatedUserInfo();
    }

    private void fetchUpdatedUserInfo() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                User currentUser = userController.getUserByDeviceID(deviceID);
                if (currentUser == null) {
                    Log.e(TAG, "Failed to get updated user info");
                    finish();
                }
                if (currentUser.getFacility() == null) {
                    // launch facility creation if user does not have a facility profile
                    startActivity(new Intent(OrganizerActivity.this, FacilityProfileActivity.class));
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

    private void loadEvents(String userId) {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                ArrayList<Event> updatedEvents = eventController.getOrganizerEvents(userId);
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