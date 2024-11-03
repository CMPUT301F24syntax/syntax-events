package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrganizerCreateEvent extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText facilityEditText;
    private EditText capacityEditText;
    private Button createEventButton;
    private Button backButton;
    private Button uploadButton;
    private ImageView eventImageView;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;
    private Bitmap qrCodeBitmap;

    // ActivityResultLauncher for selecting an image
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    eventImageView.setImageURI(imageUri);  // Display selected image in ImageView
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Initialize UI components
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        facilityEditText = findViewById(R.id.facilityEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        uploadButton = findViewById(R.id.uploadButton);
        eventImageView = findViewById(R.id.eventImageView);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Set click listener for the back button
        backButton.setOnClickListener(v -> finish());

        // Set click listener for the upload button
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Set click listener for the create event button
        createEventButton.setOnClickListener(v -> saveEventToDatabase());
    }

    // QR code generate
    private Bitmap generateQRCodeBitmap(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void saveEventToDatabase() {
        // Generate a unique eventID
        String eventID = UUID.randomUUID().toString();

        // Retrieve input data from the EditText fields
        String eventName = eventNameEditText.getText().toString();
        String eventStartDate = eventStartDateEditText.getText().toString();
        String eventEndDate = eventEndDateEditText.getText().toString();
        String facility = facilityEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();

        // Check if all fields are filled
        if (eventName.isEmpty() || eventStartDate.isEmpty() || eventEndDate.isEmpty() || facility.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(OrganizerCreateEvent.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse capacity to integer
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(OrganizerCreateEvent.this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse start and end dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(eventStartDate);
            endDate = dateFormat.parse(eventEndDate);
        } catch (ParseException e) {
            Toast.makeText(OrganizerCreateEvent.this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate QR code based on event ID
        qrCodeBitmap = generateQRCodeBitmap(eventID);

        // Create a Map to store event data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventID", eventID);
        eventData.put("eventName", eventName);
        eventData.put("startDate", startDate);
        eventData.put("endDate", endDate);
        eventData.put("facility", facility);
        eventData.put("capacity", capacity);

        // If an image is selected, upload it and save the URL
        if (imageUri != null) {
            uploadImageAndSaveEventData(eventID, eventData);
        } else {
            uploadQRCodeAndSaveEventData(eventID, eventData);
        }
    }

    private void uploadQRCodeAndSaveEventData(String eventID, Map<String, Object> eventData) {
        // upload the generated QR code to Firebase Storage
        StorageReference qrCodeRef = storage.getReference().child("qrcodes/" + eventID + ".png");
        qrCodeRef.putBytes(bitmapToByteArray(qrCodeBitmap))
                .addOnSuccessListener(taskSnapshot -> qrCodeRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            eventData.put("qrCodeUrl", uri.toString());
                            saveEventData(eventID, eventData, null);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "QR Code upload failed", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "QR Code upload failed", Toast.LENGTH_SHORT).show());
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void uploadImageAndSaveEventData(String eventID, Map<String, Object> eventData) {
        StorageReference storageRef = storage.getReference().child("event_images/" + eventID);
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            eventData.put("imageUrl", uri.toString());
                            uploadQRCodeAndSaveEventData(eventID, eventData);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void saveEventData(String eventID, Map<String, Object> eventData, String imageUrl) {
        if (imageUrl != null) {
            eventData.put("imageUrl", imageUrl);
        }
        db.collection("events").document(eventID)
                .set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrganizerCreateEvent.this, "Event Created and Saved to Database", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> Toast.makeText(OrganizerCreateEvent.this, "Failed to save event", Toast.LENGTH_SHORT).show());
    }

    private void clearInputFields() {
        eventNameEditText.setText("");
        eventStartDateEditText.setText("");
        eventEndDateEditText.setText("");
        facilityEditText.setText("");
        capacityEditText.setText("");
        eventImageView.setImageURI(null);
        uploadButton.setVisibility(View.VISIBLE); // Reset button visibility for next event
        imageUri = null;
    }
}
