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
        List<Event> events = repository.getLocalEventsList();
        for (Event event : events) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
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
     * User methods
     */
    // adds user to list of participants
    public void joinWaitingList(Event event, String userID) {
        if (event != null) {
            ArrayList<String> currentParticipants = event.getParticipants();
            if (currentParticipants.)
        }
    }

    public void leaveWaitingList(Event event, String userID) {
        if (event != null) {
            event.removeParticipant(userID);
        }
    }

    public void acceptInvitation(Event event, String userID) {
        if (event.getParticipants().contains(userID) && !event.getSelectedParticipants().contains(userID)) {
            ArrayList<String> currentSelectedParticipants = event.getSelectedParticipants();
            currentSelectedParticipants.add(userID);
            event.setSelectedParticipants(currentSelectedParticipants);
        }
    }

    public void
}