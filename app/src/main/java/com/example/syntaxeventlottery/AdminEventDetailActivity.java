package com.example.syntaxeventlottery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.syntaxeventlottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminEventDetailActivity extends AppCompatActivity {

    private ImageView eventPoster;
    private TextView eventName;
    private TextView eventDescription;
    private TextView eventFacility;
    private TextView eventCapacity;
    private TextView eventStartDate;
    private TextView eventEndDate;
    private Button backButton;
    private Button deleteImageButton;

    private FirebaseFirestore db;
    private String eventId; // Event ID passed through Intent

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get event ID from Intent
        eventId = getIntent().getStringExtra("eventId");

        // Initialize views
        eventPoster = findViewById(R.id.eventPoster);
        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        eventFacility = findViewById(R.id.eventFacility);
        eventCapacity = findViewById(R.id.eventCapacity);
        eventStartDate = findViewById(R.id.eventStartDate);
        eventEndDate = findViewById(R.id.eventEndDate);
        backButton = findViewById(R.id.backButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);

        // Set back button listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity and go back to the previous screen
            }
        });

        // Set delete image button listener
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteImage();
            }
        });

        // Load event details from Firestore
        loadEventDetails();
    }

    private void loadEventDetails() {
        if (eventId != null) {
            DocumentReference docRef = db.collection("events").document(eventId);
            docRef.get(Source.SERVER)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            eventName.setText(documentSnapshot.getString("eventName"));
                            eventDescription.setText(documentSnapshot.getString("description"));
                            eventFacility.setText("Facility: " + documentSnapshot.getString("facility"));
                            eventCapacity.setText("Capacity: " + documentSnapshot.getLong("capacity").toString());

                            // Format Date fields
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            Date startDate = documentSnapshot.getDate("startDate");
                            Date endDate = documentSnapshot.getDate("endDate");

                            if (startDate != null) {
                                eventStartDate.setText("Start Date: " + dateFormat.format(startDate));
                            } else {
                                eventStartDate.setText("Start Date: N/A");
                            }

                            if (endDate != null) {
                                eventEndDate.setText("End Date: " + dateFormat.format(endDate));
                            } else {
                                eventEndDate.setText("End Date: N/A");
                            }

                            // Load event poster image
                            String posterUrl = documentSnapshot.getString("posterUrl");
                            if (posterUrl != null) {
                                Glide.with(this).load(posterUrl).into(eventPoster);
                            }
                        } else {
                            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteImage() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImage();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteImage() {
        eventPoster.setImageDrawable(null); // Clear the image from the ImageView
        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
    }
}
