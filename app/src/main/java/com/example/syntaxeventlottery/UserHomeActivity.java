// UserHomeActivity.java

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

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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

    // Controllers
    private EventController eventController;
    private UserController userController;

    // Adapter and Data
    private EventAdapter eventAdapter;
    private String deviceID;
    private User currentUser;
    private final String TAG = "UserHomeActivity";

    // Notification
    private ListenerRegistration notificationListener;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home_page);

        // Initialize UI Components
        dateTextView = findViewById(R.id.dateTextView);
        organizerButton = findViewById(R.id.organizerButton);
        profileButton = findViewById(R.id.profileButton);
        newsButton = findViewById(R.id.newsButton);
        scanButton = findViewById(R.id.qrScanButton2);

        // Initialize Controllers
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository());

        // Get deviceId
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView for Waitlisted Events
        waitlistEventsRecyclerView = findViewById(R.id.futureEventsRecyclerView);
        waitlistEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), this);
        waitlistEventsRecyclerView.setAdapter(eventAdapter);

        // Load Waitlisted Events
        loadUserWaitlistedEvents();

        // Set Button Listeners
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

        // Set up Date and Time Updater
        updateDateTime();
    }


    /**

     Loads waitlisted events for the current user.*/
    private void loadUserWaitlistedEvents() {
        eventController.refreshRepository(new DataCallback<Void>() {@Override
        public void onSuccess(Void result) {
            eventController.getUserWaitlistedEvents(deviceID, new DataCallback<ArrayList<Event>>() {@Override
            public void onSuccess(ArrayList<Event> result) {
                eventAdapter.updateEvents(result);}

                @Override
                public void onError(Exception e) {

                }
            });
        }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    /**
     * Updates the date and time display.
     */
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

    /**
     * Checks if the user has a facility profile and launches the appropriate activity.
     */
    private void checkFacilityProfileAndLaunch() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceID);
                if (currentUser == null) {
                    Log.e(TAG, "No user found with this device ID");
                    Toast.makeText(UserHomeActivity.this, "Couldn't load user information, try again", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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

    /**
     * Sets up the notification listener when the activity starts.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Set up a listener for new notifications specific to this device
        notificationListener = db.collection("notifications")
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    Notification notification = dc.getDocument().toObject(Notification.class);
                                    notification.setId(dc.getDocument().getId());

                                    // Send system notification
                                    NotificationUtils.sendNotification(
                                            getApplicationContext(),
                                            "Event Notification",
                                            notification.getMessage(),
                                            notification.generateNotificationId(),
                                            notification.getEventId()
                                    );

                                    markNotificationAsRead(notification);
                                } catch (Exception ex) {
                                    Log.e(TAG, "Failed to deserialize notification", ex);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Removes the notification listener when the activity stops.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    /**
     * Marks a notification as read in Firestore.
     *
     * @param notification The notification to mark as read.
     */
    private void markNotificationAsRead(Notification notification) {
        if (notification == null || notification.getId() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").document(notification.getId())
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read", e));
    }
}