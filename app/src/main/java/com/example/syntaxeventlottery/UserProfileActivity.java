package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private Button backButton, editButton; // Change ImageButton to Button for backButton, and add editButton
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView;

    // Firebase Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton); // Initialize editButton
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load user profile from Firestore
        loadUserProfile();

        // Set click listener for back button
        backButton.setOnClickListener(v -> finish());

        // Set click listener for edit button
        editButton.setOnClickListener(v -> {
            // Start an activity to edit user profile or show edit dialog
            Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        // Assume "userId" is the unique ID of the user we want to load
        String userId = "exampleUserId"; // Replace with actual user ID

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve and display user data
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String profileImageUrl = document.getString("profileImageUrl");

                            // Set data to views
                            nameTextView.setText(name);
                            emailTextView.setText(email);
                            phoneTextView.setText(phone);

                            // Load profile image if URL exists
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(profileImageUrl)
                                        .into(profileImageView);
                            }
                        } else {
                            Toast.makeText(UserProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
