package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

/**
 * The {@code AdminUserDetailActivity} class displays detailed information about a user
 * to administrators, including username, user ID, email, phone number, and profile image.
 */
public class AdminUserDetailActivity extends AppCompatActivity {

    private Button backButton, deleteImageButton, deleteFacilityButton;
    private TextView userName, userId, userEmail, userPhone, userFacility;
    private ImageView profileImageView;
    private FirebaseFirestore db;
    private String userID;
    private char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        backButton = findViewById(R.id.backButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);
        deleteFacilityButton = findViewById(R.id.deleteFacilityButton);
        userName = findViewById(R.id.detailUserName);
        userId = findViewById(R.id.detailUserId);
        userEmail = findViewById(R.id.detailUserEmail);
        userPhone = findViewById(R.id.detailUserPhone);
        profileImageView = findViewById(R.id.detailUserImage);
        userFacility = findViewById(R.id.detailUserFacility);

        // Get intent data
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        String username = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String profilePhotoUrl = intent.getStringExtra("profilePhotoUrl");
        String facility = intent.getStringExtra("facility");

        // Set data to views
        userName.setText(username);
        userId.setText(userID);
        userEmail.setText(email);
        userPhone.setText(phoneNumber);
        userFacility.setText(facility);

        // Load profile image using Glide
        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
            Glide.with(this)
                    .load(profilePhotoUrl)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_avatar_placeholder); // Set a default avatar if no URL
        }

        // Set up back button functionality
        backButton.setOnClickListener(v -> finish());

        // Set up delete image button functionality
        deleteImageButton.setOnClickListener(v -> deleteProfileImage());

        // Set up delete facility button functionality
        deleteFacilityButton.setOnClickListener(v -> deleteFacility());
    }

    /**
     * Deletes the facility information in Firestore and updates the UI.
     */
    private void deleteFacility() {
        if (userID != null) {
            DocumentReference userRef = db.collection("Users").document(userID);
            userRef.update("facility", null)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Facility deleted", Toast.LENGTH_SHORT).show();
                        userFacility.setText("No Facility"); // Update the UI to indicate no facility
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminUserDetailActivity", "Failed to delete facility", e);
                        Toast.makeText(this, "Failed to delete facility", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the user's profile image URL from Firestore and sets a placeholder image.
     */
    private void deleteProfileImage() {
        if (userID != null) {
            Intent intent = getIntent();
            String username = intent.getStringExtra("username");
            String defaultProfileUrl;

            if (username != null && !username.isEmpty()) {
                char firstChar = Character.toLowerCase(username.charAt(0));
                boolean isLetterIncluded = false;

                for (char letter : letters) {
                    if (letter == firstChar) {
                        isLetterIncluded = true;
                        break;
                    }
                }

                if (isLetterIncluded) {
                    char uppercaseFirstChar = Character.toUpperCase(firstChar);
                    defaultProfileUrl = "https://firebasestorage.googleapis.com/v0/b/scanapp-7e377.appspot.com/o/"
                            + uppercaseFirstChar + ".png?alt=media&token=cb8a2589-5092-46bc-acc9-0fc31b9799e8";
                } else {
                    defaultProfileUrl = "https://firebasestorage.googleapis.com/v0/b/scanapp-7e377.appspot.com/o/default.png?alt=media&token=cb8a2589-5092-46bc-acc9-0fc31b9799e8";
                }
            } else {
                defaultProfileUrl = "https://firebasestorage.googleapis.com/v0/b/scanapp-7e377.appspot.com/o/default.png?alt=media&token=cb8a2589-5092-46bc-acc9-0fc31b9799e8";
            }

            // Update profilePhotoUrl in Firestore
            DocumentReference userRef = db.collection("Users").document(userID);
            userRef.update("profilePhotoUrl", defaultProfileUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile image reset to default", Toast.LENGTH_SHORT).show();

                        // Use Glide to show the default picture
                        Glide.with(this)
                                .load(defaultProfileUrl)
                                .into(profileImageView);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminUserDetailActivity", "Failed to reset profile URL in Firestore", e);
                        Toast.makeText(this, "Failed to reset profile URL in Firestore", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }
}