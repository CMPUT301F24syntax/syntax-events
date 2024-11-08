package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
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

public class OrganizerCreateEvent extends AppCompatActivity {

    // Declare variables for the UI components
    private EditText eventNameEditText, eventStartDateEditText, eventEndDateEditText, facilityEditText, capacityEditText, eventDescriptionEditText;
    private Button createEventButton, backButton, uploadButton;
    private ImageView eventImageView;
    private EventRepository eventRepository;
    private EventController eventController;
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

        // Initialize UI components
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        uploadButton = findViewById(R.id.uploadButton);
        eventImageView = findViewById(R.id.eventImageView);

        // initialize controller and repository
        EventRepository eventRepository = new EventRepository();
        eventController = new EventController(eventRepository);

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Image upload button click listener
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Create event button click listener
        createEventButton.setOnClickListener(v -> {
            saveEvent();
            clearInputFields();
        });
    }


    // Save event using event controller
    private void saveEvent() {
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        String eventStartDateText = eventStartDateEditText.getText().toString();
        String eventEndDateText = eventEndDateEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        String organizerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // format date texts
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(eventStartDateText);
            endDate = dateFormat.parse(eventEndDateText);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // get capacity to an integer
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        // input validation
        if (eventName.isEmpty() || eventDescription.isEmpty() || eventStartDateText.isEmpty() || eventEndDateText.isEmpty() ||  capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        // initialize event object
        Event event = new Event(eventName, eventDescription, capacity, startDate, endDate, organizerId);

        eventController.addEvent(event, imageUri, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(OrganizerCreateEvent.this, "Event creation successfull", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(OrganizerCreateEvent.this, "Event creation Error!", Toast.LENGTH_SHORT).show();
                Log.e("OrganizerCreateEvent", e.toString());
                }
            });
    }


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
