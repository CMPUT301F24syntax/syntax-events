// UserRepository.java

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
 * Manages data operations related to users, including Entrants.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private CollectionReference usersRef;
    private StorageReference usersImageRef;
    private ArrayList<User> usersDataList;


    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.usersRef = db.collection("Users");
        this.usersImageRef = storage.getReference();
        this.usersDataList = new ArrayList<>();
    }

    // Returns the cached list of users
    public ArrayList<User> getLocalUsersList() {
        return new ArrayList<>(usersDataList);
    }

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


    public void addUserToRepo(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        usersDataList.add(user);
        HashMap<String, Object> data = userToHashData(user);
        if (imageUri != null) {
            uploadProfilePhoto(user, data, imageUri, callback);
        } else {
            // generate default photo if there is not image uri
            if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
                uploadDefaultPhoto(user, data, callback);
            }
        }
    }

    public void deleteUserfromRepo(User user, DataCallback<Void> callback) {
        usersRef.document(user.getUserID()).delete()
                .addOnSuccessListener(aVoid -> {
                    usersDataList.remove(user);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

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

    public void updateUserDetails(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        for (int i = 0; i < usersDataList.size(); i++) {
            if (usersDataList.get(i).getUserID().equals(user.getUserID())) {
                usersDataList.set(i, user);
                break;
            }
        }
        HashMap<String, Object> data = userToHashData(user);

        if (imageUri != null) {
            uploadProfilePhoto(user, data, imageUri, callback);
        } else {
            // generate default photo if there is not image uri
            if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
                uploadDefaultPhoto(user, data, callback);
            } else {
                // if only updating other information
                uploadUserData(user, data, callback);
            }
        }
    }

    public void uploadDefaultPhoto(User user, HashMap<String, Object> data, DataCallback<User> callback) {
        String username = user.getUsername();
        // get first letter of username
        if (Character.isLetter(username.charAt(0))) { // make sure it is a letter
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

        // Create a map to store the location data
        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("location", location);

        // Update Firestore with the new location data
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
