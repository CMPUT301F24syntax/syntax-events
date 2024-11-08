package com.example.syntaxeventlottery;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class UserController {

    private UserRepository userRepository;
    private Context context;
    private String deviceId;
    private String userId;

    public UserController(Context context) {
        this.context = context;
        userRepository = new UserRepository(); // Use default constructor
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Retrieves the device ID.
     *
     * @return The device ID as a string.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Retrieves the user ID (Firestore document ID).
     *
     * @return The user ID as a string.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Loads the user profile data and stores the user ID.
     *
     * @param listener Callback to handle the retrieved data or errors.
     */
    public void loadUserProfile(UserRepository.OnEntrantDataFetchListener listener) {
        userRepository.getEntrantByDeviceId(context, new UserRepository.OnEntrantDataFetchListener() {
            @Override
            public void onEntrantDataFetched(Entrant entrant) {
                if (entrant != null) {
                    userId = entrant.getUserID(); // Store the actual user ID (document ID)
                }
                listener.onEntrantDataFetched(entrant);
            }

            @Override
            public void onEntrantDataFetchError(Exception e) {
                listener.onEntrantDataFetchError(e);
            }
        });
    }

    /**
     * Uploads a profile photo for the user.
     *
     * @param fileUri  The URI of the image file to upload.
     * @param listener Callback to handle the upload result.
     */
    public void uploadProfilePhoto(Uri fileUri, UserRepository.OnUploadCompleteListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onUploadFailure(new Exception("User ID is null or empty"));
            return;
        }
        userRepository.uploadProfilePhoto(userId, fileUri, listener);
    }

    /**
     * Updates the user's profile photo URL in Firestore.
     *
     * @param profileImageUrl The URL of the uploaded profile image.
     * @param listener        Callback to handle success or errors.
     */
    public void updateProfilePhotoUrl(String profileImageUrl, UserRepository.OnEntrantUpdateListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onEntrantUpdateError(new Exception("User ID is null or empty"));
            return;
        }
        userRepository.updateEntrant(userId, "profilePhotoUrl", profileImageUrl, listener);
    }

    /**
     * Retrieves the user's facility information.
     *
     * @param listener Callback to handle the retrieved data or errors.
     */
    public void getUserFacility(UserRepository.OnEntrantDataFetchListener listener) {
        userRepository.getEntrantByDeviceId(context, new UserRepository.OnEntrantDataFetchListener() {
            @Override
            public void onEntrantDataFetched(Entrant entrant) {
                if (entrant != null) {
                    userId = entrant.getUserID(); // Store the actual user ID (document ID)
                }
                listener.onEntrantDataFetched(entrant);
            }

            @Override
            public void onEntrantDataFetchError(Exception e) {
                listener.onEntrantDataFetchError(e);
            }
        });
    }
}
