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

/**
 * The {@code AdminEventDetailActivity} class displays detailed information about an event
 * to administrators, including event name, description, capacity, and event poster.
 * Administrators can delete the poster image if necessary.
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    private Button backButton, deletePosterButton, deleteQRCodeButton;
    private TextView eventName, eventDescription, eventCapacity, eventStartDate, eventEndDate;
    private ImageView eventPosterImageView, eventqrCode;
    private FirebaseFirestore db;
    private String eventID;

    private StorageReference storageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_detail);

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

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
        String name = intent.getStringExtra("eventName");
        String description = intent.getStringExtra("description");
        String capacity = intent.getStringExtra("capacity");
        Date startDate = (Date) intent.getSerializableExtra("startDate");
        Date endDate = (Date) intent.getSerializableExtra("endDate");
        String posterUrl = intent.getStringExtra("posterUrl");
        String qrCode = intent.getStringExtra("qrCode");

        // Format dates to string if they are not null
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateString = (startDate != null) ? dateFormat.format(startDate) : "N/A";
        String endDateString = (endDate != null) ? dateFormat.format(endDate) : "N/A";

        // Set data to views
        eventName.setText(name);
        eventDescription.setText(description);
        eventCapacity.setText(capacity);
        eventStartDate.setText(startDateString);
        eventEndDate.setText(endDateString);

        // Load event poster image using Glide
        if (posterUrl != null && !posterUrl.isEmpty()) {
            Glide.with(this)
                    .load(posterUrl)
                    .placeholder(R.drawable.ic_placeholder_poster_image) // Placeholder image in case of loading failure
                    .into(eventPosterImageView);
        } else {
            eventPosterImageView.setImageResource(R.drawable.ic_placeholder_poster_image); // Set default image if no URL
        }

        // Load QRcode image using Glide
        if (qrCode != null && !qrCode.isEmpty()) {
            Glide.with(this)
                    .load(qrCode)
                    .placeholder(R.drawable.ic_placeholder_poster_image) // Placeholder image in case of loading failure
                    .into(eventqrCode);
        } else {
            eventqrCode.setImageResource(R.drawable.ic_placeholder_poster_image); // Set default image if no URL
        }

        // Set up back button functionality
        backButton.setOnClickListener(v -> finish());

        // Set up delete poster button functionality
        deletePosterButton.setOnClickListener(v -> deletePosterImage());

        // Set up delete qrcode button functionality
        deleteQRCodeButton.setOnClickListener(v -> deleteQRCodeImage());
    }

    /**
     * Replaces the QR code image URL in Firestore with a default image URL.
     */
    private void deleteQRCodeImage() {
        String defaultQRCode = "https://firebasestorage.googleapis.com/v0/b/scanapp-7e377.appspot.com/o/default_qrcode.png?alt=media&token=aecbe3a4-3e8a-4c88-beed-ad6f3a029666"; // Firebase Storage default URL
        if (eventID != null) {
            DocumentReference eventRef = db.collection("events").document(eventID);
            eventRef.update("qrCode", defaultQRCode) // update Firestore QR code
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "QR code image reset to default", Toast.LENGTH_SHORT).show();
                        // use Glide show default picture
                        Glide.with(this)
                                .load(defaultQRCode)
                                .placeholder(R.drawable.default_qrcode)
                                .into(eventqrCode);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminEventDetailActivity", "Failed to reset QR code URL in Firestore", e);
                        Toast.makeText(this, "Failed to reset QR code URL in Firestore", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Replaces the poster image URL in Firestore with a default image URL.
     */
    private void deletePosterImage() {
        String defaultPosterUrl = "https://firebasestorage.googleapis.com/v0/b/scanapp-7e377.appspot.com/o/default_poster.png?alt=media&token=f9fa0dcf-2b5f-469f-be33-d50c205fc63d"; // Firebase Storage default URL
        if (eventID != null) {
            DocumentReference eventRef = db.collection("events").document(eventID);
            eventRef.update("posterUrl", defaultPosterUrl) // update Firestore poster URL
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Poster image reset to default", Toast.LENGTH_SHORT).show();
                        // use Glide show the default picture
                        Glide.with(this)
                                .load(defaultPosterUrl)
                                .placeholder(R.drawable.ic_placeholder_poster_image)
                                .into(eventPosterImageView);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminEventDetailActivity", "Failed to reset poster URL in Firestore", e);
                        Toast.makeText(this, "Failed to reset poster URL in Firestore", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
        }
    }


}
