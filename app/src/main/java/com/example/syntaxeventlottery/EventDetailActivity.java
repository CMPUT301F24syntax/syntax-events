// EventDetailActivity.java
package com.example.syntaxeventlottery;
import static androidx.constraintlayout.motion.widget.Debug.getLocation;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    // locaiton
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isLocationPermissionGranted = false;
    private UserController userController;
    private User currentUser;

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
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                event = eventController.getEventById(eventID);
                if (event == null) {
                    Log.e(TAG, "Event not found in repository.");
                    Toast.makeText(EventDetailActivity.this, "Failed to find the event", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (event.getLocationRequired()) {
                    handleLocationRequirement();
                } else {
                    updateUI(event);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing event repository: ", e);
                Toast.makeText(EventDetailActivity.this, "Failed to get updated data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void handleLocationRequirement() {
        Log.d(TAG, "Entered handleLocationRequirement.");
        AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
        builder.setTitle("Location Required")
                .setMessage("This event requires your location. Do you want to enable location permissions?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d(TAG, "User chose to enable location permissions.");
                    checkAndRequestLocationPermission();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "User chose not to enable location permissions.");
                    navigateToUserHome();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void checkAndRequestLocationPermission() {
        try {
            Log.d(TAG, "Checking and requesting location permission.");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission not granted. Requesting now...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Permission already granted.");
                isLocationPermissionGranted = true;
                proceedWithLocationAccess();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in checkAndRequestLocationPermission: ", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult triggered with requestCode: " + requestCode);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted.");
                isLocationPermissionGranted = true;
                proceedWithLocationAccess();
            } else {
                Log.d(TAG, "Location permission denied.");
                isLocationPermissionGranted = false;
            }
        }
    }


    private void proceedWithLocationAccess() {
        Log.d(TAG, "Proceeding with location access...");

        // 获取 LocationManager 实例
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // 获取最后的已知位置（可以立即使用）
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                Log.d(TAG, "Last Known Location - Lat: " + latitude + ", Lng: " + longitude);

                // 在这里存储用户的位置信息到数据库或服务器
                saveUserLocation(latitude, longitude);

                Toast.makeText(this, "Location retrieved: Lat=" + latitude + ", Lng=" + longitude, Toast.LENGTH_SHORT).show();
            } else {
                // 注册监听器以获取实时位置更新
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000, // 更新间隔时间（毫秒）
                        10,   // 最小距离（米）
                        locationListener
                );
            }
        } else {
            Toast.makeText(this, "Location permission is not granted.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Location permission not granted. Unable to proceed.");
        }
    }

    // 定义 LocationListener 以接收实时位置更新
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "Real-time Location - Lat: " + latitude + ", Lng: " + longitude);

            // 在这里存储用户的位置信息到数据库或服务器
            saveUserLocation(latitude, longitude);

            Toast.makeText(EventDetailActivity.this, "Real-time location retrieved: Lat=" + latitude + ", Lng=" + longitude, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

    private void saveUserLocation(double latitude, double longitude) {
        Log.d(TAG, "Saving user location: Lat=" + latitude + ", Lng=" + longitude);

        if (currentUser != null) {
            // Store location in the current user object
            ArrayList<Double> location = new ArrayList<>();
            location.add(latitude);
            location.add(longitude);
            currentUser.setLocation(location);

            // Update user information in the database
            userController.updateUserLocation(currentUser, latitude, longitude, new DataCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Log.d(TAG, "User location updated successfully in database.");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to update user location in database.", e);
                }
            });
        } else {
            Log.e(TAG, "Current user is null. Unable to save location.");
        }
    }


    private void navigateToUserHome() {
        Intent intent = new Intent(EventDetailActivity.this, UserHomeActivity.class);
        startActivity(intent);
        finish();
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
