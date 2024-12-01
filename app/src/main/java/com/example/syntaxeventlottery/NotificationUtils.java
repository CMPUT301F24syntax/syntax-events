package com.example.syntaxeventlottery;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
    private static final String CHANNEL_ID = "EventNotificationsChannel";
    private static final String CHANNEL_NAME = "Event Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for event updates";
    private static final String TAG = "NotificationUtils";

    /**
     * Creates the notification channel (required for Android O and above).
     *
     * @param context The application context.
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH; // High importance for immediate attention
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Sends a system notification.
     *
     * @param context        The application context.
     * @param title          The title of the notification.
     * @param message        The message body of the notification.
     * @param notificationId The unique ID for the notification.
     * @param eventID        The event ID related to the notification.
     */
    public static void sendNotification(Context context, String title, String message, int notificationId, String eventID) {
        // Intent to open EventDetailActivity when the notification is tapped
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
                .setSmallIcon(R.mipmap.ic_launcher) // Ensure this icon exists
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notifications
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
    }
}