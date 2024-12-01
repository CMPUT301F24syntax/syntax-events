// OrganizerCreateEvent.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The {@code OrganizerCreateEvent} class allows event organizers to create new events.
 */
public class OrganizerCreateEvent extends AppCompatActivity {
    private static final String TAG = "Organizer Create Event";

    private EditText eventNameEditText;
    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText capacityEditText;
    private EditText eventDescriptionEditText;
    private EditText waitingListLimitEditText;
    private Button createEventButton;
    private Button backButton;
    private ImageButton uploadPosterButton;
    private ImageButton resetPosterButton;
    private SwitchCompat waitingListLimitSwitch;
    private ImageView eventImageView;
    private Uri imageUri;
    private EventController eventController;
    private UserController userController;
    private Switch locationRequiredSwitch;


    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    eventImageView.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Initialize UI components
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        waitingListLimitSwitch = findViewById(R.id.waitingListLimitSwitch);
        waitingListLimitEditText = findViewById(R.id.waitingListLimitEditText);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        uploadPosterButton = findViewById(R.id.updatePosterButton);
        resetPosterButton = findViewById(R.id.resetPosterButton);
        eventImageView = findViewById(R.id.updatePosterView);
        locationRequiredSwitch = findViewById(R.id.locationRequiredSwitch);
        eventImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_no_event_poster));

        // Initialize controllers with repository
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository());

        // Back button listener
        backButton.setOnClickListener(v -> finish());

        // Image upload button listener
        uploadPosterButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Reset poster button listener
        resetPosterButton.setOnClickListener(v -> {
            imageUri = null;
            eventImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_no_event_poster));
        });

        // waiting list switch listener
        waitingListLimitSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (!isChecked) {
                waitingListLimitEditText.setVisibility(View.INVISIBLE); // Hide EditText when the switch is off
                waitingListLimitEditText = null; // set to null
            } else {
                waitingListLimitEditText = findViewById(R.id.waitingListLimitEditText);
                waitingListLimitEditText.setVisibility(View.VISIBLE); // Show EditText when the switch is on
            }
        });

        // Create event button listener
        createEventButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String organizerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String waitingListLimitStr = null; // default value

        // get edit text values
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        String eventStartDateText = eventStartDateEditText.getText().toString();
        String eventEndDateText = eventEndDateEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        boolean isLocationRequired = locationRequiredSwitch.isChecked();
        // check if there is a waiting list limit
        if (waitingListLimitEditText != null) {
            waitingListLimitStr = waitingListLimitEditText.getText().toString();
        }

        // First validate all inputs
        if (!validateEventInput(eventName, eventDescription, eventStartDateText,
                eventEndDateText, capacityStr, waitingListLimitStr)) {
            return; // return if event inputs are not valid
        }

        // Parse dates after validation
        Date startDate = getParsedDate(eventStartDateText);
        Date endDate = getParsedDate(eventEndDateText);

        if (startDate == null || endDate == null) {
            return; // return if dates are not parsed correctly
        }

        // Check date order
        if (startDate.after(endDate)) {
            Toast.makeText(this, "Start date is after event ends", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse capacity and limit after validation
        int capacity = Integer.parseInt(capacityStr);
        Integer waitingListLimit = null;
        if (waitingListLimitStr != null && !waitingListLimitStr.isEmpty()) {
            waitingListLimit = Integer.parseInt(waitingListLimitStr);
        }
        // ensure waitinglistlimit is larger than capacity
        if (waitingListLimit != null && waitingListLimit < capacity) {
            Toast.makeText(this, "limit must be larger than capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        // get most updated user (organizer) information facility property
        Integer finalWaitingListLimit = waitingListLimit;
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // get user and save the event
                User user = userController.getUserByDeviceID(organizerId);
                if (user == null) {
                    Log.e(TAG, "Couldn't find current user information");
                    Toast.makeText(OrganizerCreateEvent.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Event event = new Event(eventName, user.getFacility().getName(), user.getFacility().getLocation(), eventDescription, capacity, startDate, endDate, organizerId, finalWaitingListLimit, isLocationRequired);
                eventController.addEvent(event, imageUri, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Toast.makeText(OrganizerCreateEvent.this, "Event creation success", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(OrganizerCreateEvent.this, "Start Date must later than now!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, e.toString());
                        finish();
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(OrganizerCreateEvent.this, "Event creation Error2!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, e.toString());
                finish();
            }
        });
    }

    private void clearInputFields() {
        eventNameEditText.setText("");
        eventDescriptionEditText.setText("");
        eventStartDateEditText.setText("");
        eventEndDateEditText.setText("");
        capacityEditText.setText("");
        eventImageView.setImageURI(null);
        uploadPosterButton.setVisibility(View.VISIBLE);
        imageUri = null;
    }

    private boolean validateEventInput(String eventName, String eventDescription,
                                       String startDateStr, String endDateStr, String capacityStr,
                                       String waitingListLimitStr) {

        // Check if any required field is empty
        if (eventName.isEmpty() || eventDescription.isEmpty() || startDateStr.isEmpty() ||
                endDateStr.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate capacity format
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                Toast.makeText(this, "Capacity must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for capacity", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate waiting list limit if enabled
        if (waitingListLimitSwitch.isChecked()) {
            if (waitingListLimitStr == null || waitingListLimitStr.isEmpty()) {
                Toast.makeText(this, "Please enter limit or uncheck switch", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                int limit = Integer.parseInt(waitingListLimitStr);
                if (limit <= 0) {
                    Toast.makeText(this, "Waiting list limit must be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number for waiting list limit", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    // validate date
    public Date getParsedDate(String dateStr) {
        Date parsedDate; // initialize date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            parsedDate = dateFormat.parse(dateStr);
        } catch (ParseException e){
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return null;
        }
        return parsedDate;
    }
}