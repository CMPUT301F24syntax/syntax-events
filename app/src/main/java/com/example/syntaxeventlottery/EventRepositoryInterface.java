package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public interface EventRepositoryInterface {
    ArrayList<Event> getAllEventsList();
    void addEventToRepo(Event event, Uri imageUri, Bitmap qrCodeBitmap);
    void deleteEventFromRepo(Event event);
    void updateEventDetails(Event event, @Nullable Uri imageUri, @Nullable Bitmap qrCodeBitmap);

    // Event to HashMap
    HashMap<String, Object> eventToHashData(Event event);

    // New Methods
    Event getEventById(String eventId);
    List<String> getChosenList(String eventId);
    List<String> getUnChosenList(String eventId);
    void performDraw(String eventId);
    void addParticipant(String eventId, String participantId);
    void removeParticipant(String eventId, String participantId);
    void acceptInvitation(String eventId, String userId);
    void rejectInvitation(String eventId, String userId);
    List<Event> getOrganizerEvents(String organizerId);
    List<Event> getEntrantWaitingListEvents(String entrantId);
    List<Event> getEntrantSelectedListEvents(String entrantId);
    boolean isEventFull(String eventId);
    boolean isUserRegistered(String eventId, String userId);
}