package com.example.syntaxeventlottery;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class representing a Notification.
 */
public class Notification {
    private String id;
    private String deviceId;
    private String eventId;
    private String message;
    private boolean isRead;
    @ServerTimestamp
    private Date timestamp;

    // Default constructor required for Firestore deserialization
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
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    /**
     * Returns a formatted timestamp string.
     *
     * @return Formatted timestamp.
     */
    @Exclude
    public String getFormattedTimestamp() {
        if (timestamp != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return formatter.format(timestamp);
        } else {
            return "";
        }
    }


}