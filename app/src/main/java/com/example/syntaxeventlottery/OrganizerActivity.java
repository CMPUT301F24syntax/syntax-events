package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText facilityEditText;
    private EditText capacityEditText;
    private Button createEventButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventStartDateEditText = findViewById(R.id.eventStartDateEditText);
        eventEndDateEditText = findViewById(R.id.eventEndDateEditText);
        facilityEditText = findViewById(R.id.facilityEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);

        // Set click listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity and return to the previous screen
                finish();
            }
        });

        // Set click listener for the create event button
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve input data from the EditText fields
                String eventName = eventNameEditText.getText().toString();
                String eventStartDate = eventStartDateEditText.getText().toString();
                String eventEndDate = eventEndDateEditText.getText().toString();
                String facility = facilityEditText.getText().toString();
                String capacityStr = capacityEditText.getText().toString();

                // Check if all fields are filled
                if (eventName.isEmpty() || eventStartDate.isEmpty() || eventEndDate.isEmpty() || facility.isEmpty() || capacityStr.isEmpty()) {
                    Toast.makeText(OrganizerActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Parse capacity to integer
                int capacity = Integer.parseInt(capacityStr);

                // Display a success message (placeholder for actual QR code generation and saving event)
                Toast.makeText(OrganizerActivity.this, "Event Created and QR Code Generated", Toast.LENGTH_SHORT).show();

                // Optionally, clear the fields after submission
                eventNameEditText.setText("");
                eventStartDateEditText.setText("");
                eventEndDateEditText.setText("");
                facilityEditText.setText("");
                capacityEditText.setText("");
            }
        });
    }
}
