package com.example.syntaxeventlottery;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class responsible for managing notifications.
 */
public class NotificationController {

    private static final String TAG = "NotificationController";
    private FirebaseFirestore db;
    private CollectionReference notificationsRef;

    public NotificationController() {
        db = FirebaseFirestore.getInstance();
        notificationsRef = db.collection("notifications");
    }

    /**
     * Fetches unread notifications for a specific user.
     *
     * @param userId   The ID of the user.
     * @param listener Callback to handle the result.
     */
    public void fetchUnreadNotifications(String userId, final NotificationFetchListener listener) {
        notificationsRef
                .whereEqualTo("deviceId", userId)
                .whereEqualTo("read", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (var doc : task.getResult()) {
                                Notification notification = doc.toObject(Notification.class);
                                if (notification != null) {
                                    notification.setId(doc.getId());
                                    notifications.add(notification);
                                    Log.d(TAG, "Loaded notification: " + notification.getMessage());
                                }
                            }
                            listener.onFetchSuccess(notifications);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            listener.onFetchFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Marks a notification as read.
     *
     * @param notificationId The ID of the notification to mark as read.
     * @param listener        Callback to handle the result.
     */
    public void markAsRead(String notificationId, final NotificationUpdateListener listener) {
        notificationsRef.document(notificationId)
                .update("read", true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Notification marked as read: " + notificationId);
                            listener.onUpdateSuccess();
                        } else {
                            Log.w(TAG, "Failed to mark as read.", task.getException());
                            listener.onUpdateFailure(task.getException());
                        }
                    }
                });
    }

    // Listener Interfaces
    public interface NotificationFetchListener {
        void onFetchSuccess(List<Notification> notifications);
        void onFetchFailure(Exception e);
    }

    public interface NotificationUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(Exception e);
    }

    /**
     * Listens to unread notifications in real-time for a specific user.
     *
     * @param userId   The ID of the user.
     * @param listener Callback to handle the result.
     */
    public void listenUnreadNotifications(String userId, final NotificationFetchListener listener) {
        notificationsRef
                .whereEqualTo("deviceId", userId)
                .whereEqualTo("read", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            listener.onFetchFailure(error);
                            return;
                        }

                        List<Notification> notifications = new ArrayList<>();
                        for (var doc : value) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notification.setId(doc.getId());
                                notifications.add(notification);
                                Log.d(TAG, "Loaded notification: " + notification.getMessage() + ", read: " + notification.isRead());
                            }
                        }
                        Log.d(TAG, "Total notifications fetched: " + notifications.size());
                        listener.onFetchSuccess(notifications);
                    }
                });
    }
}