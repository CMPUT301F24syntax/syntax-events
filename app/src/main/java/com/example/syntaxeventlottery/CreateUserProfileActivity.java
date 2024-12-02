// CreateUserProfileActivity.java

package com.example.syntaxeventlottery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;


/**
 * Handles the creation and updating of an Entrant's profile.
 */
public class CreateUserProfileActivity extends AppCompatActivity {
    private final String TAG = "CreateUserProfileActivity";

    // UI Components
    private EditText editUsername, editEmail, editPhone;
    private Button btnSave, btnBack;
    private ImageView imgAvatar;
    private Button uploadProfilePictureButton;

    private UserController userController;

    private String deviceId;
    private Uri selectedImageUri;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgAvatar.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_user_profile);

        // Initialize UI components
        initializeUI();

        // Initialize user controller
        userController = new UserController(new UserRepository());

        // Retrieve or generate device ID
        deviceId = getIntent().getStringExtra("DEVICE_ID");
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize UI components.
     */
    private void initializeUI() {
        editUsername = findViewById(R.id.userNameEditText);
        editEmail = findViewById(R.id.edit_text_email);
        editPhone = findViewById(R.id.edit_text_phone);
        btnSave = findViewById(R.id.button_save);
        btnBack = findViewById(R.id.button_back);
        imgAvatar = findViewById(R.id.image_view_avatar);
        uploadProfilePictureButton = findViewById(R.id.upload_profile_button);
    }


    /**
     * Set click listeners for buttons.
     */
    private void setClickListeners() {
        btnSave.setOnClickListener(v -> saveUserData());
        uploadProfilePictureButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Saves the Entrant's profile data to Firebase, including uploading a new profile photo if selected.
     */
    private void saveUserData() {
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        // Validate Input
        if (username.isEmpty()) {
            Toast.makeText(CreateUserProfileActivity.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(CreateUserProfileActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            Toast.makeText(CreateUserProfileActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.isEmpty()) {
            if (!phone.matches("\\d+")) {
                Toast.makeText(CreateUserProfileActivity.this, "Phone number must contain only digits", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() > 10) {
                Toast.makeText(CreateUserProfileActivity.this, "Phone number cannot be more than 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Entrant entrant = new Entrant(deviceId, email, phone, null, username, new HashSet<String>());

        userController.addUser(entrant, selectedImageUri, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Log.d(TAG, "User saved");
                Toast.makeText(CreateUserProfileActivity.this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error creating new user profile", e);
                Toast.makeText(CreateUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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