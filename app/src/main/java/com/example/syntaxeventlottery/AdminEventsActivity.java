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
 * It provides functionality to view all events and navigate back to the previous screen.
 */
public class AdminEventsActivity extends AppCompatActivity {
    private final String TAG = "AdminEventsActivity";

    private ListView listViewEvents;
    private AdminEventAdapter eventAdapter;
    private Button backButton;
    private EventController eventController;
    private ArrayList<Event> eventsList;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the back button functionality, and loads events.
     *
     * @param savedInstanceState The saved instance state, or {@code null} if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events_main);

        // Initialize ListView, Back button, and eventController
        listViewEvents = findViewById(R.id.listViewEvents);
        backButton = findViewById(R.id.backButton);
        eventController = new EventController(new EventRepository());

        // Set up Back button
        backButton.setOnClickListener(v -> finish());

        // initialize local list
        eventsList = new ArrayList<>();
        // initialize list view and adapter
        eventAdapter = new AdminEventAdapter(this, eventsList);
        listViewEvents.setAdapter(eventAdapter);

        // Load all events
        loadEvents();
    }

    /**
     * Called when the activity resumes.
     * Ensures the latest events are loaded and displayed.
     */
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    /**
     * Loads all events from the event repository and updates the list view.
     */
    private void loadEvents() {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                eventsList.clear();
                ArrayList<Event> allEvents = eventController.getLocalEventsList();
                eventsList.addAll(allEvents);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing events: " +e.getMessage());
                Toast.makeText(AdminEventsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
