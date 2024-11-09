package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The {@code AdminEventsActivity} class displays a list of events to the administrator.
 * It fetches events from Firebase Firestore and populates a ListView using an adapter.
 */
public class AdminEventsActivity extends AppCompatActivity {

    /** The ListView that displays the list of events. */
    private ListView listViewEvents;

    /** The adapter that bridges between the ListView and the data source. */
    private AdminEventAdapter eventAdapter;

    /** The list of events fetched from the database. */
    private List<Event> eventList;

    /** The back button to navigate to the previous activity. */
    private Button backButton;

    /** The Firebase Firestore instance used to access the database. */
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
        setContentView(R.layout.admin_events_main);

        // Initialize ListView, Back button, and Firestore
        listViewEvents = findViewById(R.id.listViewEvents);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance(); // Get Firestore instance

        // Set up Back button
        backButton.setOnClickListener(v -> finish());

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        eventAdapter = new AdminEventAdapter(this, eventList); // Use AdminEventAdapter
        listViewEvents.setAdapter(eventAdapter);

        // Load events from Firestore
        loadEventsFromDatabase();
    }

    /**
     * Loads events from the Firestore database and updates the ListView.
     */
    private void loadEventsFromDatabase() {
        // Reference to the "events" collection
        CollectionReference eventsRef = db.collection("events");

        eventsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear the existing list to avoid duplicates
                            eventList.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Retrieve data from each document and create Event object
                                String eventID = document.getString("eventID");
                                String eventName = document.getString("eventName");
                                String description = document.getString("description");
                                String facility = document.getString("facility");
                                int capacity = document.getLong("capacity").intValue();
                                Date startDate = document.getDate("startDate");
                                Date endDate = document.getDate("endDate");
                                String organizerId = document.getString("organizerId");
                                String posterUrl = document.getString("posterUrl");
                                String qrCodeUrl = document.getString("qrCodeUrl");

                                // Create Event object and add it to the list
                                Event event = new Event(eventID, eventName, description, facility, capacity, startDate, endDate, organizerId);
                                event.setPosterUrl(posterUrl);
                                event.setQrCodeUrl(qrCodeUrl);

                                eventList.add(event);
                            }
                            // Notify adapter that data has changed
                            eventAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No events found in the database.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("AdminEventsActivity", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to load events from database.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}