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
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView;
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

        initializeUI();
        eventController = new EventController(new EventRepository());

        // Get event ID from intent
        eventID = getIntent().getStringExtra("eventID");
        Log.d(TAG, "Received eventID: " + eventID);

        if (eventID != null && !eventID.isEmpty()) {
            Log.d(TAG, "TTTTTTTTTTTTTT" + eventID);
            // find the event to display through the controller
            event = eventController.getEventById(eventID);
            initializeUI();
            displayEventDetails(event);
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
        joinWaitingListButton.setOnClickListener(v -> eventController.joinWaitingList(eventID, deviceID));
        leaveWaitingListButton.setOnClickListener(v -> eventController.leaveWaitingList(eventID, deviceID));

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

    @Override
    public void onPosterUpdated() {
        Log.d(TAG, "Poster updated successfully.");
        eventController.loadEventDetails(eventID);
    }
}
