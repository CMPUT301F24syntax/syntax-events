package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * The {@code EditUserProfileActivity} class allows users to edit their profile details.
 * Users can update their name, email, phone number, profile picture, and notification preferences.
 * Uses {@link UserController} for managing user-related operations.
 */
public class EditUserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, phoneEditText;
    private Button saveButton;
    private ImageButton backButton, uploadImageButton, resetImageButton;
    private ImageView profileImageView;
    private Uri selectedImageUri;

    private User user;
    private UserController userController;
    private String deviceID;
    private static final String TAG = "EditUserProfileActivity";
    private static final int REQUEST_CODE_SELECTED_IMAGE = 1002;
    private Switch notificationSwitch;


    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves the user profile, and sets up button listeners.
     *
     * @param savedInstanceState The saved instance state, or {@code null} if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_profile);

        userController = new UserController(new UserRepository());

        // Retrieve deviceID from system
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (deviceID != null && !deviceID.isEmpty()) {
            Log.d(TAG, "Device ID: " + deviceID);
        } else {
            Toast.makeText(this, "Device ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        resetImageButton = findViewById(R.id.resetImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.editName);
        emailEditText = findViewById(R.id.editEmail);
        phoneEditText = findViewById(R.id.editPhone);
        saveButton = findViewById(R.id.saveButton);
        notificationSwitch = findViewById(R.id.notificationSwitch);


        loadUserProfile();

        // Set button listeners
        backButton.setOnClickListener(v -> finish());
        uploadImageButton.setOnClickListener(v -> openImagePicker());// Set click listener for reset image button
        resetImageButton.setOnClickListener(v -> resetProfilePhoto());
        saveButton.setOnClickListener(v -> saveUserProfile());

    }

    /**
     * Loads the user's profile from the repository.
     */
    private void loadUserProfile() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                user = userController.getUserByDeviceID(deviceID);
                displayUserDetails();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load user profile", e);
                Toast.makeText(EditUserProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Displays the user's profile details in the UI.
     */
    private void displayUserDetails() {
        nameEditText.setText(user.getUsername());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhoneNumber());
        notificationSwitch.setChecked(user.isReceiveNotifications());

        Glide.with(this)
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView);
    }

    /**
     * Saves the updated user profile.
     * Validates the inputs and updates the profile in the repository.
     */
    private void saveUserProfile() {
        String newName = nameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();
        boolean receiveNotifications = notificationSwitch.isChecked();

        // Validate Input
        if (newName.isEmpty()) {
            Toast.makeText(EditUserProfileActivity.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newEmail.isEmpty()) {
            Toast.makeText(EditUserProfileActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            Toast.makeText(EditUserProfileActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPhone.isEmpty()) {
            if (!newPhone.matches("\\d+")) {
                Toast.makeText(EditUserProfileActivity.this, "Phone number must contain only digits", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPhone.length() > 10) {
                Toast.makeText(EditUserProfileActivity.this, "Phone number cannot be more than 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // if user has changed their profile name
        // and did not update their profile photo, reset so that a new profile picture is generated
        if (newName.equals(user.getUsername()) && selectedImageUri == null && user.isDefaultPhoto()) {
            resetProfilePhoto();
        }

        user.setUsername(newName);
        user.setEmail(newEmail);
        user.setPhoneNumber(newPhone);
        user.setReceiveNotifications(receiveNotifications);

        userController.updateUser(user, selectedImageUri, new DataCallback<User>() {
            @Override
            public void onSuccess(User updatedUser) {
                Toast.makeText(EditUserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to update profile", e);
                Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Resets the user's profile photo to the default state.
     */
    private void resetProfilePhoto() {
        selectedImageUri = null;
        user.setProfilePhotoUrl(null);
        user.setDefaultPhoto(true);  // Mark that this user now has a default profile photo
        displayUserDetails();
    }

    /**
     * Opens the image picker for the user to select a new profile picture.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECTED_IMAGE);
    }

    /**
     * Handles the result of the image picker activity.
     *
     * @param requestCode The request code passed to the image picker.
     * @param resultCode  The result code returned by the image picker.
     * @param data        The intent containing the selected image URI.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECTED_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri);
                user.setDefaultPhoto(false);
                Log.d(TAG, "Profile photo updated successfully");
            }
        }
    }
}