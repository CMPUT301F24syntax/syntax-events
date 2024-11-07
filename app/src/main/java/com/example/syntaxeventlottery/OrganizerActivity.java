package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerActivity allows the event organizer to view and manage their created events.
 */
public class OrganizerActivity extends AppCompatActivity {

    private Button createEventButton;  // Button to create a new event
    private Button backButton;         // Button to go back to the previous screen
    private RecyclerView eventRecyclerView; // RecyclerView to display events
    private EventAdapter eventAdapter; // Adapter for managing event list display
    private List<Event> eventList = new ArrayList<>(); // List to hold events
    private FirebaseFirestore db;      // Firestore database instance
    private String deviceID;           // Device ID to identify the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize UI components
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get device ID to identify the organizer
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, this);
        eventRecyclerView.setAdapter(eventAdapter);

        // Load events from Firestore where organizerId matches deviceID
        loadEventsFromFirestore();

        // Navigate to Create Event page on button click
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerActivity.this, OrganizerCreateEvent.class);
                startActivity(intent);
            }
        });

        // Navigate back to Home page on button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Ends this activity and returns to the previous one
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list when returning to this activity
        loadEventsFromFirestore();
    }

    /**
     * Load events from Firestore where organizerId matches the deviceID.
     * Only events created by the current user (identified by deviceID) will be displayed.
     */
    private void loadEventsFromFirestore() {
        db.collection("events")
                .whereEqualTo("organizerId", deviceID) // Only fetch events where organizerId matches deviceID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();  // Clear the list before adding fresh data
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Event event = document.toObject(Event.class); // Convert document to Event object
                            eventList.add(event); // Add event to the list
                        }
                        eventAdapter.notifyDataSetChanged(); // Notify adapter of data change
                    } else {
                        Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
