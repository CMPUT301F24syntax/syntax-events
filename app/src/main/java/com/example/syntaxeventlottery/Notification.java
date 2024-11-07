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

    public String getEventId() {
        return eventId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public Date getTimestamp() {
        return timestamp;
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