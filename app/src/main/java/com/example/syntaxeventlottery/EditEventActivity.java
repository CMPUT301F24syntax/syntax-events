package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private EditText editEventName, editEventDescription, editStartDate, editEndDate, editFacility, editCapacity;
    private Button saveEventButton, backButton;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize UI components
        editEventName = findViewById(R.id.editEventName);
        editEventDescription = findViewById(R.id.editEventDescription);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        editFacility = findViewById(R.id.editFacility);
        editCapacity = findViewById(R.id.editCapacity);
        saveEventButton = findViewById(R.id.saveEventButton);
        backButton = findViewById(R.id.backButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get event ID from Intent
        eventId = getIntent().getStringExtra("event_id");

        // Load event details if event ID is available
        if (eventId != null) {
            loadEventDetails();
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listener for the back button to close the activity
        backButton.setOnClickListener(v -> finish());

        // Set click listener for save button
        saveEventButton.setOnClickListener(v -> saveEventDetails());
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Set existing data to EditTexts
                        editEventName.setText(document.getString("eventName"));
                        editEventDescription.setText(document.getString("description"));
                        editStartDate.setText(document.getDate("startDate").toString());
                        editEndDate.setText(document.getDate("endDate").toString());
                        editFacility.setText(document.getString("facility"));
                        editCapacity.setText(String.valueOf(document.getLong("capacity")));
                    } else {
                        Toast.makeText(EditEventActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditEventActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    private void saveEventDetails() {
        String eventName = editEventName.getText().toString().trim();
        String eventDescription = editEventDescription.getText().toString().trim();
        String startDate = editStartDate.getText().toString().trim();
        String endDate = editEndDate.getText().toString().trim();
        String facility = editFacility.getText().toString().trim();
        String capacity = editCapacity.getText().toString().trim();

        // Validate fields
        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(eventDescription) ||
                TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) ||
                TextUtils.isEmpty(facility) || TextUtils.isEmpty(capacity)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map with the updated values
        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("eventName", eventName);
        eventUpdates.put("description", eventDescription); // Add description to updates
        eventUpdates.put("startDate", startDate); // You might need to parse this into a Date object
        eventUpdates.put("endDate", endDate);     // Same as above
        eventUpdates.put("facility", facility);
        eventUpdates.put("capacity", Long.parseLong(capacity));

        // Update Firestore with new details
        db.collection("events").document(eventId).update(eventUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Automatically close activity after saving
                })
                .addOnFailureListener(e -> Toast.makeText(EditEventActivity.this, "Failed to update event", Toast.LENGTH_SHORT).show());
    }
}
