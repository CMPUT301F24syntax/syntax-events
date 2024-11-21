package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserHomeActivity extends AppCompatActivity {

    private TextView dateTextView;
    private RecyclerView futureEventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private ImageButton organizerButton;
    private ImageButton profileButton;
    private ImageButton newsButton;
    private ImageButton scanButton; // New QR Scan Button
    private String deviceId;
    private ImageButton scanButton2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize views
        dateTextView = findViewById(R.id.dateTextView);
        organizerButton = findViewById(R.id.organizerButton);
        profileButton = findViewById(R.id.profileButton);
        newsButton = findViewById(R.id.newsButton);
        scanButton = findViewById(R.id.qrScanButton1);
        scanButton2 = findViewById(R.id.qrScanButton2);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get deviceId
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView for Future Events
        futureEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        futureEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        futureEventsRecyclerView.setAdapter(eventAdapter);

        // Load events from Firestore
        loadEventsFromFirestore();

        // Set click listener for Organizer button
        organizerButton.setOnClickListener(v -> checkUserAndNavigate());

        // Set click listener for Profile button
        profileButton.setOnClickListener(v -> {
            // Navigate to UserProfileActivity
            startActivity(new Intent(UserHomeActivity.this, UserProfileActivity.class));
        });

        // Set click listener for News button
        newsButton.setOnClickListener(v -> {
            // Navigate to NotificationCenterActivity
            startActivity(new Intent(UserHomeActivity.this, NotificationCenterActivity.class));
        });

        // Set click listener for Scan button
        scanButton.setOnClickListener(v -> {
            // Navigate to QRScanActivity
            startActivity(new Intent(UserHomeActivity.this, QRScanActivity.class));
        });
        scanButton2.setOnClickListener(v -> {
            // Navigate to QRScanActivity
            startActivity(new Intent(UserHomeActivity.this, QRScanActivity.class));
        });

        // Set up date and time updater
        updateDateTime();
    }

    private void checkUserAndNavigate() {
        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String facility = document.getString("facility");

                        if (facility == null || facility.isEmpty()) {
                            startActivity(new Intent(UserHomeActivity.this, FacilityProfileActivity.class));
                        } else {
                            startActivity(new Intent(UserHomeActivity.this, OrganizerActivity.class));
                        }
                    } else {
                        Log.d("Firestore", "No user found with device ID " + deviceId);
                        Toast.makeText(UserHomeActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking user", e);
                    Toast.makeText(UserHomeActivity.this, "Failed to check user information.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        eventList.clear();
                        Log.d("Firestore", "Fetched " + task.getResult().size() + " events from Firestore.");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventName = document.getString("eventName");
                            String eventID = document.getId();
                            String description = document.getString("description");
                            String facility = document.getString("facility");
                            String qrCode = document.getString("qrCode");
                            String posterUrl = document.getString("posterUrl");
                            String organizerId = document.getString("organizerId");
                            boolean isLocationRequired = document.getBoolean("locationRequired");
                            Date startDate = document.getDate("startDate");
                            Date endDate = document.getDate("endDate");

                            if (startDate != null && endDate != null) {
                                int capacity = document.getLong("capacity").intValue();

                                Event event = new Event(eventID, eventName, description, capacity, startDate, endDate, organizerId, isLocationRequired);
                                event.setEventID(eventID);
                                event.setQrCode(qrCode);
                                event.setPosterUrl(posterUrl);
                                eventList.add(event);
                                Log.d("Firestore", "Added event: " + eventName);
                            } else {
                                Log.e("Firestore", "startDate or endDate is null for event: " + eventName);
                            }
                        }
                        eventAdapter.notifyDataSetChanged();
                        Log.d("RecyclerView", "Adapter updated with item count: " + eventAdapter.getItemCount());
                    } else {
                        Log.e("Firestore", "Error loading events: ", task.getException());
                    }
                });
    }

    private void updateDateTime() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Edmonton"));

        new Thread(() -> {
            while (!isFinishing()) {
                runOnUiThread(() -> dateTextView.setText(dateFormat.format(new Date())));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
