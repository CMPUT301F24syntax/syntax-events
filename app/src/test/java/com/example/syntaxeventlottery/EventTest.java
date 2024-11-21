package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;

public class EventTest {
    private Event event;

    @Before
    public void setUp() {
        // Initialize start and end dates for the test event
        LocalDate localStartDate = LocalDate.of(2024, 11, 6);
        LocalDate localEndDate = LocalDate.of(2024, 12, 2);
        Date startDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Initialize the Event object with valid parameters
        this.event = new Event(
                "event_001",               // eventID
                "event_1",                 // eventName
                "this is a test event",    // description
                100,                       // capacity
                startDate,                 // startDate
                endDate,                   // endDate
                "testOrganizer123"         // organizerId
        );
    }

    @Test
    public void testEventInitialization() {
        // Verify if the constructor initializes attributes correctly
        assertEquals("event_001", event.getEventID());
        assertEquals("event_1", event.getEventName());
        assertEquals("this is a test event", event.getDescription());
        assertEquals(100, event.getCapacity());
        assertEquals("testOrganizer123", event.getOrganizerId());

        // Check default attributes
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

    @Test
    public void testEventIdGeneration() throws InterruptedException {
        // Simulate generating an event ID
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = formatter.format(new Date());
        String expectedEventID = timeStamp + "_testOrganizer123";
        Thread.sleep(1000); // Simulate a time difference

        // Set the event ID and verify
        event.setEventID(expectedEventID);
        assertEquals(expectedEventID, event.getEventID());
    }

    @Test
    public void testAddParticipant() {
        String testUser1ID = "user1";
        String testUser2ID = "user2";
        String testUser3ID = "user3";

        // Modify the event's capacity for testing
        event.setCapacity(2);

        // Add the first participant
        event.addParticipant(testUser1ID);
        assertEquals(1, event.getParticipants().size());
        assertTrue(event.getParticipants().contains(testUser1ID));
        assertFalse(event.isFull());

        // Attempt to add the same participant again (should not change the count)
        event.addParticipant(testUser1ID);
        assertEquals(1, event.getParticipants().size());

        // Add a second participant
        event.addParticipant(testUser2ID);
        assertEquals(2, event.getParticipants().size());
        assertTrue(event.getParticipants().contains(testUser2ID));
        assertTrue(event.isFull());

        // Attempt to add a third participant (should not be added as the event is full)
        event.addParticipant(testUser3ID);
        assertEquals(2, event.getParticipants().size());
        assertFalse(event.getParticipants().contains(testUser3ID));
    }

    @Test
    public void testRemoveParticipant() {
        // Modify the event's capacity for testing
        event.setCapacity(3);

        // Add participants
        event.addParticipant("user1");
        event.addParticipant("user2");
        event.addParticipant("user3");
        assertTrue(event.isFull());

        // Remove the first participant
        event.removeParticipant("user1");
        assertEquals(2, event.getParticipants().size());
        assertFalse(event.getParticipants().contains("user1"));
        assertFalse(event.isFull());

        // Remove the second participant
        event.removeParticipant("user2");
        assertEquals(1, event.getParticipants().size());
        assertFalse(event.getParticipants().contains("user2"));
        assertFalse(event.isFull());

        // Remove the last participant
        event.removeParticipant("user3");
        assertEquals(0, event.getParticipants().size());
        assertFalse(event.getParticipants().contains("user3"));
        assertEquals(event.getCapacity(), event.getCapacity() - event.getParticipants().size());
    }
}
