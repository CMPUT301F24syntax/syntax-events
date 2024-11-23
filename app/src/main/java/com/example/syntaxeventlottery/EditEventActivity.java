// EditEventActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The EditEventActivity class allows organizers to edit event details.
 * It uses the EventController to handle business logic.
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

    private EventController eventController;
    private String eventId;
    private Event currentEvent;

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

        // initialize event controller
        eventController = new EventController(new EventRepository());

        // Get event ID from Intent
        eventId = getIntent().getStringExtra("eventID");
        currentEvent = (Event) getIntent().getSerializableExtra("event"); // get event using intent

        if (currentEvent == null || eventId == null || eventId.isEmpty()) { // if event cannot be found
            Log.d("EditEventActivity","MMMMMMMMM"+eventId);
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateEventDetails();

        // Disable the save button until the event is loaded
        //saveEventButton.setEnabled(false);

        // Set up button listeners
        backButton.setOnClickListener(v -> finish());
        saveEventButton.setOnClickListener(v -> saveEventDetails());
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
    }

    /**
     * Saves the updated event details.
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

        eventController.updateEvent(currentEvent, null, null, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EditEventActivity.this, "Event details updated successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Event updated successfully");
                finish();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error updating event information");
                finish();
            }
        });
    }
}