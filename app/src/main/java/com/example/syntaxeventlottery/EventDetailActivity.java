package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, joinEventButton, leaveEventButton, editInfoButton, backButton, drawButton, waitingListButton;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private String organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize UI components
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        eventQRCodeImageView = findViewById(R.id.eventQRCodeImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventFacilityTextView = findViewById(R.id.eventFacilityTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        updatePosterButton = findViewById(R.id.updatePosterButton);
        joinEventButton = findViewById(R.id.joinEventButton);
        leaveEventButton = findViewById(R.id.leaveEventButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        backButton = findViewById(R.id.backButton);
        drawButton = findViewById(R.id.drawButton);
        waitingListButton = findViewById(R.id.waitingListButton); // Initialize waitingListButton

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the event ID passed from the previous screen
        eventId = getIntent().getStringExtra("event_id");

        // Load event details from Firestore
        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
        }

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        updatePosterButton.setOnClickListener(v -> openImagePicker());
        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
        joinEventButton.setOnClickListener(v -> joinEvent());
        leaveEventButton.setOnClickListener(v -> leaveEvent());
        drawButton.setOnClickListener(v -> drawParticipants());

        // Set click listener for waitingListButton
        waitingListButton.setOnClickListener(v -> {
            // Start EventWaitingListActivity
            Intent intent = new Intent(EventDetailActivity.this, EventWaitingListActivity.class);
            intent.putExtra("event_id", eventId); // Pass event ID to the waiting list activity
            startActivity(intent);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadPosterImage(imageUri);
        }
    }

    private void uploadPosterImage(Uri imageUri) {
        // Upload image to Firebase Storage and update Firestore URL
    }

    private void loadEventDetails(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve event data
                    eventName = document.getString("eventName");
                    String eventDescription = document.getString("description");
                    String eventPosterUrl = document.getString("posterUrl");
                    Date startDate = document.getDate("startDate");
                    Date endDate = document.getDate("endDate");
                    String facility = document.getString("facility");
                    Long capacity = document.getLong("capacity");
                    String qrCodeUrl = document.getString("qrCodeUrl");
                    organizerId = document.getString("organizerId");

                    // Set data to TextViews
                    eventNameTextView.setText(eventName);
                    eventDescriptionTextView.setText(eventDescription);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    eventStartDateTextView.setText("Start Date: " + (startDate != null ? displayFormat.format(startDate) : "N/A"));
                    eventEndDateTextView.setText("End Date: " + (endDate != null ? displayFormat.format(endDate) : "N/A"));
                    eventFacilityTextView.setText("Location: " + facility);
                    eventCapacityTextView.setText("Capacity: " + (capacity != null ? capacity.toString() : "N/A"));

                    if (eventPosterUrl != null) {
                        Glide.with(EventDetailActivity.this).load(eventPosterUrl).into(eventPosterImageView);
                    }
                    if (qrCodeUrl != null) {
                        Glide.with(EventDetailActivity.this).load(qrCodeUrl).into(eventQRCodeImageView);
                    }

                    // Check if current user is the organizer
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    if (organizerId != null && organizerId.equals(deviceId)) {
                        updatePosterButton.setVisibility(View.VISIBLE);
                        drawButton.setVisibility(View.VISIBLE);
                        joinEventButton.setVisibility(View.GONE);
                        leaveEventButton.setVisibility(View.GONE);
                        editInfoButton.setVisibility(View.VISIBLE);
                    } else {
                        updatePosterButton.setVisibility(View.GONE);
                        drawButton.setVisibility(View.GONE);
                        editInfoButton.setVisibility(View.GONE);

                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null && participants.contains(deviceId)) {
                            joinEventButton.setVisibility(View.GONE);
                            leaveEventButton.setVisibility(View.VISIBLE);
                        } else {
                            joinEventButton.setVisibility(View.VISIBLE);
                            leaveEventButton.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(EventDetailActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void drawParticipants() {
        // Draw participants logic
    }

    private void joinEvent() {
        // Join event logic
    }

    private void leaveEvent() {
        // Leave event logic
    }
}
