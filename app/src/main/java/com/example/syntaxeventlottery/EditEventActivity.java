// EditEventActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The {@code EditEventActivity} class allows organizers to edit event details.
 * It provides functionality to update event name, description, start and end dates,
 * capacity, location requirements, and poster images.
 * Uses {@link EventController} to handle business logic.
 */
public class EditEventActivity extends AppCompatActivity {

    private static final String TAG = "EditEventActivity";

    private EditText editEventName;
    private EditText editEventDescription;
    private EditText editStartDate;
    private EditText editEndDate;
    private EditText editCapacity;
    private Button saveEventButton;
    private Button backButton;
    private ImageButton updatePosterButton;
    private ImageButton resetPosterButton;
    private ImageView updatePosterView;
    public Switch locationSwitch;
    private Uri imageUri;

    private EventController eventController;
    private Event currentEvent;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    updatePosterView.setImageURI(imageUri);
                }
            }
    );

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves the event details, and sets up event listeners.
     *
     * @param savedInstanceState The saved instance state, or {@code null} if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize UI components
        editEventName = findViewById(R.id.editEventName);
        editEventDescription = findViewById(R.id.editEventDescription);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        editCapacity = findViewById(R.id.editCapacity);
        saveEventButton = findViewById(R.id.saveEventButton);
        backButton = findViewById(R.id.backButton);
        updatePosterButton = findViewById(R.id.updatePosterButton);
        resetPosterButton = findViewById(R.id.resetPosterButton);
        updatePosterView = findViewById(R.id.updatePosterView);
        locationSwitch = findViewById(R.id.locationSwitch);

        // initialize event controller
        eventController = new EventController(new EventRepository());

        // get event using intent
        currentEvent = (Event) getIntent().getSerializableExtra("event");
        Log.d(TAG, "current event from intent: "+ currentEvent);

        if (currentEvent == null || currentEvent.getEventID() == null || currentEvent.getEventID().isEmpty()) { // if event cannot be found
            Log.d("EditEventActivity","Event Object was null");
            Toast.makeText(this, "Error Loading Event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateEventDetails();

        // Disable the save button until the event is loaded
        //saveEventButton.setEnabled(false);

        // Set up button listeners
        backButton.setOnClickListener(v -> finish());
        saveEventButton.setOnClickListener(v -> saveEventDetails());

        resetPosterButton.setOnClickListener(v -> {
            imageUri = null;
            updatePosterView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_no_event_poster));
            currentEvent.setPosterUrl(null);
        });

        updatePosterButton.setOnClickListener(v -> {
            // listener for selecting new event poster
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    /**
     * Populates the UI fields with the event details.
     *
     */
    private void populateEventDetails() {

        editEventName.setText(currentEvent.getEventName());
        editEventDescription.setText(currentEvent.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startDateStr = (currentEvent.getStartDate() != null) ? dateFormat.format(currentEvent.getStartDate()) : "";
        String endDateStr = (currentEvent.getEndDate() != null) ? dateFormat.format(currentEvent.getEndDate()) : "";
        editStartDate.setText(startDateStr);
        editEndDate.setText(endDateStr);
        editCapacity.setText(String.valueOf(currentEvent.getCapacity()));
        locationSwitch.setChecked(currentEvent.getLocationRequired());

        if (currentEvent.getPosterUrl() != null && !currentEvent.getPosterUrl().isEmpty()) {
            Glide.with(this)  // Glide automatically cancels when the activity is destroyed
                    .load(currentEvent.getPosterUrl())
                    .into(updatePosterView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_no_event_poster)
                    .into(updatePosterView);
        }
    }

    /**
     * Saves the updated event details, including validation and updating the event in the repository.
     */
    private void saveEventDetails() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data is not loaded. Cannot save changes.", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventName = editEventName.getText().toString().trim();
        String eventDescription = editEventDescription.getText().toString().trim();
        String startDateStr = editStartDate.getText().toString().trim();
        String endDateStr = editEndDate.getText().toString().trim();
        String capacityStr = editCapacity.getText().toString().trim();
        boolean isLocationRequired = locationSwitch.isChecked();

        // Validate input fields
        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(eventDescription) ||
                TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr) ||
                TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(startDateStr);
            endDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error", e);
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse capacity
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                throw new NumberFormatException("Capacity must be positive");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Capacity parsing error", e);
            Toast.makeText(this, "Invalid capacity. It must be a positive number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the currentEvent object
        currentEvent.setEventName(eventName);
        currentEvent.setDescription(eventDescription);
        currentEvent.setStartDate(startDate);
        currentEvent.setEndDate(endDate);
        currentEvent.setCapacity(capacity);
        currentEvent.setLocationRequired(isLocationRequired);

        eventController.updateEvent(currentEvent, imageUri, null, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EditEventActivity.this, "Event details updated successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Event updated successfully");
                finish();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error updating event information");
                Toast.makeText(EditEventActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}