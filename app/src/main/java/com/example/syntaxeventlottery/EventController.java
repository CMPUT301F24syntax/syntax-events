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
        try {
            validateEvent(event);
            event.generateEventID(event.getOrganizerId());
            Bitmap qrCodeBitmap = generateQRCodeBitmap(event.getEventID());
            repository.addEventToRepo(event, imageUri, qrCodeBitmap, callback);
        } catch (IllegalArgumentException e) {
            callback.onError(e);
        }
    }

    public void updateEvent(Event event, @Nullable Uri imageUri,
                            @Nullable Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        try {
            validateEvent(event);
            repository.updateEventDetails(event, imageUri, qrCodeBitmap, callback);
        } catch (IllegalArgumentException e) {
            callback.onError(e);
        }
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
    // adds user to list of participants
    public void addUserToEventParticipants(Event event, String userID, DataCallback<Event> callback) {
        if (event == null || userID == null || userID.isEmpty()) {
            // Early exit if input is invalid
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        // Add user to participants list
        ArrayList<String> currentParticipants = event.getParticipants();
        if (!currentParticipants.contains(userID)) {
            currentParticipants.add(userID);
            event.setParticipants(currentParticipants);

            // Now we need to update the repository with the updated event
            updateEvent(event, null, null, new DataCallback<Event>() {
                @Override
                public void onSuccess(Event updatedEvent) {
                    // Once the event update is successful, we call the original callback
                    callback.onSuccess(updatedEvent);  // Pass the updated event to the original callback
                }

                @Override
                public void onError(Exception e) {
                    // If the update fails, we propagate the error to the original callback
                    callback.onError(e);  // Pass error to the original callback
                }
            });
        } else {
            // If the user is already in the list, we just return
            callback.onError(new IllegalArgumentException("User is already a participant"));
        }
    }


    public void leaveWaitingList(Event event, String userID, DataCallback<Event> callback) {
        if (event == null || userID == null || userID.isEmpty()) {
            // Early exit if input is invalid
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        // Add user to participants list
        ArrayList<String> currentParticipants = event.getParticipants();
        if (!currentParticipants.contains(userID)) {
            currentParticipants.remove(userID);
            event.setParticipants(currentParticipants);

            // Now we need to update the repository with the updated event
            updateEvent(event, null, null, new DataCallback<Event>() {
                @Override
                public void onSuccess(Event updatedEvent) {
                    // Once the event update is successful, we call the original callback
                    callback.onSuccess(updatedEvent);  // Pass the updated event to the original callback
                }

                @Override
                public void onError(Exception e) {
                    // If the update fails, we propagate the error to the original callback
                    callback.onError(e);  // Pass error to the original callback
                }
            });
        } else {
            // If the user is already in the list, we just return
            callback.onError(new IllegalArgumentException("User is already a participant"));
        }
    }

    public void acceptInvitation(Event event, String userID) {
        if (event.getParticipants().contains(userID) && !event.getSelectedParticipants().contains(userID)) {
            ArrayList<String> currentSelectedParticipants = event.getSelectedParticipants();
            currentSelectedParticipants.add(userID);
            event.setSelectedParticipants(currentSelectedParticipants);
        }
    }


    //------------ event object helper methods -----------//
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

}