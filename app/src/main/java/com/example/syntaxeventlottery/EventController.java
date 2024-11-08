package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class EventController {
    private EventRepositoryInterface repository;

    public EventController(EventRepositoryInterface repository) {
        this.repository = repository;
    }

    /**
     * Get all events from local list
     */
    private ArrayList<Event> getAllEvents() {
        return repository.getAllEventsList();
    }

    /**
     * Add a new event with validation
     */
    public void addEvent(Event event, @Nullable Uri imageUri) {
        // generate event ID
        event.generateEventID(event.getOrganizerId());
        // Validate event data
        validateEvent(event);
        // generate the bitmap of the event
        Bitmap qrCodeBitmap = generateQRCodeBitmap(event.getEventID());
        // Add to repository
        repository.addEventToRepo(event, imageUri, qrCodeBitmap);
    }

    /**
     * Update an existing event
     */
    public void updateEvent(Event event, @Nullable Uri imageUri, @Nullable Bitmap qrCodeBitmap) {
        // call repository to update event
        repository.updateEventDetails(event, imageUri, qrCodeBitmap);
    }


    /**
     * Get event by ID
     */
    public Event getEventById(String eventId) {
        for (Event event : repository.getAllEventsList()) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
    }


    /**
     * Get Organizer events by organizer id
     */
    public ArrayList<Event> getOrganizerEvents(String organizerID) {
        ArrayList<Event> organizerEvents = new ArrayList<>();
        for (Event event : repository.getAllEventsList()) {
            if (event.getOrganizerId().equals(organizerID)) {
                organizerEvents.add(event);
            }
        }

        return organizerEvents;
    }

    /**
     * Get list of events where Entrant is in waiting list
     */
    public ArrayList<Event> getEntrantWaitingListEvents(String entrantID) {
        ArrayList<Event> entrantWaitingList = new ArrayList<>();
        for (Event event : repository.getAllEventsList()) {
            if (event.getParticipants().contains(entrantID)) {
                entrantWaitingList.add(event);
            }
        }
        return entrantWaitingList;
    }

    /**
     * Get list of events where Entrant is in selected list
     */
    public ArrayList<Event> getEntrantSelectedListEvents(String entrantID) {
        ArrayList<Event> entrantSelectedList = new ArrayList<>();
        for (Event event : repository.getAllEventsList()) {
            if (event.getSelectedParticipants().contains(entrantID)) {
                entrantSelectedList.add(event);
            }
        }
        return entrantSelectedList;
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
     * Returns true if success, false otherwise
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
        repository.updateEventDetails(event, null, null); // Update in repository
        return true;
    }

    /**
     * Remove participant from event
     */
    public boolean removeParticipant(String eventId, String participantId) {
        Event event = getEventById(eventId);
        if (event == null) return false;

        event.removeParticipant(participantId);
        repository.updateEventDetails(event, null, null); // Update in repository
        return true;
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
    /*
    public ArrayList<String> getEventWinners(String eventId) {
        Event event = getEventById(eventId);
        if (event == null) return new ArrayList<>();
        return event.getSelectedParticipants();
    }*/

    /**
     * Check if user is registered for event
     */
    public boolean isUserRegistered(String eventId, String userId) {
        Event event = getEventById(eventId);
        if (event == null) return false;
        return event.getParticipants().contains(userId);
    }

    /**
     * This method creates and returns a bitmap for the event's qr code
     * @param eventID The id of the event to generate a bitmap for
     * @return The generated bitmap
     */
    private Bitmap generateQRCodeBitmap(String eventID) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(eventID, BarcodeFormat.QR_CODE, 300, 300);
            Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
