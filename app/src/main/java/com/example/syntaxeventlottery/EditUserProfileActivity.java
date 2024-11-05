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

public class EditUserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, phoneEditText;
    private Button saveButton;
    private ImageButton backButton, uploadImageButton;
    private ImageView profileImageView;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String deviceID;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_profile);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.editName);
        emailEditText = findViewById(R.id.editEmail);
        phoneEditText = findViewById(R.id.editPhone);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Retrieve deviceID from Intent or generate it
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

        // Set click listener for save button
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    /**
     * Opens the image picker to allow the user to select a new avatar image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }

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
     * Loads user profile data from Firestore using deviceID and displays it in the UI components.
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
                            String profileImageUrl = document.getString("profilePhotoUrl");

                            // Set user information to UI components
                            nameEditText.setText(name);
                            emailEditText.setText(email);
                            phoneEditText.setText(phone);

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
     * Saves the user profile information to Firestore and navigates to UserHomeActivity upon success.
     */
    private void saveUserProfile() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        db.collection("Users").document(deviceID)
                .update("username", name, "email", email, "phoneNumber", phone)
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
