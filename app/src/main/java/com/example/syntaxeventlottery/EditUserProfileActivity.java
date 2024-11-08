package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * The {@code EditUserProfileActivity} class allows users to edit their profile information,
 * including name, email, phone number, facility, and profile photo.
 * Users can upload a new profile photo, reset it to a default image, and save their updated information.
 * The class interacts with Firebase Firestore and Firebase Storage to retrieve and update user data.
 */
public class EditUserProfileActivity extends AppCompatActivity {

    /** EditText for entering the user's name. */
    private EditText nameEditText;

    /** EditText for entering the user's email. */
    private EditText emailEditText;

    /** EditText for entering the user's phone number. */
    private EditText phoneEditText;

    /** EditText for entering the user's facility. */
    private EditText facilityEditText;

    /** Button to save the updated profile information. */
    private Button saveButton;

    /** ImageButton to navigate back to the previous screen. */
    private ImageButton backButton;

    /** ImageButton to upload a new profile image. */
    private ImageButton uploadImageButton;

    /** ImageButton to reset the profile image to the default. */
    private ImageButton resetImageButton;

    /** ImageView to display the user's profile image. */
    private ImageView profileImageView;

    /** Firebase Firestore database instance. */
    private FirebaseFirestore db;

    /** Firebase Storage reference for storing profile images. */
    private StorageReference storageRef;

    /** Unique device ID used to identify the user in the database. */
    private String deviceID;

    /** URI of the selected image from the image picker. */
    private Uri selectedImageUri;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState}. <b>Note: Otherwise, it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_profile);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        resetImageButton = findViewById(R.id.resetImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.editName);
        emailEditText = findViewById(R.id.editEmail);
        phoneEditText = findViewById(R.id.editPhone);
        facilityEditText = findViewById(R.id.editFacility);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Retrieve deviceID from system
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (deviceID != null && !deviceID.isEmpty()) {
            loadUserProfileByDeviceID(deviceID);
        } else {
            Toast.makeText(this, "Device ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listener for back button
        backButton.setOnClickListener(v -> finish());

        // Set click listener for upload image button
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        // Set click listener for reset image button
        resetImageButton.setOnClickListener(v -> resetProfilePhoto());

        // Set click listener for save button
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    /**
     * Opens the image picker to allow the user to select a new profile image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }

    /**
     * Handles the result from the image picker activity.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        An Intent that carries the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
            uploadProfilePhoto();
        }
    }

    /**
     * Loads user profile data from Firestore using the device ID and displays it in the UI components.
     *
     * @param deviceID The unique device ID used to retrieve the user's profile.
     */
    private void loadUserProfileByDeviceID(String deviceID) {
        db.collection("Users").whereEqualTo("deviceCode", deviceID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String name = document.getString("username");
                            String email = document.getString("email");
                            String phone = document.getString("phoneNumber");
                            String facility = document.getString("facility");
                            String profileImageUrl = document.getString("profilePhotoUrl");

                            // Set user information to UI components
                            nameEditText.setText(name);
                            emailEditText.setText(email);
                            phoneEditText.setText(phone);
                            facilityEditText.setText(facility);

                            // Load profile image with Glide
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                loadImageWithGlide(profileImageUrl);
                            } else {
                                profileImageView.setImageResource(R.drawable.ic_avatar_placeholder);
                            }
                        } else {
                            Toast.makeText(EditUserProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditUserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Uploads the selected profile photo to Firebase Storage and updates the Firestore database.
     */
    private void uploadProfilePhoto() {
        if (selectedImageUri != null) {
            StorageReference photoRef = storageRef.child("profile_photos/" + deviceID + ".jpg");
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateProfileImage(uri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Error uploading profile photo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Updates the profile image URL in Firestore and displays it.
     *
     * @param profileImageUrl The URL of the uploaded profile image.
     */
    private void updateProfileImage(String profileImageUrl) {
        db.collection("Users").document(deviceID)
                .update("profilePhotoUrl", profileImageUrl)
                .addOnSuccessListener(aVoid -> {
                    loadImageWithGlide(profileImageUrl);
                    Toast.makeText(EditUserProfileActivity.this, "Profile photo updated successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to update profile photo URL.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads an image into the profileImageView using Glide with debugging options.
     *
     * @param imageUrl The URL of the image to load.
     */
    private void loadImageWithGlide(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Image load failed for URL: " + imageUrl, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d("GlideSuccess", "Image loaded successfully for URL: " + imageUrl);
                        return false;
                    }
                })
                .into(profileImageView);
    }

    /**
     * Resets the profile photo URL in Firestore and updates the UI with a placeholder image.
     */
    private void resetProfilePhoto() {
        db.collection("Users").document(deviceID)
                .update("profilePhotoUrl", null) // Set profilePhotoUrl to null in Firestore
                .addOnSuccessListener(aVoid -> {
                    // Update profileImageView with a placeholder image
                    profileImageView.setImageResource(R.drawable.ic_avatar_placeholder);
                    Toast.makeText(EditUserProfileActivity.this, "Profile photo reset successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to reset profile photo.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves the user profile information to Firestore.
     */
    private void saveUserProfile() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String facility = facilityEditText.getText().toString();

        db.collection("Users").document(deviceID)
                .update("username", name, "email", email, "phoneNumber", phone, "facility", facility)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditUserProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();

                    // Navigate to UserHomeActivity
                    Intent intent = new Intent(EditUserProfileActivity.this, UserHomeActivity.class);
                    startActivity(intent);
                    finish(); // Optionally, finish this activity to prevent returning to it
                })
                .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show());
    }
}
