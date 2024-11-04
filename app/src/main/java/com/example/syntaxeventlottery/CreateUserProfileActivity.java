package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Handles the creation and updating of an Entrant's profile.
 */
public class CreateUserProfileActivity extends AppCompatActivity {

    // UI Components
    private EditText edit_text_username, edit_text_email, edit_text_phone;
    private Button button_save, button_back;
    private ImageView image_view_avatar;
    private ImageButton button_remove_photo;

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
                    button_remove_photo.setVisibility(View.VISIBLE); // Show remove button when image is selected
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
        button_remove_photo = findViewById(R.id.button_remove_photo);
        button_remove_photo.setVisibility(View.GONE); // Initially hide remove button

        // Initialize UserRepository
        user_repository = new UserRepository();

        // Retrieve device ID from Intent
        Intent intent = getIntent();
        deviceId = intent.getStringExtra("DEVICE_ID");

        if (deviceId == null || deviceId.isEmpty()) {
            // Fallback to ANDROID_ID if not passed via Intent
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        user_repository.checkEntrantExists(deviceId, new UserRepository.OnCheckEntrantExistsListener() {
            @Override
            public void onCheckComplete(boolean exists, Entrant entrant) {
                if (exists && entrant != null) {
                    existingEntrant = entrant;
                    populateProfile(entrant);
                    Toast.makeText(CreateUserProfileActivity.this, "Profile exists, can edit.", Toast.LENGTH_LONG).show();
                    Log.d("EntrantStatus", "Entrant loaded successfully: " + entrant.toString());
                    button_remove_photo.setVisibility(View.VISIBLE); // 显示移除按钮
                } else {
                    Toast.makeText(CreateUserProfileActivity.this, "No profile, can't edit.", Toast.LENGTH_LONG).show();
                    button_remove_photo.setVisibility(View.GONE); // 隐藏移除按钮
                    Log.d("EntrantStatus", "No existing entrant found.");
                }
            }

            @Override
            public void onCheckError(Exception e) {
                Toast.makeText(CreateUserProfileActivity.this, "profile check error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EntrantStatus", "Error checking entrant: " + e.getMessage());
            }
        });

        // Set click listeners
        button_save.setOnClickListener(v -> {
            save_entrant_data();

            // Create an intent to navigate to UserHomeActivity after saving data
            Intent jump_to_home = new Intent(CreateUserProfileActivity.this, UserHomeActivity.class);
            startActivity(jump_to_home);
            finish();
        });

        image_view_avatar.setOnClickListener(v -> openImagePicker());
        button_remove_photo.setOnClickListener(v -> confirmRemovePhoto());
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
     * Prompts the user with a confirmation dialog to remove the current profile photo.
     */
    private void confirmRemovePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Profile Photo");
        builder.setMessage("Are you sure you want to remove your profile photo?");
        builder.setPositiveButton("Yes", (dialog, which) -> removeProfilePhoto());
        builder.setNegativeButton("No", null);
        builder.show();
    }
    /**
    /**
     * Removes the Entrant's profile photo.
     */
    private void removeProfilePhoto() {
        image_view_avatar.setImageResource(R.drawable.ic_avatar_placeholder);
        button_remove_photo.setVisibility(View.GONE); // Hide remove button
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
            Glide.with(CreateUserProfileActivity.this)
                    .load(entrant.getProfilePhotoUrl())
                    .into(image_view_avatar);
            button_remove_photo.setVisibility(View.VISIBLE); // Show remove button if photo exists
        } else {
            // Set default avatar if no photo URL
            image_view_avatar.setImageResource(R.drawable.ic_avatar_placeholder);
            button_remove_photo.setVisibility(View.GONE); // Hide remove button
        }
    }

    /**
     * Saves the Entrant's profile data to Firebase, including uploading a new profile photo if selected.
     */
    private void save_entrant_data() {
        String username = edit_text_username.getText().toString().trim();
        String email = edit_text_email.getText().toString().trim();
        String phone = edit_text_phone.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Username and Email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = existingEntrant != null ? existingEntrant.getUserID() : deviceId;

        Entrant entrant = new Entrant();
        entrant.setUsername(username);
        entrant.setEmail(email);
        entrant.setPhoneNumber(phone);
        entrant.setDeviceCode(deviceId);
        entrant.setUserID(userId);

        if (selectedImageUri != null) {
            user_repository.uploadProfilePhoto(userId, selectedImageUri, new UserRepository.OnUploadCompleteListener() {
                @Override
                public void onUploadSuccess(Uri downloadUrl) {
                    entrant.setProfilePhotoUrl(downloadUrl.toString());
                    updateEntrantData(userId, entrant);
                }

                @Override
                public void onUploadFailure(Exception e) {
                    Toast.makeText(CreateUserProfileActivity.this, "Error uploading profile photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Retain existing photo URL if updating, or set to null if creating new
            if (existingEntrant != null && existingEntrant.getProfilePhotoUrl() != null) {
                entrant.setProfilePhotoUrl(existingEntrant.getProfilePhotoUrl());
            } else {
                entrant.setProfilePhotoUrl(null); // Or set to a default URL
            }
            updateEntrantData(userId, entrant);
        }
    }

    /**
     * Updates the Entrant's profile data to Firebase, including uploading a new profile photo if selected.
     */
    private void updateEntrantData(String userId, Entrant entrant) {
        user_repository.updateEntrant(userId, entrant, new UserRepository.OnEntrantUpdateListener() {
            @Override
            public void onEntrantUpdateSuccess() {
                Toast.makeText(CreateUserProfileActivity.this, "Profile saved successfully.", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onEntrantUpdateError(Exception e) {
                Toast.makeText(CreateUserProfileActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigates the user to the UserHomeActivity.
     */
    private void navigateToHome() {
        Intent jump_to_home = new Intent(CreateUserProfileActivity.this, UserHomeActivity.class);
        startActivity(jump_to_home);
        finish();
    }
}
