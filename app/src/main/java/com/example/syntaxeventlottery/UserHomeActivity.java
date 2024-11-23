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
    private final String TAG = "UserHomeActivity";

    private TextView dateTextView;
    private RecyclerView futureEventsRecyclerView;
    private EventAdapter eventAdapter;
    private EventController eventController;
    private ImageButton organizerButton;
    private ImageButton profileButton;
    private ImageButton newsButton;
    private ImageButton scanButton; // New QR Scan Button
    private String deviceId;
    private ImageButton scanButton2;

    // refactor this!!!!!
    private FirebaseFirestore db;


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

        // refactor this!!!!
        db = FirebaseFirestore.getInstance();

        // Initialize event controller
        eventController = new EventController(new EventRepository());

        // Get deviceId
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView for Future Events
        futureEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        futureEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), this);
        futureEventsRecyclerView.setAdapter(eventAdapter);

        // load all events
        loadEvents();

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

    // reload events when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
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

    private void loadEvents() {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Events Refreshed");
                ArrayList<Event> eventsDataList = eventController.getLocalEventsList();
                eventAdapter.updateEvents(eventsDataList);

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error Refreshing Events");
                Toast.makeText(UserHomeActivity.this, "Error refreshing events, try to relaunch app", Toast.LENGTH_SHORT).show();
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
