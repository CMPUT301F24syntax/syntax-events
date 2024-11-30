// Refactor Complete
package com.example.syntaxeventlottery;

import static android.content.ContentValues.TAG;

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
        Log.d(TAG,"usercontroller:" + "1234");
        for (User user : users) {
            Log.d(TAG,"usercontroller:" + user.getUserID());
            if (user.getUserID().equals(deviceId)) {
                Log.d(TAG,"usercontroller:" + user.getUserID());
                return user;
            }
        }

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
        data.put("userID", user.getUserID());
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
        if (user == null) {
            callback.onError(new IllegalArgumentException("User object is null"));
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError(new SecurityException("Location permission not granted"));
            return;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            callback.onError(new Exception("LocationManager is unavailable"));
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            callback.onError(new Exception("Location services are disabled"));
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location == null) {
            callback.onError(new Exception("Unable to fetch location"));
            return;
        }

        // Update user location
        ArrayList<Double> newLocation = new ArrayList<>();
        newLocation.add(location.getLatitude());
        newLocation.add(location.getLongitude());
        user.setLocation(newLocation);

        // Update Firestore
        userRepository.updateLocation(user, newLocation, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(user); // Return updated user
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}