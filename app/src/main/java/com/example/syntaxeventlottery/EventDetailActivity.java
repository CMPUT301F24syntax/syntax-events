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
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, joinEventButton, leaveEventButton, editInfoButton, backButton;
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

        // Set click listener for drawButton
        drawButton.setOnClickListener(v -> drawParticipants());

    }

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
    }

    /**
     * Loads the event details from Firestore and updates the UI accordingly.
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
                    Log.d("EventDetailActivity", "Organizer ID: " + organizerId);

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
                        // User is the organizer
                        updatePosterButton.setVisibility(View.VISIBLE);
                        drawButton.setVisibility(View.VISIBLE);
                        joinEventButton.setVisibility(View.GONE);
                        leaveEventButton.setVisibility(View.GONE);
                        editInfoButton.setVisibility(View.VISIBLE);
                    } else {
                        // User is not the organizer
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

                    // Get current device ID
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                    // Compare deviceId with organizerId
                    Log.d("EventDetailActivity", "Organizer ID: " + organizerId); // Logging organizerId

                    if (organizerId != null && organizerId.equals(deviceId)) {
                        // User is the organizer
                        updatePosterButton.setVisibility(View.VISIBLE);
                        joinEventButton.setVisibility(View.GONE);
                        leaveEventButton.setVisibility(View.GONE);
                        editInfoButton.setVisibility(View.VISIBLE);
                    } else {
                        // User is not the organizer
                        updatePosterButton.setVisibility(View.GONE);
                        editInfoButton.setVisibility(View.GONE);

                        // Check if the user has already joined the event
                        List<String> participants = (List<String>) document.get("participants");

                        if (participants != null && participants.contains(deviceId)) {
                            // User has joined the event
                            joinEventButton.setVisibility(View.GONE);
                            leaveEventButton.setVisibility(View.VISIBLE);
                        } else {
                            // User has not joined the event
                            joinEventButton.setVisibility(View.VISIBLE);
                            leaveEventButton.setVisibility(View.GONE);
                        }

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
        DocumentReference userRef = db.collection("Users").document(deviceId); // Assuming deviceId is used as user ID

        // Check if the user has already joined
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(deviceId)) {
                    Toast.makeText(this, "You have already joined this event", Toast.LENGTH_SHORT).show();
                } else {
                    // Add deviceId to participants array in event
                    eventRef.update("participants", FieldValue.arrayUnion(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                // Add event name to waitingListEvents array in user
                                userRef.update("waitingListEvents", FieldValue.arrayUnion(eventName))
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Successfully joined the event", Toast.LENGTH_SHORT).show();
                                            // Update button states
                                            joinEventButton.setVisibility(View.GONE);
                                            leaveEventButton.setVisibility(View.VISIBLE);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                                        });
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

    /**
     * Handles the logic for a user to leave the event.
     */
    private void leaveEvent() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                Collections.shuffle(participants);
                ArrayList<String> ChosenList = new ArrayList<>();
                ArrayList<String> UnChosenList = new ArrayList<>();

        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference userRef = db.collection("Users").document(deviceId); // Assuming deviceId is used as user ID


        // Check if the user has joined
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(deviceId)) {
                    // Remove deviceId from participants array in event
                    eventRef.update("participants", FieldValue.arrayRemove(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                // Remove event name from waitingListEvents array in user
                                userRef.update("waitingListEvents", FieldValue.arrayRemove(eventName))
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Successfully left the event", Toast.LENGTH_SHORT).show();
                                            // Update button states
                                            joinEventButton.setVisibility(View.VISIBLE);
                                            leaveEventButton.setVisibility(View.GONE);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to leave the event", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
                } else {
                    Toast.makeText(this, "You are not part of this event", Toast.LENGTH_SHORT).show();
                }

                eventRef.update("ChosenList", ChosenList, "UnChosenList", UnChosenList)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Draw successful", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save draw results", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });

            } else {
                Toast.makeText(this, "Event does not exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }

    private void joinEvent() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference userRef = db.collection("Users").document(deviceId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(deviceId)) {
                    Toast.makeText(this, "You have already joined this event", Toast.LENGTH_SHORT).show();
                } else {
                    eventRef.update("participants", FieldValue.arrayUnion(deviceId))
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

    private void leaveEvent() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference userRef = db.collection("Users").document(deviceId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && participants.contains(deviceId)) {
                    eventRef.update("participants", FieldValue.arrayRemove(deviceId))
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

