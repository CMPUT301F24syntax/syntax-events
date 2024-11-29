package com.example.syntaxeventlottery;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    @Before
    public void setUp() {

        // Create mocks for testing
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
        Event result = eventController.getEventById("Bitcoin_to_the_moon_buy_ASAP_not_financial_advice");

        // Check if the result is null, it should be as no event exists with that ID
        assertNull(result);
    }

    @Test
    public void testAddUserToWaitingList_Success() {

        // Create a test event
        Event event = new Event("Event1", "Facility1", "Location1", "Test Event", 10, new Date(System.currentTimeMillis() + 1000), new Date(System.currentTimeMillis() + 2000), "Organizer123", 5, false);

        // Initialize the participants list for the event as an empty list
        event.setParticipants(new ArrayList<>());

        // Create a fake callback to track behavior
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

        // Create a new empty list to store participants
        ArrayList<String> participants = new ArrayList<>();

        // Add a user to the list
        participants.add("User123");

        // Set the list of participants to the event
        event.setParticipants(participants);

        // Create a mock object for the DataCallback to simulate its behavior in tests
        DataCallback<Event> mockCallback = mock(DataCallback.class);

        // Try to add user to the event's waiting list and use the callback to handle the result
        eventController.addUserToWaitingList(event, "User123", mockCallback);

        // Check if the error was triggered
        verify(mockCallback).onError(any(IllegalArgumentException.class));
    }

    @Test
    public void testPerformDraw_AllSelected() {

        // Create a test event
        Event event = new Event("Event1", "Facility1", "Location1", "Test Event", 10, new Date(System.currentTimeMillis() + 1000), new Date(System.currentTimeMillis() + 2000), "Organizer123", null, false);

        // Create a new empty list to store participants
        ArrayList<String> participants = new ArrayList<>();

        // Add some users to the participant list
        participants.add("Bitcoiner1");
        participants.add("Bitcoiner2");
        participants.add("Bitcoiner3");

        // Set the participant list to the event
        event.setParticipants(participants);

        // Create a mock object for the DataCallback to simulate its behavior in tests
        DataCallback<Event> mockCallback = mock(DataCallback.class);

        // Perform a lottery draw for the event and use the callback to handle the result
        eventController.performDraw(event, mockCallback);

        // Check if the number of selected participants matches the size of the participants list
        assertEquals(participants.size(), event.getSelectedParticipants().size());

        // Check if the waiting list is empty
        assertTrue(event.getParticipants().isEmpty());

        // Verify the draw flag is set to true
        assertTrue(event.isDrawed());
    }
}