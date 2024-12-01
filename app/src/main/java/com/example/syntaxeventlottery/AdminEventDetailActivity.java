package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * The {@code AdminEventDetailActivity} class displays detailed information about an event
 * to administrators, including event name, description, capacity, and event poster.
 * Administrators can delete the poster image if necessary.
 */
public class AdminEventDetailActivity extends AppCompatActivity {
    private final String TAG = "AdminEventDetailActivity";

    private Button backButton, deletePosterButton, deleteQRCodeButton;
    private TextView eventName, eventDescription, eventCapacity, eventStartDate, eventEndDate;
    private ImageView eventPosterImageView, eventqrCode;
    private EventController eventController;
    private String eventID;
    private Event event;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_detail);

        // initalize event controller
        eventController = new EventController(new EventRepository());

        // Initialize views
        backButton = findViewById(R.id.backButton);
        deletePosterButton = findViewById(R.id.deleteImageButton); // Button to delete the poster image
        deleteQRCodeButton = findViewById(R.id.deleteQRCodeButton);
        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        eventCapacity = findViewById(R.id.eventCapacity);
        eventStartDate = findViewById(R.id.eventStartDate);
        eventEndDate = findViewById(R.id.eventEndDate);
        eventPosterImageView = findViewById(R.id.eventPoster);
        eventqrCode = findViewById(R.id.qrCode);

        // Get intent data
        Intent intent = getIntent();
        eventID = intent.getStringExtra("eventID");

        loadEventDetails(); // load the event and its details

        // Set up back button functionality
        backButton.setOnClickListener(v -> finish());

        // Set up delete poster button functionality
        deletePosterButton.setOnClickListener(v -> deletePosterImage());

        // Set up delete qrcode button functionality
        deleteQRCodeButton.setOnClickListener(v -> deleteQRCodeImage());
    }

    private void loadEventDetails() {
        // get newest event details and display them
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                event = eventController.getEventById(eventID);
                if (event == null) {
                    Log.e(TAG, "Couldn't find Event");
                    Toast.makeText(AdminEventDetailActivity.this, "Failed to retrieve event details", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                // Set data to views
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                eventName.setText(event.getEventName());
                eventDescription.setText(event.getDescription());
                eventCapacity.setText(String.valueOf(event.getCapacity()));
                eventStartDate.setText(dateFormat.format(event.getStartDate()));
                eventEndDate.setText(dateFormat.format(event.getEndDate()));
                // Load images
                // Load event poster image using Glide
                if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                    Glide.with(AdminEventDetailActivity.this)
                            .load(event.getPosterUrl())
                            .into(eventPosterImageView);
                } else {
                    eventPosterImageView.setImageResource(R.drawable.ic_default_poster); // Set default image if no URL
                }

                // Load QRcode image using Glide
                if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
                    Glide.with(AdminEventDetailActivity.this)
                            .load(event.getQrCode())
                            .into(eventqrCode);
                } else {
                    eventqrCode.setImageResource(R.drawable.default_qrcode); // Set default image if no URL
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to update event repository", e);
                Toast.makeText(AdminEventDetailActivity.this, "Failed to update event repository", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Replaces the QR code image URL in Firestore with a default image URL.
     */
    private void deleteQRCodeImage() {
        event.setQrCode(null); // set qr code to null
        eventController.updateEvent(event, null, null, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                event = result;
                Log.d(TAG, "event qr code deleted ");
                loadEventDetails();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminEventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating event ", e);
                return;
            }
        });
    }

    /**
     * Replaces the poster image URL in Firestore with a default image URL.
     */
    private void deletePosterImage() {
        event.setPosterUrl(null); // set poster url to null
        eventController.updateEvent(event, null, null, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                event = result;
                Log.d(TAG, "event poster deleted ");
                loadEventDetails();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminEventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating event ", e);
            }
        });
    }
}