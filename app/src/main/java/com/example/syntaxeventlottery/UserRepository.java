package com.example.syntaxeventlottery;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;

import java.util.Set;

public class UserRepository {

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    // Retrieve user data
    public void getUser(String userId, OnUserDataFetchListener listener) {
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onUserDataFetched(user);
                    } else {
                        listener.onUserDataFetchError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(listener::onUserDataFetchError);
    }

    // Update user data
    public void updateUser(String userId, User user, OnUserUpdateListener listener) {
        db.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onUserUpdateSuccess())
                .addOnFailureListener(listener::onUserUpdateError);
    }

    // Upload user profile photo and update ProfilePhotoUrl field
    public void uploadProfilePhoto(String userId, Uri fileUri, OnUploadCompleteListener listener) {
        String fileName = "images/" + userId + "/" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storage.getReference().child(fileName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Update the ProfilePhotoUrl field in Firestore
                    db.collection("Users").document(userId)
                            .update("profilePhotoUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> listener.onUploadSuccess(uri))
                            .addOnFailureListener(listener::onUploadFailure);
                }))
                .addOnFailureListener(listener::onUploadFailure);
    }

    public void updateRoles(String userId, Set<String> roles) {
        db.collection("Users").document(userId)
                .update("roles", roles)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("User roles updated");
                })
                .addOnFailureListener(e -> {
                    System.err.println("User roles update failed: " + e.getMessage());

                });
        }

    public interface OnUserDataFetchListener {
        void onUserDataFetched(User user);
        void onUserDataFetchError(Exception e);
    }

    public interface OnUserUpdateListener {
        void onUserUpdateSuccess();
        void onUserUpdateError(Exception e);
    }

    public interface OnUploadCompleteListener {
        void onUploadSuccess(Uri downloadUrl);
        void onUploadFailure(Exception e);
    }
}
