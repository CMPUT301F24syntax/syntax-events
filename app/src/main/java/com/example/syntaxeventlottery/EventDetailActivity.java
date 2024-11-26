// EventDetailActivity.java
package com.example.syntaxeventlottery;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";

    // UI Components
    private ImageView posterImageView, qrCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, eventFacilityLocationTextView, eventFacilityNameTextView, eventDrawedStatusTextView;
    private Button joinWaitingListButton, leaveWaitingListButton, acceptInvitationButton, declineInvitationButton;
    private Button manageParticipantsButton, editInfoButton, drawButton;
    private ImageButton backButton;

    // Controller and Data
    private EventController eventController;
    private String eventID, deviceID;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        // initialize controller
        eventController = new EventController(new EventRepository());

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get event ID from intent
        eventID = getIntent().getStringExtra("eventID");

        if (eventID == null || eventID.isEmpty()) { // finish activity if there is error getting the event
            Log.e(TAG, "Couldn't get eventID from event");
            finish();
        } else {
            loadEvent();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload all event details when resuming activity to get most updated information
        loadEvent();
    }

    private void loadEvent() {
        // Refresh repository to get the latest data
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {

                // Fetch the event by ID from the controller
                event = eventController.getEventById(eventID);
                Log.d(TAG, "Refreshed repository, event details:"+event);
                // If the event is not found, show an error and finish
                if (event == null) {
                    Log.e(TAG, "Failed to find event in repository");
                    Toast.makeText(EventDetailActivity.this, "Failed to find the event", Toast.LENGTH_SHORT).show();
                    finish();  // Exit the activity as the event wasn't found
                    return;    // Early return to prevent the rest of the code from executing
                }
                updateUI(event); // display the most updated event details
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, e.toString());
                Toast.makeText(EventDetailActivity.this, "Failed to get updated data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI(Event event) {
        this.event = event;
        initializeUI(event);
        displayEventDetails(event);
    }

    private void displayEventDetails(Event event) {
        eventNameTextView.setText(event.getEventName());
        eventDescriptionTextView.setText(event.getDescription());
        eventFacilityNameTextView.setText("Facility Name: " +event.getFacilityName());
        eventFacilityLocationTextView.setText("Location: " + event.getFacilityLocation());
        eventStartDateTextView.setText("Start: "+ event.getStartDate().toString());
        eventEndDateTextView.setText("End: " + event.getEndDate().toString());
        eventCapacityTextView.setText("Capacity: " + String.valueOf(event.getCapacity()));
        eventDrawedStatusTextView.setText("Drawed: "+event.isDrawed());

        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(this).load(event.getPosterUrl()).into(posterImageView);
            Log.d(TAG, "Loaded poster image. poster url: " + event.getPosterUrl());
        } else {
            // load default poster
            Glide.with(this).load(R.drawable.ic_default_poster).into(posterImageView);
        }


        if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
            Glide.with(this).load(event.getQrCode()).into(qrCodeImageView);
            Log.d(TAG, "Loaded QR code image. qr code url: " + event.getQrCode());
        } else {
            // load missing qr code image
            Glide.with(this).load(R.drawable.default_qrcode).into(qrCodeImageView);
        }
    }


    private void initializeUI(Event event) {
        posterImageView = findViewById(R.id.eventPosterImageView);
        qrCodeImageView = findViewById(R.id.eventQRCodeImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        eventFacilityLocationTextView = findViewById(R.id.eventFLocationTextView);
        eventFacilityNameTextView = findViewById(R.id.eventFNameTextView);
        eventDrawedStatusTextView = findViewById(R.id.eventDrawedTextView);
        joinWaitingListButton = findViewById(R.id.joinEventButton);
        leaveWaitingListButton = findViewById(R.id.leaveEventButton);
        acceptInvitationButton = findViewById(R.id.acceptButton);
        declineInvitationButton = findViewById(R.id.rejectButton);

        manageParticipantsButton = findViewById(R.id.manageParticipantsButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        drawButton = findViewById(R.id.drawParticipantsButton);
        backButton = findViewById(R.id.backButton);

        configureButtonVisibility();
        setupButtonListeners();
    }

    private void configureButtonVisibility() {
        boolean isOrganizer = event.getOrganizerId().equals(deviceID);

        // Organizer buttons
        editInfoButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        manageParticipantsButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        drawButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        // Participant buttons
        if (!isOrganizer) {
            joinWaitingListButton.setVisibility(View.VISIBLE);
            leaveWaitingListButton.setVisibility(View.VISIBLE);
            // Additional participant-specific visibility logic
        } else {
            hideAllParticipantButtons();
        }

        // Draw button state
        if (isOrganizer && event.isDrawed()) {
            drawButton.setEnabled(false);
            drawButton.setText("Event Already Drawn");
        }
    }



    private void setupButtonListeners() {
        joinWaitingListButton.setOnClickListener(v -> eventController.addUserToWaitingList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error joining the waiting list", Toast.LENGTH_SHORT).show();
            }
        }));

        leaveWaitingListButton.setOnClickListener(v -> eventController.removeUserFromWaitingList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have left the waiting list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error joining the waiting list", Toast.LENGTH_SHORT).show();
            }
        }));

        drawButton.setOnClickListener(v -> {
            if (event.isDrawed()) {
                Toast.makeText(this, "Event draw has already occured", Toast.LENGTH_SHORT).show();
                return;
            }

            eventController.performDraw(event, new DataCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    Log.d(TAG, "Event draw performed: updated event info: "+  result);
                    Toast.makeText(EventDetailActivity.this, "Draw perfomed successfully", Toast.LENGTH_SHORT).show();
                    loadEvent();
                }
                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "Event draw error");
                    Toast.makeText(EventDetailActivity.this, "Event draw error", Toast.LENGTH_SHORT).show();
                }
            });
        });
        /*
        acceptInvitationButton.setOnClickListener(v -> eventController.acceptInvitation(event, deviceID));
        declineInvitationButton.setOnClickListener(v -> eventController.declineInvitation(event, deviceID));
        */
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "Back button is not initialized");
        }

        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event", event); // pass event object
            startActivity(intent);
        });

        manageParticipantsButton.setOnClickListener(v -> {
            if (event != null && deviceID.equals(event.getOrganizerId())) {
                Intent intent = new Intent(EventDetailActivity.this, EventParticipantsListActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            } else {
                Toast.makeText(EventDetailActivity.this, "You are not authorized to view the waiting list.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User is not authorized to view the waiting list.");
            }
        });
    }


    private void hideAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
    }
}
