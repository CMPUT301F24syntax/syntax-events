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

    private void saveUserProfile() {
        String newName = nameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();
        boolean receiveNotifications = notificationSwitch.isChecked();

        // if user has changed their profile name
        // and did not update their profile photo, reset so that a new profile picture is generated
        if (!user.getUsername().equals(newName)) {
            if (user.getProfilePhotoUrl() == null && selectedImageUri == null) {
                resetProfilePhoto();
            }
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

    private void resetProfilePhoto() {
        selectedImageUri = null;
        user.setProfilePhotoUrl(null);
        displayUserDetails();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECTED_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECTED_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri);
                Log.d(TAG, "Profile photo updated successfully");
            }
        }
    }
}