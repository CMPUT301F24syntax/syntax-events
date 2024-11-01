package com.example.syntaxeventlottery;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class EventRepository {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private ArrayList<Event> eventsDataList; // local data list

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("Events");
        eventsDataList = new ArrayList<>();
    }


    public ArrayList<Event> getEventsDataList(){
        return eventsDataList;
    }

    // get real time updates of Events List, call everytime there is a change to the db
    public void updateEventsRepository() {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore Events Repository", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    eventsDataList.clear();
                    for (QueryDocumentSnapshot doc: querySnapshots) {
                        Event event = doc.toObject(Event.class); // convert document snapshot to Event object
                        eventsDataList.add(event);
                    }
                }
            }
        });
    }

    public void addEventToRepo(Event event) {
        // check that event is valid, if not log error and return
        if (!eventIsValid(event)) {
            Log.e("Firestore Events Repository", "Event validation failed");
            return;
        }
        // add event to local list
        eventsDataList.add(event);

        // add event to Firestore collection
        HashMap<String, Object> data = new HashMap<>();
        data.put("eventID", event.getEventID());
        data.put("qrCode", event.getQrCode());
        data.put("organizer", event.getOrganizer());
        data.put("facility", event.getFacility());
        data.put("startDate", event.getStartDate());
        data.put("endDate", event.getEndDate());
        data.put("capacity", event.getCapacity());
        data.put("isFull", event.getIsFull());
        data.put("isDrawn", event.getIsDrawn());
        data.put("waitingList", event.getWaitingList());
        data.put("selectedList", event.getSelectedList());
        data.put("poster", event.getPoster());
        // log a message on success or failure
        eventsRef.document(event.getEventID()).set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore Events Repository", "Event added successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Events Repository", "Error adding Event", e);
                })
                .addOnCompleteListener(task -> {
                    // call updateEvents() for real time updates
                    updateEventsRepository();
                });
    }

    public void deleteEventFromRepo(Event event) {
        // remove from local list
        eventsDataList.remove(event);

        // remove from database
        eventsRef.document(event.getEventID()).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore Events Repository", "Event deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Events Repository", "Error deleting event",e);
                })
                .addOnCompleteListener(task -> {
                    // call updateEvents() for real time updates
                    updateEventsRepository();
                });
    }

    public void updateEventDetails(Event updatedEvent) {
        // check that event is valid
        if (!eventIsValid(updatedEvent)) {
            Log.e("Firestore Events Repository", "Event with updated details is not valid");
            return;
        }

        // find and update event in the local data list
        Event eventFoundInList = null;
        for (int i = 0; i < eventsDataList.size(); i++ ) {
            if (eventsDataList.get(i).getEventID() != null && eventsDataList.get(i).getEventID().equals(updatedEvent.getEventID())) {
                eventsDataList.set(i, updatedEvent); // update event in local data list
                eventFoundInList = eventsDataList.get(i);
                break; // break loop so extra iterations do not occur
            }
        }

        // if event is not found in data list
        if (eventFoundInList == null) {
            Log.e("Firestore Events Repository", "Event could not be found in data list");
            return;
        }

        // create hashmap with new event details
        HashMap<String,Object> data = new HashMap<>();
        data.put("eventID", updatedEvent.getEventID());
        data.put("qrCode", updatedEvent.getQrCode());
        data.put("organizer", updatedEvent.getOrganizer());
        data.put("facility", updatedEvent.getFacility());
        data.put("startDate", updatedEvent.getStartDate());
        data.put("endDate", updatedEvent.getEndDate());
        data.put("capacity", updatedEvent.getCapacity());
        data.put("isFull", updatedEvent.getIsFull());
        data.put("isDrawn", updatedEvent.getIsDrawn());
        data.put("waitingList", updatedEvent.getWaitingList());
        data.put("selectedList", updatedEvent.getSelectedList());
        data.put("poster", updatedEvent.getPoster());

        // update firestore
        eventsRef.document(eventFoundInList.getEventID()).set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore Events Repository", "Event updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Events Repository", "Error updating event",e);
                })
                .addOnCompleteListener(task -> {
                    // call updateEvents() for real time updates
                    updateEventsRepository();
                });

    }

    public boolean eventIsValid(Event event) {
        // event validation
        if (event.getEventID() == null || event.getFacility() == null || event.getQrCode() == null
        || event.getOrganizer() == null || event.getStartDate() == null || event.getEndDate() == null || event.getPoster() == null) {
            Log.e("Firestore Event validation","Missing Event fields");
            return false;
        }
        if (event.getStartDate().after(event.getEndDate())) {
            Log.e("Firestore Event validation","Event start date is after end date");
            return false;
        }
        if (event.getIsFull() || event.getIsDrawn()) {
            Log.e("Firestore Event validation","Event cannot be full or drawn on creation");
            return false;
        }
        if (event.getCapacity() <= 0) {
            Log.e("Firestore Event validation","Event capacity must be greater than 0");
            return false;
        }
        // return true if no errors validating
        return true;
    }
}
