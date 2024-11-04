    package com.example.syntaxeventlottery;

    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;

    import android.net.Uri;

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
            db.collection("Users")
                    .whereEqualTo("deviceCode", deviceId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Entrant exists
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            Entrant existingEntrant = document.toObject(Entrant.class);
                            // Set the userID from the document ID
                            existingEntrant.setUserID(document.getId());
                            listener.onCheckComplete(true, existingEntrant);
                        } else {
                            // Entrant does not exist
                            listener.onCheckComplete(false, null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        listener.onCheckError(e);
                    });
        }

        /**
         * Retrieves an Entrant's data from Firebase.
         *
         * @param userId   The unique ID of the entrant.
         * @param listener Callback to handle the retrieved data or errors.
         */
        public void getEntrant(String userId, OnEntrantDataFetchListener listener) {
            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Entrant entrant = documentSnapshot.toObject(Entrant.class);
                            // Set the userID from the document ID
                            entrant.setUserID(documentSnapshot.getId());
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
            String fileName = "images/" + userId + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(fileName);

            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update the ProfilePhotoUrl field in Firestore
                        db.collection("Users").document(userId)
                                .update("profilePhotoUrl", uri.toString())
                                .addOnSuccessListener(aVoid1 -> listener.onUploadSuccess(uri))
                                .addOnFailureListener(listener::onUploadFailure);
                    }))
                    .addOnFailureListener(listener::onUploadFailure);
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
    }
