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
    private EventRepository repository;

    public EventController(EventRepository repository) {
        this.repository = repository;
    }

    public ArrayList<Event> getLocalEventsList() {
        return repository.getLocalEventsList();
    }

    public void refreshRepository(DataCallback<Void> callback) {
        repository.updateLocalEventsList(callback);
    }

    public void addEvent(Event event, @Nullable Uri imageUri, DataCallback<Event> callback) {
        if (!validateEvent(event, callback)) {
            return;
        }
        event.generateEventID(event.getOrganizerId());
        Bitmap qrCodeBitmap = generateQRCodeBitmap(event.getEventID());
        repository.addEventToRepo(event, imageUri, qrCodeBitmap, callback);
    }

    public void updateEvent(Event event, @Nullable Uri imageUri,
                            @Nullable Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        if (!validateEvent(event, callback)) {
            return;
        }
        repository.updateEventDetails(event, imageUri, qrCodeBitmap, callback);
    }

    public void deleteEvent(Event event, DataCallback<Void> callback) {
        repository.deleteEventFromRepo(event, callback);
    }

    // Synchronous method to get event by ID from local cache
    public Event getEventById(String eventId) {
        // Check if eventId is null
        if (eventId == null) {
            return null;
        }
        ArrayList<Event> events = getLocalEventsList();
        for (Event event : events) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        // return null if no matching event found
        return null;
    }

    // get all organizer events
    public ArrayList<Event> getOrganizerEvents(String organizerID) {
        if (organizerID == null || organizerID.isEmpty()) {
            return null; 
        }
        // get all events where the passed id is the event's organizer
        ArrayList<Event> organizerEvents = new ArrayList<>();
        for (Event event : repository.getLocalEventsList()) {
            if (event.getOrganizerId().equals(organizerID)) {
                organizerEvents.add(event);
            }
        }
        return organizerEvents;
    }
    
    /**
     * User methods
     */
    // removes user id from participants list
    // and updates the repository
    /**
     * Removes a user from an event's waiting list
     */
    public void removeUserFromWaitingList(Event event, String userID, DataCallback<Event> callback) {
        // Validate inputs
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        ArrayList<String> participants = event.getParticipants();
        if (!participants.contains(userID)) {
            callback.onError(new IllegalArgumentException("User not found in events list"));
            return;
        }

        // Update participants list and save
        participants.remove(userID);
        event.setParticipants(participants);
        updateEvent(event, null, null, callback);
    }

    // adds user id to participants list
    // and updates repository
    /**
     * Adds a user to an event's waiting list
     */
    public void addUserToWaitingList(Event event, String userID, DataCallback<Event> callback) {
        // Validate inputs
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        ArrayList<String> participants = event.getParticipants();
        if (participants.contains(userID)) {
            callback.onError(new IllegalArgumentException("User is already a participant"));
            return;
        }

        // Check waiting list limit
        if (event.getWaitingListLimit() != null &&
                participants.size() >= event.getWaitingListLimit()) {
            callback.onError(new IllegalArgumentException("Waiting list capacity is reached"));
            return;
        }

        // Update participants list and save
        participants.add(userID);
        event.setParticipants(participants);
        updateEvent(event, null, null, callback);
    }


    // lottery implementation

    public void acceptInvitation(Event event, String userID) {
        if (event.getParticipants().contains(userID) && !event.getSelectedParticipants().contains(userID)) {
            ArrayList<String> currentSelectedParticipants = event.getSelectedParticipants();
            currentSelectedParticipants.add(userID);
            event.setSelectedParticipants(currentSelectedParticipants);
        }
    }


    //------------ event object helper methods -----------//
    /**
     * Validate event data and report errors through callback
     * @return true if validation passed, false if there were errors
     */
    private boolean validateEvent(Event event, DataCallback<?> callback) {
        if (event == null) {
            callback.onError(new IllegalArgumentException("Event cannot be null"));
            return false;
        }
        if (event.getEventName() == null || event.getEventName().trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Event name cannot be empty"));
            return false;
        }
        if (event.getCapacity() <= 0) {
            callback.onError(new IllegalArgumentException("Event capacity must be greater than 0"));
            return false;
        }
        if (event.getStartDate() == null || event.getEndDate() == null) {
            callback.onError(new IllegalArgumentException("Event dates cannot be null"));
            return false;
        }
        if (event.getStartDate().after(event.getEndDate())) {
            callback.onError(new IllegalArgumentException("Start date cannot be after end date"));
            return false;
        }
        if (event.getStartDate().before(new Date())) {
            callback.onError(new IllegalArgumentException("Start date cannot be in the past"));
            return false;
        }
        return true;
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

}