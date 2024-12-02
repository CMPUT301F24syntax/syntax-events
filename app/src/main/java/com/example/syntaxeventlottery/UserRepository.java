package com.example.syntaxeventlottery;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Repository class for managing user-related data operations, including Firebase Firestore and Firebase Storage integration.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private CollectionReference usersRef;
    private StorageReference usersImageRef;
    private ArrayList<User> usersDataList;

    /**
     * Initializes the UserRepository with Firestore and Firebase Storage references.
     */
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.usersRef = db.collection("Users");
        this.usersImageRef = storage.getReference();
        this.usersDataList = new ArrayList<>();
    }

    /**
     * Retrieves the cached list of users.
     *
     * @return A copy of the local users list.
     */
    public ArrayList<User> getLocalUsersList() {
        return new ArrayList<>(usersDataList);
    }

    /**
     * Fetches all users from Firestore and updates the local cache.
     *
     * @param callback Callback to handle the success or failure of the operation.
     */
    public void fetchAllUsers(DataCallback<Void> callback) {
        usersRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    usersDataList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        usersDataList.add(user);
                    }
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching users", e);
                    callback.onError(e);
                });
    }

    /**
     * Adds a new user to Firestore and uploads their profile photo if provided.
     *
     * @param user      The user to be added.
     * @param imageUri  The URI of the user's profile photo (optional).
     * @param callback  Callback to handle the success or failure of the operation.
     */
    public void addUserToRepo(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        usersDataList.add(user);
        HashMap<String, Object> data = userToHashData(user);
        if (imageUri != null) {
            uploadProfilePhoto(user, data, imageUri, callback);
        } else {
            if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
                uploadDefaultPhoto(user, data, callback);
            }
        }
    }

    /**
     * Deletes a user from Firestore and removes them from the local cache.
     *
     * @param user      The user to be deleted.
     * @param callback  Callback to handle the success or failure of the operation.
     */
    public void deleteUserfromRepo(User user, DataCallback<Void> callback) {
        usersRef.document(user.getUserID()).delete()
                .addOnSuccessListener(aVoid -> {
                    usersDataList.remove(user);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Uploads a user's profile photo to Firebase Storage and updates Firestore with the photo URL.
     *
     * @param user      The user whose photo is being uploaded.
     * @param data      The user's data to be updated in Firestore.
     * @param imageUri  The URI of the profile photo.
     * @param callback  Callback to handle the success or failure of the operation.
     */
    public void uploadProfilePhoto(User user, HashMap<String, Object> data, Uri imageUri, DataCallback<User> callback) {
        StorageReference profilePhotoRef = usersImageRef.child("user_images/" + user.getUserID());
        profilePhotoRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        profilePhotoRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    data.put("profilePhotoUrl", uri.toString());
                                    user.setProfilePhotoUrl(uri.toString());
                                    uploadUserData(user, data, callback);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to get profile photo url", e);
                                    callback.onError(e);
                                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload profile photo", e);
                    callback.onError(e);
                });
    }

    /**
     * Uploads user data to Firestore.
     *
     * @param user      The user whose data is being uploaded.
     * @param data      The user's data to be updated in Firestore.
     * @param callback  Callback to handle the success or failure of the operation.
     */
    private void uploadUserData(User user, HashMap<String, Object> data, DataCallback<User> callback) {
        usersRef.document(user.getUserID()).set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data", e);
                    callback.onError(e);
                });
    }

    /**
     * Updates the details of an existing user in Firestore and uploads their profile photo if provided.
     *
     * @param user      The user to be updated.
     * @param imageUri  The URI of the new profile photo (optional).
     * @param callback  Callback to handle the success or failure of the operation.
     */
    public void updateUserDetails(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        for (int i = 0; i < usersDataList.size(); i++) {
            if (usersDataList.get(i).getUserID().equals(user.getUserID())) {
                usersDataList.set(i, user);
                break;
            }
        }
        HashMap<String, Object> data = userToHashData(user);

        if (imageUri != null) {
            user.setDefaultPhoto(false);
            uploadProfilePhoto(user, data, imageUri, callback);
        } else {
            if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
                uploadDefaultPhoto(user, data, callback);
            } else {
                uploadUserData(user, data, callback);
            }
        }
    }

    /**
     * Uploads a default profile photo for the user based on their username.
     *
     * @param user      The user to receive the default profile photo.
     * @param data      The user's data to be updated in Firestore.
     * @param callback  Callback to handle the success or failure of the operation.
     */
    public void uploadDefaultPhoto(User user, HashMap<String, Object> data, DataCallback<User> callback) {
        String username = user.getUsername();
        if (Character.isLetter(username.charAt(0))) {
            char firstLetter = Character.toUpperCase(username.charAt(0));
            StorageReference defaultPhotoRef = usersImageRef.child(firstLetter + ".png");
            defaultPhotoRef.getDownloadUrl()
                    .addOnSuccessListener(url -> {
                        data.put("profilePhotoUrl", url.toString());
                        user.setProfilePhotoUrl(url.toString());
                        uploadUserData(user, data, callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to generate default profile photo");
                        callback.onError(e);
                    });
        }
    }

    /**
     * Updates the user's location data in Firestore.
     *
     * @param user      The user whose location needs to be updated.
     * @param location  The new location data to be stored (e.g., [latitude, longitude]).
     * @param callback  Callback to handle success or failure.
     */
    public void updateLocation(User user, ArrayList<Double> location, DataCallback<User> callback) {
        if (user == null || location == null || location.size() < 2) {
            callback.onError(new Exception("Invalid user or location data"));
            return;
        }

        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("location", location);

        usersRef.document(user.getUserID())
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Location updated successfully for user: " + user.getUserID());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update location for user: " + user.getUserID(), e);
                    callback.onError(e);
                });
    }

    /**
     * Converts a User object to a HashMap for Firestore storage.
     *
     * @param user The user to be converted.
     * @return A HashMap representation of the user.
     */
    public HashMap<String, Object> userToHashData(User user) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("userID", user.getUserID());
        data.put("deviceCode", user.getDeviceCode());
        data.put("email", user.getEmail());
        data.put("phoneNumber", user.getPhoneNumber());
        data.put("profilePhotoUrl", user.getProfilePhotoUrl());
        data.put("username", user.getUsername());
        data.put("facility", user.getFacility());
        data.put("receiveNotifications", user.isReceiveNotifications());
        data.put("allowNotification", user.isAllowNotification());
        return data;
    }
}
