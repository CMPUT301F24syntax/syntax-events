package com.example.syntaxeventlottery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class NotificationListenerService extends Service {
    private static final String TAG = "NotificationListenerService";
    private static final String CHANNEL_ID = "NotificationListenerChannel";
    private static final int NOTIFICATION_ID = 1;

    private ListenerRegistration notificationListener;
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Create the notification channel and start foreground service
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        // Start listening to Firestore
        startListeningToNotifications();
    }

    /**
     * Creates a notification channel (required for Android O and above).
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Listener Channel";
            String description = "Channel for Notification Listener Service";
            int importance = NotificationManager.IMPORTANCE_LOW; // Low importance to avoid sound/vibration
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setLightColor(Color.BLUE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Builds the notification displayed by the foreground service.
     *
     * @return The notification instance.
     */
    private android.app.Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Listening for Notifications")
                .setContentText("Your app is monitoring notifications.")
                .setSmallIcon(R.mipmap.ic_launcher) // Ensure this icon exists
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Sets up the Firestore listener for new notifications.
     */
    private void startListeningToNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

                                    // Mark the notification as read
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If the service is killed by the system, recreate it
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
}