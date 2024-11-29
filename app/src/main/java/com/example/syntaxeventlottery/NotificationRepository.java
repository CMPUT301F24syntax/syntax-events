package com.example.syntaxeventlottery;

import static java.nio.file.Paths.get;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private FirebaseFirestore db;
    private CollectionReference notificationsRef;
    private HashMap<String, ArrayList<Notification>> userNotificationsList;
    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.notificationsRef = db.collection("Notifications");
        this.userNotificationsList = new HashMap<>();
    }

    public List<Notification> getUserNotifications(String userId) {
        return userNotificationsList.getOrDefault(userId, new ArrayList<>());
    }

    public void fetchUserNotifications(String userId, DataCallback<Void> callback) {
        notificationsRef.whereEqualTo("deviceID", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Notification> notificationsList = (ArrayList<Notification>) getUserNotifications(userId);
                    notificationsList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification notification = doc.toObject(Notification.class);
                        notificationsList.add(notification);
                    }
                    userNotificationsList.put(userId, notificationsList);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user notifications", e);
                    callback.onError(e);
                });
    }

    public void addNotificationToRepo(Notification notification, DataCallback<Void> callback) {
        String userId = notification.getDeviceId();
        userNotificationsList.putIfAbsent(userId, new ArrayList<>());
        userNotificationsList.get(userId).add(notification);
        HashMap<String, Object> data = notificationToHashData(notification);
        notificationsRef.add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification added successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add notification", e);
                    callback.onError(e);
                });
    }

    public void markAsRead(String userId, String notificationId, DataCallback<Void> callback) {
        notificationsRef.document(notificationId)
                .update("read", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification Marked as Read" + notificationId);
                    ArrayList<Notification> notificationsList = userNotificationsList.get(userId);
                    if (notificationsList != null) {
                        for (Notification notification : notificationsList) {
                            if (notification.getId().equals(notificationId)) {
                                notification.setRead(true);
                                break;
                            }
                        }
                    }
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error setting to read", e);
                    callback.onError(e);
                });
    }

    public HashMap<String, Object> notificationToHashData(Notification notification) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("deviceId", notification.getDeviceId());
        data.put("eventId", notification.getEventId());
        data.put("message", notification.getMessage());
        data.put("read", notification.isRead());
        data.put("notificationType", notification.getNotificationType());
        data.put("systemNotification", notification.isSystemNotification());
        return data;
    }
}
