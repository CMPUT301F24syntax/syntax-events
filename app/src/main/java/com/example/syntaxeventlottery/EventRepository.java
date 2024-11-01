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
    private ArrayList<Event> eventsDataList;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("Events");
        eventsDataList = new ArrayList<>();
    }

    // get real time updates of Events List, call everytime there is a change to the db
    public void updateEvents() {
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
                    updateEvents();
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
                    updateEvents();
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
        if (event.isFull() || event.isDrawn()) {
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
