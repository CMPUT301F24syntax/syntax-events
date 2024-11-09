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

public class EventWaitingListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private RecyclerView waitingListRecyclerView;
    private WaitingListAdapter waitingListAdapter;
    private List<User> waitingList;

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

    private void loadWaitingList() {
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
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
