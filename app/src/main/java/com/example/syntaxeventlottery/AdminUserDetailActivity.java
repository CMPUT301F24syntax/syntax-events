package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

/**
 * The {@code AdminUserDetailActivity} class displays detailed information about a user
 * to administrators, including username, user ID, email, phone number, and profile image.
 */
public class AdminUserDetailActivity extends AppCompatActivity {

    /** Button to navigate back to the previous activity. */
    private Button backButton;

    /** TextView for displaying the user's name. */
    private TextView userName;

    /** TextView for displaying the user's ID. */
    private TextView userId;

    /** TextView for displaying the user's email. */
    private TextView userEmail;

    /** TextView for displaying the user's phone number. */
    private TextView userPhone;

    /** ImageView for displaying the user's profile image. */
    private ImageView userImage;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState}. <b>Note: Otherwise, it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        // Back Button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Finish the activity to go back

        // Initialize views
        userName = findViewById(R.id.detailUserName);
        userId = findViewById(R.id.detailUserId);
        userEmail = findViewById(R.id.detailUserEmail);
        userPhone = findViewById(R.id.detailUserPhone);
        userImage = findViewById(R.id.detailUserImage);

        // Get data from intent
        String name = getIntent().getStringExtra("username");
        String id = getIntent().getStringExtra("userID");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phoneNumber");
        String profilePhotoUrl = getIntent().getStringExtra("profilePhotoUrl");

        // Set data to views
        userName.setText(name);
        userId.setText(id);
        userEmail.setText(email);
        userPhone.setText(phone);
        Glide.with(this).load(profilePhotoUrl).into(userImage);
    }
}
