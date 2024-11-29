package com.example.syntaxeventlottery;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
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


    //------------- Event participant lists methods --------------//
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

        // Check if a waiting list limit was set
        // If true check that waiting list is not full
        if (event.getWaitingListLimit() != null && participants.size() >= event.getWaitingListLimit()) {
            callback.onError(new IllegalArgumentException("Waiting list capacity is reached"));
            return;
        }

        // Update participants list and save
        participants.add(userID);
        event.setParticipants(participants);
        updateEvent(event, null, null, callback);
    }

    /**
     * Removes a user from a associated event data
     * This is called when a user leaves the waiting list
     */
    public void removeUserFromEvent(Event event, String userID, DataCallback<Event> callback) {
        // Validate inputs
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        // Get the event lists, initialize them if they are null
        ArrayList<String> participants = event.getParticipants();
        if (participants == null) {
            participants = new ArrayList<>();
        }

        ArrayList<String> selectedParticipants = event.getSelectedParticipants();
        if (selectedParticipants == null) {
            selectedParticipants = new ArrayList<>();
        }

        ArrayList<String> confirmedParticipants = event.getConfirmedParticipants();
        if (confirmedParticipants == null) {
            confirmedParticipants = new ArrayList<>();
        }

        // Remove user from the lists (if present)
        boolean removedFromParticipants = participants.remove(userID);
        boolean removedFromSelected = selectedParticipants.remove(userID);
        boolean removedFromConfirmed = confirmedParticipants.remove(userID);

        // If the user wasn't removed from any list, you may want to log that
        if (!removedFromParticipants && !removedFromSelected && !removedFromConfirmed) {
            callback.onError(new IllegalArgumentException("User ID not found in any event list"));
            return;
        }

        // Update participants list and save
        event.setParticipants(participants);
        event.setSelectedParticipants(selectedParticipants);
        event.setConfirmedParticipants(confirmedParticipants);
        updateEvent(event, null, null, callback);
    }

    // sets the specified user as a cancelled entrant in the event object
    public void setUserCancelled(Event event, String userID, DataCallback<Event> callback) {
        // Validate inputs
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        // return if user has already been cancelled
        if (event.getCancelledParticipants().contains(userID)) {
            callback.onError(new IllegalArgumentException("User has already been set to cancelled"));
        }

        if (event.getParticipants().contains(userID)) {
            ArrayList<String> participants = event.getParticipants();
            participants.remove(userID);
            event.setParticipants(participants);
        }

        if (event.getSelectedParticipants().contains(userID)) {
            ArrayList<String> selectedParticipants = event.getSelectedParticipants();
            selectedParticipants.remove(userID);
            event.setSelectedParticipants(selectedParticipants);
        }

        if (event.getConfirmedParticipants().contains(userID)) {
            ArrayList<String> confirmedParticipants = event.getConfirmedParticipants();
            confirmedParticipants.remove(userID);
            event.setSelectedParticipants(confirmedParticipants);
        }

        // update event and repository data
        ArrayList<String> cancelledParticipants = event.getCancelledParticipants();
        cancelledParticipants.add(userID);
        event.setCancelledParticipants(cancelledParticipants);
        updateEvent(event, null, null, callback);
    }

    // called when a user accepts their invitation to the event
    public void addUserToConfirmedList(Event event, String userID, DataCallback<Event> callback) {
        // Validate parameters
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        // check that user is in the selected list
        // if true, remove them from selected list and add them to the confirmed list
        ArrayList<String> selectedParticipants = event.getSelectedParticipants();

        if (!selectedParticipants.contains(userID)) {
            callback.onError(new IllegalArgumentException("User was never selected by the draw"));
            return;
        }

        // check that user was not already confirmed for the event
        ArrayList<String> confirmedParticipants = event.getConfirmedParticipants();

        if (event.getConfirmedParticipants().contains(userID)) {
            callback.onError(new IllegalArgumentException("User has already been confirmed"));
            return;
        }

        // update the lists
        selectedParticipants.remove(userID);
        confirmedParticipants.add(userID);
        event.setSelectedParticipants(selectedParticipants);
        event.setConfirmedParticipants(confirmedParticipants);

        // update the repository
        updateEvent(event, null, null, callback);
    }

    // lottery implementation
    // perform a draw on the waiting list
    // if chosen, remove them from the waiting list and add them to the selected list
    public void performDraw(Event event, Context context, DataCallback<Event> callback) {
        if (event.isDrawed()) {
            callback.onError(new IllegalArgumentException("Event draw has already been performed"));
            sendLotteryResultNotifications(event, context);

            return;
        }

        ArrayList<String> selectedList = new ArrayList<>();
        ArrayList<String> participants = event.getParticipants();
        ArrayList<String> updatedWaitingList = new ArrayList<>(participants);
        int capacity = event.getCapacity();

        if (participants.size() <= capacity) {
            selectedList.addAll(participants);
            updatedWaitingList.clear();
        } else {
            Collections.shuffle(participants);
            List<String> chosenParticipants = participants.subList(0, capacity);
            selectedList.addAll(chosenParticipants);
            updatedWaitingList.removeAll(chosenParticipants);
        }

        // Update the event's selected participants and waiting list
        event.setSelectedParticipants(selectedList);
        event.setParticipants(updatedWaitingList);
        event.setDrawed(true);

        // Send notifications to participants
        sendLotteryResultNotifications(event, context);

        // Save the event with the updated information
        updateEvent(event, null, null, callback);
    }

    private void sendLotteryResultNotifications(Event event, Context context) {
        UserController userController = new UserController(new UserRepository());
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Notify selected participants (winners)n
                for (String userId : event.getSelectedParticipants()) {
                    User user = userController.getUserByDeviceID(userId);
                    if (user != null && user.isReceiveNotifications()) {
                        String title = "Congratulations!";
                        String message = "You've been selected for the event: " + event.getEventName();
                      
                        NotificationUtils.sendNotification(context, title, message, generateNotificationId(), event.getEventID());
                    }
                }
                // Notify participants who were not selected (non-winners)
                for (String userId : event.getParticipants()) {
                    User user = userController.getUserByDeviceID(userId);
                    if (user != null && user.isReceiveNotifications()) {
                        String title = "Lottery Result";
                        String message = "You were not selected for the event: " + event.getEventName();
                      
                        NotificationUtils.sendNotification(context, title, message, generateNotificationId(), event.getEventID());
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error sending notifications", e);
            }
        });
    }

    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Check if user is in the waiting list
     * @param event
     * @param userID
     */
    public boolean isUserInWaitingList(Event event, String userID) {
        return !event.getParticipants().isEmpty() && event.getParticipants().contains(userID);
    }

    public ArrayList<String> getEventWaitingList(Event event) {
        return event.getParticipants();
    }

    /**
     * Check if user is in the selected list
     * @param event
     * @param userID
     */
    public boolean isUserInSelectedList(Event event, String userID) {
        return !event.getParticipants().isEmpty() && event.getSelectedParticipants().contains(userID);
    }

    public ArrayList<String> getEventSelectedList(Event event) {
        return event.getSelectedParticipants();
    }


    /**
     * Check if user is in the selected list
     * @param event
     * @param userID
     */
    public boolean isUserInConfirmedList(Event event, String userID) {
        return !event.getParticipants().isEmpty() && event.getConfirmedParticipants().contains(userID);
    }

    public ArrayList<String> getEventConfirmedList(Event event) {
        return event.getConfirmedParticipants();
    }

    public boolean isUserInCancelledList(Event event, String userID) {
        return !event.getParticipants().isEmpty() && event.getCancelledParticipants().contains(userID);
    }

    public ArrayList<String> getEventCancelledList(Event event) {
        return event.getCancelledParticipants();
    }

    public void acceptInvitation(Event event, String userID) {
        if (event.getParticipants().contains(userID) && !event.getSelectedParticipants().contains(userID)) {
            ArrayList<String> currentSelectedParticipants = event.getSelectedParticipants();
            currentSelectedParticipants.add(userID);
            event.setSelectedParticipants(currentSelectedParticipants);
        }
    }
    public void getUserWaitlistedEvents(String userId, DataCallback<ArrayList<Event>> callback) {
        refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                ArrayList<Event> events = getLocalEventsList();
                ArrayList<Event> userWaitlistedEvents = new ArrayList<>();
                for (Event event : events) {
                    if (event.getParticipants().contains(userId)) {
                        userWaitlistedEvents.add(event);
                    }
                }
                callback.onSuccess(userWaitlistedEvents);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
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