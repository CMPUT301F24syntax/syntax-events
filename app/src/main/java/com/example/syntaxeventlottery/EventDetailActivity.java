package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Date;

public class EventDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, editInfoButton, backButton;
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
        editInfoButton = findViewById(R.id.editInfoButton);
        backButton = findViewById(R.id.backButton);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Retrieve the event ID passed from the previous screen
        eventId = getIntent().getStringExtra("event_id");

        // Load event details from the database using the event ID
        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
        }

        // Set click listener for the back button to return to the previous screen
        backButton.setOnClickListener(v -> finish());

        // Set click listener for update poster button to select and upload a new poster image
        updatePosterButton.setOnClickListener(v -> openImagePicker());

        // Set click listener for edit information button to open EditEventActivity
        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event_id", eventId); // Pass event ID to the edit screen
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

    private void loadEventDetails(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Fetch the event details from Firestore
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve event data and display in the views
                    String eventName = document.getString("eventName");
                    String eventDescription = document.getString("description"); // Load description
                    String eventPosterUrl = document.getString("posterUrl");
                    Date startDate = document.getDate("startDate");
                    Date endDate = document.getDate("endDate");
                    String facility = document.getString("facility");
                    Long capacity = document.getLong("capacity");
                    String qrCodeUrl = document.getString("qrCodeUrl");

                    // Set data to TextViews
                    eventNameTextView.setText(eventName);
                    eventDescriptionTextView.setText(eventDescription); // Set description
                    eventStartDateTextView.setText("Start Date: " + startDate);
                    eventEndDateTextView.setText("End Date: " + endDate);
                    eventFacilityTextView.setText("Location: " + facility);
                    eventCapacityTextView.setText("Capacity: " + (capacity != null ? capacity.toString() : "N/A"));

                    // Load event poster using Glide
                    if (eventPosterUrl != null) {
                        Glide.with(EventDetailActivity.this)
                                .load(eventPosterUrl)
                                .into(eventPosterImageView);
                    }

                    // Load QR code using Glide
                    if (qrCodeUrl != null) {
                        Glide.with(EventDetailActivity.this)
                                .load(qrCodeUrl)
                                .into(eventQRCodeImageView);
                    }
                } else {
                    Toast.makeText(EventDetailActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventDetailActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
