// UserProfileActivity.java

package com.example.syntaxeventlottery;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

/**
 * The {@code UserProfileActivity} class displays the user's profile information, including
 * name, email, phone number, facility, and profile picture. Users can edit their profile
 * or update their profile picture by selecting a new image from their device.
 */
public class UserProfileActivity extends AppCompatActivity {

    /** Button to navigate back to the previous screen. */
    private Button backButton;

    /** Button to navigate to the edit profile screen. */
    private Button editButton;

    /** ImageView to display the user's profile picture. */
    private ImageView profileImageView;

    /** TextView to display the user's name. */
    private TextView nameTextView;

    /** TextView to display the user's email address. */
    private TextView emailTextView;

    /** TextView to display the user's phone number. */
    private TextView phoneTextView;

    /** TextView to display the user's facility information. */
    private TextView facilityTextView;

    /** URI of the selected image from the image picker. */
    private Uri selectedImageUri;

    /** Firebase Firestore instance for database operations. */
    private FirebaseFirestore db;

    /** UserRepository instance for handling user-related database operations. */
    private UserRepository userRepository;

    /** Unique device ID used to identify the user. */
    private String deviceId;

    /**
     * ActivityResultLauncher for the image picker intent.
     * Allows the user to select a new profile picture from the device's gallery.
     */
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImageView.setImageURI(selectedImageUri);
                    uploadProfilePhoto();
                }
            }
    );

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        facilityTextView = findViewById(R.id.facilityTextView); // Initialize facilityTextView

        // Initialize Firestore and UserRepository
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();

        // Retrieve device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (deviceId != null && !deviceId.isEmpty()) {
            loadUserProfileByDeviceId(deviceId); // Load user profile using deviceId
        } else {
            Toast.makeText(this, "Device ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listener for back button
        backButton.setOnClickListener(v -> finish());

        // Set click listener for edit button
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
            intent.putExtra("DEVICE_ID", deviceId); // Pass deviceId to edit activity
            startActivity(intent);
        });

        // Set click listener for profile image to change avatar
        profileImageView.setOnClickListener(v -> openImagePicker());
    }

    /**
     * Opens the image picker to allow the user to select a new avatar image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Uploads the selected profile photo to Firebase Storage and updates the Firestore database.
     */
    private void uploadProfilePhoto() {
        if (selectedImageUri != null) {
            String userId = deviceId; // Using deviceId as user ID
            userRepository.uploadProfilePhoto(userId, selectedImageUri, new UserRepository.OnUploadCompleteListener() {
                @Override
                public void onUploadSuccess(Uri downloadUrl) {
                    updateProfileImage(downloadUrl.toString());
                }

                @Override
                public void onUploadFailure(Exception e) {
                    Toast.makeText(UserProfileActivity.this, "Error uploading profile photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Updates the profile image URL in Firestore and displays it.
     *
     * @param profileImageUrl The URL of the uploaded profile image.
     */
    private void updateProfileImage(String profileImageUrl) {
        db.collection("Users").document(deviceId)
                .update("profilePhotoUrl", profileImageUrl)
                .addOnSuccessListener(aVoid -> {
                    loadImageWithGlide(profileImageUrl);
                    Toast.makeText(UserProfileActivity.this, "Profile photo updated successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to update profile photo URL.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads the user profile data from Firestore using the device ID.
     *
     * @param deviceId The device ID used to retrieve the user data.
     */
    private void loadUserProfileByDeviceId(String deviceId) {
        db.collection("Users").whereEqualTo("deviceCode", deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            // Retrieve user data from Firestore document
                            String username = document.getString("username");
                            String email = document.getString("email");
                            String phoneNumber = document.getString("phoneNumber");
                            String profileImageUrl = document.getString("profilePhotoUrl");
                            String facility = document.getString("facility"); // Retrieve facility information

                            // Set retrieved data to TextViews
                            nameTextView.setText(username);
                            emailTextView.setText(email);
                            phoneTextView.setText(phoneNumber);
                            facilityTextView.setText(facility); // Display facility information

                            // Log profileImageUrl for debugging
                            Log.d("UserProfile", "Fetched profileImageUrl: " + profileImageUrl);

                            // Load profile image using Glide, or set default if URL is null or empty
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                loadImageWithGlide(profileImageUrl);
                            } else {
                                profileImageView.setImageResource(R.drawable.ic_avatar_placeholder); // Set default avatar
                            }
                        } else {
                            Toast.makeText(UserProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
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
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk cache for debugging
                .skipMemoryCache(true) // Disable memory cache for debugging
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Image load failed for URL: " + imageUrl, e);
                        return false; // Allow Glide to handle error placeholder
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d("GlideSuccess", "Image loaded successfully for URL: " + imageUrl);
                        return false;
                    }
                })
                .into(profileImageView);
    }
}
