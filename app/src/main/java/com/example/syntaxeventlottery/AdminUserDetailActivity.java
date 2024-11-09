package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class AdminUserDetailActivity extends AppCompatActivity {
    private Button backButton;
    private TextView userName, userId, userEmail, userPhone;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        //Back Button
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
