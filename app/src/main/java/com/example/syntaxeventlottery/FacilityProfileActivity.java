// FacilityProfileActivity.java

package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FacilityProfileActivity extends AppCompatActivity {
    private final String TAG = "FacilityProfileActivity";

    private EditText facilityNameEditText;
    private EditText facilityLocationEditText;
    private Button buttonBack, buttonSave;
    private UserController userController;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facility_profile);

        // Initialize views
        facilityNameEditText = findViewById(R.id.facilityNameEditText);
        facilityLocationEditText = findViewById(R.id.facilityLocationEditText);
        buttonBack = findViewById(R.id.button_back);
        buttonSave = findViewById(R.id.button_save);

        // get current user from previous activity
        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        // Initalize user controller
        userController = new UserController(new UserRepository());

        // Set onClick listener for the Back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Navigate back to the previous page
            }
        });

        // Set onClick listener for the Save button
        buttonSave.setOnClickListener(v -> saveFacilityDetails());
    }

    private void saveFacilityDetails() {
        // Get the device ID
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String facilityName = facilityNameEditText.getText().toString();
        String facilityLocation = facilityLocationEditText.getText().toString();

        // Check if the facility details input is empty
        if (facilityName.isEmpty() || facilityLocation.isEmpty()) {
            Toast.makeText(this, "Please enter fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Facility facility = new Facility(facilityName, facilityLocation, currentUser.getUserID());
        Log.d(TAG, "current user: " + currentUser);
        if (currentUser != null) {
            currentUser.setFacility(facility);
            Log.d(TAG, "facility details:" + currentUser.getFacility().getName() + " " + currentUser.getFacility().getLocation());
            userController.updateUser(currentUser, null, new DataCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Log.d(TAG, "Facility profile created and user updated: "+ result);
                    Toast.makeText(FacilityProfileActivity.this, "Facility Profile Created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FacilityProfileActivity.this, OrganizerActivity.class));
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error creating facility profile", e);
                    Toast.makeText(FacilityProfileActivity.this, "Error creating facility profile", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}
