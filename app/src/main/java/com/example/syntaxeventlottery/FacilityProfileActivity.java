package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code FacilityProfileActivity} class allows users to enter and save facility details.
 * It retrieves the device ID to identify the user and updates the facility information in
 * Firebase Firestore under the "Users" collection.
 */
public class FacilityProfileActivity extends AppCompatActivity {

    /** EditText for entering the facility details. */
    private EditText editTextUsername;

    /** Button to navigate back to the previous screen. */
    private Button buttonBack;

    /** Button to save the facility details to the database. */
    private Button buttonSave;

    /** Firebase Firestore database instance. */
    private FirebaseFirestore db;

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
        setContentView(R.layout.facility_profile);

        // Initialize views
        editTextUsername = findViewById(R.id.edit_text_username);
        buttonBack = findViewById(R.id.button_back);
        buttonSave = findViewById(R.id.button_save);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set onClick listener for the Back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Navigate back to the previous page
            }
        });

        // Set onClick listener for the Save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFacilityDetails();
            }
        });
    }

    /**
     * Saves the facility details entered by the user to the Firebase Firestore database.
     * It retrieves the device ID to identify the user and updates the "facility" field
     * in the corresponding user document.
     */
    private void saveFacilityDetails() {
        // Get the device ID
        final String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String facilityDetails = editTextUsername.getText().toString();

        // Check if the facility details input is empty
        if (facilityDetails.isEmpty()) {
            Toast.makeText(this, "Please enter facility details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query the Users collection to find a user with a matching deviceCode
        db.collection("Users")
                .whereEqualTo("deviceCode", deviceID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    /**
                     * Called when the query to find the user is complete.
                     *
                     * @param task The task representing the query operation.
                     */
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Iterate through matched users (if there are multiple)
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Prepare data to update the facility field
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("facility", facilityDetails);

                                // Update the facility field in the user's document
                                db.collection("Users").document(document.getId())
                                        .update(updateData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(FacilityProfileActivity.this, "Facility details saved successfully!", Toast.LENGTH_SHORT).show();
                                            finish(); // Return to previous screen on success
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(FacilityProfileActivity.this, "Failed to save facility details", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // No matching user found
                            Toast.makeText(FacilityProfileActivity.this, "No matching user found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
