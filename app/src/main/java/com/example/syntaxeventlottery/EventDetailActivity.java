package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// Import Firebase and other necessary libraries
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;


/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity implements EventController.EventControllerListener {

    // UI Components
    private ImageView posterImageView;
    private ImageView qrCodeImageView;
    private TextView eventNameTextView;
    private TextView eventDescriptionTextView;
    private TextView eventStartDateTextView;
    private TextView eventEndDateTextView;
    private TextView eventCapacityTextView;

    // Participant Buttons
    private Button joinWaitingListButton;
    private Button leaveWaitingListButton;
    private Button acceptInvitationButton;
    private Button declineInvitationButton;

    // Organizer Buttons
    private Button drawButton;
    private Button updatePosterButton;
    private Button editInfoButton;
    private Button waitingListButton;

    // Controller and Data
    private EventController eventController;
    private String eventId;
    private Event event;
    private String deviceId;

    private ImageButton backButton;


    private static final int REQUEST_CODE_SELECT_POSTER = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize UI components
        initializeUI();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("event_id");

        // Get device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize EventController
        eventController = new EventController(this);

        // Load event details
        eventController.loadEventDetails(eventId);

        // Set up button listeners
        setupButtonListeners();
    }

    private void initializeUI() {
        // Initialize views
        posterImageView = findViewById(R.id.eventPosterImageView);
        qrCodeImageView = findViewById(R.id.eventQRCodeImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);

        joinWaitingListButton = findViewById(R.id.joinEventButton);
        leaveWaitingListButton = findViewById(R.id.leaveEventButton);
        acceptInvitationButton = findViewById(R.id.acceptButton);
        declineInvitationButton = findViewById(R.id.rejectButton);

        drawButton = findViewById(R.id.drawButton);
        updatePosterButton = findViewById(R.id.updatePosterButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        waitingListButton = findViewById(R.id.waitingListButton);

        backButton = findViewById(R.id.backButton);
        editInfoButton = findViewById(R.id.editInfoButton);
    }

    private void setupButtonListeners() {
        joinWaitingListButton.setOnClickListener(v -> eventController.joinWaitingList(eventId, deviceId));

        leaveWaitingListButton.setOnClickListener(v -> eventController.leaveWaitingList(eventId, deviceId));

        acceptInvitationButton.setOnClickListener(v -> eventController.acceptInvitation(eventId, deviceId));

        declineInvitationButton.setOnClickListener(v -> eventController.declineInvitation(eventId, deviceId));

        drawButton.setOnClickListener(v -> {
            if (event != null && !event.isDrawed()) {
                eventController.performDraw(eventId);
            } else {
                Toast.makeText(this, "The draw has already been performed.", Toast.LENGTH_SHORT).show();
            }
        });

        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();  // Simply closes the current activity
                }
            });
        } else {
            Log.e("EventDetailActivity", "Back button is not initialized");
        }

        editInfoButton.setOnClickListener(v -> {
            // Open the EditEventActivity to edit event details
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        updatePosterButton.setOnClickListener(v -> {
            // Open image picker to select a new poster
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_SELECT_POSTER);
        });


        waitingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the current user is the organizer
                if (deviceId.equals(event.getOrganizerId())) {
                    Intent intent = new Intent(EventDetailActivity.this, EventWaitingListActivity.class);
                    intent.putExtra("eventId", eventId);  // Assuming you have a variable 'eventId'
                    startActivity(intent);
                } else {
                    Toast.makeText(EventDetailActivity.this, "You are not authorized to view the waiting list.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onEventLoaded(Event event) {
        this.event = event;
        displayEventDetails(event);

        // Determine if current user is the organizer
        if (deviceId.equals(event.getOrganizerId())) {
            // Show organizer buttons
            showOrganizerButtons();
        } else {
            // Hide organizer buttons
            hideOrganizerButtons();

            // Check participant status
            eventController.checkParticipantStatus(eventId, deviceId);
        }
    }

    @Override
    public void onParticipantStatusChecked(boolean isInWaitingList, boolean isSelected, Event event) {
        if (isSelected) {
            // Participant has been selected
            showAcceptDeclineButtons();
        } else if (isInWaitingList) {
            // Participant is in waiting list
            showLeaveWaitingListButton();
        } else {
            // Participant is not in waiting list
            showJoinWaitingListButton();
        }
    }

    @Override
    public void onWaitingListJoined() {
        Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
        // Refresh participant status
        eventController.checkParticipantStatus(eventId, deviceId);
    }

    @Override
    public void onWaitingListLeft() {
        Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
        // Refresh participant status
        eventController.checkParticipantStatus(eventId, deviceId);
    }

    @Override
    public void onInvitationAccepted() {
        Toast.makeText(this, "Invitation accepted", Toast.LENGTH_SHORT).show();
        // Update UI if necessary
        hideAllParticipantButtons();
    }

    @Override
    public void onInvitationDeclined() {
        Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
        // Update UI if necessary
        hideAllParticipantButtons();
    }

    @Override
    public void onDrawPerformed() {
        Toast.makeText(this, "Draw has been performed successfully.", Toast.LENGTH_SHORT).show();
        // Optionally, disable the draw button to prevent further clicks
        drawButton.setEnabled(false);
    }

    @Override
    public void onEventSaved() {
        // Not used here
    }

    @Override
    public void onEventListLoaded(List<Event> eventList) {
        // Not used here
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void displayEventDetails(Event event) {
        eventNameTextView.setText(event.getEventName());
        eventDescriptionTextView.setText(event.getDescription());
        eventStartDateTextView.setText(event.getStartDate().toString());
        eventEndDateTextView.setText(event.getEndDate().toString());
        eventCapacityTextView.setText(String.valueOf(event.getCapacity()));

        // Load images using Glide
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(this).load(event.getPosterUrl()).into(posterImageView);
        }

        if (event.getQrCodeUrl() != null && !event.getQrCodeUrl().isEmpty()) {
            Glide.with(this).load(event.getQrCodeUrl()).into(qrCodeImageView);
        }
    }

    private void showOrganizerButtons() {
        drawButton.setVisibility(View.VISIBLE);
        updatePosterButton.setVisibility(View.VISIBLE);
        editInfoButton.setVisibility(View.VISIBLE);
        waitingListButton.setVisibility(View.VISIBLE);

        // Hide participant buttons
        hideAllParticipantButtons();
    }

    private void hideOrganizerButtons() {
        drawButton.setVisibility(View.GONE);
        updatePosterButton.setVisibility(View.GONE);
        editInfoButton.setVisibility(View.GONE);
        waitingListButton.setVisibility(View.GONE);
    }

    private void showJoinWaitingListButton() {
        joinWaitingListButton.setVisibility(View.VISIBLE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
    }

    private void showLeaveWaitingListButton() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.VISIBLE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
    }

    private void showAcceptDeclineButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.VISIBLE);
        declineInvitationButton.setVisibility(View.VISIBLE);
    }

    private void hideAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_POSTER && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // Use the EventController to update the poster
            eventController.updateEventPoster(eventId, imageUri);
        }
    }

    @Override
    public void onPosterUpdated() {
        Toast.makeText(this, "Poster updated successfully", Toast.LENGTH_SHORT).show();
        // Reload event details to refresh the poster image
        eventController.loadEventDetails(eventId);
    }
}