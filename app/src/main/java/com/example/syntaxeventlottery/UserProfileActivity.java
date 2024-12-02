package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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

    // UI Components
    private Button backButton;
    private Button editButton;
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView, facilityTextView;
    private Switch allowNotificationSwitch;

    private User currentUser;
    private UserController userController;
    private String deviceID;
    private static final String TAG = "UserProfileActivity";
    private static final int REQUEST_CODE_EDIT_PROFILE = 2001;

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
        facilityTextView = findViewById(R.id.facilityTextView);
        allowNotificationSwitch = findViewById(R.id.NotificationPermission);

        loadUserProfile();

        // Set click listeners
        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
            intent.putExtra("deviceID", deviceID);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
        });

        allowNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentUser.setAllowNotification(isChecked);
                userController.updateUser(currentUser, null, new DataCallback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        Toast.makeText(UserProfileActivity.this, "Notification preference updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to update notification preference", e);
                        Toast.makeText(UserProfileActivity.this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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

        if (user.getFacility() == null || user.getFacility().getName().isEmpty()) {
            facilityTextView.setText("No Facility Profile");
        } else {
            facilityTextView.setText("Facility Name: " + user.getFacility().getName());
        }

        Glide.with(this)
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView);

        allowNotificationSwitch.setChecked(user.isAllowNotification());

        Log.d(TAG, "User profile data loaded successfully");
    }
}