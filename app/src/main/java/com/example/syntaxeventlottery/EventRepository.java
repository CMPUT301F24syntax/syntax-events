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
    }

    // get real time updates of Events List, call everytime there is a change to the db
    public void updateEvents() {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore Events", error.toString());
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
        // add event to local list
        eventsDataList.add(event);
        HashMap<String, Users, >
    }

}
