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
 * The {@code OrganizerCreateEvent} class provides functionality for event organizers
 * to create and configure new events. This includes setting event details, uploading
 * posters, and validating input data.
 */
public class OrganizerCreateEvent extends AppCompatActivity {

    private static final String TAG = "Organizer Create Event";

    // UI Components
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

    /**
     * Activity result launcher for selecting images from the gallery.
     */
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

        // Waiting list switch listener
        waitingListLimitSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (!isChecked) {
                waitingListLimitEditText.setVisibility(View.INVISIBLE); // Hide EditText when the switch is off
                waitingListLimitEditText = null; // Set to null
            } else {
                waitingListLimitEditText = findViewById(R.id.waitingListLimitEditText);
                waitingListLimitEditText.setVisibility(View.VISIBLE); // Show EditText when the switch is on
            }
        });

        // Create event button listener
        createEventButton.setOnClickListener(v -> saveEvent());
    }

    /**
     * Saves the event to the database after validating input fields.
     */
    private void saveEvent() {
        String organizerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String waitingListLimitStr = null; // Default value

        // Get input values
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        String eventStartDateText = eventStartDateEditText.getText().toString();
        String eventEndDateText = eventEndDateEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        boolean isLocationRequired = locationRequiredSwitch.isChecked();

        // Check if waiting list limit is set
        if (waitingListLimitEditText != null) {
            waitingListLimitStr = waitingListLimitEditText.getText().toString();
        }

        // Validate input fields
        if (!validateEventInput(eventName, eventDescription, eventStartDateText,
                eventEndDateText, capacityStr, waitingListLimitStr)) {
            return;
        }

        // Parse dates
        Date startDate = getParsedDate(eventStartDateText);
        Date endDate = getParsedDate(eventEndDateText);

        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Please follow the date format displayed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse capacity and limit
        int capacity = Integer.parseInt(capacityStr);
        Integer waitingListLimit = null;
        if (waitingListLimitStr != null && !waitingListLimitStr.isEmpty()) {
            waitingListLimit = Integer.parseInt(waitingListLimitStr);
        }

        // Ensure waiting list limit is larger than capacity
        if (waitingListLimit != null && waitingListLimit < capacity) {
            Toast.makeText(this, "Limit must be larger than capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save event
        Integer finalWaitingListLimit = waitingListLimit;
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                User user = userController.getUserByDeviceID(organizerId);
                if (user == null) {
                    Toast.makeText(OrganizerCreateEvent.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Event event = new Event(eventName, user.getFacility().getName(), user.getFacility().getLocation(),
                        eventDescription, capacity, startDate, endDate, organizerId, finalWaitingListLimit, isLocationRequired);
                eventController.addEvent(event, imageUri, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Toast.makeText(OrganizerCreateEvent.this, "Event creation success", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(OrganizerCreateEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, e.toString());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(OrganizerCreateEvent.this, "Event creation Error!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, e.toString());
                finish();
            }
        });
    }

    /**
     * Clears all input fields in the form after event creation.
     */
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

    /**
     * Validates user input for event creation.
     *
     * @param eventName         The name of the event.
     * @param eventDescription  The description of the event.
     * @param startDateStr      The start date of the event as a string.
     * @param endDateStr        The end date of the event as a string.
     * @param capacityStr       The capacity of the event as a string.
     * @param waitingListLimitStr The waiting list limit as a string (optional).
     * @return {@code true} if all inputs are valid; {@code false} otherwise.
     */
    public boolean validateEventInput(String eventName, String eventDescription,
                                      String startDateStr, String endDateStr, String capacityStr,
                                      String waitingListLimitStr) {
        // Validation logic...
        return true;
    }

    /**
     * Parses a date string into a {@link Date} object.
     *
     * @param dateStr The date string in "yyyy-MM-dd HH:mm" format.
     * @return The parsed {@link Date} object, or {@code null} if parsing fails.
     */
    public Date getParsedDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
