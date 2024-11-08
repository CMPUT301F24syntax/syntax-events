// CreateUserProfileActivity.java

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
    private EditText editUsername, editEmail, editPhone;
    private Button btnSave, btnBack;
    private ImageView imgAvatar;
    private ImageButton btnRemovePhoto;

    // Repository for data operations
    private UserRepository userRepository;

    // Device ID and Entrant data
    private String deviceId;
    private Entrant existingEntrant = null;
    private Uri selectedImageUri = null;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgAvatar.setImageURI(selectedImageUri);
                    btnRemovePhoto.setVisibility(View.VISIBLE); // Show remove button when image is selected
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_user_profile);

        // Initialize UI components
        initializeUI();

        // Initialize UserRepository
        userRepository = new UserRepository();

        // Retrieve or generate device ID
        retrieveDeviceId();

        // Check if user profile exists
        checkExistingProfile();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize UI components.
     */
    private void initializeUI() {
        editUsername = findViewById(R.id.edit_text_username);
        editEmail = findViewById(R.id.edit_text_email);
        editPhone = findViewById(R.id.edit_text_phone);
        btnSave = findViewById(R.id.button_save);
        btnBack = findViewById(R.id.button_back);
        imgAvatar = findViewById(R.id.image_view_avatar);
        btnRemovePhoto = findViewById(R.id.button_remove_photo);
        btnRemovePhoto.setVisibility(View.GONE); // Initially hide remove button
    }

    /**
     * Retrieve the device ID, either from Intent or using Android ID.
     */
    private void retrieveDeviceId() {
        deviceId = getIntent().getStringExtra("DEVICE_ID");
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    /**
     * Check if there is an existing profile for the current device ID.
     */
    private void checkExistingProfile() {
        userRepository.checkEntrantExists(deviceId, new UserRepository.OnCheckEntrantExistsListener() {
            @Override
            public void onCheckComplete(boolean exists, Entrant entrant) {
                if (exists && entrant != null) {
                    existingEntrant = entrant;
                    populateProfile(entrant);
                    Toast.makeText(CreateUserProfileActivity.this, "Profile exists. You can edit.", Toast.LENGTH_LONG).show();
                    Log.d("EntrantStatus", "Entrant loaded successfully: " + entrant);
                } else {
                    Toast.makeText(CreateUserProfileActivity.this, "No profile found. Please create one.", Toast.LENGTH_LONG).show();
                    Log.d("EntrantStatus", "No existing entrant found.");
                }
            }

            @Override
            public void onCheckError(Exception e) {
                Toast.makeText(CreateUserProfileActivity.this, "Error checking profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EntrantStatus", "Error checking entrant: " + e.getMessage());
            }
        });
    }

    /**
     * Set click listeners for buttons.
     */
    private void setClickListeners() {
        btnSave.setOnClickListener(v -> saveEntrantData());
        imgAvatar.setOnClickListener(v -> openImagePicker());
        btnRemovePhoto.setOnClickListener(v -> confirmRemovePhoto());
        btnBack.setOnClickListener(v -> finish());
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
        new AlertDialog.Builder(this)
                .setTitle("Remove Profile Photo")
                .setMessage("Are you sure you want to remove your profile photo?")
                .setPositiveButton("Yes", (dialog, which) -> removeProfilePhoto())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Removes the Entrant's profile photo.
     */
    private void removeProfilePhoto() {
        imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        btnRemovePhoto.setVisibility(View.GONE); // Hide remove button
        selectedImageUri = null; // Clear selected image URI
    }

    /**
     * Populates the UI fields with existing Entrant data for updating.
     *
     * @param entrant The existing Entrant object.
     */
    private void populateProfile(Entrant entrant) {
        editUsername.setText(entrant.getUsername());
        editEmail.setText(entrant.getEmail());
        editPhone.setText(entrant.getPhoneNumber());

        // Load profile photo using Glide
        if (entrant.getProfilePhotoUrl() != null && !entrant.getProfilePhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(entrant.getProfilePhotoUrl())
                    .into(imgAvatar);
            btnRemovePhoto.setVisibility(View.VISIBLE);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            btnRemovePhoto.setVisibility(View.GONE);
        }
    }

    /**
     * Saves the Entrant's profile data to Firebase, including uploading a new profile photo if selected.
     */
    private void saveEntrantData() {
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Username and Email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = (existingEntrant != null) ? existingEntrant.getUserID() : deviceId;

        Entrant entrant = new Entrant();
        entrant.setUsername(username);
        entrant.setEmail(email);
        entrant.setPhoneNumber(phone);
        entrant.setDeviceCode(deviceId);
        entrant.setUserID(userId);

        if (selectedImageUri != null) {
            userRepository.uploadProfilePhoto(userId, selectedImageUri, new UserRepository.OnUploadCompleteListener() {
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
            entrant.setProfilePhotoUrl((existingEntrant != null) ? existingEntrant.getProfilePhotoUrl() : null);
            updateEntrantData(userId, entrant);
        }
    }

    /**
     * Updates the Entrant's profile data in Firebase.
     */
    private void updateEntrantData(String userId, Entrant entrant) {
        userRepository.updateEntrant(userId, entrant, new UserRepository.OnEntrantUpdateListener() {
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
        Intent intent = new Intent(CreateUserProfileActivity.this, UserHomeActivity.class);
        startActivity(intent);
        finish();
    }
}