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

// Import Firebase and other necessary libraries
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;


/**
 * Activity to display event details and manage event actions.
 */
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
            finish();
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

        // Set click listener for drawButton

        drawButton.setOnClickListener(v -> drawParticipants());

        // Set click listener for waitingListButton
        waitingListButton.setOnClickListener(v -> {
            // Start EventWaitingListActivity
            Intent intent = new Intent(EventDetailActivity.this, EventWaitingListActivity.class);
            intent.putExtra("event_id", eventId); // Pass event ID to the waiting list activity
            startActivity(intent);
        });
    }


    /**
     * Opens the image picker to select an image for the event poster.
     */

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Handle the result from the image picker

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadPosterImage(imageUri);
        }
    }

    /**
     * Uploads the selected poster image to Firebase Storage and updates the event's poster URL.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadPosterImage(Uri imageUri) {

        // Implement the upload logic here
        String fileName = "images/" + eventId + "/poster.jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);
        DocumentReference eventRef = db.collection("events").document(eventId);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    eventRef.update("posterUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Poster updated successfully", Toast.LENGTH_SHORT).show();
                                Glide.with(this).load(uri.toString()).into(eventPosterImageView);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update poster URL", Toast.LENGTH_SHORT).show();
                                Log.e("UploadPoster", "Failed to update poster URL", e);
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload poster image", Toast.LENGTH_SHORT).show();
                    Log.e("UploadPoster", "Failed to upload poster image", e);
                });
    }

    /**
     * Loads the event details from Firestore and updates the UI.
     *
     * @param eventId The ID of the event to load.
     */
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
                        Glide.with(this).load(eventPosterUrl).into(eventPosterImageView);
                    }
                    if (qrCodeUrl != null) {
                        Glide.with(this).load(qrCodeUrl).into(eventQRCodeImageView);
                    }

                    // Check if current user is the organizer
                    String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    if (organizerId != null && organizerId.equals(currentDeviceId)) {
                        // User is the organizer
>
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

                        if (participants != null && participants.contains(currentDeviceId)) {

                            joinEventButton.setVisibility(View.GONE);
                            leaveEventButton.setVisibility(View.VISIBLE);
                        } else {
                            joinEventButton.setVisibility(View.VISIBLE);
                            leaveEventButton.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Draws participants and creates notifications for both winners and non-winners.
     */
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

                // Prevent multiple draws
                List<String> chosenList = (List<String>) documentSnapshot.get("ChosenList");
                if (chosenList != null && !chosenList.isEmpty()) {
                    Toast.makeText(this, "Draw has already been performed", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Shuffle participants and select winners
                Collections.shuffle(participants);
                ArrayList<String> ChosenList = new ArrayList<>();
                ArrayList<String> UnChosenList = new ArrayList<>();

                if (participants.size() <= capacity) {
                    ChosenList.addAll(participants);
                } else {
                    ChosenList.addAll(participants.subList(0, capacity.intValue()));
                    UnChosenList.addAll(participants.subList(capacity.intValue(), participants.size()));
                }

                // Update Firestore with draw results
                eventRef.update("ChosenList", ChosenList, "UnChosenList", UnChosenList)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Draw successful", Toast.LENGTH_SHORT).show();

                            // Create notifications for winners
                            for (String deviceId : ChosenList) {
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("deviceId", deviceId);
                                notification.put("eventId", eventId);
                                notification.put("message", "Congratulations! You are chosen in the event \"" + eventName + "\"!");
                                notification.put("timestamp", FieldValue.serverTimestamp());
                                notification.put("isRead", false);

                                db.collection("notifications").add(notification)
                                        .addOnSuccessListener(docRef -> Log.d("Notification", "Notification sent to deviceId: " + deviceId))
                                        .addOnFailureListener(e -> Log.e("Notification", "Failed to send notification to deviceId: " + deviceId, e));
                            }

                            // Create notifications for non-winners
                            for (String deviceId : UnChosenList) {
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("deviceId", deviceId);
                                notification.put("eventId", eventId);
                                notification.put("message", "We are sorry! You were not selected in the event \"" + eventName + "\".");
                                notification.put("timestamp", FieldValue.serverTimestamp());
                                notification.put("isRead", false);

                                db.collection("notifications").add(notification)
                                        .addOnSuccessListener(docRef -> Log.d("Notification", "Notification sent to deviceId: " + deviceId))
                                        .addOnFailureListener(e -> Log.e("Notification", "Failed to send notification to deviceId: " + deviceId, e));
                            }

                            // Optional: Record draw results
                            Map<String, Object> drawResult = new HashMap<>();
                            drawResult.put("chosenList", ChosenList);
                            drawResult.put("unChosenList", UnChosenList);
                            drawResult.put("timestamp", FieldValue.serverTimestamp());

                            db.collection("events").document(eventId).collection("drawResults").add(drawResult)
                                    .addOnSuccessListener(docRef -> Log.d("DrawResult", "Draw result recorded"))
                                    .addOnFailureListener(e -> Log.e("DrawResult", "Failed to record draw result", e));

                        })
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

    /**
     * Allows the user to join the event.
     */
    private void joinEvent() {

        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference userRef = db.collection("Users").document(currentDeviceId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(currentDeviceId)) {
                    Toast.makeText(this, "You have already joined this event", Toast.LENGTH_SHORT).show();
                } else {
                    eventRef.update("participants", FieldValue.arrayUnion(currentDeviceId))
                            .addOnSuccessListener(aVoid -> userRef.update("waitingListEvents", FieldValue.arrayUnion(eventName))
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Successfully joined the event", Toast.LENGTH_SHORT).show();
                                        joinEventButton.setVisibility(View.GONE);
                                        leaveEventButton.setVisibility(View.VISIBLE);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show()));
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show());

    }

    /**
     * Allows the user to leave the event.
     */
    private void leaveEvent() {

        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference userRef = db.collection("Users").document(currentDeviceId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(currentDeviceId)) {
                    eventRef.update("participants", FieldValue.arrayRemove(currentDeviceId))
                            .addOnSuccessListener(aVoid -> userRef.update("waitingListEvents", FieldValue.arrayRemove(eventName))
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Successfully left the event", Toast.LENGTH_SHORT).show();
                                        joinEventButton.setVisibility(View.VISIBLE);
                                        leaveEventButton.setVisibility(View.GONE);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show()));
                } else {
                    Toast.makeText(this, "You are not part of this event", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show());
    }
}