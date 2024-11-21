// EventDetailActivity.java
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
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity implements EventController.EventControllerListener {

    // UI Components
    private ImageView posterImageView, qrCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, eventGeoTextView;
    private Button joinWaitingListButton, leaveWaitingListButton, acceptInvitationButton, declineInvitationButton;
    private Button drawButton, updatePosterButton, editInfoButton, waitingListButton;
    private ImageButton backButton;
    private TextView acceptedTextView, declinedTextView;

    // Controller and Data
    private EventController eventController;
    private String eventID, deviceID;
    private Event event;
    private boolean geolocation;

    private static final int REQUEST_CODE_SELECT_POSTER = 1001;
    private static final String TAG = "EventDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        initializeUI();
        eventController = new EventController(this);

        // Get event ID from intent
        eventID = getIntent().getStringExtra("eventID");
        Log.d(TAG, "Received eventID: " + eventID);

        if (eventID != null && !eventID.isEmpty()) {
            Log.d(TAG, "TTTTTTTTTTTTTT" + eventID);
            eventController.loadEventDetails(eventID);
        } else {
            Log.d(TAG, "TTTTTTTTTTTTTTAAAAA" + eventID);
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceID);

        setupButtonListeners();
    }

    private void initializeUI() {
        posterImageView = findViewById(R.id.eventPosterImageView);
        qrCodeImageView = findViewById(R.id.eventQRCodeImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        eventGeoTextView = findViewById(R.id.eventGeo);

        joinWaitingListButton = findViewById(R.id.joinEventButton);
        leaveWaitingListButton = findViewById(R.id.leaveEventButton);
        acceptInvitationButton = findViewById(R.id.acceptButton);
        declineInvitationButton = findViewById(R.id.rejectButton);

        drawButton = findViewById(R.id.drawButton);
        updatePosterButton = findViewById(R.id.updatePosterButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        waitingListButton = findViewById(R.id.waitingListButton);
        backButton = findViewById(R.id.backButton);

        acceptedTextView = findViewById(R.id.acceptedTextView);
        declinedTextView = findViewById(R.id.declinedTextView);
    }

    private void setupButtonListeners() {
        joinWaitingListButton.setOnClickListener(v -> eventController.joinWaitingList(eventID, deviceID));
        leaveWaitingListButton.setOnClickListener(v -> eventController.leaveWaitingList(eventID, deviceID));

        acceptInvitationButton.setOnClickListener(v -> {
            eventController.acceptInvitation(eventID, deviceID);

            acceptedTextView.setVisibility(View.VISIBLE);

            declinedTextView.setVisibility(View.GONE);

            acceptInvitationButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);

        });

        declineInvitationButton.setOnClickListener(v -> {
            eventController.declineInvitation(eventID, deviceID);

            declinedTextView.setVisibility(View.VISIBLE);

            acceptedTextView.setVisibility(View.GONE);

            acceptInvitationButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);

        });


        acceptInvitationButton.setOnClickListener(v -> eventController.acceptInvitation(eventID, deviceID));
        declineInvitationButton.setOnClickListener(v -> eventController.declineInvitation(eventID, deviceID));

        drawButton.setOnClickListener(v -> {
            if (event != null && !event.isDrawed()) {
                eventController.performDraw(eventID);
            } else {
                Toast.makeText(this, "The draw has already been performed.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Draw has already been performed.");
            }
        });

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "Back button is not initialized");
        }

        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("eventID", eventID);
            startActivity(intent);
        });

        updatePosterButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_SELECT_POSTER);
        });

        waitingListButton.setOnClickListener(v -> {
            if (event != null && deviceID.equals(event.getOrganizerId())) {
                Intent intent = new Intent(EventDetailActivity.this, EventWaitingListActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            } else {
                Toast.makeText(EventDetailActivity.this, "You are not authorized to view the waiting list.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User is not authorized to view the waiting list.");
            }
        });

        acceptInvitationButton.setOnClickListener(v -> {
            eventController.acceptInvitation(eventID, deviceID);

            acceptedTextView.setVisibility(View.VISIBLE);

            declinedTextView.setVisibility(View.GONE);

            acceptInvitationButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);

        });

        declineInvitationButton.setOnClickListener(v -> {
            eventController.declineInvitation(eventID, deviceID);

            declinedTextView.setVisibility(View.VISIBLE);

            acceptedTextView.setVisibility(View.GONE);

            acceptInvitationButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);

        });
    }

    @Override
    public void onEventLoaded(Event event) {
        if (event == null) {
            Log.e(TAG, "Event is null.");
            Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.event = event;
        Log.d(TAG, "Event loaded: " + event.getEventName());
        displayEventDetails(event);



        if (deviceID.equals(event.getOrganizerId())) {
            showOrganizerButtons();
        } else {
            hideOrganizerButtons();
            eventController.checkParticipantStatus(eventID, deviceID);
        }
    }

    @Override
    public void onParticipantStatusChecked(boolean isInWaitingList, boolean isSelected, Event event) {
        if (isSelected) {
            showAcceptDeclineButtons();
            Log.d(TAG, "Participant has been selected.");
        } else if (isInWaitingList) {
            showLeaveWaitingListButton();
            Log.d(TAG, "Participant is in waiting list.");
        } else {
            showJoinWaitingListButton();
            Log.d(TAG, "Participant is not in waiting list.");
        }
    }

    @Override
    public void onWaitingListJoined() {
        Log.d(TAG, "Joined waiting list");
        eventController.checkParticipantStatus(eventID, deviceID);
    }

    @Override
    public void onWaitingListLeft() {
        Log.d(TAG, "Left waiting list");
        eventController.checkParticipantStatus(eventID, deviceID);
    }

    @Override
    public void onInvitationAccepted() {
        Log.d(TAG, "Invitation accepted");
        hideAllParticipantButtons();
    }

    @Override
    public void onInvitationDeclined() {
        Log.d(TAG, "Invitation declined");
        hideAllParticipantButtons();
    }

    @Override
    public void onDrawPerformed() {
        Log.d(TAG, "Draw has been performed successfully.");
        drawButton.setEnabled(false);
    }

    @Override
    public void onEventSaved() {}

    @Override
    public void onEventListLoaded(List<Event> eventList) {}

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error: " + errorMessage);
    }

    private void displayEventDetails(Event event) {
        eventNameTextView.setText(event.getEventName());
        eventDescriptionTextView.setText("Description: "+event.getDescription());
        eventStartDateTextView.setText("StartDate :"+event.getStartDate().toString());
        eventEndDateTextView.setText("EndDate："+event.getEndDate().toString());
        eventCapacityTextView.setText("Capacity: "+String.valueOf(event.getCapacity()));
        eventGeoTextView.setText(event.isLocationRequired() ? "Location Required" : "No Location Required");


        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(this).load(event.getPosterUrl()).into(posterImageView);
            Log.d(TAG, "Loaded poster image.");
        }

        if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
            Glide.with(this).load(event.getQrCode()).into(qrCodeImageView);
            Log.d(TAG, "Loaded QR code image.");
        }

        Log.d(TAG, "Event Details - Name: " + event.getEventName() + ", Description: " + event.getDescription() +
                ", Start Date: " + event.getStartDate() + ", End Date: " + event.getEndDate() +
                ", Capacity: " + event.getCapacity() + ", Organizer ID: " + event.getOrganizerId());
    }

    private void showOrganizerButtons() {
        drawButton.setVisibility(View.VISIBLE);
        updatePosterButton.setVisibility(View.VISIBLE);
        editInfoButton.setVisibility(View.VISIBLE);
        waitingListButton.setVisibility(View.VISIBLE);
        hideAllParticipantButtons();
        Log.d(TAG, "Organizer buttons are now visible.");
    }

    private void hideOrganizerButtons() {
        drawButton.setVisibility(View.GONE);
        updatePosterButton.setVisibility(View.GONE);
        editInfoButton.setVisibility(View.GONE);
        waitingListButton.setVisibility(View.GONE);
        Log.d(TAG, "Organizer buttons are now hidden.");
    }

    private void showJoinWaitingListButton() {
        joinWaitingListButton.setVisibility(View.VISIBLE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
        Log.d(TAG, "Shown: Join Waiting List button.");
    }

    private void showLeaveWaitingListButton() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.VISIBLE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
        Log.d(TAG, "Shown: Leave Waiting List button.");
    }

    private void showAcceptDeclineButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.VISIBLE);
        declineInvitationButton.setVisibility(View.VISIBLE);
        Log.d(TAG, "Shown: Accept and Decline Invitation buttons.");
    }

    private void hideAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
        Log.d(TAG, "All participant buttons are now hidden.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_POSTER && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            eventController.updateEventPoster(eventID, imageUri);
            Log.d(TAG, "Selected new poster URI: " + imageUri.toString());
        }
    }

    private void updateStatusTextViews(boolean accepted) {
        if (accepted) {
            acceptedTextView.setVisibility(View.VISIBLE);
            declinedTextView.setVisibility(View.GONE);
        } else {
            acceptedTextView.setVisibility(View.GONE);
            declinedTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPosterUpdated() {
        Log.d(TAG, "Poster updated successfully.");
        eventController.loadEventDetails(eventID);
    }
}
