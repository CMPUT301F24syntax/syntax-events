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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, joinEventButton, editInfoButton, backButton;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String eventId;

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
        joinEventButton = findViewById(R.id.joinEventButton); // Newly added
        editInfoButton = findViewById(R.id.editInfoButton);
        backButton = findViewById(R.id.backButton);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

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

        // Set click listener for the join event button to handle joining the event
        joinEventButton.setOnClickListener(v -> joinEvent());
    }

    /**
     * Opens the image picker to select a new event poster.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result from the image picker intent.
     *
     * @param requestCode The request code passed with the intent.
     * @param resultCode  The result code returned by the child activity.
     * @param data        The intent data returned.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadPosterImage(imageUri);
        }
    }

    /**
     * Uploads the selected poster image to Firebase Storage and updates Firestore with the new URL.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadPosterImage(Uri imageUri) {
        if (imageUri != null && eventId != null) {
            StorageReference fileRef = storageRef.child("eventPosters/" + eventId + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String posterUrl = uri.toString();
                db.collection("events").document(eventId).update("posterUrl", posterUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EventDetailActivity.this, "Poster updated successfully", Toast.LENGTH_SHORT).show();
                            Glide.with(this).load(posterUrl).into(eventPosterImageView);
                        })
                        .addOnFailureListener(e -> Toast.makeText(EventDetailActivity.this, "Failed to update poster", Toast.LENGTH_SHORT).show());
            })).addOnFailureListener(e -> Toast.makeText(EventDetailActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Loads the event details from Firestore and updates the UI accordingly.
     *
     * @param eventId The ID of the event to load.
     */
    private void loadEventDetails(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Fetch event details from Firestore
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve event data
                    String eventName = document.getString("eventName");
                    String eventDescription = document.getString("description");
                    String eventPosterUrl = document.getString("posterUrl");
                    Date startDate = document.getDate("startDate");
                    Date endDate = document.getDate("endDate");
                    String facility = document.getString("facility");
                    Long capacity = document.getLong("capacity");
                    String qrCodeUrl = document.getString("qrCodeUrl");
                    String organizerId = document.getString("organizerId"); // Retrieve organizerId

                    // Set data to TextViews
                    eventNameTextView.setText(eventName);
                    eventDescriptionTextView.setText(eventDescription);

                    // Format and set dates
                    SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    if (startDate != null) {
                        eventStartDateTextView.setText("Start Date: " + displayFormat.format(startDate));
                    } else {
                        eventStartDateTextView.setText("Start Date: N/A");
                    }

                    if (endDate != null) {
                        eventEndDateTextView.setText("End Date: " + displayFormat.format(endDate));
                    } else {
                        eventEndDateTextView.setText("End Date: N/A");
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

                    // Get current device ID
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    Log.d("DeviceCheck", "Device ID: " + deviceId);  // Output the device ID

// Compare deviceId with organizerId
                    if (organizerId != null) {
                        Log.d("DeviceCheck", "Organizer ID: " + organizerId);  // Output the organizer ID
                        if (organizerId.equals(deviceId)) {
                            // User is the organizer
                            updatePosterButton.setVisibility(View.VISIBLE);
                            joinEventButton.setVisibility(View.GONE);
                            editInfoButton.setVisibility(View.VISIBLE);
                        } else {
                            // User is not the organizer
                            updatePosterButton.setVisibility(View.GONE);
                            joinEventButton.setVisibility(View.VISIBLE);
                            editInfoButton.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d("DeviceCheck", "Organizer ID is null");  // Handle null case for organizerId
                    }

                } else {
                    Toast.makeText(EventDetailActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventDetailActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the logic for a user to join the event.
     */
    private void joinEvent() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        DocumentReference eventRef = db.collection("events").document(eventId);

        // Check if the user has already joined
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(deviceId)) {
                    Toast.makeText(this, "You have already joined this event", Toast.LENGTH_SHORT).show();
                } else {
                    // Add deviceId to participants array
                    eventRef.update("participants", FieldValue.arrayUnion(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Successfully joined the event", Toast.LENGTH_SHORT).show();
                                // Update button state
                                joinEventButton.setEnabled(false);
                                joinEventButton.setText("Joined");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to join the event", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
                }
            } else {
                Toast.makeText(this, "Event does not exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }
}