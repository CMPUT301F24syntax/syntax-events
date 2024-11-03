package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class EventDetailActivity extends AppCompatActivity {

    private ImageView eventPosterImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView,
            eventEndDateTextView, eventFacilityTextView, eventCapacityTextView;
    private Button updatePosterButton, editInfoButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize UI components
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventFacilityTextView = findViewById(R.id.eventFacilityTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        updatePosterButton = findViewById(R.id.updatePosterButton);
        editInfoButton = findViewById(R.id.editInfoButton);

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

        // Set click listener for updating the poster
        updatePosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement logic to open gallery or camera for poster selection
                // This could involve starting an intent to pick an image and then uploading to Firebase Storage
            }
        });

        // Set click listener for editing information
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement logic to open an edit screen or dialog for event details
                // You could open a new activity or a dialog where the user can update event details
            }
        });
    }

    private void loadEventDetails(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String eventName = document.getString("eventName");
                    String posterUrl = document.getString("poster");

                    eventNameTextView.setText(eventName != null ? eventName : "No event name");

                    // Check if poster URL is null
                    if (posterUrl != null) {
                        Glide.with(this)
                                .load(posterUrl)
                                .into(eventPosterImageView);
                    } else {
                        // Load a placeholder image if poster URL is null
                        eventPosterImageView.setImageResource(R.drawable.placeholder); // Ensure placeholder exists in drawable
                    }
                } else {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
