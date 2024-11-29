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
    private String notificationType;
    private boolean systemNotification;

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

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isSystemNotification() {
        return systemNotification;
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