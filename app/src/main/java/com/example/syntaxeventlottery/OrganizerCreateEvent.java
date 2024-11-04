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

    private EditText eventNameEditText, eventStartDateEditText, eventEndDateEditText, facilityEditText, capacityEditText, eventDescriptionEditText;
    private Button createEventButton, backButton, uploadButton;
    private ImageView eventImageView;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;
    private Bitmap qrCodeBitmap;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    eventImageView.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Initialize UI and Firebase
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        facilityEditText = findViewById(R.id.facilityEditText);
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

    // Generate QR Code
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

    // Save event data to Firestore
    private void saveEventToDatabase() {
        String eventID = UUID.randomUUID().toString();
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString(); // 获取描述
        String eventStartDate = eventStartDateEditText.getText().toString();
        String eventEndDate = eventEndDateEditText.getText().toString();
        String facility = facilityEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventStartDate.isEmpty() || eventEndDate.isEmpty() || facility.isEmpty() || capacityStr.isEmpty()) {
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
            startDate = dateFormat.parse(eventStartDate);
            endDate = dateFormat.parse(eventEndDate);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        qrCodeBitmap = generateQRCodeBitmap(eventID);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventID", eventID);
        eventData.put("eventName", eventName);
        eventData.put("description", eventDescription); // 添加描述
        eventData.put("startDate", startDate);
        eventData.put("endDate", endDate);
        eventData.put("facility", facility);
        eventData.put("capacity", capacity);

        if (imageUri != null) {
            uploadImageAndSaveEventData(eventID, eventData);
        } else {
            uploadQRCodeAndSaveEventData(eventID, eventData);
        }
    }

    // Upload QR code to Firebase Storage
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

    // Convert Bitmap to ByteArray
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // Upload event poster image and save event data
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

    // Save event data to Firestore
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

    private void clearInputFields() {
        eventNameEditText.setText("");
        eventDescriptionEditText.setText("");
        eventStartDateEditText.setText("");
        eventEndDateEditText.setText("");
        facilityEditText.setText("");
        capacityEditText.setText("");
        eventImageView.setImageURI(null);
        uploadButton.setVisibility(View.VISIBLE);
        imageUri = null;
    }
}
