package com.example.syntaxeventlottery;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationController {
    private static final String TAG = "NotificationController";
    private FirebaseFirestore db;

    public NotificationController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches unread notifications for a specific user.
     *
     * @param deviceId The device ID of the user.
     * @param callback Callback to handle the list of notifications or errors.
     */
    public void fetchUnreadNotificationsForUser(String deviceId, DataCallback<List<Notification>> callback) {
        db.collection("notifications")
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notifications.add(notification);
                        }
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching notifications", e);
                    callback.onError(e);
                });
    }

    /**
     * Marks a notification as read in Firestore.
     *
     * @param notification The notification to mark as read.
     */
    public void markNotificationAsRead(Notification notification) {
        if (notification == null || notification.getId() == null) return;

        db.collection("notifications").document(notification.getId())
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read", e));
    }
}