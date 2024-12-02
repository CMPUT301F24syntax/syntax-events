package com.example.syntaxeventlottery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

/**
 * The {@code AdminUserDetailActivity} class displays detailed information about a user
 * to administrators. It provides functionality to view and manage user details,
 * including username, user ID, email, phone number, profile image, and facility information.
 */
public class AdminUserDetailActivity extends AppCompatActivity {
    private final String TAG="AdminUserDetailActivity";

    private Button backButton, deleteImageButton, deleteFacilityButton;
    private TextView userNameTextView, userIdTextView, userEmailTextView, userPhoneTextView, userFacilityNameTextView, userFacilityLocationTextView;
    private ImageView profileImageView;
    private UserController userController;
    private EventController eventController;
    private String userID;
    private User user;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up button functionality, and loads user details.
     *
     * @param savedInstanceState The saved instance state, or {@code null} if none exists.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);

        // Initialize controllers
        userController = new UserController(new UserRepository());
        eventController = new EventController(new EventRepository());

        // Initialize views
        backButton = findViewById(R.id.backButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);
        deleteFacilityButton = findViewById(R.id.deleteFacilityButton);
        userNameTextView = findViewById(R.id.detailUserName);
        userIdTextView = findViewById(R.id.detailUserId);
        userEmailTextView = findViewById(R.id.detailUserEmail);
        userPhoneTextView = findViewById(R.id.detailUserPhone);
        profileImageView = findViewById(R.id.detailUserImage);
        userFacilityNameTextView = findViewById(R.id.detailUserFacilityName);
        userFacilityLocationTextView = findViewById(R.id.detailUserFacilityLocation);

        // get user id from intent
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

        loadUserDetails();

        // Set up back button functionality
        backButton.setOnClickListener(v -> finish());

        // Set up delete image button functionality
        deleteImageButton.setOnClickListener(v -> deleteProfileImage());

        // Set up delete facility button functionality
        deleteFacilityButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Facility")
                    .setMessage("Are you sure you want to delete this facility profile? All events associated with this facility will also be deleted.")
                    .setPositiveButton("Yes, Delete", (dialog, which) -> {
                        deleteFacility();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss(); // Dismiss the dialog if the user cancels
                    })
                    .setCancelable(false) // Prevent dismissal by clicking outside the dialog
                    .show();
        });
    }

    /**
     * Loads the user's details from the repository and updates the UI.
     */
    private void loadUserDetails() {
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                user = userController.getUserByDeviceID(userID);
                if (user == null) {
                    Log.e(TAG, "Couldn't find User");
                    Toast.makeText(AdminUserDetailActivity.this, "Couldn't load user details", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                // Set data to views
                userNameTextView.setText(user.getUsername());
                userIdTextView.setText(user.getUserID());
                userEmailTextView.setText(user.getEmail());
                userPhoneTextView.setText(user.getPhoneNumber());

                // Load user profile image
                if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                    Glide.with(AdminUserDetailActivity.this)
                            .load(user.getProfilePhotoUrl())
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile); // Set default image if no URL
                    deleteImageButton.setText("No Profile Image");
                    deleteImageButton.setEnabled(false);
                }

                // if user does not have a facility, hide the button
                if (user.getFacility() == null) {
                    deleteFacilityButton.setEnabled(false);
                    deleteFacilityButton.setVisibility(View.GONE);
                    userFacilityNameTextView.setText("No facility profile");
                    userFacilityLocationTextView.setText("No facility profile");
                } else {
                    userFacilityNameTextView.setText(user.getFacility().getName());
                    userFacilityLocationTextView.setText(user.getFacility().getLocation());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to update user repository", e);
                Toast.makeText(AdminUserDetailActivity.this, "Failed to load User Details repository", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Deletes the user's facility profile and all associated events.
     */
    private void deleteFacility() {
        user.setFacility(null);
        userController.updateUser(user, null, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Toast.makeText(AdminUserDetailActivity.this, "Deleted user facility profile", Toast.LENGTH_SHORT).show();
                deleteFacilityEvents(result.getUserID());
                loadUserDetails();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "failed to delete facility", e);
                Toast.makeText(AdminUserDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes all events associated with the user's facility profile.
     *
     * @param userID The ID of the user whose facility events are to be deleted.
     */
    private void deleteFacilityEvents(String userID) {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                ArrayList<Event> facilityEvents = eventController.getOrganizerEvents(userID);
                if (facilityEvents.isEmpty()) {
                    Log.d(TAG, "No events to delete for this facility");
                    Toast.makeText(AdminUserDetailActivity.this, "Facility profile deleted successfully", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Event e : facilityEvents) {
                    eventController.deleteEvent(e, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {

                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Failed to delete an event");
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to refresh event repository");
            }
        });
    }

    /**
     * Deletes the user's profile image URL from Firestore and sets a placeholder image.
     */
    private void deleteProfileImage() {
        userController.deleteUserProfilePhoto(user, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Toast.makeText(AdminUserDetailActivity.this, "Deleted user profile image", Toast.LENGTH_SHORT).show();
                loadUserDetails();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "failed to delete profile image", e);
                Toast.makeText(AdminUserDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}