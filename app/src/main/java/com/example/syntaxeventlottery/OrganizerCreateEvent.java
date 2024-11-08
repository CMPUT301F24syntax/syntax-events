package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
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

/**
 * The {@code OrganizerCreateEvent} class allows event organizers to create new events.
 * It provides functionality to input event details, upload an event image, generate a QR code,
 * and save the event data to Firebase Firestore and Firebase Storage.
 */
public class OrganizerCreateEvent extends AppCompatActivity {

    /** EditText for entering the event name. */
    private EditText eventNameEditText;

    /** EditText for entering the event start date. */
    private EditText eventStartDateEditText;

    /** EditText for entering the event end date. */
    private EditText eventEndDateEditText;

    /** EditText for entering the event facility. */
    private EditText facilityEditText;

    /** EditText for entering the event capacity. */
    private EditText capacityEditText;

    /** EditText for entering the event description. */
    private EditText eventDescriptionEditText;

    /** Button to create the event. */
    private Button createEventButton;

    /** Button to go back to the previous screen. */
    private Button backButton;

    /** Button to upload an image for the event. */
    private Button uploadButton;

    /** ImageView to display the event image. */
    private ImageView eventImageView;

    /** Firebase Firestore instance for database operations. */
    private FirebaseFirestore db;

    /** Firebase Storage instance for storing images and files. */
    private FirebaseStorage storage;

    /** URI of the selected image from the image picker. */
    private Uri imageUri;

    /** Bitmap of the generated QR code. */
    private Bitmap qrCodeBitmap;

    /** ActivityResultLauncher for the image picker intent. */
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    eventImageView.setImageURI(imageUri);
                }
            }
    );

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState}. <b>Note: Otherwise, it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Initialize UI and Firebase
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        uploadButton = findViewById(R.id.uploadButton);
        eventImageView = findViewById(R.id.eventImageView);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Image upload button click listener
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Create event button click listener
        createEventButton.setOnClickListener(v -> saveEventToDatabase());
    }

    /**
     * Generates a QR code bitmap from the given content string.
     *
     * @param content The content to encode in the QR code.
     * @return A Bitmap representing the QR code, or null if an error occurs.
     */
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

    /**
     * Collects event data from input fields, validates them, generates a QR code,
     * and initiates the process to save the event data to the database.
     */
    private void saveEventToDatabase() {
        String eventID = UUID.randomUUID().toString();
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        String eventStartDateText = eventStartDateEditText.getText().toString();
        String eventEndDateText = eventEndDateEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        String organizerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventStartDateText.isEmpty() || eventEndDateText.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(eventStartDateText);
            endDate = dateFormat.parse(eventEndDateText);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        qrCodeBitmap = generateQRCodeBitmap(eventID);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventID", eventID);
        eventData.put("eventName", eventName);
        eventData.put("description", eventDescription);
        eventData.put("startDate", new com.google.firebase.Timestamp(startDate));  // Use Firebase Timestamp
        eventData.put("endDate", new com.google.firebase.Timestamp(endDate));      // Use Firebase Timestamp
        eventData.put("capacity", capacity);
        eventData.put("organizerId", organizerId);

        if (imageUri != null) {
            uploadImageAndSaveEventData(eventID, eventData);
        } else {
            uploadQRCodeAndSaveEventData(eventID, eventData);
        }
    }

    /**
     * Uploads the generated QR code to Firebase Storage and saves event data to Firestore.
     *
     * @param eventID   The unique identifier for the event.
     * @param eventData A map containing event data to be saved.
     */
    private void uploadQRCodeAndSaveEventData(String eventID, Map<String, Object> eventData) {
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

    /**
     * Converts a Bitmap object to a byte array.
     *
     * @param bitmap The Bitmap to convert.
     * @return A byte array representing the Bitmap.
     */
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Uploads the event image to Firebase Storage, then uploads the QR code and saves event data.
     *
     * @param eventID   The unique identifier for the event.
     * @param eventData A map containing event data to be saved.
     */
    private void uploadImageAndSaveEventData(String eventID, Map<String, Object> eventData) {
        StorageReference storageRef = storage.getReference().child("event_images/" + eventID);
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            eventData.put("posterUrl", uri.toString());
                            uploadQRCodeAndSaveEventData(eventID, eventData);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves the event data to Firebase Firestore.
     *
     * @param eventID   The unique identifier for the event.
     * @param eventData A map containing event data to be saved.
     * @param imageUrl  The URL of the uploaded image, if available.
     */
    private void saveEventData(String eventID, Map<String, Object> eventData, String imageUrl) {
        if (imageUrl != null) {
            eventData.put("posterUrl", imageUrl);
        }
        db.collection("events").document(eventID)
                .set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Created and Saved to Database", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Clears all input fields and resets the UI components.
     */
    private void clearInputFields() {
        eventNameEditText.setText("");
        eventDescriptionEditText.setText("");
        eventStartDateEditText.setText("");
        eventEndDateEditText.setText("");
        capacityEditText.setText("");
        eventImageView.setImageURI(null);
        uploadButton.setVisibility(View.VISIBLE);
        imageUri = null;
    }
}
