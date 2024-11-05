package com.example.syntaxeventlottery;

import android.net.Uri;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
        this.eventsDataList = new ArrayList<>();
    }

    public void updateEvents() {
        eventsRef.addSnapshotListener((querySnapshots, error) -> {
            if (error != null) {
                Log.e("Firestore Events", error.toString());
                return;
            }
            if (querySnapshots != null) {
                eventsDataList.clear();
                for (QueryDocumentSnapshot doc : querySnapshots) {
                    Event event = doc.toObject(Event.class);
                    eventsDataList.add(event);
                }
            }
        });
    }

    public void uploadImageToStorage(Uri imageUri, String path, OnCompleteListener<Uri> onCompleteListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);
        storageRef.putFile(imageUri)
                .continueWithTask(task -> storageRef.getDownloadUrl())
                .addOnCompleteListener(onCompleteListener);
    }

    public void addEventToRepo(Event event, Uri posterUri, Uri qrCodeUri) {
        uploadImageToStorage(posterUri, "posters/" + event.getEventID(), posterTask -> {
            if (posterTask.isSuccessful()) {
                event.setPosterUrl(posterTask.getResult().toString());

                uploadImageToStorage(qrCodeUri, "qrcodes/" + event.getEventID(), qrTask -> {
                    if (qrTask.isSuccessful()) {
                        event.setQrCodeUrl(qrTask.getResult().toString());
                        saveEventToFirestore(event);
                    } else {
                        Log.e("Storage Upload", "QR code upload failed", qrTask.getException());
                    }
                });
            } else {
                Log.e("Storage Upload", "Poster upload failed", posterTask.getException());
            }
        });
    }

    private void saveEventToFirestore(Event event) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("eventID", event.getEventID());
        data.put("posterUrl", event.getPosterUrl());
        data.put("qrCodeUrl", event.getQrCodeUrl());
        data.put("organizerId", event.getOrganizerId());
        data.put("facility", event.getFacility());
        data.put("startDate", event.getStartDate());
        data.put("endDate", event.getEndDate());
        data.put("capacity", event.getCapacity());
        data.put("description", event.getDescription());

        eventsRef.document(event.getEventID()).set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore Events", "Event added successfully"))
                .addOnFailureListener(e -> Log.e("Firestore Events", "Error adding event", e));
    }
}
