package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import android.Manifest;

/**
 * The {@code UserController} class handles operations related to user management.
 * It acts as a mediator between the {@code UserRepository} and the rest of the application,
 * managing user data validation, CRUD operations, and location updates.
 */
public class UserController {

    private UserRepository userRepository;
    private LocationManager locationManager;

    /**
     * Constructs a {@code UserController} with the specified {@code UserRepository}.
     *
     * @param userRepository The {@code UserRepository} to use for data operations.
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Constructs a {@code UserController} with the specified {@code UserRepository} and {@code LocationManager}.
     *
     * @param userRepository The {@code UserRepository} to use for data operations.
     * @param locationManager The {@code LocationManager} to use for location-related operations.
     */
    public UserController(UserRepository userRepository, LocationManager locationManager) {
        this.userRepository = userRepository;
        this.locationManager = locationManager;
    }

    /**
     * Validates the provided {@code User} object.
     *
     * @param user     The {@code User} to validate.
     * @param callback The callback to notify if validation fails.
     * @return {@code true} if the user is valid, {@code false} otherwise.
     */
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
            return false;
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty() && !user.getPhoneNumber().matches("\\d+")) {
            callback.onError(new IllegalArgumentException("User phone number must only contain digits"));
            return false;
        }
        return true;
    }

    /**
     * Retrieves the cached list of users.
     *
     * @return A list of users from the local cache.
     */
    public ArrayList<User> getLocalUsersList() {
        return (ArrayList<User>) userRepository.getLocalUsersList();
    }

    /**
     * Refreshes the user repository by fetching all users from the data source.
     *
     * @param callback The callback to handle the result of the operation.
     */
    public void refreshRepository(DataCallback<Void> callback) {
        userRepository.fetchAllUsers(callback);
    }

    /**
     * Retrieves a user by their device ID.
     *
     * @param deviceId The device ID of the user.
     * @return The {@code User} object if found, {@code null} otherwise.
     */
    public User getUserByDeviceID(String deviceId) {
        if (deviceId == null) {
            return null;
        }
        ArrayList<User> users = getLocalUsersList();
        for (User user : users) {
            if (user.getUserID().equals(deviceId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Adds a new user to the repository.
     *
     * @param user     The {@code User} to add.
     * @param imageUri The profile image URI of the user (nullable).
     * @param callback The callback to handle the result of the operation.
     */
    public void addUser(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            return;
        }
        userRepository.addUserToRepo(user, imageUri, callback);
    }

    /**
     * Updates the details of an existing user.
     *
     * @param user     The {@code User} to update.
     * @param imageUri The new profile image URI of the user (nullable).
     * @param callback The callback to handle the result of the operation.
     */
    public void updateUser(User user, @Nullable Uri imageUri, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            Log.d("UserController", "Invalid user when updating");
            return;
        }
        userRepository.updateUserDetails(user, imageUri, callback);
    }

    /**
     * Deletes a user's profile photo and sets it to the default photo.
     *
     * @param user     The {@code User} whose profile photo is to be deleted.
     * @param callback The callback to handle the result of the operation.
     */
    public void deleteUserProfilePhoto(User user, DataCallback<User> callback) {
        if (!validateUser(user, callback)) {
            callback.onError(new IllegalArgumentException("Invalid user"));
            return;
        }
        user.setProfilePhotoUrl(null);
        user.setDefaultPhoto(true);
        userRepository.updateUserDetails(user, null, callback);
    }

    /**
     * Deletes a user from the repository.
     *
     * @param user     The {@code User} to delete.
     * @param callback The callback to handle the result of the operation.
     */
    public void deleteUser(User user, DataCallback<Void> callback) {
        userRepository.deleteUserfromRepo(user, callback);
    }

    /**
     * Retrieves the facility information of a user.
     *
     * @param user The {@code User} whose facility information is to be retrieved.
     * @return The {@code Facility} object associated with the user, or {@code null} if not found.
     */
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

    /**
     * Updates a user's location based on the device's current location.
     *
     * @param user     The {@code User} whose location is to be updated.
     * @param context  The application context.
     * @param callback The callback to handle the result of the operation.
     */
    public void updateUserLocation_main(User user, Context context, DataCallback<User> callback) {
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

        ArrayList<Double> newLocation = new ArrayList<>();
        newLocation.add(location.getLatitude());
        newLocation.add(location.getLongitude());
        user.setLocation(newLocation);

        userRepository.updateLocation(user, newLocation, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Updates a user's location based on specific latitude and longitude.
     *
     * @param user      The {@code User} whose location is to be updated.
     * @param latitude  The latitude of the new location.
     * @param longitude The longitude of the new location.
     * @param callback  The callback to handle the result of the operation.
     */
    public void updateUserLocation_eventdetail(User user, double latitude, double longitude, DataCallback<User> callback) {
        if (user == null) {
            callback.onError(new IllegalArgumentException("User cannot be null"));
            return;
        }

        ArrayList<Double> location = new ArrayList<>();
        location.add(latitude);
        location.add(longitude);
        user.setLocation(location);

        userRepository.updateUserDetails(user, null, callback);
    }
}
