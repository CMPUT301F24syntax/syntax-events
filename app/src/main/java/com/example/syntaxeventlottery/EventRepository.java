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

public class EventRepository  {
    private static final String TAG = "EventRepository";
    private FirebaseFirestore db;
    private FirebaseStorage imageDb;
    private CollectionReference eventsRef;
    private StorageReference eventsImageRef;
    private ArrayList<Event> eventsDataList;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.imageDb = FirebaseStorage.getInstance();
        this.eventsRef = db.collection("events");
        this.eventsImageRef = imageDb.getReference();
        this.eventsDataList = new ArrayList<>();

    }

    // Return the cached list - this is synchronous
    public ArrayList<Event> getLocalEventsList() {
        return new ArrayList<>(eventsDataList); // Return a copy to prevent modification
    }

    public void updateLocalEventsList(DataCallback<Void> callback) {
        eventsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    eventsDataList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        eventsDataList.add(event);
                    }
                    Log.d(TAG, "Local Events List updated");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events", e);
                    callback.onError(new IllegalArgumentException("Failed to retrieve data"));
                });
    }

    public void addEventToRepo(Event event, @Nullable Uri imageUri, Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        eventsDataList.add(event);
        HashMap<String, Object> data = eventToHashData(event);

        if (imageUri != null) {
            uploadImage(event, data, imageUri, qrCodeBitmap, callback);
        } else {
            uploadQrCode(event, data, qrCodeBitmap, callback);
        }
    }

    private void uploadImage(Event event, HashMap<String, Object> data, Uri imageUri,
                             Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        StorageReference posterRef = eventsImageRef.child("event_poster_images/" + event.getEventID());
        posterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        posterRef.getDownloadUrl()
                                .addOnSuccessListener(url -> {
                                    data.put("posterUrl", url.toString());
                                    event.setPosterUrl(url.toString());
                                    // Upload QR code only if qrCodeBitmap is not null
                                    if (qrCodeBitmap != null) {
                                        uploadQrCode(event, data, qrCodeBitmap, callback);
                                    } else {
                                        uploadEventData(event, data, callback);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to get poster URL", e);
                                    callback.onError(e);
                                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload image", e);
                    callback.onError(new IllegalArgumentException("Failed to save image"));
                });
    }

    private void uploadQrCode(Event event, HashMap<String, Object> data,
                              Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        StorageReference qrCodeRef = eventsImageRef.child("event_qrcode_images/" + event.getEventID() + ".png");
        qrCodeRef.putBytes(bitmapToByteArray(qrCodeBitmap))
                .addOnSuccessListener(taskSnapshot ->
                        qrCodeRef.getDownloadUrl()
                                .addOnSuccessListener(url -> {
                                    data.put("qrCode", url.toString());
                                    event.setQrCode(url.toString());
                                    uploadEventData(event, data, callback);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to get QR Code URL", e);
                                    callback.onError(e);
                                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload QR code", e);
                    callback.onError(new IllegalArgumentException("Failed to save QR code"));
                });
    }

    private void uploadEventData(Event event, HashMap<String, Object> data, DataCallback<Event> callback) {
        eventsRef.document(event.getEventID()).set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event saved successfully");
                    callback.onSuccess(event);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save event", e);
                    callback.onError(new IllegalArgumentException("Failed to save event"));
                });
    }

    public void deleteEventFromRepo(Event event, DataCallback<Void> callback) {
        eventsRef.document(event.getEventID()).delete()
                .addOnSuccessListener(aVoid -> {
                    eventsDataList.remove(event);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateEventDetails(Event event, @Nullable Uri imageUri,
                                   @Nullable Bitmap qrCodeBitmap, DataCallback<Event> callback) {
        // Update local list
        for (int i = 0; i < eventsDataList.size(); i++) {
            if (eventsDataList.get(i).getEventID().equals(event.getEventID())) {
                eventsDataList.set(i, event);
                break;
            }
        }

        HashMap<String, Object> data = eventToHashData(event);
        Log.d(TAG, "event details: " +data);

        // Preserve existing QR code URL
        if (event.getQrCode() != null) {
            data.put("qrCode", event.getQrCode());
        }

        // Preserve existing poster URL
        if (event.getPosterUrl() != null) {
            data.put("posterUrl", event.getPosterUrl());
        }

        // Case 1: If both imageUri and qrCodeBitmap are null, just upload event data
        if (imageUri == null && qrCodeBitmap == null) {
            uploadEventData(event, data, callback);
        }
        // Case 2: If only updating image
        else if (imageUri != null && qrCodeBitmap == null) {
            uploadImage(event, data, imageUri, null, callback);
        }
        // Case 3: If only updating QR code
        else if (imageUri == null && qrCodeBitmap != null) {
            uploadQrCode(event, data, qrCodeBitmap, callback);
        }
        // Case 4: If updating both image and QR code
        else {
            uploadImage(event, data, imageUri, qrCodeBitmap, callback);
        }
    }

    // Convert Bitmap to ByteArray
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // Event to HashMap
    public HashMap<String, Object> eventToHashData(Event event) {
        HashMap<String, Object> data = new HashMap<>(); // initialize hashmap
        data.put("eventID", event.getEventID());
        data.put("eventName", event.getEventName());
        data.put("facilityName", event.getFacilityName());
        data.put("facilityLocation", event.getFacilityLocation());
        data.put("description", event.getDescription());
        data.put("capacity", event.getCapacity());
        data.put("startDate", new com.google.firebase.Timestamp(event.getStartDate()));  // Use Firebase Timestamp
        data.put("endDate", new com.google.firebase.Timestamp(event.getEndDate()));      // Use Firebase Timestamp
        data.put("organizerId", event.getOrganizerId());
        data.put("participants", event.getParticipants());
        data.put("selectedParticipants", event.getSelectedParticipants());
        data.put("confirmedParticipants", event.getConfirmedParticipants());
        data.put("waitingListLimit", event.getWaitingListLimit());
        data.put("cancelledParticipants", event.getCancelledParticipants());
        data.put("capacityFull", event.getCapacityFull());
        data.put("waitingListFull", event.getWaitingListFull());
        data.put("drawed", event.isDrawed());
        data.put("isLocationRequired", event.isLocationRequired());
        data.put("locationDetails", event.getLocationDetails());
        return data;
    }

}