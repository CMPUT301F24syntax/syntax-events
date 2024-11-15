package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Activity to display the Notification Center with all unread notifications.
 */
public class NotificationCenterActivity extends AppCompatActivity {

    private static final String TAG = "NotificationCenterActivity";
    RecyclerView notificationRecyclerView;
    private NotificationAdapter adapter;
    private ImageButton backButton;
    private NotificationController notificationController;
    private String userId; // Using Device ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        // Initialize userId
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "User ID (Device ID): " + userId);

        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        backButton = findViewById(R.id.backButton);

        adapter = new NotificationAdapter(this);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationRecyclerView.setAdapter(adapter);

        notificationController = new NotificationController();

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up item click listener
        adapter.setOnItemClickListener(new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Notification notification) {
                // Optimistically remove the notification from the adapter
                adapter.removeNotificationById(notification.getId());

                // Mark the notification as read
                notificationController.markAsRead(notification.getId(), new NotificationController.NotificationUpdateListener() {
                    @Override
                    public void onUpdateSuccess() {
                        Toast.makeText(NotificationCenterActivity.this, "Notification marked as read", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUpdateFailure(Exception e) {
                        Toast.makeText(NotificationCenterActivity.this, "Failed to update notification", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to mark notification as read", e);
                        // Optionally, you can re-add the notification to the adapter if marking as read fails
                        adapter.addNotification(notification);
                    }
                });

                // Navigate to EventDetailActivity with eventId
                String eventId = notification.getEventId();
                if (eventId != null && !eventId.isEmpty()) {
                    Intent intent = new Intent(NotificationCenterActivity.this, EventDetailActivity.class);
                    intent.putExtra("event_id", eventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(NotificationCenterActivity.this, "Invalid event ID", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Notification has invalid eventId: " + eventId);
                }
            }
        });

        // Start listening for notifications
        listenNotifications();
    }

    /**
     * Start real-time listening for unread notifications
     */
    private void listenNotifications() {
        Log.d(TAG, "Listening for notifications with userId: " + userId);
        notificationController.listenUnreadNotifications(userId, new NotificationController.NotificationFetchListener() {
            @Override
            public void onFetchSuccess(List<Notification> notifications) {
                runOnUiThread(() -> {
                    if (notifications.isEmpty()) {
                        showSnackbar("No new notifications");
                        Log.d(TAG, "No notifications found for userId: " + userId);
                    } else {
                        adapter.setNotifications(notifications);
                        Log.d(TAG, "Total notifications loaded: " + notifications.size());
                    }
                });
            }

            @Override
            public void onFetchFailure(Exception e) {
                runOnUiThread(() -> {
                    showSnackbar("Failed to load notifications: " + e.getMessage());
                    Log.e(TAG, "Error fetching notifications", e);
                });
            }
        });
    }

    /**
     * Display a Snackbar message
     *
     * @param message The message to display
     */
    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
}