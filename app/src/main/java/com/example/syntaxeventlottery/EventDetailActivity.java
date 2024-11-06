package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.annotation.NonNull;

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
import java.util.Random;

public class EventDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, joinEventButton, leaveEventButton, editInfoButton, backButton, drawButton;
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
        drawButton = findViewById(R.id.drawButton); // Initialize drawButton

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

        // Set click listener for the back button to return to the previous screen
        backButton.setOnClickListener(v -> finish());

        // Set click listener for the update poster button to select and upload a new poster image
        updatePosterButton.setOnClickListener(v -> openImagePicker());

        // Set click listener for the edit information button to open EditEventActivity
        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event_id", eventId); // Pass event ID to the edit screen
            startActivity(intent);
        });

        // Set click listener for the join event button
        joinEventButton.setOnClickListener(v -> joinEvent());

        // Set click listener for the leave event button
        leaveEventButton.setOnClickListener(v -> leaveEvent());

        // Set click listener for the draw button
        drawButton.setOnClickListener(v -> drawParticipants());
    }

    /**
     * Opens the image picker to select a new event poster.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle image selection for poster update
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadPosterImage(imageUri);
        }
    }

    private void uploadPosterImage(Uri imageUri) {
        // Your existing code for uploading the poster image
    }

    private void loadEventDetails(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Fetch event details from Firestore
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

                    // Format and set dates
                    SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    if (startDate != null) {
                        eventStartDateTextView.setText("Start Date: " + displayFormat.format(startDate));
                    }
                    if (endDate != null) {
                        eventEndDateTextView.setText("End Date: " + displayFormat.format(endDate));
                    }

                    eventFacilityTextView.setText("Location: " + facility);
                    eventCapacityTextView.setText("Capacity: " + (capacity != null ? capacity.toString() : "N/A"));

                    // Load images using Glide
                    if (eventPosterUrl != null) {
                        Glide.with(EventDetailActivity.this)
                                .load(eventPosterUrl)
                                .into(eventPosterImageView);
                    }
                    if (qrCodeUrl != null) {
                        Glide.with(EventDetailActivity.this)
                                .load(qrCodeUrl)
                                .into(eventQRCodeImageView);
                    }
                } else {
                    Toast.makeText(EventDetailActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void drawParticipants() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                Long capacity = documentSnapshot.getLong("capacity");

                if (participants == null || participants.isEmpty()) {
                    Toast.makeText(this, "No participants to choose from", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (capacity == null || capacity <= 0) {
                    Toast.makeText(this, "Invalid capacity", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Shuffle participants and split into chosen and unchosen lists
                Collections.shuffle(participants);
                ArrayList<String> ChosenList = new ArrayList<>();
                ArrayList<String> UnChosenList = new ArrayList<>();

                if (participants.size() <= capacity) {
                    ChosenList.addAll(participants);
                } else {
                    ChosenList.addAll(participants.subList(0, capacity.intValue()));
                    UnChosenList.addAll(participants.subList(capacity.intValue(), participants.size()));
                }

                // Update Firestore with chosen and unchosen lists
                eventRef.update("ChosenList", ChosenList, "UnChosenList", UnChosenList)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Draw successful", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save draw results", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });

            } else {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }

    private void joinEvent() {
        // Your existing joinEvent method
    }

    private void leaveEvent() {
        // Your existing leaveEvent method
    }
}
