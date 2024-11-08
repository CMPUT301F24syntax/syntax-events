package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code EventWaitingListActivity} class displays the waiting list of participants for a specific event.
 * It retrieves the list of participant device codes from Firestore and loads their user information
 * to display in a RecyclerView.
 */
public class EventWaitingListActivity extends AppCompatActivity {

    /** Firebase Firestore database instance. */
    private FirebaseFirestore db;

    /** The ID of the event whose waiting list is to be displayed. */
    private String eventId;

    /** RecyclerView to display the list of waiting participants. */
    private RecyclerView waitingListRecyclerView;

    /** Adapter for the RecyclerView to manage waiting list items. */
    private WaitingListAdapter waitingListAdapter;

    /** List of users in the waiting list. */
    private List<User> waitingList;

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
        setContentView(R.layout.event_waiting_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve event ID passed from EventDetailActivity
        eventId = getIntent().getStringExtra("event_id");

        // Initialize RecyclerView and Adapter
        waitingListRecyclerView = findViewById(R.id.waitingListRecyclerView);
        waitingListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        waitingList = new ArrayList<>();
        waitingListAdapter = new WaitingListAdapter(waitingList);
        waitingListRecyclerView.setAdapter(waitingListAdapter);

        // Set up the Back Button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (eventId != null) {
            loadWaitingList();
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads the waiting list of participants for the specified event from Firestore.
     * It retrieves the list of participant device codes and loads their user information.
     */
    private void loadWaitingList() {
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                @SuppressWarnings("unchecked")
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants != null && !participants.isEmpty()) {
                    for (String deviceCode : participants) {
                        loadUserInfo(deviceCode);
                    }
                } else {
                    Toast.makeText(this, "No participants found for this event", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load waiting list", Toast.LENGTH_SHORT).show();
            Log.e("EventWaitingList", "Error loading waiting list", e);
        });
    }

    /**
     * Loads user information for a given device code from Firestore and adds it to the waiting list.
     *
     * @param deviceCode The device code of the user to load information for.
     */
    private void loadUserInfo(String deviceCode) {
        db.collection("Users").whereEqualTo("deviceCode", deviceCode).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                waitingList.add(user);
                                waitingListAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Log.d("EventWaitingList", "User not found for deviceCode: " + deviceCode);
                    }
                })
                .addOnFailureListener(e -> Log.e("EventWaitingList", "Error loading user info for deviceCode: " + deviceCode, e));
    }
}
