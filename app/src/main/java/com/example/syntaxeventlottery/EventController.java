package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.net.Uri;
import java.util.ArrayList;
import java.util.Date;

public class EventController {
    private EventRepositoryInterface repository;
    private ArrayList<Event> localEventsList;

    public EventController(EventRepositoryInterface repository) {
        this.repository = repository;
        this.localEventsList = new ArrayList<>();
    }

    /**
     * Add a new event with validation
     */
    public void addEvent(Event event, Uri imageUri, Bitmap qrCode) {
        // Validate event data
        validateEvent(event);

        // Add to repository
        repository.addEventToRepo(event, imageUri, qrCode);
        updateLocalList();
    }

    /**
     * Get all events
     */
    public ArrayList<Event> getAllEvents() {
        updateLocalList();
        return localEventsList;
    }

    /**
     * Get event by ID
     */
    public Event getEventById(String eventId) {
        updateLocalList();
        for (Event event : localEventsList) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Check if event is full
     */
    public boolean isEventFull(String eventId) {
        Event event = getEventById(eventId);
        if (event == null) return false;
        return event.getParticipants().size() >= event.getCapacity();
    }

    /**
     * Add participant to event
     */
    public boolean addParticipant(String eventId, String participantId) {
        Event event = getEventById(eventId);
        if (event == null) return false;

        // Check if event is full
        if (isEventFull(eventId)) {
            return false;
        }

        // Check if participant is already registered
        if (event.getParticipants().contains(participantId)) {
            return false;
        }

        // Add participant
        event.addParticipant(participantId);
        repository.addEventToRepo(event, null, null); // Update in repository
        return true;
    }

    /**
     * Remove participant from event
     */
    public boolean removeParticipant(String eventId, String participantId) {
        Event event = getEventById(eventId);
        if (event == null) return false;

        boolean removed = event.removeParticipant(participantId);
        if (removed) {
            repository.addEventToRepo(event, null, null); // Update in repository
        }
        return removed;
    }

    /**
     * Draw winners for an event
     */
    public boolean drawEventWinners(String eventId, int numberOfWinners) {
        Event event = getEventById(eventId);
        if (event == null) return false;

        // Check if already drawn
        if (event.isDrawed()) {
            return false;
        }

        // Perform the draw
        boolean drawSuccess = event.drawWinners(numberOfWinners);
        if (drawSuccess) {
            repository.addEventToRepo(event, null, null); // Update in repository
        }
        return drawSuccess;
    }

    /**
     * Update local list from repository
     */
    private void updateLocalList() {
        this.localEventsList = repository.getEventsList();
    }

    /**
     * Validate event data
     */
    private void validateEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.getEventName() == null || event.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty");
        }
        if (event.getCapacity() <= 0) {
            throw new IllegalArgumentException("Event capacity must be greater than 0");
        }
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new IllegalArgumentException("Event dates cannot be null");
        }
        if (event.getStartDate().after(event.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (event.getStartDate().before(new Date())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
    }

    /**
     * Get list of winners for an event
     */
    public ArrayList<String> getEventWinners(String eventId) {
        Event event = getEventById(eventId);
        if (event == null) return new ArrayList<>();
        return event.getSelectedParticipants();
    }

    /**
     * Check if user is registered for event
     */
    public boolean isUserRegistered(String eventId, String userId) {
        Event event = getEventById(eventId);
        if (event == null) return false;
        return event.getParticipants().contains(userId);
    }
}
