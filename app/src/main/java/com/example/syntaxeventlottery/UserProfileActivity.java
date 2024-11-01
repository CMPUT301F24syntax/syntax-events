package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

/**
 * UserProfileActivity handles the display, editing, and saving of a user's profile information.
 * It uses Firebase through the UserRepository to load and update data.
 */
public class UserProfileActivity extends AppCompatActivity {

    // Initialize variables
    private EditText edit_text_username, edit_text_email, edit_text_phone;
    private Button save_button;
    private UserRepository user_repository;

    // Will be replaced when user ID is set up
    private String user_ID = "test_user";

    /**
     * Called when the activity is first created. Initializes UI components, sets up the user repository,
     * and loads the user data from firebase. Sets up a click listener for save button.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Use user profile activity xml
        setContentView(R.layout.user_profile_activity);

        // Initialize variables for UI components
        edit_text_username = findViewById(R.id.edit_text_username);
        edit_text_email = findViewById(R.id.edit_text_email);
        edit_text_phone = findViewById(R.id.edit_text_phone);
        save_button = findViewById(R.id.button_save);

        // Create the users repository to store their profile
        user_repository = new UserRepository();

        // Call load user data function
        load_user_data();

        // Assign an onclick listener lambda expression to the save button
        save_button.setOnClickListener(v -> save_user_data());
    }

    // Need to implement
    private void load_user_data() {
    }

    // Need to implement
    private void save_user_data() {
    }
}