package com.example.syntaxeventlottery;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

/**
 * The {@code EventRepository} class provides methods to interact with Firebase Firestore
 * and Firebase Storage for event-related operations. It handles uploading images,
 * adding events to the repository, and updating the list of events.
 */
public class EventRepository {
    /** Firebase Firestore database instance. */
    private FirebaseFirestore db;

    /** Reference to the 'Events' collection in Firestore. */
    private CollectionReference eventsRef;

    /** List containing event data retrieved from Firestore. */
    private ArrayList<Event> eventsDataList;

    /**
     * Constructs a new {@code EventRepository} and initializes Firestore references.
     */
    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("Events");
        this.eventsDataList = new ArrayList<>();
    }

    /**
     * Updates the list of events by listening to changes in the Firestore 'Events' collection.
     * The method sets up a snapshot listener that updates the {@code eventsDataList}
     * whenever the data changes.
     */
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

    /**
     * Uploads an image to Firebase Storage at the specified path and triggers a callback upon completion.
     *
     * @param imageUri           The URI of the image to upload.
     * @param path               The storage path where the image will be uploaded.
     * @param onCompleteListener The listener to be called upon completion of the upload.
     */
    public void uploadImageToStorage(Uri imageUri, String path, OnCompleteListener<Uri> onCompleteListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);
        storageRef.putFile(imageUri)
                .continueWithTask(task -> storageRef.getDownloadUrl())
                .addOnCompleteListener(onCompleteListener);
    }

    /**
     * Adds an event to the repository by uploading the poster and QR code images,
     * then saving the event data to Firestore.
     *
     * @param event      The {@code Event} object containing event details.
     * @param posterUri  The URI of the event poster image.
     * @param qrCodeUri  The URI of the event QR code image.
     */
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

    /**
     * Saves the event data to Firestore under the 'Events' collection.
     *
     * @param event The {@code Event} object containing the event data to be saved.
     */
    private void saveEventToFirestore(Event event) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("eventID", event.getEventID());
        data.put("posterUrl", event.getPosterUrl());
        data.put("qrCodeUrl", event.getQrCodeUrl());
        data.put("organizerId", event.getOrganizerId());
        data.put("startDate", event.getStartDate());
        data.put("endDate", event.getEndDate());
        data.put("capacity", event.getCapacity());
        data.put("description", event.getDescription());

        eventsRef.document(event.getEventID()).set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore Events", "Event added successfully"))
                .addOnFailureListener(e -> Log.e("Firestore Events", "Error adding event", e));
    }
}
