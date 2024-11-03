package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrganizerActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText facilityEditText;
    private EditText capacityEditText;
    private Button createEventButton;
    private Button backButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        facilityEditText = findViewById(R.id.facilityEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set click listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set click listener for the create event button
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEventToDatabase();
            }
        });
    }

    private void saveEventToDatabase() {
        // Generate a unique eventID
        String eventID = UUID.randomUUID().toString();

        // Retrieve input data from the EditText fields
        String eventName = eventNameEditText.getText().toString();
        String eventStartDate = eventStartDateEditText.getText().toString();
        String eventEndDate = eventEndDateEditText.getText().toString();
        String facility = facilityEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();

        // Check if all fields are filled
        if (eventName.isEmpty() || eventStartDate.isEmpty() || eventEndDate.isEmpty() || facility.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(OrganizerActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse capacity to integer
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(OrganizerActivity.this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse start and end dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(eventStartDate);
            endDate = dateFormat.parse(eventEndDate);
        } catch (ParseException e) {
            Toast.makeText(OrganizerActivity.this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Map to store event data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventID", eventID);
        eventData.put("eventName", eventName);
        eventData.put("startDate", startDate);
        eventData.put("endDate", endDate);
        eventData.put("facility", facility);
        eventData.put("capacity", capacity);

        // Save to Firestore using eventID as the document ID
        db.collection("events").document(eventID)
                .set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrganizerActivity.this, "Event Created and Saved to Database", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> Toast.makeText(OrganizerActivity.this, "Failed to save event", Toast.LENGTH_SHORT).show());
    }

    private void clearInputFields() {
        eventNameEditText.setText("");
        eventStartDateEditText.setText("");
        eventEndDateEditText.setText("");
        facilityEditText.setText("");
        capacityEditText.setText("");
    }
}
