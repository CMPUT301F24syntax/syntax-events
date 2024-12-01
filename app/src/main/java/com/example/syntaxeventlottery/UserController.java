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
            Log.d("UserController"," invalid user when updating");
        }
        userRepository.updateUserDetails(user, imageUri, callback);
    }

    // For location
    public void updateUserLocation(User user, double latitude, double longitude, DataCallback<User> callback) {
        if (user == null) {
            callback.onError(new IllegalArgumentException("User cannot be null"));
            return;
        }

        // update user location info
        ArrayList<Double> location = new ArrayList<>();
        location.add(latitude);
        location.add(longitude);
        user.setLocation(location);

        userRepository.updateUserDetails(user, null, callback); // only update the location info
    }



    public void deleteUserProfilePhoto(User user, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            callback.onError(new IllegalArgumentException("Invalid user"));
            return;
        }
        user.setProfilePhotoUrl(null);
        userRepository.updateUserDetails(user, null, callback);
    }

    public void deleteUser(User user, DataCallback<Void> callback) {
        userRepository.deleteUserfromRepo(user, callback);
    }


    public Facility getUserFacility(User user) {
        if (user == null) {
            return null;
        }

        User userToRetrieve = getUserByDeviceID(user.getUserID());
        if (userToRetrieve != null) {
            return userToRetrieve.getFacility();
        }
        return null;

    }

    public void updateUserLocation(User user, Context context, DataCallback<User> callback) {
        if (user == null) {
            Log.d("MainActivity","AAAAAAAAAAAA"+user);
            callback.onError(new IllegalArgumentException("User object is null"));
            return;
        }
        Log.d("MainActivity","AAAABBBB"+user);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError(new SecurityException("Location permission not granted"));
            return;
        }
        Log.d("MainActivity","AAAABBBB1"+user);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            callback.onError(new Exception("LocationManager is unavailable"));
            return;
        }
        Log.d("MainActivity","AAAABBBB2"+user);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            callback.onError(new Exception("Location services are disabled"));
            return;
        }
        Log.d("MainActivity","AAAABBBB3"+user);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        Log.d("MainActivity","AAAABBBB4"+user);
        if (location == null) {
            callback.onError(new Exception("Unable to fetch location"));
            return;
        }
        Log.d("MainActivity","AAAABBBB5"+user);
        // Update user location
        ArrayList<Double> newLocation = new ArrayList<>();
        newLocation.add(location.getLatitude());
        newLocation.add(location.getLongitude());
        user.setLocation(newLocation);
        Log.d("MainActivity","AAAABBBB6"+user);
        // Update Firestore
        userRepository.updateLocation(user, newLocation, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Log.d("MainActivity","AAAABBBBsueccess"+user);
                callback.onSuccess(user); // Return updated user
            }

            @Override
            public void onError(Exception e) {
                Log.d("MainActivity","AAAABBBBerror"+user);
                callback.onError(e);
            }
        });
    }
}
