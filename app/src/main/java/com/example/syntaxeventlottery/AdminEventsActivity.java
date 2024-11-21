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

public class AdminEventsActivity extends AppCompatActivity {

    private ListView listViewEvents;
    private AdminEventAdapter eventAdapter;
    private List<Event> eventList;
    private Button backButton;

    // Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events_main);

        // Initialize ListView, Back button, and Firestore
        listViewEvents = findViewById(R.id.listViewEvents);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance(); // 获取 Firestore 实例

        // Set up Back button
        backButton.setOnClickListener(v -> finish());

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        eventAdapter = new AdminEventAdapter(this, eventList); // 使用 AdminEventAdapter
        listViewEvents.setAdapter(eventAdapter);

        // Load events from Firestore
        loadEventsFromDatabase();
    }

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
                                int capacity = document.getLong("capacity").intValue();
                                Date startDate = document.getDate("startDate");
                                Date endDate = document.getDate("endDate");
                                String organizerId = document.getString("organizerId");
                                String posterUrl = document.getString("posterUrl");
                                String qrCode = document.getString("qrCode");

                                // Create Event object and add it to the list
                                // public Event(String eventID, String eventName, String description, String facility, int capacity,
                                //                 Date startDate, Date endDate, String organizerId)
                                Event event = new Event(eventID, eventName, description, capacity, startDate, endDate, organizerId);
                                event.setPosterUrl(posterUrl);
                                event.setQrCode(qrCode);

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
