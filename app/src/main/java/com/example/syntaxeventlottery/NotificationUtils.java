// NotificationUtils.java

package com.example.syntaxeventlottery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationUtils {
    private static final String CHANNEL_ID = "event_lottery_channel";
    private static final String CHANNEL_NAME = "Event Lottery Notifications";
    private static final String CHANNEL_DESC = "Notifications for event lottery results";
    private static final String TAG = "NotificationUtils";

    /**
     * Creates a notification channel. Required for Android 8.0 and above.
     *
     * @param context The application context.
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.e(TAG, "NotificationManager is null, channel not created");
            }
        }
    }

    /**
     * Sends a system notification.
     *
     * @param context         The application context.
     * @param title           The notification title.
     * @param message         The notification message.
     * @param notificationId  Unique identifier for the notification.
     * @param eventID         The event ID associated with the notification.
     */
    public static void sendNotification(Context context, String title, String message, int notificationId, String eventID) {
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("eventID", eventID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_news)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted");
                return;
            }
        }

        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notification sent with ID: " + notificationId);
    }

    private void markNotificationAsRead(Notification notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").document(notification.getId())
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read", e));
    }
}