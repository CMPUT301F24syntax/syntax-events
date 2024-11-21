// OrganizerActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
public class OrganizerActivity extends AppCompatActivity implements EventController.EventControllerListener {

    private Button createEventButton;
    private Button backButton;
    private RecyclerView eventRecyclerView;
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

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(null, this);
        eventRecyclerView.setAdapter(eventAdapter);

        // Initialize EventController
        eventController = new EventController(this);

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
        eventController.loadEventsByOrganizerId(deviceID);
    }

    // EventControllerListener methods

    @Override
    public void onEventLoaded(Event event) {
        // Not used here
    }

    @Override
    public void onEventSaved() {
        // Not used here
    }

    @Override
    public void onEventListLoaded(List<Event> eventList) {
        if (eventList != null) {
            eventAdapter.updateEvents(eventList);
        } else {
            eventAdapter.updateEvents(new ArrayList<>());
        }
    }

    @Override
    public void onParticipantStatusChecked(boolean isInWaitingList, boolean isSelected, Event event) {

    }

    @Override
    public void onWaitingListJoined() {

    }

    @Override
    public void onWaitingListLeft() {

    }

    @Override
    public void onInvitationAccepted() {

    }

    @Override
    public void onInvitationDeclined() {

    }

    @Override
    public void onDrawPerformed() {

    }


    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPosterUpdated() {

    }
}