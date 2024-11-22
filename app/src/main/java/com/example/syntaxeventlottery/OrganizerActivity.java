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
    private ArrayList<Event> eventsDataList;
    private EventAdapter eventAdapter;
    private String deviceID;
    private EventController eventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        // initalize events data list
        eventsDataList = new ArrayList<>();

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventsDataList, this);
        eventRecyclerView.setAdapter(eventAdapter);

        // Initialize EventController
        eventController = new EventController(new EventRepository());

        // Load events
        loadEvents();

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
        loadEvents();
    }

    private void loadEvents() {
        // get the most updated events
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // clear local list and retrieve newest data
                eventsDataList.clear();
                eventsDataList.addAll(eventController.getOrganizerEvents(deviceID));
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Failed to load events",e);
                Toast.makeText(OrganizerActivity.this, "Failed to load events. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}