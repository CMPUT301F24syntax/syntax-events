package com.example.syntaxeventlottery;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

/**
 * The {@code EventController} class manages the business logic for handling {@link Event} objects
 * in the Event Lottery System. It interacts with the {@link EventRepository} to perform CRUD
 * operations and additional functionalities, such as managing participant lists and sending notifications.
 */
public class EventController {
    private EventRepository repository;

    /**
     * Constructs a new {@code EventController}.
     *
     * @param repository The {@link EventRepository} instance used for data operations.
     */
    public EventController(EventRepository repository) {
        this.repository = repository;
    }


    /**
     * Fetches the locations of all participants for a given event.
     *
     * @param eventID  The ID of the event.
     * @param callback The callback to handle the list of {@link LatLng} locations.
     */
    public void getAllParticipantLocations(String eventID, DataCallback<List<LatLng>> callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("events")
                .document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Combine participants, selectedParticipants, and confirmedParticipants into one list
                        List<String> allParticipants = new ArrayList<>();

                        // Add participants to the list
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants != null && !participants.isEmpty()) {
                            allParticipants.addAll(participants);
                        }

                        // Add selectedParticipants to the list
                        List<String> selectedParticipants = (List<String>) documentSnapshot.get("selectedParticipants");
                        if (selectedParticipants != null && !selectedParticipants.isEmpty()) {
                            allParticipants.addAll(selectedParticipants);
                        }

                        // Add confirmedParticipants to the list
                        List<String> confirmedParticipants = (List<String>) documentSnapshot.get("confirmedParticipants");
                        if (confirmedParticipants != null && !confirmedParticipants.isEmpty()) {
                            allParticipants.addAll(confirmedParticipants);
                        }

                        // Check if the combined list is not empty
                        if (!allParticipants.isEmpty()) {
                            // Fetch locations for all participants
                            fetchParticipantLocations(allParticipants, callback);
                        } else {
                            callback.onError(new Exception("No participants found for this event."));
                        }
                    } else {
                        callback.onError(new Exception("Event not found."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }



    /**
     * Helper method to fetch participant locations based on their device IDs.
     *
     * @param participants List of participant device IDs.
     * @param callback     The callback to handle the list of LatLng locations.
     */
    private void fetchParticipantLocations(List<String> participants, DataCallback<List<LatLng>> callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<LatLng> participantLocations = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(participants.size());

        for (String deviceID : participants) {
            firestore.collection("Users")
                    .document(deviceID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the location field for the participant
                            List<Double> location = (List<Double>) documentSnapshot.get("location");
                            if (location != null && location.size() == 2) {
                                double latitude = location.get(0);
                                double longitude = location.get(1);
                                LatLng latLng = new LatLng(latitude, longitude);
                                participantLocations.add(latLng);
                            } else {
                                Log.d(TAG, "Location not found for user: " + deviceID);
                            }
                        } else {
                            Log.d(TAG, "User not found for deviceID: " + deviceID);
                        }
                        if (remaining.decrementAndGet() == 0) {
                            callback.onSuccess(participantLocations);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load user data for deviceID: " + deviceID, e);
                        if (remaining.decrementAndGet() == 0) {
                            callback.onSuccess(participantLocations);
                        }
                    });
        }
    }



    public ArrayList<Event> getLocalEventsList() {
        return repository.getLocalEventsList();
    }

    /**
     * Refreshes the local repository of events.
     *
     * @param callback The callback to handle the refresh result.
     */
    public void refreshRepository(DataCallback<Void> callback) {
        repository.updateLocalEventsList(callback);
    }

    /**
     * Adds a new event to the repository, including uploading an optional image and generating a QR code.
     *
     * @param event      The {@link Event} object to add.
     * @param imageUri   The {@link Uri} of the event poster image (optional).
     * @param callback   The callback to handle the operation result.
     */
    public void addEvent(Event event, @Nullable Uri imageUri, DataCallback<Event> callback) {
        if (!validateEvent(event, callback)) {
            return;
        }
        event.generateEventID(event.getOrganizerId());
        Bitmap qrCodeBitmap = generateQRCodeBitmap(event.getEventID());
        repository.addEventToRepo(event, imageUri, qrCodeBitmap, callback);
    }

    /**
     * Updates the details of an existing event in the repository.
     *
     * @param event          The {@link Event} object with updated details.
     * @param imageUri       The {@link Uri} of the new event poster image (optional).
     * @param qrCodeBitmap   The updated QR code as a {@link Bitmap} (optional).
     * @param callback       The callback to handle the operation result.
     */
    public void updateEvent(Event event, @Nullable Uri imageUri,
                            @Nullable Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        if (!validateEvent(event, callback)) {
            return;
        }
        repository.updateEventDetails(event, imageUri, qrCodeBitmap, callback);
    }


    /**
     * Deletes an event from the repository.
     *
     * @param event    The {@link Event} object to delete.
     * @param callback The callback to handle the operation result.
     */
    public void deleteEvent(Event event, DataCallback<Void> callback) {
        repository.deleteEventFromRepo(event, callback);
    }

    /**
     * Retrieves an {@link Event} by its unique ID.
     *
     * @param eventId The unique identifier of the event to retrieve.
     * @return The {@link Event} object matching the specified ID, or {@code null} if not found.
     */
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


    /**
     * Retrieves a list of events organized by a specific user.
     *
     * @param organizerID The ID of the organizer.
     * @return A list of {@link Event} objects organized by the specified user.
     */
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
     * Adds a user to the waiting list of a specified event.
     * Validates the input, checks for duplicate participation, and ensures the waiting list is not full.
     *
     * @param event    The {@link Event} object representing the event to which the user is being added.
     * @param userID   The unique ID of the user being added to the waiting list.
     * @param callback The {@link DataCallback} to handle the result of the operation.
     */
    public void addUserToWaitingList(Event event, String userID, DataCallback<Event> callback) {
        // Validate inputs
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Failed to retrieve event data, try again later"));
            return;
        }

        ArrayList<String> participants = event.getParticipants();
        if (participants.contains(userID)) {
            callback.onError(new IllegalArgumentException("Failed to join waiting list: You are already a participant"));
            return;
        }

        // Check if a waiting list limit was set
        // If true check that waiting list is not full
        if (event.getWaitingListLimit() != null && participants.size() >= event.getWaitingListLimit()) {
            callback.onError(new IllegalArgumentException("Failed to join waiting list: No spots available"));
            return;
        }

        // Update participants list and save
        participants.add(userID);
        event.setParticipants(participants);
        updateEvent(event, null, null, callback);
    }

    /**
     * Removes a user from all relevant lists in the specified event.
     * This method is invoked when a user opts to leave the event or is removed.
     *
     * @param event    The {@link Event} object from which the user is being removed.
     * @param userID   The unique ID of the user being removed.
     * @param callback The {@link DataCallback} to handle the result of the operation.
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

    /**
     * Sets the specified user as a cancelled entrant in the event.
     *
     * @param event   The {@link Event} object containing the user's participation.
     * @param userID  The unique ID of the user to cancel.
     * @param callback The {@link DataCallback} to handle the result of the operation.
     */
    public void setUserCancelled(Event event, String userID, DataCallback<Event> callback) {
        // Validate inputs
        if (event == null || userID == null || userID.isEmpty()) {
            callback.onError(new IllegalArgumentException("Invalid event or user ID"));
            return;
        }

        // return if user has already been cancelled
        if (event.getCancelledParticipants().contains(userID)) {
            callback.onError(new IllegalArgumentException("This user has already been set to cancelled"));
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

    /**
     * Adds a user to the confirmed list for the event.
     *
     * @param event   The {@link Event} object containing the user.
     * @param userID  The unique ID of the user to confirm.
     * @param callback The {@link DataCallback} to handle the result of the operation.
     */
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
            callback.onError(new IllegalArgumentException("Failed to enroll: You were not selected by the draw"));
            return;
        }

        // check that user was not already confirmed for the event
        ArrayList<String> confirmedParticipants = event.getConfirmedParticipants();

        if (event.getConfirmedParticipants().contains(userID)) {
            callback.onError(new IllegalArgumentException("Failed to enroll: You have already been confirmed"));
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

    /**
     * Performs a draw on the waiting list of the event, selecting participants.
     *
     * @param event   The {@link Event} object for which the draw is being performed.
     * @param context The {@link Context} of the calling activity.
     * @param callback The {@link DataCallback} to handle the result of the operation.
     */
    public void performDraw(Event event, Context context, DataCallback<Event> callback) {
        if (event.isDrawed()) {
            callback.onError(new IllegalArgumentException("Event draw has already been performed"));
            return;
        }

        if (event.getParticipants().isEmpty()) {
            callback.onError(new IllegalArgumentException("Cannot Draw: No users in the waiting list"));
            return;
        }

        ArrayList<String> selectedList = new ArrayList<>();
        ArrayList<String> participants = event.getParticipants();
        ArrayList<String> updatedWaitingList = new ArrayList<>(participants);
        int capacity = event.getCapacity();

        if (participants.size() <= capacity) {
            // add all users to the waiting list, if the waiting list is less than capacity
            selectedList.addAll(participants);
            updatedWaitingList.clear();
        } else {
            // perform the draw
            Collections.shuffle(participants);
            List<String> chosenParticipants = participants.subList(0, capacity);
            selectedList.addAll(chosenParticipants);
            // remove selected list from the participants
            updatedWaitingList.removeAll(chosenParticipants);
        }

        // Update the event's selected participants and waiting list
        event.setSelectedParticipants(selectedList);
        event.setParticipants(updatedWaitingList);
        event.setDrawed(true);

        // Send notifications to participants
        sendLotteryResultNotifications(event, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error queuing notifications", e);
            }
        });

        // Save the event with the updated information
        updateEvent(event, null, null, callback);
    }

    /**
     * Redraws participants from the waiting list in the event of cancellations.
     *
     * @param event   The {@link Event} object for which the redraw is being performed.
     * @param context The {@link Context} of the calling activity.
     * @param callback The {@link DataCallback} to handle the result of the operation.
     */
    public void performRedraw(Event event, Context context, DataCallback<Event> callback) {
        // Check that the initial draw has occurred
        if (!event.isDrawed()) {
            callback.onError(new IllegalArgumentException("Cannot redraw since the initial draw has not occurred"));
            return;
        }

        if (event.getParticipants().isEmpty()) {
            callback.onError(new IllegalArgumentException("Cannot redraw: No users in the waiting list"));
            return;
        }

        ArrayList<String> participants = event.getParticipants(); // Waiting list
        ArrayList<String> selectedList = new ArrayList<>(event.getSelectedParticipants()); // Clone the selected list
        int confirmedCount = event.getConfirmedParticipants().size();
        int capacity = event.getCapacity();

        // Calculate remaining spots in the selected list
        int remainingSpots = capacity - (selectedList.size() + confirmedCount);

        if (remainingSpots <= 0) {
            callback.onError(new IllegalArgumentException("Cannot redraw: No spots available"));
            return;
        }

        // If there is less participants than remaining spots, add all of them
        if (participants.size() <= remainingSpots) {
            selectedList.addAll(participants);
            participants.clear();
        } else {
            // Shuffle the waiting list to randomize the selection
            Collections.shuffle(participants);
            // Select a random subset of participants for the remaining spots
            List<String> newSelections = participants.subList(0, remainingSpots);
            selectedList.addAll(newSelections);
            // Remove the newly selected participants from the waiting list
            participants.removeAll(newSelections);
        }

        // Update the event with the new selected and waiting list
        event.setSelectedParticipants(selectedList);
        event.setParticipants(participants);

        // Send notifications to participants
        sendLotteryResultNotifications(event, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error queuing notifications", e);
            }
        });

        // Save the updated event
        updateEvent(event, null, null, callback);
    }

    /**
     * Sends lottery result notifications by adding them to the database.
     */
    public void sendLotteryResultNotifications(Event event, DataCallback<Void> callback) {
        UserController userController = new UserController(new UserRepository());
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Prepare lists
                final List<String> selectedUsers = new ArrayList<>(event.getSelectedParticipants());
                final List<String> notSelectedUsers = new ArrayList<>(event.getParticipants());

                final String selectedMessage = "You've been selected for the event: " + event.getEventName();
                final String notSelectedMessage = "You were not selected for the event: " + event.getEventName();
                final String eventId = event.getEventID();

                // Add notifications for selected users
                addNotificationsToDatabase(selectedUsers, selectedMessage, eventId, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Notifications for selected users added successfully.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error adding notifications for selected users.", e);
                    }
                });

                // Add notifications for not selected users
                addNotificationsToDatabase(notSelectedUsers, notSelectedMessage, eventId, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Notifications for not selected users added successfully.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error adding notifications for not selected users.", e);
                    }
                });

                // Notify the original callback that the process is complete
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user repository while sending notifications.", e);
                callback.onError(e);
            }
        });
    }

    /**
     * Notify all selected entrant but not accept event to accept accept the event.
     */
    public void notifyAcceptInvitation(Event event, DataCallback<Void> callback) {
        UserController userController = new UserController(new UserRepository());
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Prepare lists
                final List<String> selectedUsers = new ArrayList<>(event.getSelectedParticipants());

                final String selectedMessage = "You've been selected for the event: " + event.getEventName() + "\nPlease accept the event as soon as possible.";
                final String eventId = event.getEventID();

                // Add notifications for selected users
                addNotificationsToDatabase(selectedUsers, selectedMessage, eventId, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Notifications for selected users added successfully.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error adding notifications for selected users.", e);
                    }
                });

                // Notify the original callback that the process is complete
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user repository while sending notifications.", e);
                callback.onError(e);
            }
        });
    }

    /**
     * Adds notifications to the database for a list of users.
     */
    public void addNotificationsToDatabase(List<String> userIds, String message, String eventId, DataCallback<Void> callback) {
        NotificationRepository notificationRepository = new NotificationRepository();
        notificationRepository.addNotifications(userIds, message, eventId, callback);
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
        return !event.getSelectedParticipants().isEmpty() && event.getSelectedParticipants().contains(userID);
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
        return !event.getConfirmedParticipants().isEmpty() && event.getConfirmedParticipants().contains(userID);
    }

    public ArrayList<String> getEventConfirmedList(Event event) {
        return event.getConfirmedParticipants();
    }

    public boolean isUserInCancelledList(Event event, String userID) {
        return !event.getCancelledParticipants().isEmpty() && event.getCancelledParticipants().contains(userID);
    }

    public ArrayList<String> getEventCancelledList(Event event) {
        return event.getCancelledParticipants();
    }

    public ArrayList<Event> getUserWaitlistedEvents(String userId) {
        ArrayList<Event> events = getLocalEventsList();
        ArrayList<Event> userWaitlistedEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getParticipants().contains(userId)) {
                userWaitlistedEvents.add(event);
            }
        }
        return userWaitlistedEvents;
    }

    public ArrayList<Event> getUserSelectedEvents(String userId) {
        ArrayList<Event> events = getLocalEventsList();
        ArrayList<Event> userSelectedListEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getSelectedParticipants().contains(userId)) {
                userSelectedListEvents.add(event);
            }
        }
        return userSelectedListEvents;
    }

    public ArrayList<Event> getUserEnrolledEvents(String userId) {
        ArrayList<Event> events = getLocalEventsList();
        ArrayList<Event> userEnrolledListEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getConfirmedParticipants().contains(userId)) {
                userEnrolledListEvents.add(event);
            }
        }
        return userEnrolledListEvents;
    }


    public ArrayList<Event> getUserCancelledEvents(String userId) {
        ArrayList<Event> events = getLocalEventsList();
        ArrayList<Event> userCancelledListEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getCancelledParticipants().contains(userId)) {
                userCancelledListEvents.add(event);
            }
        }
        return userCancelledListEvents;
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
        if (event.getWaitingListLimit() != null)
            if (event.getWaitingListLimit() < event.getCapacity()) {
            callback.onError(new IllegalArgumentException("Waiting list limit cannot be smaller than the capacity"));
        }
        if (event.getConfirmedParticipants() != null) {
            if (event.getCapacity() < event.getConfirmedParticipants().size()) {
                callback.onError(new IllegalArgumentException(event.getConfirmedParticipants().size() + " entrants confirmed, capacity cannot be reduced smaller"));
            }
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

    public void sendNotificationsToGroup(Event event, String group, String message, EventDetailActivity eventDetailActivity, DataCallback<Void> callback) {
        if (message == null || message.isEmpty()) {
            message = getDefaultMessage(group, event.getEventName());
        }

        UserController userController = new UserController(new UserRepository());
        String finalMessage = message;
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<String> userIds;

                switch (group) {
                    case "waitingList":
                        userIds = event.getParticipants();
                        break;
                    case "selectedParticipants":
                        userIds = event.getSelectedParticipants();
                        break;
                    case "cancelledParticipants":
                        userIds = event.getCancelledParticipants();
                        break;
                    default:
                        callback.onError(new Exception("Invalid group specified"));
                        return;
                }

                addNotificationsToDatabase(userIds, finalMessage, event.getEventID(), new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error adding notifications to database", e);
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error sending notifications to group", e);
                callback.onError(e);
            }
        });
    }


    private String getDefaultMessage(String group, String eventName) {
        switch (group) {
            case "waitingList":
                return "You are on the waiting list for " + eventName + ". Stay tuned for updates!";
            case "selectedParticipants":
                return "You have been selected for " + eventName + "! Please confirm your participation.";
            case "cancelledParticipants":
                return "Your participation in " + eventName + " has been cancelled.";
            default:
                return "Notification regarding " + eventName;
        }
    }


}