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
public class EventDetailActivity extends AppCompatActivity {

    // UI Components
    private ImageView posterImageView, qrCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, eventFacilityTextView;
    private Button joinWaitingListButton, leaveWaitingListButton, acceptInvitationButton, declineInvitationButton;
    private Button drawButton, updatePosterButton, editInfoButton, viewParticipantsButton;
    private ImageButton backButton;

    // Controller and Data
    private EventController eventController;
    private String eventID, deviceID;
    private Event event;

    private static final int REQUEST_CODE_SELECT_POSTER = 1001;
    private static final String TAG = "Event Detail Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        // initialize controller
        eventController = new EventController(new EventRepository());

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceID);


        // Get event ID from intent
        eventID = getIntent().getStringExtra("eventID");
        Log.d(TAG, "Received eventID: " + eventID);

        if (eventID != null && !eventID.isEmpty()) {
            Log.d(TAG, "Event ID found: " + eventID);

            // Refresh repository to get the latest data
            eventController.refreshRepository(new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Fetch the event by ID from the controller
                    event = eventController.getEventById(eventID);

                    // If the event is not found, show an error and finish
                    if (event == null) {
                        Log.d(TAG, "Failed to find event in repository");
                        Toast.makeText(EventDetailActivity.this, "Failed to find the event", Toast.LENGTH_SHORT).show();
                        finish();  // Exit the activity as the event wasn't found
                        return;    // Early return to prevent the rest of the code from executing
                    }

                    // Initialize UI and display event details
                    initializeUI();
                    displayEventDetails(event);

                    // Display buttons based on whether the user is the organizer or not
                    if (event.getEventID().equals(deviceID)) { // User is the organizer
                        hideAllParticipantButtons();
                        showOrganizerButtons(event);
                    } else { // User is a potential entrant
                        hideOrganizerButtons();
                        showJoinWaitingListButton();
                        showAcceptDeclineButtons();
                        showLeaveWaitingListButton();
                    }

                    // Set up button listeners after the UI is updated
                    setupButtonListeners();
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, e.toString());
                    Toast.makeText(EventDetailActivity.this, "Failed to get updated data", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Log.d(TAG, "Event ID is missing");
            Toast.makeText(this, "Couldn't retrieve event id", Toast.LENGTH_SHORT).show();
            finish();  // Exit the activity as there's no event ID to fetch
        }
    }

    private void initializeUI() {
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
        viewParticipantsButton = findViewById(R.id.viewParticipantsButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupButtonListeners() {
        joinWaitingListButton.setOnClickListener(v -> eventController.addUserToEventParticipants(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error joining the waiting list", Toast.LENGTH_SHORT).show();
            }
        }));

        leaveWaitingListButton.setOnClickListener(v -> eventController.leaveWaitingList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have left the waiting list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error joining the waiting list", Toast.LENGTH_SHORT).show();
            }
        }));

        acceptInvitationButton.setOnClickListener(v -> eventController.acceptInvitation(event, deviceID));
        declineInvitationButton.setOnClickListener(v -> eventController.declineInvitation(event, deviceID));

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

        viewParticipantsButton.setOnClickListener(v -> {
            if (event != null && deviceID.equals(event.getOrganizerId())) {
                Intent intent = new Intent(EventDetailActivity.this, EventWaitingListActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            } else {
                Toast.makeText(EventDetailActivity.this, "You are not authorized to view the waiting list.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User is not authorized to view the waiting list.");
            }
        });
    }

    private void displayEventDetails(Event event) {
        eventNameTextView.setText(event.getEventName());
        eventDescriptionTextView.setText(event.getDescription());
        eventFacilityTextView.setText(event.getFacility());
        eventStartDateTextView.setText(event.getStartDate().toString());
        eventEndDateTextView.setText(event.getEndDate().toString());
        eventCapacityTextView.setText(String.valueOf(event.getCapacity()));

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

    private void showOrganizerButtons(Event event) {
        drawButton.setVisibility(View.VISIBLE);
        updatePosterButton.setVisibility(View.VISIBLE);
        editInfoButton.setVisibility(View.VISIBLE);
        viewParticipantsButton.setVisibility(View.VISIBLE);
        if (event.isDrawed()) {
            viewParticipantsButton.setText("View Waiting List");
        } else {
            viewParticipantsButton.setText("View Invited Participants");
        }
        hideAllParticipantButtons();
        Log.d(TAG, "Organizer buttons are now visible.");
    }

    private void hideOrganizerButtons() {
        drawButton.setVisibility(View.GONE);
        updatePosterButton.setVisibility(View.GONE);
        editInfoButton.setVisibility(View.GONE);
        viewParticipantsButton.setVisibility(View.GONE);
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

}
