package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class EventController {
    private EventRepositoryInterface repository;

    public EventController(EventRepositoryInterface repository) {
        this.repository = repository;
    }

    /**
     * Get all events from the repository
     */
    public ArrayList<Event> getAllEvents() {
        return repository.getAllEventsList();
    }

    /**
     * Add a new event with validation and QR code generation
     */
    public void addEvent(Event event, @Nullable Uri imageUri, Bitmap qrCodeBitmap) {
        // Generate event ID
        event.generateEventID(event.getOrganizerId());
        // Validate event data
        validateEvent(event);
        // Add to repository
        repository.addEventToRepo(event, imageUri, qrCodeBitmap);
    }

    /**
     * Overloaded addEvent method that generates QR code internally
     */
    public void addEvent(Event event, @Nullable Uri imageUri) {
        Bitmap qrCodeBitmap = generateQRCodeBitmap(event.getEventID());
        if (qrCodeBitmap != null) {
            addEvent(event, imageUri, qrCodeBitmap);
        } else {
            // Handle QR code generation failure
            // For example, you might throw an exception or notify the user
        }
    }

    /**
     * Update an existing event
     */
    public void updateEvent(Event event, @Nullable Uri imageUri, @Nullable Bitmap qrCodeBitmap) {
        repository.updateEventDetails(event, imageUri, qrCodeBitmap);
    }

    /**
     * Get event by ID
     */
    public Event getEventById(String eventId) {
        return repository.getEventById(eventId);
    }

    /**
     * Get Organizer events by organizer id
     */
    public List<Event> getOrganizerEvents(String organizerId) {
        return repository.getOrganizerEvents(organizerId);
    }

    /**
     * Get list of events where Entrant is in waiting list
     */
    public List<Event> getEntrantWaitingListEvents(String entrantId) {
        return repository.getEntrantWaitingListEvents(entrantId);
    }

    /**
     * Get list of events where Entrant is in selected list
     */
    public List<Event> getEntrantSelectedListEvents(String entrantId) {
        return repository.getEntrantSelectedListEvents(entrantId);
    }

    /**
     * Check if event is full
     */
    public boolean isEventFull(String eventId) {
        return repository.isEventFull(eventId);
    }

    /**
     * Add participant to event
     * Returns true if success, false otherwise
     */
    public boolean addParticipant(String eventId, String participantId) {
        if (isEventFull(eventId)) {
            return false;
        }
        if (repository.isUserRegistered(eventId, participantId)) {
            return false;
        }
        repository.addParticipant(eventId, participantId);
        return true;
    }

    /**
     * Remove participant from event
     */
    public boolean removeParticipant(String eventId, String participantId) {
        if (!repository.isUserRegistered(eventId, participantId)) {
            return false;
        }
        repository.removeParticipant(eventId, participantId);
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
     * Generates a QR Code bitmap for the given event ID.
     *
     * @param eventId The ID of the event.
     * @return The generated QR Code bitmap.
     */
    public Bitmap generateQRCodeBitmap(String eventId) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 300, 300);
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

    /**
     * Accept an event invitation
     */
    public void acceptInvitation(String eventId, String userId) {
        repository.acceptInvitation(eventId, userId);
    }

    /**
     * Reject an event invitation
     */
    public void rejectInvitation(String eventId, String userId) {
        repository.rejectInvitation(eventId, userId);
    }

    /**
     * Perform draw for an event
     */
    public void performDraw(String eventId) {
        repository.performDraw(eventId);
    }

    /**
     * Check if user is registered for event
     */
    public boolean isUserRegistered(String eventId, String userId) {
        return repository.isUserRegistered(eventId, userId);
    }
}