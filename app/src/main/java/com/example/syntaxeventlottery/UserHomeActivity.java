// UserHomeActivity.java

package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserHomeActivity extends AppCompatActivity {

    private TextView dateTextView;
    private RecyclerView futureEventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private ImageButton organizerButton, profileButton, newsButton;
    private String deviceId;
    private UserController userController;
    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize views
        dateTextView = findViewById(R.id.dateTextView);
        organizerButton = findViewById(R.id.organizerButton);
        profileButton = findViewById(R.id.profileButton);
        newsButton = findViewById(R.id.newsButton);

        // Initialize UserController
        userController = new UserController(this);

        // Initialize EventRepository
        eventRepository = new EventRepository();

        // Set listener to update adapter when data changes
        eventRepository.setOnEventsDataChangeListener(() -> {
            runOnUiThread(() -> {
                eventAdapter.notifyDataSetChanged();
            });
        });

        // Get deviceId from UserController
        deviceId = userController.getDeviceId();

        // Set up RecyclerView for Future Events
        futureEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        futureEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = eventRepository.getAllEventsList();
        eventAdapter = new EventAdapter(eventList, this);
        futureEventsRecyclerView.setAdapter(eventAdapter);

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

        // Set up date and time updater
        updateDateTime();
    }

    /**
     * Checks the user's facility attribute and navigates accordingly using UserController.
     */
    private void checkUserAndNavigate() {
        userController.getUserFacility(new UserRepository.OnEntrantDataFetchListener() {
            @Override
            public void onEntrantDataFetched(Entrant entrant) {
                if (entrant != null) {
                    String facility = entrant.getFacility();
                    if (facility == null || facility.isEmpty()) {
                        // Facility is empty, navigate to FacilityProfileActivity
                        startActivity(new Intent(UserHomeActivity.this, FacilityProfileActivity.class));
                    } else {
                        // Facility is set, navigate to OrganizerActivity
                        startActivity(new Intent(UserHomeActivity.this, OrganizerActivity.class));
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEntrantDataFetchError(Exception e) {
                Toast.makeText(UserHomeActivity.this, "Failed to check user information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the date and time in the TextView every second.
     */
    private void updateDateTime() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Edmonton"));

        // Update the time every second
        new Thread(() -> {
            while (!isFinishing()) {
                runOnUiThread(() -> {
                    String currentDateTime = dateFormat.format(new Date());
                    dateTextView.setText(currentDateTime);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
