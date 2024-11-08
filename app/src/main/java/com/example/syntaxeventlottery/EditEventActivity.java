package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class EditEventActivity extends AppCompatActivity {

    private static final String TAG = "EditEventActivity";

    private EditText editEventName;
    private EditText editEventDescription;
    private EditText editStartDate;
    private EditText editEndDate;
    private EditText editCapacity;
    private Button saveEventButton;
    private Button backButton;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize UI components and check for nulls
        editEventName = findViewById(R.id.editEventName);
        checkNull(editEventName, "editEventName");

        editEventDescription = findViewById(R.id.editEventDescription);
        checkNull(editEventDescription, "editEventDescription");

        editStartDate = findViewById(R.id.editStartDate);
        checkNull(editStartDate, "editStartDate");

        editEndDate = findViewById(R.id.editEndDate);
        checkNull(editEndDate, "editEndDate");


        editCapacity = findViewById(R.id.editCapacity);
        checkNull(editCapacity, "editCapacity");

        saveEventButton = findViewById(R.id.saveEventButton);
        checkNull(saveEventButton, "saveEventButton");

        backButton = findViewById(R.id.backButton);
        checkNull(backButton, "backButton");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get event ID from Intent and check for null
        eventId = getIntent().getStringExtra("event_id");
        Log.d(TAG, "Event ID received: " + eventId);

        if (eventId != null) {
            loadEventDetails();
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton.setOnClickListener(v -> finish());
        saveEventButton.setOnClickListener(v -> saveEventDetails());
    }

    private void checkNull(Object view, String viewName) {
        if (view == null) {
            Log.e(TAG, viewName + " is null. Check the ID or layout XML.");
            Toast.makeText(this, "Error: " + viewName + " is missing in layout.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Log.d(TAG, "Event document found. Populating data.");
                        try {
                            editEventName.setText(document.getString("eventName"));
                            editEventDescription.setText(document.getString("description"));

                            Date startDate = document.getDate("startDate");
                            Date endDate = document.getDate("endDate");
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            editStartDate.setText(startDate != null ? dateFormat.format(startDate) : "");
                            editEndDate.setText(endDate != null ? dateFormat.format(endDate) : "");
                            editCapacity.setText(String.valueOf(document.getLong("capacity")));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event data", e);
                            Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Event document does not exist.");
                        Toast.makeText(EditEventActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event details", e);
                    Toast.makeText(EditEventActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEventDetails() {
        String eventName = editEventName.getText().toString().trim();
        String eventDescription = editEventDescription.getText().toString().trim();
        String startDateStr = editStartDate.getText().toString().trim();
        String endDateStr = editEndDate.getText().toString().trim();
        String capacity = editCapacity.getText().toString().trim();

        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(eventDescription) ||
                TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr) ||
              TextUtils.isEmpty(capacity)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

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

        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("eventName", eventName);
        eventUpdates.put("description", eventDescription);
        eventUpdates.put("startDate", startDate);
        eventUpdates.put("endDate", endDate);
        eventUpdates.put("capacity", Long.parseLong(capacity));

        db.collection("events").document(eventId).update(eventUpdates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event updated successfully");
                    Toast.makeText(EditEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditEventActivity.this, UserHomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update event", e);
                    Toast.makeText(EditEventActivity.this, "Failed to update event", Toast.LENGTH_SHORT).show();
                });
    }
}
