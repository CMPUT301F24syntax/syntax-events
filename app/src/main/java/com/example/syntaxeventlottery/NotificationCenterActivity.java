package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the Notification Center with all unread notifications.
 */
public class NotificationCenterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private ListView notificationListView;
    private ArrayAdapter<String> adapter;
    private List<String> notificationMessages;
    private List<String> notificationIds;
    private ImageButton backButton; // Back Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get deviceId
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UI components
        notificationListView = findViewById(R.id.notificationListView);
        backButton = findViewById(R.id.backButton); // Initialize Back Button

        notificationMessages = new ArrayList<>();
        notificationIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationMessages);
        notificationListView.setAdapter(adapter);

        // Load notifications
        loadNotifications();

        // Set item click listener to mark as read when clicked
        notificationListView.setOnItemClickListener((parent, view, position, id) -> {
            String notificationId = notificationIds.get(position);
            markNotificationAsRead(notificationId);
            Toast.makeText(NotificationCenterActivity.this, "Notification marked as read", Toast.LENGTH_SHORT).show();
        });

        // Set click listener for Back Button
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Loads unread notifications for the current user.
     */
    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isRead", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null && !snapshots.isEmpty()) {
                            notificationMessages.clear();
                            notificationIds.clear();
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                String message = doc.getString("message");
                                notificationMessages.add(message);
                                notificationIds.add(doc.getId());
                                Log.d("NotificationCenter", "Loaded notification: " + message);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(NotificationCenterActivity.this, "No new notifications", Toast.LENGTH_SHORT).show();
                            Log.d("NotificationCenter", "No notifications found for deviceId: " + deviceId);
                        }
                    } else {
                        Log.w("NotificationCenter", "Error getting documents.", task.getException());
                        Toast.makeText(NotificationCenterActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Marks a notification as read.
     *
     * @param notificationId The ID of the notification to mark as read.
     */
    private void markNotificationAsRead(String notificationId) {
        db.collection("notifications").document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> Log.d("NotificationCenter", "Notification marked as read"))
                .addOnFailureListener(e -> Log.e("NotificationCenter", "Failed to mark notification as read", e));
    }
}