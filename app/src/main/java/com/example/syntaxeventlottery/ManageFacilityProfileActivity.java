package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManageFacilityProfileActivity extends AppCompatActivity {
    private final String TAG = "ManageFacilityProfileActivity";

    public EditText facilityNameEditText;
    public EditText facilityLocationEditText;
    private Button buttonBack;
    public Button buttonSave;
    public UserController userController;
    public EventController eventController;
    public User currentUser;
    private String deviceID;
    private Facility facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_facility_profile);

        // Initialize views
        facilityNameEditText = findViewById(R.id.editFacilityNameEditText);
        facilityLocationEditText = findViewById(R.id.editFacilityLocationEditText);
        buttonBack = findViewById(R.id.back_Button);
        buttonSave = findViewById(R.id.save_Button);

        // get the deviceID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initalize controllers
        userController = new UserController(new UserRepository());
        eventController = new EventController(new EventRepository());

        // Set onClick listener for the Back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Navigate back to the previous page
            }
        });

        // Set onClick listener for the Save button
        buttonSave.setOnClickListener(v -> updateFacilityDetails());
        buttonSave.setEnabled(false); // set false just incase pressed before loading

        getCurrentFacilityDetails();
    }

    public void getCurrentFacilityDetails() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceID);
                if (currentUser == null) {
                    Toast.makeText(ManageFacilityProfileActivity.this, "Failed to retrieve facility information.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // if no facility profile found launch the activity
                    if (currentUser.getFacility() == null) {
                        Toast.makeText(ManageFacilityProfileActivity.this, "No facility profile found, please create one.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ManageFacilityProfileActivity.this, FacilityProfileActivity.class);
                        intent.putExtra("currentUser", currentUser);
                        startActivity(intent);
                    } else {
                        facility = currentUser.getFacility();
                        // if facility is found
                        facilityNameEditText.setText(facility.getName());
                        facilityLocationEditText.setText(facility.getLocation());
                        buttonSave.setEnabled(true);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing repository", e);
                finish();
            }
        });
    }

    public void updateFacilityDetails() {
        String facilityName = facilityNameEditText.getText().toString();
        String facilityLocation = facilityLocationEditText.getText().toString();

        // Check if the facility details input is empty
        if (facilityName.isEmpty() || facilityLocation.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Facility facility = new Facility(facilityName, facilityLocation, currentUser.getUserID());
        Log.d(TAG, "current user: " + currentUser);

        if (currentUser != null) {
            currentUser.setFacility(facility);
            Log.d(TAG, "facility details:" + currentUser.getFacility().getName() + " " + currentUser.getFacility().getLocation());

            userController.updateUser(currentUser, null, new DataCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Log.d(TAG, "Facility profile details updated: "+ result);
                    updateUserEvents(result, result.getFacility().getName(), result.getFacility().getLocation());
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error updating facility profile", e);
                    Toast.makeText(ManageFacilityProfileActivity.this, "Error updating facility profile", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    private void updateUserEvents(User user, String newFacilityName, String newFacilityLocation) {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                ArrayList<Event> organizerEvents = eventController.getOrganizerEvents(user.getUserID());
                if (organizerEvents.isEmpty()) {
                    Toast.makeText(ManageFacilityProfileActivity.this, "Facility Profile updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ManageFacilityProfileActivity.this, OrganizerActivity.class));
                    finish();
                    return;
                }

                // Proceed with updating all associated events
                // Use AtomicBoolean to track if all events were updated successfully
                final AtomicBoolean allEventsUpdatedSuccessfully = new AtomicBoolean(true);
                for (Event e : organizerEvents) {
                    e.setFacilityName(newFacilityName);
                    e.setFacilityLocation(newFacilityLocation);
                    eventController.updateEvent(e, null, null, new DataCallback<Event>() {
                        @Override
                        public void onSuccess(Event result) {
                            Log.d(TAG, "Event details updated: " + result);
                        }

                        @Override
                        public void onError(Exception e) {
                            // Log error but continue to process other events
                            Log.e(TAG, "Error updating event: " + e.getMessage());
                            allEventsUpdatedSuccessfully.set(false);
                        }
                    });
                }

                // Navigate to OrganizerActivity after all updates are attempted
                if (allEventsUpdatedSuccessfully.get()) {
                    Toast.makeText(ManageFacilityProfileActivity.this, "Facility Profile updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ManageFacilityProfileActivity.this, OrganizerActivity.class));
                } else {
                    Toast.makeText(ManageFacilityProfileActivity.this, "Some events could not be updated", Toast.LENGTH_SHORT).show();
                }
                finish(); // Finish the activity after trying to update the events
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageFacilityProfileActivity.this, "Error retrieving event data.", Toast.LENGTH_SHORT).show();
                finish(); // Finish activity if there's an error with retrieving event data
            }
        });
    }

}
