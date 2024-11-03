package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class EventDetailActivity extends AppCompatActivity {

    private ImageView eventPosterImageView, eventQRCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, editInfoButton, backButton;
    private FirebaseFirestore db;

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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the event ID passed from the previous screen
        String eventId = getIntent().getStringExtra("event_id");

        // Load event details from the database using the event ID
        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
        }

        // Set click listener for the back button to return to the previous screen
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this activity and returns to the previous one
            }
        });
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
                    String eventDescription = document.getString("eventDescription");
                    String eventPosterUrl = document.getString("posterUrl");
                    Date startDate = document.getDate("startDate");
                    Date endDate = document.getDate("endDate");
                    String facility = document.getString("facility");
                    Long capacity = document.getLong("capacity");
                    String qrCodeUrl = document.getString("qrCodeUrl");

                    // Set data to TextViews
                    eventNameTextView.setText(eventName);
                    eventDescriptionTextView.setText(eventDescription);
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
