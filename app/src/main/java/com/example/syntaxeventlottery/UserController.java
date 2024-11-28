// Refactor Complete
package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import android.Manifest;




public class UserController {

    private UserRepository userRepository;
    private LocationManager locationManager;




    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public UserController(UserRepository userRepository,LocationManager locationManager) {
        this.userRepository = userRepository;
        this.locationManager = locationManager;
    }
    // User validation
    private boolean validateUser(User user, DataCallback<?> callback) {
        if (user == null) {
            callback.onError(new IllegalArgumentException("User cannot be null"));
            return false;
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("User name cannot be empty"));
            return false;
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("User email cannot be empty"));
            return false;
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            callback.onError(new IllegalArgumentException("User email is invalid"));
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty() && !user.getPhoneNumber().matches("\\d+")) {
            callback.onError(new IllegalArgumentException("User phone number must only contain digits"));
            return false;
        }
        return true;
    }


    public ArrayList<User> getLocalUsersList() {
        return (ArrayList<User>) userRepository.getLocalUsersList();
    }

    public void refreshRepository(DataCallback<Void> callback) {
        userRepository.fetchAllUsers(callback);
    }

    public User getUserByDeviceID(String deviceId) {
        // Check if deviceId is null
        if (deviceId == null) {
            return null;
        }
        ArrayList<User> users = getLocalUsersList();
        for (User user : users) {
            if (user.getUserID().equals(deviceId)) {
                return user;
            }
        }
        // return null if no matching user found
        return null;


    }

    public void addUser(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            return;
        }
        userRepository.addUserToRepo(user, imageUri, callback);
    }

    public void updateUser(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            return;
        }
        userRepository.updateUserDetails(user, imageUri, callback);
    }

    public void deleteUserProfilePhoto(User user, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            return;
        }
        userRepository.deleteProfilePhoto(user, callback);
    }

    public void deleteUser(User user, DataCallback<Void> callback) {
        userRepository.deleteUserfromRepo(user, callback);
    }

    public void uploadUserProfileImage(User user, Uri imageUri, DataCallback<User> callback) {
        if (user == null) {
            callback.onError(new IllegalArgumentException("User cannot be null"));
            return;
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserID());
        data.put("userName", user.getUsername());
        userRepository.uploadProfilePhoto(user, data, imageUri, callback);
    }

    public void getUserFacility(Context context, DataCallback<User> callback) {
        userRepository.getEntrantByDeviceId(context, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    String userId = user.getUserID();
                    user.setUserID(userId);
                }
                callback.onSuccess(user);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
    public void updateUserLocation(User user, Context context, DataCallback<User> callback) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, notify the callback
            callback.onError(new SecurityException("Location permission not granted"));
            return;
        }

        // Try to get the last known location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // Try network provider as fallback
        }

        if (location != null) {
            // If location is available, update Firestore
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            ArrayList<Double> newLocation = new ArrayList<>();
            newLocation.add(latitude);
            newLocation.add(longitude);

            // Debug log
            Log.d("UserController", "Updating location: " + newLocation + " for user: " + user.getUserID());

            userRepository.updateLocation(user, newLocation, callback);
        } else {
            // If no location is available, notify the callback
            Toast.makeText(context, "No last known location available. Enable GPS and try again.", Toast.LENGTH_SHORT).show();
            callback.onError(new Exception("No last known location available"));
        }
    }
}