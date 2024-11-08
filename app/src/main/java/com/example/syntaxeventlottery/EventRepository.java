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
import java.util.List;

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

    // Getter for local data list
    @Override
    public ArrayList<Event> getAllEventsList() {
        return eventsDataList;
    }

    // Live updates to backend
    // @Override
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

    // Add event
    @Override
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

    // Delete an event
    @Override
    public void deleteEventFromRepo(Event event) {
        eventsRef.document(event.getEventID()).delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore Event Repository", "Event deleted successfully"))
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to delete event", e));
    }

    // Update an event
    @Override
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

    // Upload image to Firebase Storage
    private void uploadImage(Event event, HashMap<String, Object> data, Uri imageUri, Bitmap qrCodeBitmap) {
        // Get reference for poster path in storage
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
                                Log.e("UploadImage", "Failed to get poster URL", e);
                            });
                })
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to save Event to Firestore", e));
    }

    // Upload QR code to storage
    private void uploadQrCode(Event event, HashMap<String, Object> data, Bitmap qrCodeBitmap) {
        // Get reference for QR code path in storage
        StorageReference qrCodeRef = eventsImageRef.child("qrcodes/" + event.getEventID() + ".png");
        qrCodeRef.putBytes(bitmapToByteArray(qrCodeBitmap))
                .addOnSuccessListener(taskSnapshot -> {
                    qrCodeRef.getDownloadUrl()
                            .addOnSuccessListener(url -> {
                                data.put("qrCodeUrl", url.toString());
                                event.setQrCodeUrl(url.toString());
                                uploadEventData(event, data);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UploadQRCode", "Failed to get QR Code URL", e);
                            });
                })
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to save QR Code to Storage", e));
    }

    // Upload event data to Firestore
    private void uploadEventData(Event event, HashMap<String, Object> data) {
        // Save to Firestore
        eventsRef.document(event.getEventID()).set(data)
                .addOnFailureListener(e -> Log.e("Firestore Event Repository", "Failed to save Event to Firebase", e))
                .addOnSuccessListener(aVoid -> Log.d("Firestore Event Repository", "Event saved to Firebase"));
    }

    // Convert Bitmap to ByteArray
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // Event to HashMap
    @Override
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

    // Implementing New Methods

    @Override
    public Event getEventById(String eventId) {
        for (Event event : eventsDataList) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    @Override
    public List<String> getChosenList(String eventId) {
        Event event = getEventById(eventId);
        return event != null ? event.getSelectedParticipants() : new ArrayList<>();
    }

    @Override
    public List<String> getUnChosenList(String eventId) {
        Event event = getEventById(eventId);
        return event != null ? event.getUnChosenList() : new ArrayList<>();
    }

    @Override
    public void performDraw(String eventId) {
        // Implement the draw logic here similar to the EventController's drawParticipants method
        // For brevity, this implementation is omitted
        // You can call the appropriate methods from EventController or implement the logic here
    }

    @Override
    public void addParticipant(String eventId, String participantId) {
        Event event = getEventById(eventId);
        if (event != null && !isEventFull(eventId) && !event.getParticipants().contains(participantId)) {
            event.addParticipant(participantId);
            updateEventDetails(event, null, null);
        }
    }

    @Override
    public void removeParticipant(String eventId, String participantId) {
        Event event = getEventById(eventId);
        if (event != null && event.getParticipants().contains(participantId)) {
            event.removeParticipant(participantId);
            updateEventDetails(event, null, null);
        }
    }

    @Override
    public void acceptInvitation(String eventId, String userId) {
        // Implement acceptance logic
        // For brevity, this implementation is omitted
    }

    @Override
    public void rejectInvitation(String eventId, String userId) {
        // Implement rejection logic
        // For brevity, this implementation is omitted
    }

    @Override
    public List<Event> getOrganizerEvents(String organizerId) {
        List<Event> organizerEvents = new ArrayList<>();
        for (Event event : eventsDataList) {
            if (event.getOrganizerId().equals(organizerId)) {
                organizerEvents.add(event);
            }
        }
        return organizerEvents;
    }

    @Override
    public List<Event> getEntrantWaitingListEvents(String entrantId) {
        List<Event> waitingListEvents = new ArrayList<>();
        for (Event event : eventsDataList) {
            if (event.getParticipants().contains(entrantId)) {
                waitingListEvents.add(event);
            }
        }
        return waitingListEvents;
    }

    @Override
    public List<Event> getEntrantSelectedListEvents(String entrantId) {
        List<Event> selectedListEvents = new ArrayList<>();
        for (Event event : eventsDataList) {
            if (event.getSelectedParticipants().contains(entrantId)) {
                selectedListEvents.add(event);
            }
        }
        return selectedListEvents;
    }

    @Override
    public boolean isEventFull(String eventId) {
        Event event = getEventById(eventId);
        if (event == null) return false;
        return event.getParticipants().size() >= event.getCapacity();
    }

    @Override
    public boolean isUserRegistered(String eventId, String userId) {
        Event event = getEventById(eventId);
        if (event == null) return false;
        return event.getParticipants().contains(userId);
    }


}