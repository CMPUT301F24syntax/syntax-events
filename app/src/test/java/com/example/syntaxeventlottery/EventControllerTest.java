package com.example.syntaxeventlottery;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;

public class EventControllerTest {

    // Initialize controller and mock repository
    private EventController eventController;

    @Mock
    private EventRepository mockRepository;

    @Mock
    private Context mockContext;

    @Before
    public void setUp() {

        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create controller using the mock repository
        eventController = new EventController(mockRepository);
    }

    @Test
    public void testGetEventById_Found() {

        // Create list of events
        ArrayList<Event> events = new ArrayList<>();

        // Create a new event for testing
        Event event = new Event("Event1", "Facility1", "Location1", "Test Event", 100, new Date(System.currentTimeMillis() + 1000), new Date(System.currentTimeMillis() + 2000), "Organizer123", null, false);

        // Set an event ID
        event.setEventID("EventID");

        // Add the new event to the events list
        events.add(event);

        // Use mock repository to return the test event list
        when(mockRepository.getLocalEventsList()).thenReturn(events);

        // Use the controller to find the event by its ID
        Event result = eventController.getEventById("EventID");

        // Check if the event found matches the one added
        assertEquals(event, result);
    }

    @Test
    public void testGetEventById_NotFound() {

        // Mock an empty list of events using the mockRepository
        when(mockRepository.getLocalEventsList()).thenReturn(new ArrayList<>());

        // Find event with an ID that doesn't exist
        Event result = eventController.getEventById("NonExistentID");

        // Check if the result is null, as no event exists with that ID
        assertNull(result);
    }

    @Test
    public void testAddUserToWaitingList_Success() {

        // Create a test event
        Event event = new Event("Event1", "Facility1", "Location1", "Test Event", 10, new Date(System.currentTimeMillis() + 1000), new Date(System.currentTimeMillis() + 2000), "Organizer123", 5, false);

        // Initialize the participants list for the event as an empty list
        event.setParticipants(new ArrayList<>());

        // Create a mock callback to track behavior
        DataCallback<Event> mockCallback = mock(DataCallback.class);

        // Add a user to the waiting list
        eventController.addUserToWaitingList(event, "User123", mockCallback);

        // Check if the user was added to the list
        assertTrue(event.getParticipants().contains("User123"));
    }

    @Test
    public void testAddUserToWaitingList_AlreadyExists() {

        // Create a test event
        Event event = new Event("Event1", "Facility1", "Location1", "Test Event", 10, new Date(System.currentTimeMillis() + 1000), new Date(System.currentTimeMillis() + 2000), "Organizer123", 5, false);

        // Create participant list
        ArrayList<String> participants = new ArrayList<>();

        // Add a user to the participants list
        participants.add("User123");

        // Set the participant list to the event
        event.setParticipants(participants);

        // Create a mock callback
        DataCallback<Event> mockCallback = mock(DataCallback.class);

        // Try to add the same user to the waiting list
        eventController.addUserToWaitingList(event, "User123", mockCallback);

        // Verify that an error was triggered
        verify(mockCallback).onError(any(IllegalArgumentException.class));
    }
}