package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;

    private Button updatePosterButton, joinEventButton, leaveEventButton, editInfoButton,
            drawButton, acceptButton, rejectButton, waitingListButton;
    private ImageButton backButton;
    private TextView drawEndedTextView;

    private EventController eventController; // initialize event controller
    private EventRepository eventRepository; // initialize repository

    private String eventId;
    private String eventName;
    private String organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize UI components
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        eventQRCodeImageView = findViewById(R.id.eventQRCodeImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventFacilityTextView = findViewById(R.id.eventFacilityTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        updatePosterButton = findViewById(R.id.updatePosterButton);
        joinEventButton = findViewById(R.id.joinEventButton);
        leaveEventButton = findViewById(R.id.leaveEventButton);
        editInfoButton = findViewById(R.id.editInfoButton);

        backButton = findViewById(R.id.backButton);
        drawButton = findViewById(R.id.drawButton);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);
        drawEndedTextView = findViewById(R.id.drawEndedTextView);
        waitingListButton = findViewById(R.id.waitingListButton); // Initialize waitingListButton

        // Initialize repository and controller
        eventRepository = new EventRepository();
        eventController = new EventController(eventRepository);

        // Retrieve the event ID passed from the previous screen
        eventId = getIntent().getStringExtra("event_id");

        // Load event details using controller
        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        updatePosterButton.setOnClickListener(v -> openImagePicker());
        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
        /*
        joinEventButton.setOnClickListener(v -> joinEvent());
        leaveEventButton.setOnClickListener(v -> leaveEvent());
        drawButton.setOnClickListener(v -> drawParticipants());
        acceptButton.setOnClickListener(v -> acceptDraw());
        rejectButton.setOnClickListener(v -> rejectDraw());
        */
        // Set click listener for waitingListButton
        waitingListButton.setOnClickListener(v -> {
            // Start EventWaitingListActivity
            Intent intent = new Intent(EventDetailActivity.this, EventWaitingListActivity.class);
            intent.putExtra("event_id", eventId); // Pass event ID to the waiting list activity
            startActivity(intent);
        });
    }

    /**
     * Opens the image picker to select an image for the event poster.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result from the image picker
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadPosterImage(imageUri);
        }
    }

    /**
     * Uploads the selected poster image using the controller.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadPosterImage(Uri imageUri) {
        // Generate QR Code Bitmap
        Bitmap qrCodeBitmap = eventController.generateQRCodeBitmap(eventId); // You need to implement this method in EventController

        // Update the poster image via controller
        // Assuming you have access to the Event object
        Event event = eventController.getEventById(eventId);
        if (event != null) {
            eventController.updateEvent(event, imageUri, qrCodeBitmap, new DataCallback<Event>() {
                @Override
                public void onSuccess(Event result) {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    /**
     * Loads the event details using the controller and updates the UI.
     *
     * @param eventId The ID of the event to load.
     */
    private void loadEventDetails(String eventId) {
        // implementing callbacks for asynchronous operation
        eventController.getAllEvents(new DataCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                Event eventToDisplay = eventController.getEventById(eventId);
                // Populate the UI with event details
                eventName = eventToDisplay.getEventName();
                String eventDescription = eventToDisplay.getDescription();
                String eventPosterUrl = eventToDisplay.getPosterUrl();
                Date startDate = eventToDisplay.getStartDate();
                Date endDate = eventToDisplay.getEndDate();
                String facility = eventToDisplay.getFacility();
                int capacity = eventToDisplay.getCapacity();
                String qrCodeUrl = eventToDisplay.getQrCodeUrl();
                organizerId = eventToDisplay.getOrganizerId();

                // Set details to TextViews
                eventNameTextView.setText(eventName);
                eventDescriptionTextView.setText(eventDescription);
                SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                eventStartDateTextView.setText("Start Date: " + (startDate != null ? displayFormat.format(startDate) : "N/A"));
                eventEndDateTextView.setText("End Date: " + (endDate != null ? displayFormat.format(endDate) : "N/A"));
                eventFacilityTextView.setText("Location: " + facility);
                eventCapacityTextView.setText("Capacity: " + (capacity > 0 ? String.valueOf(capacity) : "N/A"));

                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                // show organizer display if event has been created by the current user
                if (eventToDisplay.getOrganizerId().equals(deviceId)) {
                    showOrganizerDisplay(eventToDisplay, deviceId);
                } else {
                    // else show normal user display
                    showUserDisplay(eventToDisplay, deviceId);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this,"Event details could not be loaded", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOrganizerDisplay(Event event, String deviceId) {
        // Set organizer UI visible
        updatePosterButton.setVisibility(View.VISIBLE);
        drawButton.setVisibility(View.VISIBLE);
        joinEventButton.setVisibility(View.GONE);
        leaveEventButton.setVisibility(View.GONE);
        editInfoButton.setVisibility(View.VISIBLE);
        acceptButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
        drawEndedTextView.setVisibility(View.GONE);
    }

    private void showUserDisplay(Event event, String deviceId) {
        // Set user UI visible
        updatePosterButton.setVisibility(View.GONE);
        drawButton.setVisibility(View.GONE);
        editInfoButton.setVisibility(View.GONE);
        //String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (eventController.isUserRegistered(event.getEventID(), deviceId)) {
            // if user is registered in waiting list
            joinEventButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.VISIBLE);
        } else if (eventController.isUserSelected(event.getEventID(), deviceId)) {
            // if user is selected to participate
            acceptButton.setVisibility(View.VISIBLE);
            rejectButton.setVisibility(View.VISIBLE);
            joinEventButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.GONE);
        } else {
            // neither is true
            joinEventButton.setVisibility(View.VISIBLE);
            leaveEventButton.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);
            rejectButton.setVisibility(View.GONE);

            if (eventController.getEventById(event.getEventID()).isDrawed()) {
                drawEndedTextView.setVisibility(View.VISIBLE);
            } else {
                drawEndedTextView.setVisibility(View.GONE);
            }
        }
    }


    private void drawParticipants() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }
        // perform draw on event and return
        Event event = eventController.performDraw(eventId);
        // if draw has been performed successfully
        if (event != null) {
            eventController.updateEvent(event, null, null, new DataCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    Log.d("EventDetailActivity", "Event performed succesfully");
                }

                @Override
                public void onError(Exception e) {
                    Log.e("EventDetailActivity", e.toString());

                }
            });
        } else {
            Toast.makeText(this, "Event draw cannot be performed at this time", Toast.LENGTH_SHORT).show();
        }
    }


    private void acceptDraw() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        eventController.acceptInvitation(eventId, currentDeviceId);
        Toast.makeText(this, "You have accepted the invitation", Toast.LENGTH_SHORT).show();
        acceptButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
    }


    private void rejectDraw() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        eventController.rejectInvitation(eventId, currentDeviceId);
        Toast.makeText(this, "You have rejected the invitation", Toast.LENGTH_SHORT).show();
        acceptButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
    }


    private void joinEvent() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        boolean success = eventController.addParticipant(eventId, currentDeviceId);
        if (success) {
            Toast.makeText(this, "Successfully joined the event", Toast.LENGTH_SHORT).show();
            joinEventButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Failed to join the event. It might be full or you are already registered.", Toast.LENGTH_SHORT).show();
        }
    }


    private void leaveEvent() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        boolean success = eventController.removeParticipant(eventId, currentDeviceId);
        if (success) {
            Toast.makeText(this, "Successfully left the event", Toast.LENGTH_SHORT).show();
            joinEventButton.setVisibility(View.VISIBLE);
            leaveEventButton.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Failed to leave the event. You might not be registered.", Toast.LENGTH_SHORT).show();
        }
    }

}