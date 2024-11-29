package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotificationTest {

    // Notification object for testing
    private Notification notification;

    @Before
    public void setUp() {
        // Create a new Notification object for each test
        notification = new Notification();
    }

    @Test
    public void testSetAndGetId() {

        // Create a test ID
        String testId = "12345";

        // Set the test ID
        notification.setId(testId);

        // Check if the ID was set correctly
        assertEquals(testId, notification.getId());
    }

    @Test
    public void testSetAndGetDeviceId() {

        // Create a test device ID
        String testDeviceId = "Device123";

        // Set the device ID
        notification.setDeviceId(testDeviceId);

        // Check if the device ID was set correctly
        assertEquals(testDeviceId, notification.getDeviceId());
    }

    @Test
    public void testSetAndGetEventId() {

        // Create a test event ID
        String testEventId = "Event123";

        // Set the event ID
        notification.setEventId(testEventId);

        // Check if the event ID was set correctly
        assertEquals(testEventId, notification.getEventId());
    }

    @Test
    public void testSetAndGetMessage() {

        // Create a test message
        String testMessage = "Bitcoin will hit 500k before 2030";

        // Set the test message
        notification.setMessage(testMessage);

        // Check if the test message was set successfully
        assertEquals(testMessage, notification.getMessage());
    }

    @Test
    public void testSetAndGetReadStatus() {

        // Set the read status to true
        notification.setRead(true);

        // Check if the read status is true
        assertTrue(notification.isRead());

        // Set the read status to false
        notification.setRead(false);

        // Check if the read status is false
        assertFalse(notification.isRead());
    }

    @Test
    public void testSetAndGetTimestamp() {

        // Create a test timestamp
        Date testDate = new Date();

        // Set the test timestamp
        notification.setTimestamp(testDate);

        // Check if the timestamp was set correctly
        assertEquals(testDate, notification.getTimestamp());
    }

    @Test
    public void testGetFormattedTimestamp() {

        // Create a test timestamp
        Date testDate = new Date();

        // Set the test timestamp
        notification.setTimestamp(testDate);

        // Format the date as year-month-day hour:minute
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String expectedFormattedDate = formatter.format(testDate);

        // Check if the formatted timestamp matches the expected format
        assertEquals(expectedFormattedDate, notification.getFormattedTimestamp());
    }

    @Test
    public void testGetFormattedTimestampEmptyWhenNull() {

        // Ensure the timestamp is null
        notification.setTimestamp(null);

        // Check if the formatted timestamp returns an empty string
        assertEquals("", notification.getFormattedTimestamp());
    }
}
