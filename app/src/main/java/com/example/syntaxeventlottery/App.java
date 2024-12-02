package com.example.syntaxeventlottery;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * The {@code App} class is the main application class for the Syntax Event Lottery app.
 * It initializes global application state and creates a notification channel for the app.
 */
public class App extends Application {
    public static final String CHANNEL_ID = "syntax_event_lottery_channel";

    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * have been created. Initializes the notification channel.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Creates a notification channel for the app.
     * This is required for devices running Android Oreo (API level 26) and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Syntax Event Lottery Notifications";
            String description = "Notifications for event lottery results";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}