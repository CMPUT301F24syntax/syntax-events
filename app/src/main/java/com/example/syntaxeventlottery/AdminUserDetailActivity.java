package com.example.syntaxeventlottery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.syntaxeventlottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class AdminUserDetailActivity extends AppCompatActivity {

    private ImageView userProfileImage;
    private TextView userName;
    private TextView userId;
    private TextView userEmail;
    private TextView userPhone;
    private Button backButton;
    private Button deleteImageButton;

    private FirebaseFirestore db;
    private String userIdStr; // User ID passed through Intent

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get user ID from Intent
        userIdStr = getIntent().getStringExtra("userId");

        // Initialize views
        userProfileImage = findViewById(R.id.userProfileImage);
        userName = findViewById(R.id.userName);
        userId = findViewById(R.id.userId);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);
        backButton = findViewById(R.id.backButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);

        // Set back button listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity and go back to the previous screen
            }
        });

        // Set delete image button listener
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteImage();
            }
        });

        // Load user details from Firestore
        loadUserDetails();
    }

    private void loadUserDetails() {
        if (userIdStr != null) {
            DocumentReference docRef = db.collection("users").document(userIdStr);
            docRef.get(Source.SERVER)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userName.setText(documentSnapshot.getString("username"));
                            userId.setText("User ID: " + documentSnapshot.getString("userId"));
                            userEmail.setText("Email: " + documentSnapshot.getString("email"));
                            userPhone.setText("Phone: " + documentSnapshot.getString("phoneNumber"));

                            // Load user profile image
                            String profileImageUrl = documentSnapshot.getString("profilePhotoUrl");
                            if (profileImageUrl != null) {
                                Glide.with(this).load(profileImageUrl).into(userProfileImage);
                            }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteImage() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImage();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteImage() {
        userProfileImage.setImageDrawable(null); // Clear the image from the ImageView
        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
    }
}
