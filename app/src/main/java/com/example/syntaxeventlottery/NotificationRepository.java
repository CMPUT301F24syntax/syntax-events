package com.example.syntaxeventlottery;

import android.util.Log;

import com.example.syntaxeventlottery.DataCallback;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for managing notifications in the application.
 */
public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private FirebaseFirestore db;

    /**
     * Constructor to initialize the Firebase Firestore instance.
     */
    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Adds notifications for a list of users based on their notification preferences.
     *
     * @param userIds  The list of user IDs to send notifications to.
     * @param message  The message to include in the notification.
     * @param eventId  The event ID associated with the notification.
     * @param callback The callback to handle success or failure.
     */
    public void addNotifications(List<String> userIds, String message, String eventId, DataCallback<Void> callback) {
        for (String userId : userIds) {
            // Retrieve user's allowNotification setting
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean allowNotification = documentSnapshot.getBoolean("allowNotification");
                            if (allowNotification != null && allowNotification) {
                                // User allows notifications, add the notification
                                Map<String, Object> notificationData = new HashMap<>();
                                notificationData.put("deviceId", userId);
                                notificationData.put("message", message);
                                notificationData.put("eventId", eventId);
                                notificationData.put("isRead", false);
                                notificationData.put("timestamp", FieldValue.serverTimestamp());

                                db.collection("notifications")
                                        .add(notificationData)
                                        .addOnSuccessListener(documentReference -> {
                                            Log.d(TAG, "Notification added for user: " + userId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error adding notification for user: " + userId, e);
                                            callback.onError(e);
                                        });
                            } else {
                                // User does not allow notifications, skip
                                Log.d(TAG, "User " + userId + " has notifications disabled.");
                            }
                        } else {
                            Log.d(TAG, "User document not found for userId: " + userId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error retrieving user document for userId: " + userId, e);
                    });
        }
        // Handle the callback after processing all users
        callback.onSuccess(null);
    }
}
