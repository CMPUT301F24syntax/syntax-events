package com.example.syntaxeventlottery;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;

public class EventTest {
    private Event event;

    @Before
    public void setUp() {
        LocalDate localStartDate = LocalDate.of(2024, 11, 6);
        LocalDate localEndDate = LocalDate.of(2024, 12, 2);
        // Convert local date objects to date objects
        Date startDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        this.event = new Event(
                null,
                "event_1",
                "this is a test event",
                "testFacility123",
                100,
                startDate,
                endDate,
                "testOrganizer123"
        );
    }

    // test the constructor
    @Test
    public void testEventInitialization() {
        // Check attributes passed as arguments to constructor
        assertEquals("event_1", event.getEventName());
        assertEquals("this is a test event", event.getDescription());
        assertEquals("testFacility123", event.getFacility());
        assertEquals(100, event.getCapacity());
        assertEquals("testOrganizer123", event.getOrganizerId());

        // check other attributes
        assertFalse(event.isFull());
        assertFalse(event.isDrawed());
        assertTrue(event.getParticipants().isEmpty());
        assertTrue(event.getSelectedParticipants().isEmpty());

        // Verify start and end dates
        LocalDate expectedStartDate = LocalDate.of(2024, 11, 6);
        LocalDate expectedEndDate = LocalDate.of(2024, 12, 2);
        assertEquals(Date.from(expectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), event.getStartDate());
        assertEquals(Date.from(expectedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), event.getEndDate());

    }

    // test event ID generation
    @Test
    public void testEventIdGeneration() throws InterruptedException {
        // create a test event ID
        String testOrganizerID = "testOrganizer123";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String testTimeStamp = formatter.format(new Date());
        String testEventID = testTimeStamp + "_" + testOrganizerID; // this is the eventId for testing
        // sleep for 1 second to create small time difference
        Thread.sleep(1000);
        // Generate event ID
        event.generateEventID(event.getOrganizerId());
        String eventID = event.getEventID();

        String[] arrayEventID = eventID.split("_");

        assertEquals(arrayEventID[1], testOrganizerID);
        assertNotEquals(arrayEventID[0], testTimeStamp);
        assertEquals(eventID.length(), testEventID.length());
    }

    // test adding a participant
    @Test
    public void testAddParticipant() {
        String testUser1ID = "user1";
        String testUser2ID = "user2";
        String testUser3ID = "user3";

        // change event capacity for testing
        event.setCapacity(2);

        // add user1 to participants list
        event.addParticipant(testUser1ID);
        assertEquals(1, event.getParticipants().size());
        assertTrue(event.getParticipants().contains(testUser1ID));
        assertFalse(event.isFull()); // event should not be full

        // ensure the same user cannot be added
        event.addParticipant(testUser1ID);
        assertEquals(1, event.getParticipants().size());
        assertTrue(event.getParticipants().contains(testUser1ID)); // make sure original user was not removed somehow

        // add user 2 to participants list
        event.addParticipant(testUser2ID);
        assertEquals(2, event.getParticipants().size());
        assertTrue(event.getParticipants().contains(testUser2ID));
        assertTrue(event.isFull());

        // attempt to add user 3 to participants list
        event.addParticipant(testUser3ID);
        assertEquals(2, event.getParticipants().size()); // participant should not be added
        assertFalse(event.getParticipants().contains(testUser3ID));
    }

    // test removing a participant
    @Test
    public void testRemoveParticipant() {
        event.setCapacity(3); // change event capacity for test

        // add some test users
        event.addParticipant("user1");
        event.addParticipant("user2");
        event.addParticipant("user3");
        // check that event is full
        assertTrue(event.isFull());

        // remove user1
        event.removeParticipant("user1");
        assertEquals(2, event.getParticipants().size());
        assertFalse(event.getParticipants().contains("user1"));
        assertFalse(event.isFull());

        // remove user2
        event.removeParticipant("user2");
        assertEquals(1, event.getParticipants().size());
        assertFalse(event.getParticipants().contains("user2"));
        assertFalse(event.isFull());

        // remove user 3
        event.removeParticipant("user3");
        assertEquals(0, event.getParticipants().size());
        assertFalse(event.getParticipants().contains("user3"));
        assertEquals(event.getCapacity(), event.getCapacity() - event.getParticipants().size());

    }

}

