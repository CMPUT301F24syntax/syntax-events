package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
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

    private Button createEventButton;  // Button to create a new event
    private Button backButton;         // Button to go back to the previous screen
    private RecyclerView eventRecyclerView; // RecyclerView to display events
    private EventAdapter eventAdapter; // Adapter for managing event list display
    private EventRepository eventRepository; // Event repository
    private EventController eventController; // Event controller
    private List<Event> eventList = new ArrayList<>(); // List to hold events
    private String deviceID;           // Device ID to identify the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        // Initialize repository and controller
        eventRepository = new EventRepository();
        eventController = new EventController(eventRepository);

        // Get device ID to identify the organizer
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, this);
        eventRecyclerView.setAdapter(eventAdapter);

        // Load events from event controller where organizerId matches deviceID
        loadEvents();

        // Navigate to Create Event page on button click
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerActivity.this, OrganizerCreateEvent.class);
                startActivity(intent);
            }
        });

        // Navigate back to Home page on button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Ends this activity and returns to the previous one
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list when returning to this activity
        loadEvents();
    }

    /**
     * Load events retrieved from eventController
     */
    private void loadEvents() {
        eventController.getAllEvents(new DataCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                eventList.clear(); // clear any previous events
                eventList.addAll(result);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(OrganizerActivity.this, "No events found, try creating an event!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}