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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class UserHomeActivity extends AppCompatActivity {

    // UI Components
    private TextView dateTextView;
    private RecyclerView waitlistEventsRecyclerView;
    private ImageButton organizerButton, newsButton, profileButton, scanButton;

    private EventController eventController;
    private UserController userController;
    private EventAdapter eventAdapter;
    private String deviceID;
    private User currentUser;
    private final String TAG = "UserHomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize UI Components
        dateTextView = findViewById(R.id.dateTextView);
        organizerButton = findViewById(R.id.organizerButton);
        profileButton = findViewById(R.id.profileButton);
        newsButton = findViewById(R.id.newsButton);
        scanButton = findViewById(R.id.qrScanButton2);

        // Initialize controllers
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository());

        // Get deviceId
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView for Future Events
        waitlistEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        waitlistEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), this);
        waitlistEventsRecyclerView.setAdapter(eventAdapter);

        // load all events
        loadUserWaitlistedEvents();

        // Set button listeners
        organizerButton.setOnClickListener(v -> checkFacilityProfileAndLaunch());

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, UserProfileActivity.class);
            intent.putExtra("deviceID", deviceID);
            startActivity(intent);
        });

        newsButton.setOnClickListener(v -> {
            // Navigate to NotificationCenterActivity
            startActivity(new Intent(UserHomeActivity.this, NotificationCenterActivity.class));
        });

        scanButton.setOnClickListener(v -> {
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
        loadAllEvents();
        /*
        changed for testing
        loadUserWaitlistedEvents();

         */
    }

    private void loadAllEvents() {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                eventAdapter.updateEvents(eventController.getLocalEventsList());
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void loadUserWaitlistedEvents() {
        eventController.getUserWaitlistedEvents(deviceID, new DataCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> waitlistedEvents) {
                Log.d(TAG, "Waitlisted events loaded successfully");
                eventAdapter.updateEvents(waitlistedEvents);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to Load Waitlisted events", e);
                Toast.makeText(UserHomeActivity.this, "Failed to Load Waitlisted events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDateTime() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
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

    // get the most updated user info and check if they have a facility profile
    // launch create facility activity if they do not have a facility
    private void checkFacilityProfileAndLaunch() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceID);
                if (currentUser == null) {
                    Log.e(TAG, "No user found with this device ID");
                    Toast.makeText(UserHomeActivity.this, "Couldn't load user information, try again", Toast.LENGTH_SHORT).show();
                    finish();
                }
                Facility facility = currentUser.getFacility();
                if (facility == null) {
                    Intent intent = new Intent(UserHomeActivity.this, FacilityProfileActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(UserHomeActivity.this, OrganizerActivity.class));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing repository", e);
                finish();
            }
        });
    }
}
