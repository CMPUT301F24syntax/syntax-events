package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class EventRepository implements EventRepositoryInterface {
    private FirebaseFirestore db;
    private FirebaseStorage imageDb;
    private CollectionReference eventsRef;
    private StorageReference eventsImageRef;
    private ArrayList<Event> eventsDataList;
    private OnEventsDataChangeListener dataChangeListener;


    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.imageDb = FirebaseStorage.getInstance();
        this.eventsRef = db.collection("events");
        this.eventsImageRef = imageDb.getReference();
        this.eventsDataList = new ArrayList<>();

        // initialize snapshot listener on creation
        setUpSnapshotListener();
    }

    // getter for local datalist
    public ArrayList<Event> getAllEventsList() {
        return eventsDataList;
    }

    // live updates to backend
    public void setUpSnapshotListener() {
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

            if (dataChangeListener != null) {
                dataChangeListener.onEventsDataChanged();
            }
        });
    }

    // Add this interface
    public interface OnEventsDataChangeListener {
        void onEventsDataChanged();
    }

    // Add this method
    public void setOnEventsDataChangeListener(OnEventsDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    // add event
    public void addEventToRepo(Event event, @Nullable Uri imageUri, Bitmap qrCodeBitmap) {
        eventsDataList.add(event); // add to local list
        // create event data hashmap
        HashMap<String, Object> data = eventToHashData(event);

        // start upload process by saving event poster
        // this will start the upload chain: poster image -> qrcode image -> event data
        if (imageUri != null) {
            uploadImage(event, data, imageUri, qrCodeBitmap);
        } else {
            // if an upload image was not uploaded
            uploadQrCode(event, data, qrCodeBitmap);
        }
    }

    // delete an event
    public void deleteEventFromRepo(Event event) {}

    // update and event
    public void updateEventDetails(Event event, @Nullable Uri imageUri, @Nullable Bitmap qrCodeBitmap) {
        // Update local list
        for (int i = 0; i < eventsDataList.size(); i++) {
            if (eventsDataList.get(i).getEventID().equals(event.getEventID())) {
                eventsDataList.set(i, event);
                break;
            }
        }

        // Create event data hashmap
        HashMap<String, Object> data = eventToHashData(event);

        if (imageUri != null || qrCodeBitmap != null) {
            // If there is new image or QR code, start upload chain
            if (imageUri != null) {
                uploadImage(event, data, imageUri, qrCodeBitmap);
            } else {
                // If only QR code is updated skip image upload
                uploadQrCode(event, data, qrCodeBitmap);
            }
        } else {
            // If no new images, just update the event data
            uploadEventData(event, data);
        }
    }

    // upload image to firebase storage
    private void uploadImage(Event event, HashMap<String,Object> data, Uri imageUri, Bitmap qrCodeBitmap) {
        // get reference for poster path in storage
        StorageReference posterRef = eventsImageRef.child("event_images/" + event.getEventID());
        posterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    posterRef.getDownloadUrl()
                    .addOnSuccessListener(url -> {
                        data.put("posterUrl", url.toString());
                        event.setPosterUrl(url.toString());
                        uploadQrCode(event, data, qrCodeBitmap);
                    })
                    .addOnFailureListener(e -> {
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to save Event to Firestore"));
    }

    // upload qr code to storage
    private void uploadQrCode(Event event, HashMap<String,Object> data, Bitmap qrCodeBitmap) {
        // get reference for qr code path in storage
        StorageReference qrCodeRef = eventsImageRef.child("qrcodes/"+ event.getEventID() + ".png");
        qrCodeRef.putBytes(bitmapToByteArray(qrCodeBitmap))
                .addOnSuccessListener(taskSnapshot -> {
                    qrCodeRef.getDownloadUrl()
                    .addOnSuccessListener(url -> {
                        data.put("qrCodeUrl", url.toString());
                        event.setQrCodeUrl(url.toString());
                        uploadEventData(event, data);
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to save Qr Code to Storage"));
    }

    // upload event data to firestore
    private void uploadEventData(Event event, HashMap<String,Object> data) {
        // save to firestore
        eventsRef.document(event.getEventID()).set(data)
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to save Event to Firebase"))
                .addOnSuccessListener(aVoid -> Log.d("Firestore Event Repository", "Event saved to Firebase"));
    }


    // Convert Bitmap to ByteArray
    // helper function
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    // event to hashmap
    public HashMap<String, Object> eventToHashData(Event event) {
        HashMap<String, Object> data = new HashMap<>(); // initialize hashmap
        data.put("eventID", event.getEventID());
        data.put("eventName", event.getEventName());
        data.put("facility", event.getFacility());
        data.put("description", event.getDescription());
        data.put("capacity", event.getCapacity());
        data.put("startDate", new com.google.firebase.Timestamp(event.getStartDate()));  // Use Firebase Timestamp
        data.put("endDate", new com.google.firebase.Timestamp(event.getEndDate()));      // Use Firebase Timestamp
        data.put("organizerId", event.getOrganizerId());
        data.put("participants", event.getParticipants());
        data.put("selectedParticipants", event.getSelectedParticipants());
        data.put("isFull", event.isFull());
        data.put("isDrawed", event.isDrawed());
        return data;
    }
}
