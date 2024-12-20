package com.example.syntaxeventlottery;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";

    // UI Components
    private ImageView posterImageView, qrCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, eventFacilityLocationTextView, eventFacilityNameTextView, eventDrawedStatusTextView;
    private TextView eventActionsTextView;
    private Button joinWaitingListButton, acceptInvitationButton, declineInvitationButton, leaveWaitingListButton;
    private Button manageParticipantsButton, editInfoButton, drawButton, viewMapButton;
    private ImageButton backButton;
    private Button notifyWaitingListButton;
    private Button notifySelectedEntrantsButton;
    private Button notifyCancelledEntrantsButton;
    private Button notifyAcceptInvitationButton;
    private boolean FirstTimeVist = true;

    // Controller and Data
    private EventController eventController;
    private String eventID, deviceID;
    private Event event;

    //Location
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isLocationPermissionGranted = false;
    private UserController userController;
    private User currentUser;


    /**
     * Called when the activity is first created.
     * Initializes the event details screen by setting up controllers, retrieving device and event IDs,
     * and loading the event details. Handles invalid or missing event data by closing the activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null");
            Toast.makeText(this, "Error: Unable to initialize LocationManager", Toast.LENGTH_LONG).show();
            return;
        }

        // initialize controller
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository(), locationManager);

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        currentUser = userController.getUserByDeviceID(deviceID);

        // Get event ID from intent
        eventID = getIntent().getStringExtra("eventID");

        // set up back button listener
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (eventID == null || eventID.isEmpty()) { // finish activity if there is error getting the event
            Log.e(TAG, "Couldn't get eventID from event");
            finish();
        } else {
            loadEvent(deviceID);
        }
    }

    /**
     * Called when the activity comes to the foreground.
     * Ensures the most up-to-date event details are displayed by reloading the event information.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // reload all event details when resuming activity to get most updated information
        loadEvent(deviceID);
    }

    /**
     * Loads the event details by refreshing the event repository and retrieving the specified event.
     * Updates the UI based on whether the user is an organizer or a participant.
     * Handles location requirements for participants if the event requires location sharing.
     *
     * @param deviceID The unique ID of the device, used to identify the current user.
     */
    private void loadEvent(String deviceID) {
        // Refresh the event repository and load the details of the specified event
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                event = eventController.getEventById(eventID);

                if (event == null) {
                    Log.e(TAG, "Event not found in the repository.");
                    Toast.makeText(EventDetailActivity.this, "Failed to find the event.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Log the fetched event and its location requirement
                Log.d(TAG, "Fetched event: " + event);
                Log.d(TAG, "Event location requirement: " + event.getLocationRequired());

                // Check if the current user is the organizer
                boolean isOrganizer = event.getOrganizerId().equals(deviceID);
                Log.d(TAG, "Is Organizer? " + isOrganizer);

                if (isOrganizer) {
                    // If the current user is the organizer, proceed to update the UI
                    updateUI(event);
                }
                else {
                    // If the current user is an entrant, handle location requirement
                    if (event.getLocationRequired()) {
                        if (FirstTimeVist == true) {
                            showLocationWarningDialog(deviceID);
                            FirstTimeVist = false;
                            updateUI(event);
                        }
                        updateUI(event);
                    }
                    else {
                        updateUI(event);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing the event repository: ", e);
                Toast.makeText(EventDetailActivity.this, "Failed to get updated data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Displays a dialog warning the user that location permission is required for the event.
     * Allows the user to enable location permissions or decline and navigate back to the home screen.
     *
     * @param deviceID The unique ID of the device, used to identify the current user.
     */
    private void showLocationWarningDialog(String deviceID) {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("This event requires access to your location. Do you want to enable location access?")
                .setCancelable(false) // Prevent dismissing by tapping outside the dialog
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User agrees to enable location permissions
                    handleLocationRequirement(deviceID);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User declines to enable location permissions
                    Toast.makeText(EventDetailActivity.this, "Location is required for this event.", Toast.LENGTH_SHORT).show();
                    navigateToUserHome(); // Redirect to UserHomeActivity
                })
                .show();
    }

    /**
     * Redirects the user to the UserHomeActivity and ends the current activity.
     * Typically called when the user declines to enable location services for the event.
     */
    private void navigateToUserHome() {
        // Redirect the user to the UserHomeActivity
        Intent intent = new Intent(this, UserHomeActivity.class);
        startActivity(intent);
        finish(); // End the current activity
    }


    /**
     * Handles location requirements for events that require location sharing.
     * Checks if the user's location is already stored; if not, requests location permissions.
     * Updates the UI after location data is successfully retrieved.
     *
     * @param deviceID The unique ID of the device, used to identify the current user.
     */
    private void handleLocationRequirement(String deviceID) {
        Log.d(TAG, "Entered handleLocationRequirement with deviceID: " + deviceID);

        // Refresh user data to ensure we have the latest information
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // After refreshing, retrieve the current user based on the deviceID
                currentUser = userController.getUserByDeviceID(deviceID);
                Log.d(TAG, "Current user after refresh: " + deviceID+currentUser);

                if (currentUser != null) {
                    // Retrieve the user's stored location
                    ArrayList<Double> userLocation = currentUser.getLocation();

                    if (userLocation != null && !userLocation.isEmpty()) {
                        // If the location data exists, proceed to update the UI
                        Log.d(TAG, "User's current location exists. Lat: " + userLocation.get(0) + ", Lng: " + userLocation.get(1));
                        updateUI(event);
                    } else {
                        // If the location data is not set, request location permissions
                        Log.d(TAG, "User location is not set or empty. Requesting permissions.");
                        checkAndRequestLocationPermission();
                    }
                } else {
                    // If the user is still null after refresh, show an error message
                    Toast.makeText(EventDetailActivity.this, "User not found after refreshing data.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Current user is null after refresh.");
                }
            }

            @Override
            public void onError(Exception e) {
                // If refreshing the repository fails, log the error and show a message
                Log.e(TAG, "Failed to refresh user repository.", e);
                Toast.makeText(EventDetailActivity.this, "Failed to refresh user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks whether location services are enabled on the device.
     * If disabled, prompts the user to enable location services.
     * If enabled, proceeds to access the user's location data.
     */
    private void checkAndRequestLocationPermission() {
        Log.d(TAG, "Checking if location service is enabled.");

        // Get the LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if GPS or Network Location is enabled
        boolean isLocationEnabled = locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!isLocationEnabled) {
            Log.d(TAG, "Location service is not enabled. Prompting user to enable it.");

            // Show a dialog asking the user if they want to enable location services
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location Service")
                    .setMessage("Location services are required for this feature. Do you want to enable them?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User agrees to enable location services
                        Log.d(TAG, "User agreed to enable location services.");
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User declines to enable location services
                        Log.d(TAG, "User declined to enable location services. Returning to UserProfileActivity.");
                        Intent intent = new Intent(this, UserHomeActivity.class);
                        startActivity(intent);
                        finish(); // End the current activity
                    })
                    .show();
        } else {
            Log.d(TAG, "Location service is enabled. Proceeding with location access.");
            proceedWithLocationAccess(); // Location service is enabled, proceed to access location
        }
    }


    /**
     * Handles the result of permission requests, specifically for location permissions.
     * If the location permission is granted, it proceeds to access the user's location.
     * Otherwise, logs the denial and does not proceed.
     *
     * @param requestCode  The integer request code originally supplied to requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult triggered with requestCode: " + requestCode);

        // Handle the result of the location permission request
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted.");
                isLocationPermissionGranted = true;
                proceedWithLocationAccess(); // Permission granted, proceed to access location
            } else {
                Log.d(TAG, "Location permission denied.");
                isLocationPermissionGranted = false;
            }
        }
    }

    /**
     * Proceeds to access the user's location after verifying that location permissions are granted.
     * Retrieves the last known location if available, otherwise registers a listener for real-time updates.
     * Saves the retrieved location to the database.
     */
    private void proceedWithLocationAccess() {
        Log.d(TAG, "Proceeding with location access...");

        // Get the LocationManager instance
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verify location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Get the last known location for immediate use
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                Log.d(TAG, "Last Known Location - Lat: " + latitude + ", Lng: " + longitude);

                // Save the user's location to the database
                saveUserLocation(latitude, longitude);

                Toast.makeText(this, "Location retrieved: Lat=" + latitude + ", Lng=" + longitude, Toast.LENGTH_SHORT).show();
            } else {
                // Register a listener to receive real-time location updates
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000, // Update interval in milliseconds
                        10,   // Minimum distance in meters
                        locationListener
                );
            }
        } else {
            Toast.makeText(this, "Location permission is not granted.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Location permission not granted. Unable to proceed. Please turn on your location permission on your phone for this app.");
            Log.e(TAG, "Please turn on your location permission on your phone for this app.");
        }
    }


    /**
     * A listener to receive real-time location updates.
     * Updates the UI and saves the location to the database when the location changes.
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "Real-time Location - Lat: " + latitude + ", Lng: " + longitude);

            // Save the user's location to the database
            saveUserLocation(latitude, longitude);

            Toast.makeText(EventDetailActivity.this, "Real-time location retrieved: Lat=" + latitude + ", Lng=" + longitude, Toast.LENGTH_SHORT).show();
            updateUI(event);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };


    /**
     * Saves the user's location to the database.
     * Updates the user's current location in the `User` object and persists the changes in the database.
     *
     * @param latitude  The latitude of the user's current location.
     * @param longitude The longitude of the user's current location.
     */
    private void saveUserLocation(double latitude, double longitude) {
        Log.d(TAG, "Saving user location: Lat=" + latitude + ", Lng=" + longitude);

        if (currentUser != null) {
            // Store location in the current user object
            ArrayList<Double> location = new ArrayList<>();
            location.add(latitude);
            location.add(longitude);
            currentUser.setLocation(location);

            // Update user information in the database
            userController.updateUserLocation_eventdetail(currentUser, latitude, longitude, new DataCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Log.d(TAG, "User location updated successfully in the database.");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to update user location in the database.", e);
                }
            });
        } else {
            Log.e(TAG, "Current user is null. Unable to save location.");
        }
    }

    /**
     * Updates the user interface with the details of the given event.
     * Initializes the UI components and displays the event details.
     *
     * @param event The {@link Event} object containing details to be displayed.
     */
    private void updateUI(Event event) {
        this.event = event;
        initializeUI(event);
        displayEventDetails(event);
    }

    /**
     * Displays the details of the provided event on the screen.
     * Updates text views, images, and other UI elements with event-specific information.
     *
     * @param event The {@link Event} object containing details to display.
     */
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
            Glide.with(this).load(R.drawable.ic_no_event_poster).into(posterImageView);
        }


        if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
            Glide.with(this).load(event.getQrCode()).into(qrCodeImageView);
            Log.d(TAG, "Loaded QR code image. qr code url: " + event.getQrCode());
        } else {
            // load missing qr code image
            Glide.with(this).load(R.drawable.default_qrcode).into(qrCodeImageView);
        }
    }

    /**
     * Initializes the UI components for displaying event details and participant/organizer actions.
     *
     * @param event The {@link Event} object containing details to display.
     */
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
        eventActionsTextView = findViewById(R.id.eventActionsTextView);
        joinWaitingListButton = findViewById(R.id.joinEventButton);
        leaveWaitingListButton = findViewById(R.id.leaveEventButton);
        acceptInvitationButton = findViewById(R.id.acceptButton);
        declineInvitationButton = findViewById(R.id.declineInvitationButton);

        manageParticipantsButton = findViewById(R.id.manageParticipantsButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        drawButton = findViewById(R.id.drawParticipantsButton);

        // notification part
        notifyWaitingListButton = findViewById(R.id.notifyWaitingListButton);
        notifyAcceptInvitationButton = findViewById(R.id.notifyAcceptInvitationButton);
        notifySelectedEntrantsButton = findViewById(R.id.notifySelectedEntrantsButton);
        notifyCancelledEntrantsButton = findViewById(R.id.notifyCancelledEntrantsButton);


        viewMapButton = findViewById(R.id.viewMapButton);

        configureButtonVisibility();
        setupButtonListeners();
    }

    private void configureButtonVisibility() {
        boolean isOrganizer = event.getOrganizerId().equals(deviceID);

        // Determine which buttons to display
        if (!isOrganizer) {
            hideAllOrganizerButtons();
            displayParticipantButtons();
        } else {
            hideAllParticipantButtons();
            displayOrganizerButtons();
        }
    }

    /**
     * Sets up click listeners for various buttons in the event detail screen.
     * Handles actions such as joining/leaving waiting lists, accepting/declining invitations,
     * performing draws, managing participants, and sending notifications.
     */
    private void setupButtonListeners() {
        // user joins waiting list
        joinWaitingListButton.setOnClickListener(v -> eventController.addUserToWaitingList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
                // refresh event data
                loadEvent(deviceID);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Error joining the waiting list", Toast.LENGTH_SHORT).show();
            }
        }));


        // user leaves the waiting list, remove user from all event data
        leaveWaitingListButton.setOnClickListener(v -> {
            // Create and show a confirmation dialog
            new AlertDialog.Builder(EventDetailActivity.this)
                    .setTitle("Leave Waiting List")
                    .setMessage("Are you sure you want to leave the waiting list? If you leave, you will need to rejoin to be considered for the lottery and have a chance to participate in the event.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User confirmed; remove all user information from the event
                        eventController.removeUserFromEvent(event, deviceID, new DataCallback<Event>() {
                            @Override
                            public void onSuccess(Event result) {
                                Toast.makeText(EventDetailActivity.this, "You have left waiting list for this event", Toast.LENGTH_SHORT).show();
                                loadEvent(deviceID);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error leaving the Event", e);
                                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User canceled; dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        });

        // user declines their invitation
        declineInvitationButton.setOnClickListener(v -> {
            // Create and show a confirmation dialog
            new AlertDialog.Builder(EventDetailActivity.this)
                    .setTitle("Decline Invitation")
                    .setMessage("Are you sure you decline your invitation? You will not be allowed to rejoin the waiting list for this event")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User confirmed; remove all user information from the event
                        eventController.setUserCancelled(event, deviceID, new DataCallback<Event>() {
                            @Override
                            public void onSuccess(Event result) {
                                Toast.makeText(EventDetailActivity.this, "You have rejected your invitation", Toast.LENGTH_SHORT).show();
                                loadEvent(deviceID);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error leaving the Event", e);
                                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User canceled; dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        });


        // user accepts invitation
        acceptInvitationButton.setOnClickListener(v -> eventController.addUserToConfirmedList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "Successfully Enrolled in the Event!", Toast.LENGTH_SHORT).show();
                loadEvent(deviceID);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error adding user to confirmed list", e);
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));


        // perform event draw
        // Inside the drawButton.setOnClickListener

        drawButton.setOnClickListener(v -> {
            if (!event.isDrawed()) {
                // perform initial event draw
                eventController.performDraw(event, EventDetailActivity.this, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Log.d(TAG, "Event draw performed: updated event info: " + result);
                        Toast.makeText(EventDetailActivity.this, "Draw performed successfully", Toast.LENGTH_SHORT).show();
                        loadEvent(deviceID);
                        checkForNewNotifications();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "Event draw error");
                        Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // allow redraw for users still in the waiting list
                eventController.performRedraw(event, EventDetailActivity.this, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Log.d(TAG, "Event redraw performed: updated event info: "+  result);
                        Toast.makeText(EventDetailActivity.this, "Redraw perfomed successfully", Toast.LENGTH_SHORT).show();
                        loadEvent(deviceID);
                        checkForNewNotifications();
                    }
                    // Inside the drawButton.setOnClickListener

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Event draw error", e);
                        Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
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

        viewMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, MapActivity.class);
            // send current eventID
            intent.putExtra("eventID", eventID);
            startActivity(intent);
        });

        notifyWaitingListButton.setOnClickListener(v -> sendNotificationToGroup("waitingList"));
        notifyAcceptInvitationButton.setOnClickListener(v -> {
            eventController.notifyAcceptInvitation(event, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailActivity.this, "Invitations sent successfully.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Notifications were successfully sent.");
                }

                @Override
                public void onError(Exception e) {
                    // Handle the error scenario, such as showing an error message
                    Toast.makeText(EventDetailActivity.this, "Failed to send invitations.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error sending invitations.", e);
                }
            });
        });
        notifySelectedEntrantsButton.setOnClickListener(v -> sendNotificationToGroup("selectedParticipants"));
        notifyCancelledEntrantsButton.setOnClickListener(v -> sendNotificationToGroup("cancelledParticipants"));
    }

    /**
     * Opens a dialog to prompt the user for a custom notification message
     * and sends the notification to a specific group of participants.
     *
     * @param group The group to send the notification to (e.g., "waitingList", "selectedParticipants").
     */
    private void sendNotificationToGroup(String group) {
        final EditText input = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Send Notification")
                .setMessage("Enter the notification message:")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String customMessage = input.getText().toString();
                    sendNotifications(group, customMessage);
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Sends a notification to a specified group of participants with a custom message.
     *
     * @param group   The group to notify (e.g., "waitingList", "selectedParticipants").
     * @param message The custom notification message.
     */
    private void sendNotifications(String group, String message) {
        eventController.sendNotificationsToGroup(event, group, message, this, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(EventDetailActivity.this, "Notifications sent.", Toast.LENGTH_SHORT).show();
                checkForNewNotifications();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Failed to send notifications.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending notifications", e);
            }
        });
    }

    /**
     * Checks for new unread notifications for the current user.
     * If unread notifications are found, they are displayed as notifications and marked as read in the database.
     */
    private void checkForNewNotifications() {
        NotificationController notificationController = new NotificationController();
        notificationController.fetchUnreadNotificationsForUser(deviceID, new DataCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                for (Notification notification : notifications) {
                    // Display the notification
                    NotificationUtils.sendNotification(EventDetailActivity.this, "Event Notification", notification.getMessage(), notification.generateNotificationId(), notification.getEventId());

                    // Mark the notification as read in the database
                    notificationController.markNotificationAsRead(notification);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching notifications", e);
            }
        });
    }

    //------------ helper methods for displaying UI --------------//

    /**
     * Displays the appropriate participant action buttons based on the user's current status in the event.
     * The buttons are conditionally shown or hidden depending on whether the user is in the waiting list,
     * selected list, confirmed list, or cancelled list.
     */
    private void displayParticipantButtons() {
        boolean isInWaitingList = eventController.isUserInWaitingList(event, deviceID);
        boolean isInSelectedList = eventController.isUserInSelectedList(event, deviceID);
        boolean isInConfirmedList = eventController.isUserInConfirmedList(event, deviceID);
        boolean isInCancelledList = eventController.isUserInCancelledList(event, deviceID);

        joinWaitingListButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);

        if (!(isInWaitingList || isInSelectedList || isInConfirmedList || isInCancelledList)) {
            // If the event draw has occurred
            if (event.isDrawed()) {
                eventActionsTextView.setText("Event draw has already occurred, checkout another event!");
                return;
            }

            // If there is a waiting list limit
            if (event.getWaitingListLimit() != null) {
                if (event.getWaitingListFull()) {
                    eventActionsTextView.setText("Waiting list is currently full, try again later.");
                    return;
                } else {
                    // Waiting list is not full
                    eventActionsTextView.setText("Join the waiting list for a chance to participate!");
                    joinWaitingListButton.setVisibility(View.VISIBLE);
                    return;
                }
            } else {
                // No waiting list limit (unlimited)
                eventActionsTextView.setText("Join the waiting list for a chance to participate!");
                joinWaitingListButton.setVisibility(View.VISIBLE);
                return;
            }

        }


        // if user is in the waiting list
        if (isInWaitingList) {
            eventActionsTextView.setText("You are currently in the waiting list for this event!");
            leaveWaitingListButton.setVisibility(View.VISIBLE);
            return;
        }

        if (isInSelectedList) {
            eventActionsTextView.setText("You have been selected to participate!\n" + "Please accept or decline your invitation as soon as possible");
            declineInvitationButton.setVisibility(View.VISIBLE);
            acceptInvitationButton.setVisibility(View.VISIBLE);
            return;
        }

        if (isInConfirmedList || isInCancelledList) {
            if (isInConfirmedList) {
                eventActionsTextView.setText("You are currently enrolled for this event.\n" + "See you there!");
            } else {
                eventActionsTextView.setText("Either you have been removed by the creator of this event or you have previously declined your invitation");
            }
            return;
        }
    }


    /**
     * Displays the appropriate organizer action buttons and updates the organizer-specific UI elements.
     * Organizer options include editing event details, managing participants, performing draws, and sending notifications.
     */
    private void displayOrganizerButtons() {
        eventActionsTextView.setText("You are the organizer of this event!\n" + "Edit event details, perfom the event draw or manage entrants who have joined");
        // Organizer buttons
        editInfoButton.setVisibility(View.VISIBLE);
        manageParticipantsButton.setVisibility(View.VISIBLE);
        drawButton.setVisibility(View.VISIBLE);
        notifyWaitingListButton.setVisibility(View.VISIBLE);
        notifyAcceptInvitationButton.setVisibility(View.VISIBLE);
        notifySelectedEntrantsButton.setVisibility(View.VISIBLE);
        notifyCancelledEntrantsButton.setVisibility(View.VISIBLE);
        viewMapButton.setVisibility(View.VISIBLE);
        // Draw button state
        if (event.isDrawed()) {
            drawButton.setText("Draw Replacement Participants");
        }
    }

    /**
     * Hides all buttons and actions specific to event organizers.
     * This is used when the current user is not the organizer of the event.
     */
    private void hideAllOrganizerButtons() {
        editInfoButton.setVisibility(View.GONE);
        manageParticipantsButton.setVisibility(View.GONE);
        drawButton.setVisibility(View.GONE);
        notifyWaitingListButton.setVisibility(View.GONE);
        notifyAcceptInvitationButton.setVisibility(View.GONE);
        notifySelectedEntrantsButton.setVisibility(View.GONE);
        notifyCancelledEntrantsButton.setVisibility(View.GONE);
        viewMapButton.setVisibility(View.GONE);
    }

    /**
     * Hides all buttons and actions specific to event participants.
     * This is used when the current user is the organizer of the event.
     */
    private void hideAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
    }
}
