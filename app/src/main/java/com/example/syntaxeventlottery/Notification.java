// Notification.java
package com.example.syntaxeventlottery;

import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class for a notification.
 */
public class Notification {
    private String id;
    private String deviceId;
    private String eventId;
    private String message;
    private boolean read;
    @ServerTimestamp
    private Date timestamp;

    // Required empty constructor
    public Notification() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) { this.message = message; }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) { this.read = read; }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    // Add the generateNotificationId() method
    public int generateNotificationId() {
        // Use a hash of the Firestore document ID to ensure uniqueness
        if (id != null) {
            return id.hashCode();
        } else {
            // Fallback to current time in milliseconds if ID is not set
            return (int) System.currentTimeMillis();
        }
    }


    public String getFormattedTimestamp() {
        if (timestamp != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return formatter.format(timestamp);
        } else {
            return "";
        }
    }
}