package com.example.syntaxeventlottery;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Manages data operations related to users, including Entrants.
 */
public class UserRepository {

    private FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // -------------------------------------------------------------------------
    // Entrant Data Operations
    // -------------------------------------------------------------------------

    /**
     * Checks if an Entrant with the given device ID already exists.
     *
     * @param deviceId The device ID to check.
     * @param listener Callback to handle the result.
     */
    public void checkEntrantExists(String deviceId, OnCheckEntrantExistsListener listener) {
        // Query Firestore to find any document in "Users" collection with deviceCode equal to deviceId
        db.collection("Users")
                .whereEqualTo("deviceCode", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // If a match is found, get the first document
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Entrant existingEntrant = document.toObject(Entrant.class);

                        if (existingEntrant != null) {
                            existingEntrant.setUserID(document.getId()); // Set the document ID as userID
                        }
                        // Call onCheckComplete with exists = true and the found Entrant
                        listener.onCheckComplete(true, existingEntrant);
                    } else {
                        // If no match is found, call onCheckComplete with exists = false
                        listener.onCheckComplete(false, null);
                    }
                })
                .addOnFailureListener(listener::onCheckError);
    }

    /**
     * Retrieves an Entrant's data from Firebase using the device ID.
     *
     * @param context  The context to access system services.
     * @param listener Callback to handle the retrieved data or errors.
     */
    public void getEntrantByDeviceId(Context context, OnEntrantDataFetchListener listener) {
        // Retrieve the device ID
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Query Firestore to find any document in "Users" collection with deviceCode equal to deviceId
        db.collection("Users")
                .whereEqualTo("deviceCode", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // If a match is found, get the first document
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Entrant entrant = document.toObject(Entrant.class);

                        if (entrant != null) {
                            entrant.setUserID(document.getId()); // Set the document ID as userID
                        }
                        listener.onEntrantDataFetched(entrant);
                    } else {
                        listener.onEntrantDataFetchError(new Exception("Entrant not found"));
                    }
                })
                .addOnFailureListener(listener::onEntrantDataFetchError);
    }

    /**
     * Updates or creates an Entrant's data in Firebase.
     *
     * @param userId   The unique ID of the entrant.
     * @param entrant  The Entrant object containing updated data.
     * @param listener Callback to handle success or errors.
     */
    public void updateEntrant(String userId, Entrant entrant, OnEntrantUpdateListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onEntrantUpdateError(new Exception("User ID is null or empty"));
            return;
        }

        db.collection("Users").document(userId)
                .set(entrant)
                .addOnSuccessListener(aVoid -> listener.onEntrantUpdateSuccess())
                .addOnFailureListener(listener::onEntrantUpdateError);
    }

    /**
     * Uploads an Entrant's profile photo to Firebase Storage and updates the profilePhotoUrl in Firestore.
     *
     * @param userId   The unique ID of the entrant.
     * @param fileUri  The URI of the image file to upload.
     * @param listener Callback to handle the upload result.
     */
    public void uploadProfilePhoto(String userId, Uri fileUri, OnUploadCompleteListener listener) {
        String fileName = "images/" + userId + "/profile.jpg";
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(fileName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("Users").document(userId)
                            .update("profilePhotoUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> listener.onUploadSuccess(uri))
                            .addOnFailureListener(listener::onUploadFailure);
                }))
                .addOnFailureListener(listener::onUploadFailure);
    }

    /**
     * Deletes an Entrant's profile photo from Firebase Storage.
     *
     * @param userId   The unique ID of the entrant.
     * @param listener Callback to handle the deletion result.
     */
    public void deleteProfilePhoto(String userId, OnDeleteCompleteListener listener) {
        String fileName = "images/" + userId + "/profile.jpg";
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(fileName);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> listener.onDeleteSuccess())
                .addOnFailureListener(listener::onDeleteFailure);
    }

    // -------------------------------------------------------------------------
    // Listener Interfaces
    // -------------------------------------------------------------------------

    /**
     * Listener for checking if an Entrant exists.
     */
    public interface OnCheckEntrantExistsListener {
        void onCheckComplete(boolean exists, Entrant entrant);
        void onCheckError(Exception e);
    }

    /**
     * Listener for fetching Entrant data.
     */
    public interface OnEntrantDataFetchListener {
        void onEntrantDataFetched(Entrant entrant);
        void onEntrantDataFetchError(Exception e);
    }

    /**
     * Listener for updating Entrant data.
     */
    public interface OnEntrantUpdateListener {
        void onEntrantUpdateSuccess();
        void onEntrantUpdateError(Exception e);
    }

    /**
     * Listener for upload operations.
     */
    public interface OnUploadCompleteListener {
        void onUploadSuccess(Uri downloadUrl);
        void onUploadFailure(Exception e);
    }

    /**
     * Listener for delete operations.
     */
    public interface OnDeleteCompleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(Exception e);
    }
}
