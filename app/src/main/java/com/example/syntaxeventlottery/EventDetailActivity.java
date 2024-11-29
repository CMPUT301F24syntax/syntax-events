// EventDetailActivity.java
package com.example.syntaxeventlottery;
import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import android.Manifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";

    // UI Components
    private ImageView posterImageView, qrCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, eventFacilityLocationTextView, eventFacilityNameTextView, eventDrawedStatusTextView;
    private Button joinWaitingListButton, acceptInvitationButton, leaveEventButton;
    private Button manageParticipantsButton, editInfoButton, drawButton;
    private ImageButton backButton;

    // Controller and Data
    private EventController eventController;
    private String eventID, deviceID;
    private Event event;
    private boolean isRequireLocation;

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

        // set up back button listener
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

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
                Log.d(TAG, "Event from local cache: " + event.getLocationRequired());
                Log.d(TAG, "Refreshed repository, event details:"+event);
                // If the event is not found, show an error and finish
                if (event == null) {
                    Log.e(TAG, "Failed to find event in repository");
                    Toast.makeText(EventDetailActivity.this, "Failed to find the event", Toast.LENGTH_SHORT).show();
                    finish();  // Exit the activity as the event wasn't found
                    return;    // Early return to prevent the rest of the code from executing
                }
                Log.d("EventDetailActivivty","check islocationn11"+event.getLocationRequired());
                Log.d("EventDetailActivivty","check islocationn1122"+event);
                //event.setLocationRequired(true);
                // check if need location
                if (event.getLocationRequired()) {
                    Log.d("EventDetailActivivty","check islocationn"+event.getLocationRequired());
                    handleLocationRequirement();
                } else {
                    updateUI(event); // Proceed if location is not required
                }

            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, e.toString());
                Toast.makeText(EventDetailActivity.this, "Failed to get updated data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void handleLocationRequirement() {
        // Check if the app has the permission to access fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isRequireLocation = true; // Set the flag indicating location is required
            getLocation(); // Call the method to retrieve the current location
        } else {
            requestLocationPermission(); // Request location permission from the user
        }
    }

    private void requestLocationPermission() {
        // Request fine location permission with a unique request code
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle the result of the location permission request
        if (requestCode == 1001) { // Match the request code for location permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted by the user
                isRequireLocation = true; // Update the flag
                getLocation(); // Retrieve the location since permission is granted
            } else {
                // Permission denied by the user
                Toast.makeText(this, "Location permission denied. Unable to proceed.", Toast.LENGTH_SHORT).show();
                updateUI(event); // Update the UI even if permission is denied
            }
        }
    }

    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Successfully retrieved location
                        Log.d(TAG, "Location: Latitude=" + location.getLatitude() + ", Longitude=" + location.getLongitude());

                        // Create and set location details
                        Map<String, String> locationDetail = new HashMap<>();
                        locationDetail.put("latitude", String.valueOf(location.getLatitude()));
                        locationDetail.put("longitude", String.valueOf(location.getLongitude()));

                        List<Map<String, String>> locationDetails = new ArrayList<>();
                        locationDetails.add(locationDetail);
                        event.setLocationDetails(locationDetails);

                        updateUI(event); // Update the UI with the retrieved location
                    } else {
                        // Location is null
                        Log.e(TAG, "Unable to retrieve location");
                        updateUI(event);
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to retrieve location
                    Log.e(TAG, "Error retrieving location: " + e.getMessage());
                    updateUI(event);
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
        leaveEventButton = findViewById(R.id.leaveEventButton);
        acceptInvitationButton = findViewById(R.id.acceptButton);

        manageParticipantsButton = findViewById(R.id.manageParticipantsButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        drawButton = findViewById(R.id.drawParticipantsButton);

        configureButtonVisibility();
        setupButtonListeners();
    }

    private void configureButtonVisibility() {
        boolean isOrganizer = event.getOrganizerId().equals(deviceID);

        // Organizer buttons
        editInfoButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        manageParticipantsButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        drawButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        // Determin which buttons to display
        if (!isOrganizer) {
            displayParticipantButtons();
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
        // user joins waiting list
        joinWaitingListButton.setOnClickListener(v -> eventController.addUserToWaitingList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
                // refresh event data
                loadEvent();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error joining the waiting list", Toast.LENGTH_SHORT).show();
            }
        }));

        // user accepts invitation
        acceptInvitationButton.setOnClickListener(v -> eventController.addUserToConfirmedList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "Successfully Enrolled in the Event!", Toast.LENGTH_SHORT).show();
                loadEvent();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error adding user to confirmed list", e);
                Toast.makeText(EventDetailActivity.this, "Error accepting invitation", Toast.LENGTH_SHORT).show();
            }
        }));

        // user leaves the event, remove from all lists
        leaveEventButton.setOnClickListener(v -> eventController.removeUserFromEvent(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have left the event", Toast.LENGTH_SHORT).show();
                loadEvent();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error leaving the Event", e);
                Toast.makeText(EventDetailActivity.this, "Error leaving the event", Toast.LENGTH_SHORT).show();
            }
        }));

        // perform event draw
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

    //------------ helper methods for displaying UI --------------//
    private void displayParticipantButtons() {
        boolean isInWaitingList = eventController.isUserInWaitingList(event, deviceID);
        boolean isInSelectedList = eventController.isUserInSelectedList(event, deviceID);
        boolean isInConfirmedList = eventController.isUserInConfirmedList(event, deviceID);

        // Handle Waiting List buttons
        if (isInWaitingList && !isInSelectedList) {
            joinWaitingListButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.VISIBLE);
            leaveEventButton.setText("Leave Waiting List");
        } else {
            leaveEventButton.setVisibility(View.GONE);
            joinWaitingListButton.setVisibility(View.VISIBLE);
        }

        // Handle Selected List buttons
        if (isInSelectedList && !isInConfirmedList) {
            joinWaitingListButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.VISIBLE);
            leaveEventButton.setText("Decline Invitation");
            acceptInvitationButton.setVisibility(View.VISIBLE);
        }

        // Handle Confirmed List buttons
        if (isInConfirmedList) {
            joinWaitingListButton.setVisibility(View.GONE);
            acceptInvitationButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.VISIBLE);
            leaveEventButton.setText("Leave Event");
        }
    }

    private void hideAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        leaveEventButton.setVisibility(View.GONE);
    }
}
