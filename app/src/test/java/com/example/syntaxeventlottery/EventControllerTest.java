package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventControllerTest {
    private EventController testController;
    private EventRepositoryInterface mockEventRepository;

    @Before
    public void setUp() {
        this.mockEventRepository = new MockEventRepository();
        this.testController = new EventController(mockEventRepository);
    }

    @Test
    public void testAddEvent() {
        // Arrange
        Event event = new Event("event123", "TestEvent", "this is a test", "test facility",
                0, new Date(), new Date(), "test_organizer");

        // Act
        testController.addEvent(event, null);

        // Assert
        assertTrue("The repository should contain the added event",
                mockEventRepository.getAllEventsList().contains(event));
    }


    @Test
    public void testUpdateEvent() {
        // Arrange
        Event event = new Event("event123", "TestEvent", "this is a test", "test facility",
                0, new Date(), new Date(), "test_organizer");
        mockEventRepository.addEventToRepo(event, null, null);

        // Act
        event.setEventName("UpdatedName");
        testController.updateEvent(event, null, null);

        // Assert
        Event updatedEvent = mockEventRepository.getAllEventsList().get(0);
        assertEquals("The event title should be updated", "Updated Title", updatedEvent.getEventName());
    }

    // MockEventRepository class to store events in-memory for testing
    private static class MockEventRepository implements EventRepositoryInterface {
        private final List<Event> events = new ArrayList<>();

        @Override
        public ArrayList<Event> getAllEventsList() {
            return new ArrayList<>(events);
        }

        @Override
        public void addEventToRepo(Event event, Uri imageUri, Bitmap qrCodeBitmap) {
            events.add(event);
        }

        @Override
        public void deleteEventFromRepo(Event event) {
            events.remove(event);
        }

        @Override
        public void updateEventDetails(Event event, @Nullable Uri imageUri, @Nullable Bitmap qrCodeBitmap) {
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(event.getId())) {
                    events.set(i, event);
                    break;
                }
            }
        }
    }
}
