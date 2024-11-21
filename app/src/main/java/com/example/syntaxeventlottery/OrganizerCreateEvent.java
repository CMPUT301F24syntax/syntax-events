// OrganizerCreateEvent.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The {@code OrganizerCreateEvent} class allows event organizers to create new events.
 */
public class OrganizerCreateEvent extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText capacityEditText;
    private EditText eventDescriptionEditText;
    private Switch locationRequiredSwitch;
    private Button createEventButton;
    private Button backButton;
    private Button uploadButton;
    private ImageView eventImageView;
    private Uri imageUri;
    private EventController eventController;

    private EventController.EventCreateCallback eventCreateCallback;

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
        locationRequiredSwitch = findViewById(R.id.locationRequiredSwitch);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        uploadButton = findViewById(R.id.uploadButton);
        eventImageView = findViewById(R.id.eventImageView);

        // Initialize EventController
        eventController = new EventController(null);

        // Initialize EventCreateCallback
        eventCreateCallback = new EventController.EventCreateCallback() {
            @Override
            public void onEventCreated() {
                Toast.makeText(OrganizerCreateEvent.this, "Event Created and Saved to Database", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(OrganizerCreateEvent.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        // Back button listener
        backButton.setOnClickListener(v -> finish());

        // Image upload button listener
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Create event button listener
        createEventButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String eventID = UUID.randomUUID().toString();
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        String eventStartDateText = eventStartDateEditText.getText().toString();
        String eventEndDateText = eventEndDateEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        boolean isLocationRequired = locationRequiredSwitch.isChecked();
        Log.d("Oragensj","TTATATAT"+isLocationRequired);
        String organizerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventStartDateText.isEmpty() || eventEndDateText.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(eventStartDateText);
            endDate = dateFormat.parse(eventEndDateText);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new Event object
        //isLocationRequired = locationRequiredSwitch.isChecked(); // Get the state of the Switch
        Event event = new Event(eventID, eventName, eventDescription, capacity, startDate, endDate, organizerId, isLocationRequired);

        // Use EventController to create event
        eventController.createEvent(event, imageUri, eventCreateCallback);
    }

    private void clearInputFields() {
        eventNameEditText.setText("");
        eventDescriptionEditText.setText("");
        eventStartDateEditText.setText("");
        eventEndDateEditText.setText("");
        capacityEditText.setText("");
        locationRequiredSwitch.setChecked(false);
        eventImageView.setImageURI(null);
        uploadButton.setVisibility(View.VISIBLE);
        imageUri = null;
    }


}