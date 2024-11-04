package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Handles the creation and updating of an Entrant's profile.
 */
public class UserProfileActivity extends AppCompatActivity {

    // UI Components
    private EditText edit_text_username, edit_text_email, edit_text_phone;
    private Button button_save, button_back;
    private ImageView image_view_avatar;

    // Repository for data operations
    private UserRepository user_repository;

    // Device ID
    private String deviceId;

    // Entrant data if exists
    private Entrant existingEntrant = null;

    private Uri selectedImageUri = null;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    image_view_avatar.setImageURI(selectedImageUri);
                }
            }
    );

    /**
     * Initializes the activity, sets up UI components, and checks for existing Entrant data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_user_profile); // Ensure correct layout name

        // Initialize UI components
        edit_text_username = findViewById(R.id.edit_text_username);
        edit_text_email = findViewById(R.id.edit_text_email);
        edit_text_phone = findViewById(R.id.edit_text_phone);
        button_save = findViewById(R.id.button_save);
        button_back = findViewById(R.id.button_back);
        image_view_avatar = findViewById(R.id.image_view_avatar);

        // Initialize UserRepository
        user_repository = new UserRepository();

        // Retrieve device ID from Intent
        Intent intent = getIntent();
        deviceId = intent.getStringExtra("DEVICE_ID");

        if (deviceId == null || deviceId.isEmpty()) {
            // Fallback to ANDROID_ID if not passed via Intent
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        // Check if Entrant with this device ID already exists
        user_repository.checkEntrantExists(deviceId, new UserRepository.OnCheckEntrantExistsListener() {
            @Override
            public void onCheckComplete(boolean exists, Entrant entrant) {
                if (exists && entrant != null) {
                    existingEntrant = entrant;
                    populateProfile(entrant);
                    Toast.makeText(UserProfileActivity.this, "Existing profile found. You can update your information.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "No existing profile found. Please create a new profile.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCheckError(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Error checking profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listeners
        button_save.setOnClickListener(v -> {
            save_entrant_data();

            // Create an intent to navigate to UserHomeActivity after saving data
            Intent jump_to_home = new Intent(UserProfileActivity.this, UserHomeActivity.class);
            startActivity(jump_to_home); // Use the correct intent here

            finish();
        });

        image_view_avatar.setOnClickListener(v -> openImagePicker());
        button_back.setOnClickListener(v -> finish());
    }

    /**
     * Opens the image picker to allow the user to select a new avatar image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Populates the UI fields with existing Entrant data for updating.
     *
     * @param entrant The existing Entrant object.
     */
    private void populateProfile(Entrant entrant) {
        edit_text_username.setText(entrant.getUsername());
        edit_text_email.setText(entrant.getEmail());
        edit_text_phone.setText(entrant.getPhoneNumber());

        // Load profile photo using Glide
        if (entrant.getProfilePhotoUrl() != null && !entrant.getProfilePhotoUrl().isEmpty()) {
            Glide.with(UserProfileActivity.this)
                    .load(entrant.getProfilePhotoUrl())
                    .into(image_view_avatar);
        } else {
            // Set default avatar if no photo URL
            image_view_avatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    /**
     * Saves or updates the Entrant's profile data to Firebase.
     */
    private void save_entrant_data() {
        String username = edit_text_username.getText().toString().trim();
        String email = edit_text_email.getText().toString().trim();
        String phone = edit_text_phone.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Username and Email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use deviceId as userId for uniqueness
        String userId = deviceId;

        // Create an Entrant object with updated data
        Entrant entrant = new Entrant();
        entrant.setUsername(username);
        entrant.setEmail(email);
        entrant.setPhoneNumber(phone);
        entrant.setDeviceCode(deviceId);
        entrant.setUserID(userId); // Ensure userID is set

        // Handle profile photo upload if a new image was selected
        if (selectedImageUri != null) {
            user_repository.uploadProfilePhoto(userId, selectedImageUri, new UserRepository.OnUploadCompleteListener() {
                @Override
                public void onUploadSuccess(Uri downloadUrl) {
                    entrant.setProfilePhotoUrl(downloadUrl.toString());
                    // Save Entrant data to Firebase
                    user_repository.updateEntrant(userId, entrant, new UserRepository.OnEntrantUpdateListener() {
                        @Override
                        public void onEntrantUpdateSuccess() {
                            Toast.makeText(UserProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onEntrantUpdateError(Exception e) {
                            Toast.makeText(UserProfileActivity.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onUploadFailure(Exception e) {
                    Toast.makeText(UserProfileActivity.this, "Error uploading profile photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If no new image, retain existing photo URL if updating, or set to null if creating new
            if (existingEntrant != null) {
                entrant.setProfilePhotoUrl(existingEntrant.getProfilePhotoUrl());
            } else {
                entrant.setProfilePhotoUrl(null); // Or set a default URL
            }

            // Save Entrant data to Firebase
            user_repository.updateEntrant(userId, entrant, new UserRepository.OnEntrantUpdateListener() {
                @Override
                public void onEntrantUpdateSuccess() {
                    Toast.makeText(UserProfileActivity.this, "Profile saved successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onEntrantUpdateError(Exception e) {
                    Toast.makeText(UserProfileActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

