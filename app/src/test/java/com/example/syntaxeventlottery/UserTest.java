package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Tests for the User class.
 */
public class UserTest {

    private User user;
    private Entrant entrant;

    @Before
    public void setUp() {
        // Initialize User with sample data
        user = new User("device1", "test_email@example.com", "1234", "http://example.com", "test", "VVC");

        // Initialize Entrant with sample data
        entrant = new Entrant("device2", "entrant_email@example.com", "5678", "http://example.com/entrant", "testEntrant", "FacilityA");
    }

    @Test
    public void testConstructor() {

        assertEquals("device1", user.getDeviceCode());
        assertEquals("test_email@example.com", user.getEmail());
        assertEquals("1234", user.getPhoneNumber());
        assertEquals("http://example.com", user.getProfilePhotoUrl());
        assertEquals("test", user.getUsername());
        assertEquals("VVC", user.getFacility());
    }

    @Test
    public void testSetDeviceCode() {
        user.setDeviceCode("device2");
        assertEquals("device2", user.getDeviceCode());
    }

    @Test
    public void testSetEmail() {
        user.setEmail("test2_email@example.com");
        assertEquals("test2_email@example.com", user.getEmail());
    }

    @Test
    public void testSetPhoneNumber() {
        user.setPhoneNumber("5678");
        assertEquals("5678", user.getPhoneNumber());
    }

    @Test
    public void testSetProfilePhotoUrl() {
        user.setProfilePhotoUrl("http://example2.com");
        assertEquals("http://example2.com", user.getProfilePhotoUrl());
    }

    @Test
    public void testSetUsername() {
        user.setUsername("new_test");
        assertEquals("new_test", user.getUsername());
    }

    @Test
    public void testSetUserID() {
        user.setUserID("new_userID");
        assertEquals("new_userID", user.getUserID());
    }

    @Test
    public void testGenerateUserID() {
        String userID = user.getUserID();
        assertNotNull(userID);
        assertTrue(userID.startsWith("test_"));
    }

    @Test
    public void testEntrantConstructor() {
        // Verify that constructor initializes fields correctly
        assertEquals("device2", entrant.getDeviceCode());
        assertEquals("entrant_email@example.com", entrant.getEmail());
        assertEquals("5678", entrant.getPhoneNumber());
        assertEquals("http://example.com/entrant", entrant.getProfilePhotoUrl());
        assertEquals("testEntrant", entrant.getUsername());
        assertEquals("FacilityA", entrant.getFacility());

        // Check default values for Entrant-specific fields
        assertNotNull(entrant.getWaitingListEvents());
        assertNotNull(entrant.getSelectedEvents());
        assertNotNull(entrant.getInvitations());
        assertTrue(entrant.isReceiveNotifications());
        assertNull(entrant.getGeoLocation());
    }

    @Test
    public void testEntrantSetters() {

        // Test setter methods specific to Entrant
        entrant.setReceiveNotifications(false);
        assertFalse(entrant.isReceiveNotifications());

        entrant.setGeoLocation("51 N, 51 W");
        assertEquals("51 N, 51 W", entrant.getGeoLocation());
    }

    @Test
    public void testWaitingListEvents() {
        // Initially empty
        assertTrue(entrant.getWaitingListEvents().isEmpty());

        // Add events
        entrant.addWaitingListEvent("Event1");
        entrant.addWaitingListEvent("Event2");

        assertEquals(2, entrant.getWaitingListEvents().size());
        assertTrue(entrant.getWaitingListEvents().contains("Event1"));
        assertTrue(entrant.getWaitingListEvents().contains("Event2"));

        // Attempt to add duplicate event
        entrant.addWaitingListEvent("Event1");
        assertEquals(2, entrant.getWaitingListEvents().size()); // Should not add duplicate

        // Remove event
        entrant.removeWaitingListEvent("Event1");
        assertEquals(1, entrant.getWaitingListEvents().size());
        assertFalse(entrant.getWaitingListEvents().contains("Event1"));
    }

    @Test
    public void testSelectedEvents() {
        // Initially empty
        assertTrue(entrant.getSelectedEvents().isEmpty());

        // Add events
        entrant.addSelectedEvent("SelectedEvent1");
        entrant.addSelectedEvent("SelectedEvent2");

        assertEquals(2, entrant.getSelectedEvents().size());
        assertTrue(entrant.getSelectedEvents().contains("SelectedEvent1"));
        assertTrue(entrant.getSelectedEvents().contains("SelectedEvent2"));

        // Attempt to add duplicate event
        entrant.addSelectedEvent("SelectedEvent1");
        assertEquals(2, entrant.getSelectedEvents().size()); // Should not add duplicate

        // Remove event
        entrant.removeSelectedEvent("SelectedEvent2");
        assertEquals(1, entrant.getSelectedEvents().size());
        assertFalse(entrant.getSelectedEvents().contains("SelectedEvent2"));
    }

    @Test
    public void testInvitations() {
        // Initially empty
        assertTrue(entrant.getInvitations().isEmpty());

        // Add invitations
        entrant.addInvitation("Invite1");
        entrant.addInvitation("Invite2");

        assertEquals(2, entrant.getInvitations().size());
        assertTrue(entrant.getInvitations().contains("Invite1"));
        assertTrue(entrant.getInvitations().contains("Invite2"));

        // Attempt to add duplicate invitation
        entrant.addInvitation("Invite1");
        assertEquals(2, entrant.getInvitations().size());

        // Remove invitation
        entrant.removeInvitation("Invite1");
        assertEquals(1, entrant.getInvitations().size());
        assertFalse(entrant.getInvitations().contains("Invite1"));
    }

    @Test
    public void testGenerateDeterministicProfilePicture() {

        // If profile photo URL is set
        String profileUrl = entrant.generateDeterministicProfilePicture();
        assertEquals("http://example.com/entrant", profileUrl);

        // If profile photo URL is not set
        entrant.setProfilePhotoUrl("");
        String defaultProfileUrl = entrant.generateDeterministicProfilePicture();
        assertEquals("https://example.com/default_profile_picture.png", defaultProfileUrl);
    }

    @Test
    public void testEntrantToString() {

        // Set entrant data
        entrant.addWaitingListEvent("Event1");
        entrant.addSelectedEvent("SelectedEvent1");
        entrant.addInvitation("Invite1");
        entrant.setReceiveNotifications(false);
        entrant.setGeoLocation("51 N, 51 W");

        // Test string format
        String expected = "Entrant{" +
                "userID='" + entrant.getUserID() + '\'' +
                ", deviceCode='" + entrant.getDeviceCode() + '\'' +
                ", email='" + entrant.getEmail() + '\'' +
                ", phoneNumber='" + entrant.getPhoneNumber() + '\'' +
                ", profilePhotoUrl='" + entrant.getProfilePhotoUrl() + '\'' +
                ", username='" + entrant.getUsername() + '\'' +
                ", waitingListEvents=[Event1]" +
                ", selectedEvents=[SelectedEvent1]" +
                ", invitations=[Invite1]" +
                ", receiveNotifications=false" +
                ", geoLocation='51 N, 51 W'" +
                '}';
        assertEquals(expected, entrant.toString());
    }

    @Test
    public void testJoinEvent() {

        // Create a mock Event
        Event mockEvent = new Event();
        mockEvent.setEventName("MockEvent");
        mockEvent.setParticipants(new ArrayList<>());

        // Entrant joins the event
        entrant.joinEvent(mockEvent);

        // Verify that the event's participant list includes the entrant's username
        assertTrue(mockEvent.getParticipants().contains(entrant.getUsername()));

        // Verify that the entrant's waiting list includes the event name
        assertTrue(entrant.getWaitingListEvents().contains("MockEvent"));
    }

    @Test
    public void testLeaveEvent() {

        // Create a mock Event
        Event mockEvent = new Event();
        mockEvent.setEventName("MockEvent");
        mockEvent.setParticipants(new ArrayList<>());

        // Entrant joins and then leaves the event
        entrant.joinEvent(mockEvent);
        entrant.leaveEvent(mockEvent);

        // Verify that the event's participant list does not include the entrant's username
        assertFalse(mockEvent.getParticipants().contains(entrant.getUsername()));

        // Verify that the entrant's waiting list does not include the event name
        assertFalse(entrant.getWaitingListEvents().contains("MockEvent"));
    }
}
