// UserProfileActivity.java

package com.example.syntaxeventlottery;

import android.content.Intent;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;


/**
 * The {@code UserProfileActivity} class displays the user's profile information, including
 * name, email, phone number, facility, and profile picture. Users can edit their profile
 * or update their profile picture by selecting a new image from their device.
 */
public class UserProfileActivity extends AppCompatActivity {

    //UI Components
    private Button backButton;
    private Button editButton;
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView;



    private User currentUser;
    private UserController userController;
    private String deviceID;
    private static final String TAG = "UserProfileActivity";
    private static final int REQUEST_CODE_EDIT_PROFILE = 2001;


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

        userController = new UserController(new UserRepository());

        // Retrieve device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceID);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);

        loadUserProfile();

        // Set click listeners
        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
            intent.putExtra("deviceID", deviceID); // Pass user to edit activity
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            loadUserProfile();
        }
    }

    public void loadUserProfile() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUser = userController.getUserByDeviceID(deviceID);
                displayUserDetails(currentUser);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load user profile", e);
                Toast.makeText(UserProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayUserDetails(User user) {
        nameTextView.setText(user.getUsername());
        emailTextView.setText(user.getEmail());
        phoneTextView.setText(user.getPhoneNumber());

        Glide.with(this)
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView);

        Log.d(TAG, "User profile data loaded successfully");
    }
}