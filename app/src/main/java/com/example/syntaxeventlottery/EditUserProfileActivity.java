package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditUserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, phoneEditText;
    private Button saveButton;
    private ImageButton backButton, uploadImageButton;
    private ImageView profileImageView;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String userId = "exampleUserId"; // Replace with the actual user ID

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

        // Load current user profile data
        loadUserProfile();

        // Set click listener for back button
        backButton.setOnClickListener(v -> finish());

        // Set click listener for upload image button
        uploadImageButton.setOnClickListener(v -> openImagePicker());
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        nameEditText.setText(name);
                        emailEditText.setText(email);
                        phoneEditText.setText(phone);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .into(profileImageView);
                        }
                    } else {
                        Toast.makeText(EditUserProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child("profileImages/" + userId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        db.collection("users").document(userId).update("profileImageUrl", profileImageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(EditUserProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                    Glide.with(this).load(profileImageUrl).into(profileImageView);
                                })
                                .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }
}
