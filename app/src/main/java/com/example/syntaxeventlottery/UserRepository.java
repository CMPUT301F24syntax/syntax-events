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
    public List<User> getLocalUsersList() {
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
            if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
                user.setProfilePhotoUrl(generateDefaultProfilePhotoUrl(user.getUsername()));
            }
            uploadUserData(user, data, callback);
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
        StorageReference profilePhotoRef = usersImageRef.child("user_images/" + user.getUserID() + ".png");
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
            if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
                user.setProfilePhotoUrl(generateDefaultProfilePhotoUrl(user.getUsername()));
            }
            uploadUserData(user, data, callback);
        }
    }

    public void getUserByDeviceCode(String deviceCode, DataCallback<User> callback) {
        usersRef.whereEqualTo("deviceCode", deviceCode).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user by device code", e);
                    callback.onError(e);
                });
    }

    /**
     * Retrieves an Entrant's data from Firebase using the device ID.
     *
     * @param context  The context to access system services.
     * @param callback Callback to handle the retrieved data or errors.
     */
    public void getEntrantByDeviceId(Context context, DataCallback<User> callback) {
        // Retrieve the device ID
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Query Firestore to find any document in "Users" collection with deviceCode equal to deviceId
        usersRef.whereEqualTo("deviceCode", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // If a match is found, get the first document
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        User user = document.toObject(User.class);

                        if (user != null) {
                            user.setUserID(document.getId()); // Set the document ID as userID
                        }
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("Entrant not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void deleteProfilePhoto(User user, DataCallback<User> callback) {
        if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
            callback.onError(new Exception("No profile photo exists to be deleted"));
            return;
        }
        StorageReference profilePhotoRef = storage.getReferenceFromUrl(user.getProfilePhotoUrl());
        profilePhotoRef.delete()
                .addOnSuccessListener(aVoid -> {
                    user.setProfilePhotoUrl(generateDefaultProfilePhotoUrl(user.getUsername()));
                    updateUserDetails(user, null, callback);
                    Log.d(TAG, "Profile photo successfully deleted");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete profile photo");
                    callback.onError(e);
                });
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
        return data;
    }

    public String generateDefaultProfilePhotoUrl(String username) {
        return "https://robohash.org/" + username + ".png";
    }
}
