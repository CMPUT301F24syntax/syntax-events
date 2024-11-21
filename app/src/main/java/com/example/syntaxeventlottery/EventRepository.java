// EventRepository.java
package com.example.syntaxeventlottery;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;


import androidx.annotation.NonNull;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * The EventRepository class provides methods to interact with Firebase Firestore
 * and Firebase Storage for event-related operations.
 */
public class EventRepository {
    private static final String TAG = "EventRepository";
    private FirebaseFirestore db;
    private CollectionReference eventsRef;


    /**
     * Constructor for EventRepository.
     * Initializes Firestore references.
     */
    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("events"); // Ensure this matches the collection name used when saving events
    }


    /**
     * Callback interface for fetching a list of events.
     */
    public interface EventListCallback {
        void onSuccess(List<Event> eventList);
        void onFailure(Exception e);
    }


    /**
     * Retrieves events by organizer ID.
     *
     * @param organizerId The organizer's ID.
     * @param callback    The callback to handle success or failure.
     */
    public void getEventsByOrganizerId(String organizerId, EventListCallback callback) {
        eventsRef.whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> eventList = queryDocumentSnapshots.toObjects(Event.class);
                    callback.onSuccess(eventList);
                    Log.d(TAG, "Fetched " + eventList.size() + " events for organizerId: " + organizerId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    Log.e(TAG, "Error fetching events for organizerId: " + organizerId, e);
                });
    }


    /**
     * Saves a new event to Firestore.
     *
     * @param event    The event object to save.
     * @param callback The callback to handle success or failure.
     */
    public void saveNewEvent(Event event, EventUpdateCallback callback) {
        if (event.getEventID() == null || event.getEventID().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Event ID is null or empty"));
            Log.e(TAG, "Attempted to save event with null or empty ID");
            return;
        }


        eventsRef.document(event.getEventID()).set(event)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Event saved successfully: " + event.getEventName());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    Log.e(TAG, "Failed to save event: " + event.getEventName(), e);
                });
    }


    /**
     * Uploads a file to Firebase Storage.
     *
     * @param fileUri            The URI of the file to upload.
     * @param path               The storage path.
     * @param onCompleteListener The listener to call upon completion.
     */
    public void uploadFileToStorage(Uri fileUri, String path, OnCompleteListener<Uri> onCompleteListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);
        storageRef.putFile(fileUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                })
                .addOnCompleteListener(onCompleteListener);
    }


    /**
     * Uploads data to Firebase Storage.
     *
     * @param data               The byte array data to upload.
     * @param path               The storage path.
     * @param onCompleteListener The listener to call upon completion.
     */
    public void uploadDataToStorage(byte[] data, String path, OnCompleteListener<Uri> onCompleteListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);
        storageRef.putBytes(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                })
                .addOnCompleteListener(onCompleteListener);
    }
    public void uploadImageToStorage(Uri imageUri, String path, OnCompleteListener<Uri> onCompleteListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);
        storageRef.putFile(imageUri)
                .continueWithTask(task -> storageRef.getDownloadUrl())
                .addOnCompleteListener(onCompleteListener);
    }


    /**
     * Callback interface for fetching event details.
     */
    public interface EventCallback {
        void onSuccess(Event event);
        void onFailure(Exception e);
    }


    /**
     * Callback interface for updating an event.
     */
    public interface EventUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }


    /**
     * Retrieves an event by its ID.
     *
     * @param eventId  The ID of the event to retrieve.
     * @param callback The callback to handle success or failure.
     */
    // EventRepository.java
    public void getEventById(String eventId, EventCallback callback) {
        Log.d("EventRepository", "Fetching event with ID: " + eventId);
        eventsRef.document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        Log.d("EventRepository", "Event found: " + event.getEventName());
                        if (callback != null) {
                            callback.onSuccess(event);
                        }
                    } else {
                        Log.e("EventRepository", "Event does not exist in Firestore");
                        if (callback != null) {
                            callback.onFailure(new Exception("Event not found"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error fetching event: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }




    /**
     * Updates an existing event in the Firestore database.
     *
     * @param event    The event object containing updated details.
     * @param callback The callback to handle success or failure.
     */
    public void updateEvent(Event event, EventUpdateCallback callback) {
        eventsRef.document(event.getEventID()).set(event)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }
    /**
     * Saves a new event to Firestore with an optional image.
     *
     * @param event    The event object to save.
     * @param imageUri The URI of the image to upload, can be null.
     * @param callback The callback to handle success or failure.
     */
    public void saveNewEventWithImage(Event event, Uri imageUri, EventUpdateCallback callback) {
        if (event.getEventID() == null || event.getEventID().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Event ID is null or empty"));
            Log.e(TAG, "Attempted to save event with null or empty ID");
            return;
        }


        if (imageUri != null) {
            // Upload image and then save event
            uploadImageToStorage(imageUri, "event_images/" + event.getEventID(), task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    event.setPosterUrl(downloadUri.toString());
                    saveEventWithQRCode(event, callback);
                } else {
                    callback.onFailure(task.getException());
                }
            });
        } else {
            // No image to upload, proceed to save event
            saveEventWithQRCode(event, callback);
        }
    }




    // QR code generation and saving functionality


    /**
     * Generates a QR code bitmap from the provided content.
     *
     * @param content The content to encode in the QR code.
     * @return The generated QR code bitmap.
     */
    private Bitmap generateQRCodeBitmap(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code bitmap", e);
            return null;
        }
    }


    /**
     * Converts a bitmap image to a byte array.
     *
     * @param bitmap The bitmap image to convert.
     * @return The byte array representation of the bitmap.
     */
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    /**
     * Saves an event to Firestore after generating and uploading its QR code.
     *
     * @param event    The event to save.
     * @param callback The callback to handle success or failure.
     */
    public void saveEventWithQRCode(Event event, EventUpdateCallback callback) {
        Bitmap qrCodeBitmap = generateQRCodeBitmap(event.getEventID());
        if (qrCodeBitmap != null) {
            byte[] qrCodeData = bitmapToByteArray(qrCodeBitmap);
            uploadDataToStorage(qrCodeData, "qrcodes/" + event.getEventID() + ".png", task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    event.setQrCode(downloadUri.toString());
                    eventsRef.document(event.getEventID()).set(event)
                            .addOnSuccessListener(aVoid -> {
                                callback.onSuccess();
                                Log.d(TAG, "Event saved successfully with QR code: " + event.getEventName());
                            })
                            .addOnFailureListener(e -> {
                                callback.onFailure(e);
                                Log.e(TAG, "Failed to save event: " + event.getEventName(), e);
                            });
                } else {
                    callback.onFailure(task.getException());
                }
            });
        } else {
            callback.onFailure(new Exception("Failed to generate QR code"));
        }
    }


    public void addParticipantToEvent(String eventId, String participantId, EventUpdateCallback callback) {
        eventsRef.document(eventId).update("participants", FieldValue.arrayUnion(participantId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }


    public void removeParticipantFromEvent(String eventId, String participantId, EventUpdateCallback callback) {
        eventsRef.document(eventId).update("participants", FieldValue.arrayRemove(participantId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }


    /**
     * Performs the draw to select participants and sends notifications to unselected participants.
     *
     * @param eventId  The ID of the event.
     * @param callback The callback to handle success or failure.
     */
    public void performDraw(String eventId, EventUpdateCallback callback) {
        eventsRef.document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            if (event.isDrawed()) {
                                callback.onFailure(new Exception("Draw has already been performed."));
                                return;
                            }
                            List<String> participants = event.getParticipants();
                            int capacity = event.getCapacity();


                            // Shuffle and select participants
                            Collections.shuffle(participants);
                            List<String> selectedParticipants = new ArrayList<>(participants.subList(0, Math.min(capacity, participants.size())));


                            event.setSelectedParticipants(selectedParticipants);
                            event.setDrawed(true); // Mark the event as drawn


                            // Update the event in Firestore
                            eventsRef.document(eventId).set(event)
                                    .addOnSuccessListener(aVoid -> {
                                        // Send notifications to unselected participants
                                        List<String> unselectedParticipants = new ArrayList<>(participants);
                                        unselectedParticipants.removeAll(selectedParticipants);


                                        sendNotificationsToUnselectedParticipants(unselectedParticipants, eventId, event.getEventName());
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> callback.onFailure(e));
                        } else {
                            callback.onFailure(new Exception("Event data is null"));
                        }
                    } else {
                        callback.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }


    /**
     * Sends notifications to unselected participants after the draw.
     *
     * @param unselectedParticipants List of participant IDs who were not selected.
     * @param eventId                The ID of the event.
     * @param eventName              The name of the event.
     */
    private void sendNotificationsToUnselectedParticipants(List<String> unselectedParticipants, String eventId, String eventName) {
        for (String participantId : unselectedParticipants) {
            Notification notification = new Notification();
            notification.setDeviceId(participantId);
            notification.setEventId(eventId);
            notification.setMessage("You were not selected for the event: " + eventName);
            notification.setRead(false);
            notification.setTimestamp(new Date());


            // Generate a unique ID for the notification
            String notificationId = db.collection("notifications").document().getId();
            notification.setId(notificationId);


            // Save the notification to Firestore
            db.collection("notifications").document(notificationId)
                    .set(notification)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Notification sent to participant: " + participantId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to send notification to participant: " + participantId, e);
                    });
        }
    }


    public void acceptInvitation(String eventId, String participantId, EventUpdateCallback callback) {
        eventsRef.document(eventId).update("confirmedParticipants", FieldValue.arrayUnion(participantId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }


    public void declineInvitation(String eventId, String participantId, EventUpdateCallback callback) {
        eventsRef.document(eventId).update("selectedParticipants", FieldValue.arrayRemove(participantId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }


    public void updateEventPoster(String eventId, Uri imageUri, EventUpdateCallback callback) {
        if (imageUri != null) {
            // Upload new poster image
            uploadImageToStorage(imageUri, "event_images/" + eventId, task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    // Update the event's posterUrl field
                    eventsRef.document(eventId).update("posterUrl", downloadUri.toString())
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e));
                } else {
                    callback.onFailure(task.getException());
                }
            });
        } else {
            callback.onFailure(new IllegalArgumentException("Image URI is null"));
        }
    }
}

