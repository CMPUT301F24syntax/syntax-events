package com.example.syntaxeventlottery;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Adds notifications for a list of users.
     *
     * @param userIds   List of user device IDs.
     * @param message   Notification message.
     * @param eventId   Related event ID.
     * @param callback  Callback to handle success or error.
     */
    public void addNotifications(List<String> userIds, String message, String eventId, DataCallback<Void> callback) {
        for (String userId : userIds) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceId", userId);
            notificationData.put("message", message);
            notificationData.put("eventId", eventId);
            notificationData.put("isRead", false);
            notificationData.put("timestamp", System.currentTimeMillis());

            db.collection("notifications")
                    .add(notificationData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Notification added for user: " + userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding notification for user: " + userId, e);
                        callback.onError(e);
                    });
        }
        callback.onSuccess(null);
    }
}