package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EventTest {

    // Initialize variable
    private Event event;

    @Before
    public void setUp() {
        // Test creation of a new event with test values
        event = new Event(
                "Sample Event",
                "Sample Facility",
                "Sample Location",
                "This is a test event",
                50,
                new Date(),
                new Date(System.currentTimeMillis() + 3600 * 1000),
                "Organizer123",
                10,
                true
        );
    }

    @Test
    public void testGettersAndSetters() {
        // Test event name
        event.setEventName("Updated Event");
        assertEquals("Updated Event", event.getEventName());

        // Test facility name
        event.setFacilityName("Updated Facility");
        assertEquals("Updated Facility", event.getFacilityName());

        // Test description
        event.setDescription("Updated Description");
        assertEquals("Updated Description", event.getDescription());

        // Test capacity
        event.setCapacity(100);
        assertEquals(100, event.getCapacity());

        // Test start and end dates
        Date newStartDate = new Date();
        Date newEndDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        event.setStartDate(newStartDate);
        event.setEndDate(newEndDate);
        assertEquals(newStartDate, event.getStartDate());
        assertEquals(newEndDate, event.getEndDate());

        // Test organizer ID
        event.setOrganizerId("UpdatedOrganizer");
        assertEquals("UpdatedOrganizer", event.getOrganizerId());
    }

    @Test
    public void testWaitingListLimit() {

        // Test setting waiting list limit
        event.setWaitingListLimit(20);
        assertEquals(Integer.valueOf(20), event.getWaitingListLimit());

        // Test unlimited waiting list
        event.setWaitingListLimit(null);
        assertFalse(event.getWaitingListFull());
    }

    @Test
    public void testParticipantsManagement() {

        // Create test array
        ArrayList<String> participants = new ArrayList<>();
        participants.add("Participant1");
        participants.add("Participant2");

        // Test array size
        event.setParticipants(participants);
        assertEquals(2, event.getParticipants().size());
        assertEquals("Participant1", event.getParticipants().get(0));

        // Test waiting list full logic
        event.setWaitingListLimit(2);
        assertTrue(event.getWaitingListFull());
    }

    @Test
    public void testCapacityFullLogic() {

        // Test if participant has been added to the array
        ArrayList<String> confirmedParticipants = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            confirmedParticipants.add("Participant" + i);
        }

        event.setConfirmedParticipants(confirmedParticipants);
        assertTrue(event.getCapacityFull());
    }

    @Test
    public void testGenerateEventID() {

        // Create test organizer ID
        String organizerId = "Organizer123";

        // Generate Event ID from Organizer ID
        event.generateEventID(organizerId);

        // Assert the ID is not Null
        assertNotNull(event.getEventID());

        // Create a test date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        // Format date with organizer ID
        String expectedPrefix = formatter.format(new Date()) + "_" + organizerId;
        assertTrue(event.getEventID().startsWith(expectedPrefix));
    }

    @Test
    public void testToString() {

        // Test the string format
        String expected = "Event{eventID='null', eventName='Sample Event', description='This is a test event', capacity=50, capacityFull=false, waitingListLimit=10, waitingListFull=false, isDrawed=false, startDate=";
        assertTrue(event.toString().contains(expected));
    }

    @Test
    public void testDrawedFlag() {
        assertFalse(event.isDrawed());
        event.setDrawed(true);
        assertTrue(event.isDrawed());
    }

    @Test
    public void testPosterAndQrCode() {

        // Set test URL and QR code
        event.setPosterUrl("https://example.com/poster.jpg");
        event.setQrCode("QRCode123");

        // Assert they were set successfully
        assertEquals("https://example.com/poster.jpg", event.getPosterUrl());
        assertEquals("QRCode123", event.getQrCode());
    }

    @Test
    public void testCancelledParticipantsManagement() {

        // Create test array for cancelled participants
        ArrayList<String> cancelledParticipants = new ArrayList<>();

        // Add two participants to the list
        cancelledParticipants.add("CancelledParticipant1");
        cancelledParticipants.add("CancelledParticipant2");

        // Test setting cancelled participants
        event.setCancelledParticipants(cancelledParticipants);

        // Test that cancelled participants were added to the list
        assertEquals(2, event.getCancelledParticipants().size());
        assertEquals("CancelledParticipant1", event.getCancelledParticipants().get(0));
    }

    @Test
    public void testLocationDetails() {

        // Create an empty list to store maps, where each map holds event ID and its location
        List<Map<String, String>> locationDetails = new ArrayList<>();

        // Create a map to store an event ID and its corresponding location
        Map<String, String> location = new HashMap<>();

        // Add an event ID and location to the map
        location.put("Event1", "Location1");
        locationDetails.add(location);

        // Set the list of location details to the event
        event.setLocationDetails(locationDetails);

        // Test that the list has 1 item
        assertEquals(1, event.getLocationDetails().size());
        assertEquals("Location1", event.getLocationDetails().get(0).get("Event1"));

        // Add another event ID and location to the map
        event.addLocationDetail("Event2", "Location2");

        // Test that the list has 2 items
        assertEquals(2, event.getLocationDetails().size());
        assertEquals("Location2", event.getLocationDetails().get(1).get("Event2"));
    }

    @Test
    public void testIsLocationRequired() {

        // Verify that the event initially requires a location
        assertTrue(event.isLocationRequired());

        // Set now to false
        event.setLocationRequired(false);

        // Verify the event location required is set to false
        assertFalse(event.isLocationRequired());
    }
}