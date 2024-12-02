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

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Notification() {}

    /**
     * Gets the unique identifier of the notification.
     *
     * @return The notification ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the notification.
     *
     * @param id The notification ID.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets the device ID associated with the notification.
     *
     * @return The device ID.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID associated with the notification.
     *
     * @param deviceId The device ID.
     */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /**
     * Gets the event ID related to the notification.
     *
     * @return The event ID.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the event ID related to the notification.
     *
     * @param eventId The event ID.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Gets the message content of the notification.
     *
     * @return The notification message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message content of the notification.
     *
     * @param message The notification message.
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Checks whether the notification has been read.
     *
     * @return True if the notification is read, false otherwise.
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Marks the notification as read or unread.
     *
     * @param read True to mark as read, false otherwise.
     */
    public void setRead(boolean read) { this.read = read; }

    /**
     * Gets the timestamp of when the notification was created.
     *
     * @return The timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the notification.
     *
     * @param timestamp The timestamp.
     */
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    /**
     * Generates a unique notification ID for display purposes.
     *
     * @return A hash of the notification ID or current time in milliseconds if the ID is not set.
     */
    public int generateNotificationId() {
        if (id != null) {
            return id.hashCode();
        } else {
            return (int) System.currentTimeMillis();
        }
    }

    /**
     * Formats the timestamp into a human-readable date and time string.
     *
     * @return The formatted timestamp, or an empty string if the timestamp is null.
     */
    public String getFormattedTimestamp() {
        if (timestamp != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return formatter.format(timestamp);
        } else {
            return "";
        }
    }
}
